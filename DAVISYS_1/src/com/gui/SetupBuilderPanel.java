package com.gui;

import com.dao.SetupCartDAO.CartContext;
import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.service.SetupBuilderFacade;
import com.service.SetupMoney;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.JTabbedPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Single embedded UI, combining GUI branch criteria/theme with the integration branch cart flow. */
public class SetupBuilderPanel extends JPanel {
    private final SetupBuilderFacade service;
    private final String username;
    private final Runnable cartRefresh;
    private final JTextField budgetField = new JTextField("10000000", 12);
    private final JComboBox<String> purposeBox = new JComboBox<>(new String[]{"Không lọc mục đích",
        "Học tập", "Văn phòng", "Gaming", "Thiết kế đồ họa", "Làm việc đa nhiệm"});
    private final JComboBox<String> styleBox = new JComboBox<>(new String[]{"Không lọc phong cách",
        "Tối giản", "Hiện đại", "Năng động", "Sang trọng"});
    private final DefaultListModel<String> categories = new DefaultListModel<>();
    private final JList<String> categoryList = new JList<>(categories);
    private final Map<String, String> categoryNames = new HashMap<>();
    private final JComboBox<CartContext> customerBox = new JComboBox<>();
    private final JTabbedPane comboTabs = new JTabbedPane();
    private final List<SetupCombo> combos = new ArrayList<>();
    private final JLabel summaryLabel = new JLabel("Chọn nhóm sản phẩm rồi tạo combo.");
    private final JLabel statusLabel = new JLabel("Chưa tải dữ liệu.");
    private final JButton loadButton = new JButton("Tải lại dữ liệu");
    private final JButton buildButton = new JButton("Tạo bộ phụ kiện");
    private final JButton replaceButton = new JButton("Đổi phụ kiện");
    private final JButton addButton = new JButton("Thêm vào giỏ hàng");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã", "Tên phụ kiện", "Loại", "Giá (VNĐ)", "SL mua", "Điểm /5", "Lý do"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 3 ? Long.class : column == 4 || column == 5 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private boolean busy;
    private long currentBudget;
    private List<SetupProduct> filteredProducts = List.of();
    private SetupCombo combo;

    public SetupBuilderPanel(SetupBuilderFacade service, String username, Runnable cartRefresh) {
        this.service = Objects.requireNonNull(service);
        this.username = username; this.cartRefresh = cartRefresh;
        buildLayout();
        setDarkMode(false);
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
        clearCombos();
        runBackground(() -> new Options(service.getProducts(null, null), service.getCarts(username)), options -> {
            categories.clear();
            categoryNames.clear();
            for (SetupProduct product : options.products) {
                categoryNames.put(product.getMaLoai(), product.getTenLoai());
            }
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
            statusLabel.setText("Đã tải " + options.products.size() + " sản phẩm. "
                    + (options.carts.isEmpty() ? "Chưa có giỏ của bạn; tạo giỏ tại màn hình bán hàng rồi tải lại."
                    : "Chọn nhóm sản phẩm; mỗi nhóm mua 1 món."));
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
        customerBox.setPreferredSize(new Dimension(420, 28));
        customerBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list, value == null ? "— Chọn giỏ khách hàng —" : value,
                        index, selected, focus);
            }
        });
        budgetField.getAccessibleContext().setAccessibleName("Ngân sách bằng VNĐ");
        budgetField.setToolTipText("Nhập số nguyên VNĐ, ví dụ: 1500000 (không dấu phân cách)");
        categoryList.getAccessibleContext().setAccessibleName("Nhóm sản phẩm, Ctrl để chọn nhiều");
        table.getAccessibleContext().setAccessibleName("Chi tiết phương án phụ kiện");
        criteria.add(first); criteria.add(second); criteria.add(third);

        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        categoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                String code = (String) value;
                setText(categoryNames.getOrDefault(code, code));
                setToolTipText(code);
                return this;
            }
        });
        JPanel groups = new JPanel(new BorderLayout(4, 4));
        groups.add(new JLabel("Nhóm sản phẩm (Ctrl để chọn nhiều):"), BorderLayout.NORTH);
        groups.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(195, 232, 229));
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(300);
        table.setDefaultRenderer(Long.class, new DefaultTableCellRenderer() {
            @Override protected void setValue(Object value) { setText(value == null ? "" : money((Long) value)); }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setColumnHeaderView(table.getTableHeader());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groups, tableScroll);
        split.setDividerLocation(225); split.setResizeWeight(0.2);
        JPanel center = new JPanel(new BorderLayout(8, 8));
        JPanel results = new JPanel(new BorderLayout(4, 4));
        comboTabs.setPreferredSize(new Dimension(400, 32));
        results.add(comboTabs, BorderLayout.NORTH); results.add(split, BorderLayout.CENTER);
        center.add(criteria, BorderLayout.NORTH); center.add(results, BorderLayout.CENTER);
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
        comboTabs.addChangeListener(e -> {
            int index = comboTabs.getSelectedIndex();
            combo = index < 0 || index >= combos.size() ? null : combos.get(index);
            showCombo();
        });
        customerBox.addActionListener(e -> setBusy(busy));
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
                combos.addAll(generated.combos);
                for (SetupCombo result : generated.combos) {
                    JPanel tab = new JPanel(); tab.setOpaque(false);
                    comboTabs.addTab(result.getLoaiCombo(), tab);
                }
                statusLabel.setText(generated.combos.isEmpty()
                        ? "Chưa có bộ phù hợp. Thử tăng ngân sách, giảm số nhóm hoặc chọn Không lọc."
                        : "Có " + generated.combos.size() + " phương án khác nhau. Chọn tab để so sánh; chọn dòng để đổi món.");
            });
        } catch (RuntimeException ex) { showError(message(ex)); }
    }

    private void showCombo() {
        tableModel.setRowCount(0);
        if (combo == null) { summaryLabel.setText("Chưa chọn combo."); setBusy(busy); return; }
        for (SetupProduct p : combo.getProducts()) tableModel.addRow(new Object[]{p.getMaSP(), p.getTenSP(),
            p.getMaLoai(), p.getGiaBan(), 1, p.getDiemPhuHop(), p.getLyDo()});
        summaryLabel.setText(combo.getProducts().size() + " phụ kiện – Tổng: " + money(combo.getTongTien())
                + " VNĐ – Còn dư: " + money(currentBudget - combo.getTongTien()) + " VNĐ – Điểm TB: "
                + String.format(new Locale("vi", "VN"), "%.1f/5", combo.getDiemTrungBinh()));
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
        SetupProduct chosen = chooseReplacement(choices);
        if (chosen == null) return;
        try {
            SetupCombo changed = service.replaceProduct(combo, chosen, currentBudget);
            int selected = comboTabs.getSelectedIndex();
            for (int i = 0; i < combos.size(); i++) {
                if (i != selected && combos.get(i).getSignature().equals(changed.getSignature())) {
                    showError("Combo sau khi đổi trùng với gợi ý khác. Hãy chọn combo đó hoặc sản phẩm khác.");
                    return;
                }
            }
            combos.set(selected, changed); combo = changed; showCombo();
            statusLabel.setText("Đã đổi sang " + chosen.getTenSP() + ". Tổng tiền vẫn trong ngân sách.");
        } catch (RuntimeException ex) { showError(message(ex)); }
    }

    private void addToCart() {
        if (busy || combo == null) return;
        CartContext cart = (CartContext) customerBox.getSelectedItem();
        if (cart == null) { showError("Chọn giỏ hàng khách hàng trước."); return; }
        SetupCombo selected = combo;
        runBackground(() -> { service.addToCart(cart, selected); return null; }, prices -> {
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
        combos.clear(); comboTabs.removeAll(); combo = null; filteredProducts = List.of(); showCombo();
    }
    private void setBusy(boolean value) {
        busy = value;
        loadButton.setEnabled(!value); buildButton.setEnabled(!value); budgetField.setEnabled(!value);
        purposeBox.setEnabled(!value); styleBox.setEnabled(!value); categoryList.setEnabled(!value);
        customerBox.setEnabled(!value); comboTabs.setEnabled(!value);
        for (int i = 0; i < comboTabs.getTabCount(); i++) comboTabs.setEnabledAt(i, !value);
        replaceButton.setEnabled(!value && combo != null && table.getSelectedRow() >= 0);
        addButton.setEnabled(!value && combo != null && getSelectedCart() != null);
    }
    private static String money(long amount) { return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount); }
    private static String message(Throwable ex) { return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(); }
    protected void showError(String text) { JOptionPane.showMessageDialog(this, text, "Smart Setup Builder", JOptionPane.WARNING_MESSAGE); }
    protected SetupProduct chooseReplacement(List<SetupProduct> choices) {
        return (SetupProduct) JOptionPane.showInputDialog(this, "Chọn phụ kiện cùng loại, trong ngân sách:",
                "Đổi phụ kiện", JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), choices.get(0));
    }

    /** Apply the Home theme without changing global UIManager defaults. */
    public void setDarkMode(boolean dark) {
        Color background = dark ? new Color(32, 41, 48) : new Color(235, 249, 250);
        Color foreground = dark ? new Color(235, 243, 245) : new Color(35, 60, 65);
        applyColors(this, background, foreground);
        Color surface = dark ? new Color(44, 56, 65) : Color.WHITE;
        for (Component input : new Component[]{budgetField, purposeBox, styleBox, customerBox, categoryList, table}) {
            input.setBackground(surface); input.setForeground(foreground);
        }
        budgetField.setCaretColor(foreground);
        Color selection = dark ? new Color(35, 101, 102) : new Color(195, 232, 229);
        comboTabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void paintTabBackground(java.awt.Graphics g, int placement, int index,
                    int x, int y, int width, int height, boolean selected) {
                g.setColor(selected ? selection : background);
                g.fillRect(x, y, width, height);
            }
        });
        table.setSelectionBackground(selection); table.setSelectionForeground(foreground);
        categoryList.setSelectionBackground(selection); categoryList.setSelectionForeground(foreground);
        table.setGridColor(dark ? new Color(70, 86, 96) : new Color(210, 225, 226));
        table.getTableHeader().setBackground(dark ? new Color(40, 77, 83) : new Color(128, 201, 208));
        table.getTableHeader().setForeground(foreground);
        repaint();
    }
    private static void applyColors(Component component, Color background, Color foreground) {
        component.setBackground(background); component.setForeground(foreground);
        if (component instanceof Container) for (Component child : ((Container) component).getComponents())
            applyColors(child, background, foreground);
    }
    private record Options(List<SetupProduct> products, List<CartContext> carts) { }
    private record Generated(List<SetupProduct> products, List<SetupCombo> combos) { }
}
