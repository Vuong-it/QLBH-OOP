package com.gui;

import com.dao.KhachHangDAO;
import com.dao.SetupCartDAO;
import com.entity.KhachHangEntity;
import com.entity.SanPhamEntity;
import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.service.SetupBuilderService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** DAVISY screen for generating, editing, and adding a suggested setup to the existing cart. */
public class SetupBuilderPanel extends JPanel {
    private final SetupBuilderService service;
    private final SetupCartDAO cartDAO;
    private final Runnable cartRefresh;
    private final JTextField budgetField = new JTextField("10000000", 12);
    private final JLabel summaryLabel = new JLabel("Chưa có combo được tạo.");
    private final JComboBox<String> categoryBox = new JComboBox<String>();
    private final JComboBox<CustomerOption> customerBox = new JComboBox<CustomerOption>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã sản phẩm", "Tên sản phẩm", "Loại", "Giá", "SL", "Lý do"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private SetupCombo combo;

    public SetupBuilderPanel(SetupBuilderService service, SetupCartDAO cartDAO, Runnable cartRefresh) {
        this.service = service;
        this.cartDAO = cartDAO;
        this.cartRefresh = cartRefresh;
        buildLayout();
        loadOptions();
    }

    public String getSelectedCustomerPhone() {
        CustomerOption option = (CustomerOption) customerBox.getSelectedItem();
        return option == null ? null : option.phone;
    }

    private void buildLayout() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        JLabel heading = new JLabel("Smart Setup Builder – Tư vấn combo phụ kiện");
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);

        JPanel controls = new JPanel(new GridLayout(2, 1, 8, 8));
        JPanel criteria = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        criteria.add(new JLabel("Ngân sách (VND):"));
        criteria.add(budgetField);
        criteria.add(new JLabel("Loại ưu tiên:"));
        criteria.add(categoryBox);
        JButton buildButton = new JButton("Tạo combo");
        buildButton.addActionListener(e -> buildCombo());
        criteria.add(buildButton);
        JPanel customer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        customer.add(new JLabel("Giỏ hàng khách:"));
        customer.add(customerBox);
        controls.add(criteria);
        controls.add(customer);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(controls, BorderLayout.NORTH);
        table.setRowHeight(28);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        summaryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        center.add(summaryLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton replaceButton = new JButton("Thay sản phẩm");
        replaceButton.addActionListener(e -> replaceSelected());
        JButton addButton = new JButton("Thêm combo vào giỏ hàng");
        addButton.addActionListener(e -> addToCart());
        actions.add(replaceButton);
        actions.add(addButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void loadOptions() {
        categoryBox.addItem("Tất cả loại");
        try {
            for (SanPhamEntity product : service.getAvailableProducts()) {
                String category = product.getMaLH();
                if (category != null && categoryBox.getItemCount() < 100 && !containsCategory(category)) categoryBox.addItem(category);
            }
            for (KhachHangEntity customer : new KhachHangDAO().selectAll()) {
                if (customer.getDienThoai() != null && !customer.getDienThoai().trim().isEmpty())
                    customerBox.addItem(new CustomerOption(customer.getDienThoai(), customer.getHoTen()));
            }
        } catch (RuntimeException ex) {
            showError("Không tải được dữ liệu sản phẩm/khách hàng. Kiểm tra kết nối cơ sở dữ liệu.\n" + message(ex));
        }
    }

    private boolean containsCategory(String category) {
        for (int i = 1; i < categoryBox.getItemCount(); i++) if (category.equalsIgnoreCase(categoryBox.getItemAt(i))) return true;
        return false;
    }

    private void buildCombo() {
        try {
            double budget = Double.parseDouble(budgetField.getText().trim());
            String category = categoryBox.getSelectedIndex() <= 0 ? null : (String) categoryBox.getSelectedItem();
            combo = service.buildCombo(budget, category);
            showCombo();
        } catch (Exception ex) { showError(message(ex)); }
    }

    private void showCombo() {
        tableModel.setRowCount(0);
        if (combo == null) return;
        for (SetupProduct product : combo.getProducts()) {
            tableModel.addRow(new Object[]{product.getMaSP(), product.getTenSP(), product.getMaLoai(),
                money(product.getGiaBan()), product.getSoLuong(), product.getLyDo()});
        }
        int quantity = 0;
        for (SetupProduct product : combo.getProducts()) quantity += product.getSoLuong();
        summaryLabel.setText("Số sản phẩm: " + quantity + "    Tổng tiền dự kiến: " + money(combo.getTongTien()) + " VND");
    }

    private void replaceSelected() {
        if (combo == null || table.getSelectedRow() < 0) { showError("Chọn một sản phẩm trong combo trước."); return; }
        try {
            List<SanPhamEntity> candidates = service.getAvailableProducts();
            List<SanPhamEntity> replacements = new ArrayList<SanPhamEntity>();
            String oldCode = combo.getProducts().get(table.convertRowIndexToModel(table.getSelectedRow())).getMaSP();
            for (SanPhamEntity item : candidates) if (!oldCode.equalsIgnoreCase(item.getMaSP())) replacements.add(item);
            if (replacements.isEmpty()) { showError("Không có sản phẩm khác còn hàng để thay."); return; }
            SanPhamEntity chosen = (SanPhamEntity) JOptionPane.showInputDialog(this, "Chọn sản phẩm thay thế:",
                    "Thay sản phẩm", JOptionPane.PLAIN_MESSAGE, null, replacements.toArray(), replacements.get(0));
            if (chosen == null) return;
            SetupProduct replacement = new SetupProduct(chosen.getMaSP(), chosen.getTenSP(), chosen.getMaLH(),
                    chosen.getGiaBan(), 1, 1, "Sản phẩm thay thế còn hàng");
            combo.getProducts().set(table.convertRowIndexToModel(table.getSelectedRow()), replacement);
            combo = service.recalculate(combo.getProducts(), combo.getLoaiCombo());
            showCombo();
        } catch (Exception ex) { showError(message(ex)); }
    }

    private void addToCart() {
        if (combo == null) { showError("Tạo một combo trước khi thêm vào giỏ hàng."); return; }
        CustomerOption customer = (CustomerOption) customerBox.getSelectedItem();
        if (customer == null) { showError("Cần có khách hàng để gắn combo vào giỏ hàng."); return; }
        try {
            java.util.Map<String, Double> currentPrices = cartDAO.addCombo(customer.phone, combo.getProducts());
            for (SetupProduct product : combo.getProducts()) {
                Double currentPrice = currentPrices.get(product.getMaSP());
                if (currentPrice != null) product.setGiaBan(currentPrice);
            }
            combo = service.recalculate(combo.getProducts(), combo.getLoaiCombo());
            showCombo();
            String refreshMessage = "";
            if (cartRefresh != null) {
                try { cartRefresh.run(); }
                catch (RuntimeException ex) { refreshMessage = "\nKhông tải lại được giao diện giỏ hàng: " + message(ex); }
            }
            JOptionPane.showMessageDialog(this, "Đã thêm " + combo.getProducts().size()
                    + " sản phẩm vào giỏ " + customer.phone + ". Giá và tồn kho đã được kiểm tra lại." + refreshMessage,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError(message(ex)); }
    }

    private String money(double amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount);
    }
    private String message(Throwable ex) { return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(); }
    private void showError(String text) { JOptionPane.showMessageDialog(this, text, "Smart Setup Builder", JOptionPane.WARNING_MESSAGE); }

    private static final class CustomerOption {
        private final String phone;
        private final String name;
        CustomerOption(String phone, String name) { this.phone = phone; this.name = name; }
        public String toString() { return name == null || name.trim().isEmpty() ? phone : name + " – " + phone; }
    }
}
