package com.gui;

import com.dao.SetupCartDAO;
import com.dao.SetupCartDAO.CartContext;
import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.service.SetupBuilderFacade;
import com.service.SetupMoney;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Single embedded UI, combining GUI branch criteria/theme with the integration branch cart flow. */
public class SetupBuilderPanel extends JPanel {
    private final SetupBuilderFacade service;
    private final SetupCartDAO cartDAO;
    private final String username;
    private final Runnable cartRefresh;
    private final JTextField budgetField = new JTextField("10000000", 12);
    private final JComboBox<String> purposeBox = new JComboBox<>(new String[]{"Không lọc mục đích",
        "Học tập", "Văn phòng", "Gaming", "Thiết kế đồ họa", "Làm việc đa nhiệm"});
    private final JComboBox<String> styleBox = new JComboBox<>(new String[]{"Không lọc phong cách",
        "Tối giản", "Hiện đại", "Năng động", "Sang trọng"});
    private final DefaultListModel<String> categories = new DefaultListModel<>();
    private final JList<String> categoryList = new JList<>(categories);
    private final JComboBox<CartContext> customerBox = new JComboBox<>();
    private final JComboBox<ComboOption> comboBox = new JComboBox<>();
    private final JLabel summaryLabel = new JLabel("Chọn nhóm sản phẩm rồi tạo combo.");
    private final JLabel statusLabel = new JLabel("Chưa tải dữ liệu.");
    private final JButton loadButton = new JButton("Tải lại dữ liệu");
    private final JButton buildButton = new JButton("Tạo bộ phụ kiện");
    private final JButton replaceButton = new JButton("Đổi phụ kiện");
    private final JButton addButton = new JButton("Thêm vào giỏ hàng");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã", "Tên phụ kiện", "Loại", "Giá (VNĐ)", "SL mua", "Lý do"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private boolean busy;
    private long currentBudget;
    private List<SetupProduct> filteredProducts = List.of();
    private SetupCombo combo;

    public SetupBuilderPanel(SetupBuilderFacade service, SetupCartDAO cartDAO, String username, Runnable cartRefresh) {
        this.service = Objects.requireNonNull(service); this.cartDAO = Objects.requireNonNull(cartDAO);
        this.username = username; this.cartRefresh = cartRefresh;
        buildLayout();
        setBusy(false);
    }

    public CartContext getSelectedCart() {
        return (CartContext) customerBox.getSelectedItem();
    }

    public String getSelectedCartId() {
        CartContext cart = (CartContext) customerBox.getSelectedItem();
        return cart == null ? null : cart.cartId;
    }
    public String getSelectedCustomerPhone() {
        CartContext cart = (CartContext) customerBox.getSelectedItem();
        return cart == null ? null : cart.phone;
    }

    public void refreshOptions() {
        if (busy) return;
        List<String> selected = categoryList.getSelectedValuesList();
        CartContext old = (CartContext) customerBox.getSelectedItem();
        runBackground(() -> new Options(service.getProducts(null, null), cartDAO.getCarts(username)), options -> {
            categories.clear();
            options.products.stream().map(SetupProduct::getMaLoai).distinct().sorted().forEach(categories::addElement);
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) if (selected.contains(categories.get(i))) indices.add(i);
            categoryList.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
            customerBox.removeAllItems();
            customerBox.addItem(null);
            for (CartContext cart : options.carts) {
                customerBox.addItem(cart);
                if (old != null && old.cartId.equals(cart.cartId)) customerBox.setSelectedItem(cart);
            }
            clearCombos();
            statusLabel.setText("Đã tải " + options.products.size() + " sản phẩm. Chọn một hoặc nhiều nhóm; mỗi nhóm mua 1 món.");
        });
    }

    private void buildLayout() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(235, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel heading = new JLabel("DAVISY – Smart Setup Builder");
        heading.setOpaque(true); heading.setBackground(new Color(128, 201, 208));
        heading.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);

        JPanel criteria = new JPanel(new GridLayout(3, 1, 6, 6));
        JPanel first = new JPanel(new FlowLayout(FlowLayout.LEFT));
        first.add(new JLabel("Ngân sách (VNĐ):")); first.add(budgetField);
        first.add(buildButton); first.add(loadButton);
        JPanel second = new JPanel(new FlowLayout(FlowLayout.LEFT));
        second.add(new JLabel("Mục đích:")); second.add(purposeBox);
        second.add(new JLabel("Phong cách:")); second.add(styleBox);
        JPanel third = new JPanel(new FlowLayout(FlowLayout.LEFT));
        third.add(new JLabel("Giỏ khách hàng:")); third.add(customerBox);
        third.add(new JLabel("Combo:")); third.add(comboBox);
        criteria.add(first); criteria.add(second); criteria.add(third);

        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JPanel groups = new JPanel(new BorderLayout(4, 4));
        groups.add(new JLabel("Nhóm sản phẩm (Ctrl để chọn nhiều):"), BorderLayout.NORTH);
        groups.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(195, 232, 229));
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(300);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groups, new JScrollPane(table));
        split.setDividerLocation(225); split.setResizeWeight(0.2);
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(criteria, BorderLayout.NORTH); center.add(split, BorderLayout.CENTER);
        center.add(summaryLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(statusLabel, BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(replaceButton); actions.add(addButton);
        footer.add(actions, BorderLayout.SOUTH); add(footer, BorderLayout.SOUTH);
        loadButton.addActionListener(e -> refreshOptions());
        buildButton.addActionListener(e -> buildCombos());
        replaceButton.addActionListener(e -> replaceSelected());
        addButton.addActionListener(e -> addToCart());
        comboBox.addActionListener(e -> {
            ComboOption option = (ComboOption) comboBox.getSelectedItem();
            combo = option == null ? null : option.value;
            showCombo();
        });
        table.getSelectionModel().addListSelectionListener(e -> setBusy(busy));
        purposeBox.addActionListener(e -> invalidateCriteria());
        styleBox.addActionListener(e -> invalidateCriteria());
        categoryList.addListSelectionListener(e -> invalidateCriteria());
        budgetField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateCriteria(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateCriteria(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateCriteria(); }
        });
    }

    private void invalidateCriteria() {
        if (!busy && combo != null) {
            clearCombos();
            statusLabel.setText("Tiêu chí đã thay đổi. Hãy tạo lại combo.");
        }
    }

    private void buildCombos() {
        try {
            long budget = SetupMoney.parseBudget(budgetField.getText());
            List<String> requested = categoryList.getSelectedValuesList();
            if (requested.isEmpty()) throw new IllegalArgumentException("Chọn ít nhất một nhóm sản phẩm.");
            String purpose = purposeBox.getSelectedIndex() == 0 ? null : (String) purposeBox.getSelectedItem();
            String style = styleBox.getSelectedIndex() == 0 ? null : (String) styleBox.getSelectedItem();
            clearCombos();
            runBackground(() -> {
                List<SetupProduct> products = service.getProducts(purpose, style);
                return new Generated(products, service.generateCombos(products, budget, requested));
            }, generated -> {
                currentBudget = budget; filteredProducts = generated.products;
                for (SetupCombo result : generated.combos) comboBox.addItem(new ComboOption(result));
                statusLabel.setText(generated.combos.isEmpty()
                        ? "Không có combo đủ nhóm/ngân sách. Metadata thiếu: chọn Không lọc hoặc bổ sung mô tả."
                        : "Đã tạo " + generated.combos.size() + " combo khác nhau; chọn combo để xem và thêm vào giỏ.");
            });
        } catch (RuntimeException ex) { showError(message(ex)); }
    }

    private void showCombo() {
        tableModel.setRowCount(0);
        if (combo == null) { summaryLabel.setText("Chưa chọn combo."); setBusy(busy); return; }
        for (SetupProduct p : combo.getProducts()) tableModel.addRow(new Object[]{p.getMaSP(), p.getTenSP(),
            p.getMaLoai(), money(p.getGiaBan()), 1, p.getLyDo()});
        summaryLabel.setText(combo.getProducts().size() + " phụ kiện – Tổng: " + money(combo.getTongTien())
                + " VNĐ – Còn dư: " + money(currentBudget - combo.getTongTien()) + " VNĐ");
        setBusy(busy);
    }

    private void replaceSelected() {
        if (busy || combo == null || table.getSelectedRow() < 0) return;
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        SetupProduct old = combo.getProducts().get(row);
        List<SetupProduct> choices = filteredProducts.stream()
                .filter(p -> p.getMaLoai().equals(old.getMaLoai()) && !p.getMaSP().equals(old.getMaSP())
                        && p.getSoLuong() > 0 && p.getGiaBan() <= currentBudget - (combo.getTongTien() - old.getGiaBan())).toList();
        if (choices.isEmpty()) { showError("Không có sản phẩm cùng loại phù hợp bộ lọc và ngân sách để đổi."); return; }
        SetupProduct chosen = (SetupProduct) JOptionPane.showInputDialog(this, "Chọn phụ kiện thay thế:",
                "Đổi phụ kiện", JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), choices.get(0));
        if (chosen == null) return;
        try {
            SetupCombo changed = service.replaceProduct(combo, chosen, currentBudget);
            int selected = comboBox.getSelectedIndex();
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                if (i != selected && comboBox.getItemAt(i).value.getSignature().equals(changed.getSignature())) {
                    showError("Combo sau khi đổi trùng với gợi ý khác. Hãy chọn combo đó hoặc sản phẩm khác.");
                    return;
                }
            }
            comboBox.removeItemAt(selected); comboBox.insertItemAt(new ComboOption(changed), selected);
            comboBox.setSelectedIndex(selected);
        } catch (RuntimeException ex) { showError(message(ex)); }
    }

    private void addToCart() {
        if (busy || combo == null) return;
        CartContext cart = (CartContext) customerBox.getSelectedItem();
        if (cart == null) { showError("Chọn giỏ hàng khách hàng trước."); return; }
        SetupCombo selected = combo;
        runBackground(() -> cartDAO.addCombo(cart, selected.getProducts()), prices -> {
            // Refresh only after a committed transaction; clear the selection to avoid accidental repeated adds.
            clearCombos();
            String refreshMessage = "";
            if (cartRefresh != null) {
                try { cartRefresh.run(); }
                catch (RuntimeException ex) { refreshMessage = " Giỏ đã ghi; tải lại màn hình bán hàng để xem."; }
            }
            statusLabel.setText("Đã thêm " + selected.getProducts().size() + " phụ kiện vào giỏ " + cart.cartId + "." + refreshMessage);
        });
    }

    private <T> void runBackground(Callable<T> operation, Consumer<T> success) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("UI phải cập nhật trên EDT.");
        if (busy) return;
        setBusy(true); statusLabel.setText("Đang xử lý...");
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return operation.call(); }
            @Override protected void done() {
                try { success.accept(get()); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); showError("Đã ngắt thao tác."); }
                catch (ExecutionException ex) { statusLabel.setText("Thao tác thất bại."); showError(message(ex.getCause())); }
                catch (RuntimeException ex) { statusLabel.setText("Không cập nhật được giao diện."); showError(message(ex)); }
                finally { setBusy(false); }
            }
        }.execute();
    }

    private void clearCombos() {
        comboBox.removeAllItems(); combo = null; filteredProducts = List.of(); showCombo();
    }
    private void setBusy(boolean value) {
        busy = value;
        loadButton.setEnabled(!value); buildButton.setEnabled(!value); budgetField.setEnabled(!value);
        purposeBox.setEnabled(!value); styleBox.setEnabled(!value); categoryList.setEnabled(!value);
        customerBox.setEnabled(!value); comboBox.setEnabled(!value);
        replaceButton.setEnabled(!value && combo != null && table.getSelectedRow() >= 0);
        addButton.setEnabled(!value && combo != null);
    }
    private static String money(long amount) { return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount); }
    private static String message(Throwable ex) { return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(); }
    private void showError(String text) { JOptionPane.showMessageDialog(this, text, "Smart Setup Builder", JOptionPane.WARNING_MESSAGE); }
    private record Options(List<SetupProduct> products, List<CartContext> carts) { }
    private record Generated(List<SetupProduct> products, List<SetupCombo> combos) { }
    private static final class ComboOption {
        final SetupCombo value;
        ComboOption(SetupCombo value) { this.value = value; }
        @Override public String toString() { return value.getLoaiCombo() + " – " + money(value.getTongTien()) + " VNĐ"; }
    }
}
