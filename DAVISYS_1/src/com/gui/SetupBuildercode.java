
package com.gui;

import com.dao.SanPhamDAO;
import com.entity.SanPhamEntity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class SetupBuildercode extends JFrame {

    // ===== MÀU SẮC =====
    private final Color HEADER_COLOR = new Color(128, 201, 208);
    private final Color BACKGROUND_COLOR = new Color(235, 249, 250);
    private final Color BUTTON_COLOR = new Color(48, 145, 125);
    private final Color TEXT_COLOR = new Color(45, 70, 75);
    private final Color SUCCESS_COLOR = new Color(35, 125, 75);
    private final Color WARNING_COLOR = new Color(190, 120, 0);
    private final Color ERROR_COLOR = new Color(190, 45, 45);

    // ===== THÀNH PHẦN GIAO DIỆN =====
    private JTextField txtBudget;
    private JComboBox<String> cboPurpose;
    private JComboBox<String> cboStyle;

    private JTable table;
    private DefaultTableModel model;

    private JLabel lblTotal;
    private JLabel lblBudgetStatus;

    private JButton btnGenerate;
    private JButton btnChange;
    private JButton btnAdd;

    // ===== DỮ LIỆU =====
    private final List<SanPhamEntity> displayedProducts = new ArrayList<>();

    private double currentBudget = 0;

    public SetupBuildercode() {
        initComponents();
    }

    private void initComponents() {
        setTitle("DAVISY - Smart Setup Builder");
        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BACKGROUND_COLOR);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    // ===== HEADER =====
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel lblBrand = new JLabel("DAVISY");
        lblBrand.setFont(new Font("Arial", Font.BOLD, 24));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel(
                "Smart Setup Builder",
                SwingConstants.CENTER
        );
        lblTitle.setFont(new Font("Arial", Font.BOLD, 27));
        lblTitle.setForeground(TEXT_COLOR);

        header.add(lblBrand, BorderLayout.WEST);
        header.add(lblTitle, BorderLayout.CENTER);

        return header;
    }

    // ===== NỘI DUNG CHÍNH =====
    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BACKGROUND_COLOR);
        main.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        main.add(createForm(), BorderLayout.NORTH);
        main.add(createListPanel(), BorderLayout.CENTER);

        return main;
    }

    // ===== FORM NHẬP THÔNG TIN =====
    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(220, 243, 244));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 18, 15, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblBudget = createLabel("Ngân sách (VNĐ):");

        txtBudget = new JTextField();
        txtBudget.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBudget.setToolTipText(
                "Nhập số tiền, ví dụ: 5000000 hoặc 5.000.000"
        );

        JLabel lblPurpose = createLabel("Mục đích sử dụng:");

        cboPurpose = new JComboBox<>(new String[]{
            "Học tập",
            "Văn phòng",
            "Gaming",
            "Đồ họa"
        });

        JLabel lblStyle = createLabel("Phong cách:");

        cboStyle = new JComboBox<>(new String[]{
            "Đơn giản",
            "Hiện đại",
            "Gaming RGB",
            "Tối giản"
        });

        btnGenerate = createButton("Tạo bộ phụ kiện");
        btnGenerate.addActionListener(e -> generateSetup());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(lblBudget, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(txtBudget, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(lblPurpose, gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        panel.add(cboPurpose, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(lblStyle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(cboStyle, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(btnGenerate, gbc);

        return panel;
    }

    // ===== BẢNG SẢN PHẨM =====
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblListTitle = new JLabel("Danh sách phụ kiện đề xuất");
        lblListTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblListTitle.setForeground(TEXT_COLOR);

        String[] columns = {
            "Tên phụ kiện",
            "Mô tả",
            "Giá bán (VNĐ)"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(195, 232, 229));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 14)
        );
        table.getTableHeader().setBackground(HEADER_COLOR);
        table.setAutoCreateRowSorter(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 300));

        lblTotal = new JLabel("Tổng tiền: 0 VNĐ");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 17));
        lblTotal.setForeground(BUTTON_COLOR);

        lblBudgetStatus = new JLabel("Hãy nhập ngân sách để bắt đầu.");
        lblBudgetStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblBudgetStatus.setForeground(TEXT_COLOR);

        btnChange = createButton("Đổi phụ kiện");
        btnChange.addActionListener(e -> changeProduct());

        btnAdd = createButton("Thêm vào giỏ hàng");
        btnAdd.addActionListener(e -> addToCart());

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnChange);
        buttonPanel.add(btnAdd);

        // Dùng panel riêng để hiển thị tổng tiền và trạng thái ngân sách.
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new javax.swing.BoxLayout(
                totalPanel, javax.swing.BoxLayout.Y_AXIS
        ));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.add(lblTotal);
        totalPanel.add(javax.swing.Box.createVerticalStrut(6));
        totalPanel.add(lblBudgetStatus);

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBackground(Color.WHITE);
        bottom.add(totalPanel, BorderLayout.CENTER);
        bottom.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(lblListTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ===== TẠO LABEL =====
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    // ===== TẠO BUTTON =====
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(175, 38));
        return button;
    }

    // ===== ĐỌC VÀ KIỂM TRA NGÂN SÁCH =====
    private double readBudget() {
        String value = txtBudget.getText().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "M hãy nhập ngân sách trước nhé!"
            );
        }

        // Chỉ nhận số nguyên hoặc số có dấu phân cách hàng nghìn.
        // Ví dụ hợp lệ: 5000000, 5.000.000, 5,000,000.
        if (!value.matches(
                "\\d+|\\d{1,3}(?:[.,]\\d{3})+"
        )) {
            throw new IllegalArgumentException(
                    "Ngân sách không hợp lệ!\n"
                    + "Chỉ nhập số nguyên, ví dụ 5000000 "
                    + "hoặc 5.000.000."
            );
        }

        // Bỏ dấu phân cách hàng nghìn trước khi chuyển thành số.
        String normalized = value.replace(".", "")
                                 .replace(",", "");

        long budget;

        try {
            budget = Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Ngân sách quá lớn hoặc không hợp lệ!"
            );
        }

        if (budget <= 0) {
            throw new IllegalArgumentException(
                    "Ngân sách phải lớn hơn 0 VNĐ!"
            );
        }

        return (double) budget;
    }

    // ===== TẠO BỘ PHỤ KIỆN =====
    private void generateSetup() {
        double budget;

        try {
            budget = readBudget();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Ngân sách không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );
            txtBudget.requestFocusInWindow();
            txtBudget.selectAll();
            return;
        }

        try {
            SanPhamDAO dao = new SanPhamDAO();
            List<SanPhamEntity> products = dao.selectAll();

            if (products == null) {
                throw new IllegalStateException(
                        "Danh sách sản phẩm trả về không hợp lệ."
                );
            }

            model.setRowCount(0);
            displayedProducts.clear();
            currentBudget = budget;

            double total = 0;

            for (SanPhamEntity sp : products) {
                if (sp == null || sp.getSl() <= 0) {
                    continue;
                }

                double price = sp.getGiaBan();

                if (Double.isNaN(price)
                        || Double.isInfinite(price)
                        || price <= 0
                        || price > budget) {
                    continue;
                }

                // Chỉ thêm sản phẩm nếu tổng mới không vượt ngân sách.
                if (total + price <= currentBudget) {
                    addProductToTable(sp);
                    total += price;
                }
            }

            updateTotal();

            if (displayedProducts.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy sản phẩm còn hàng phù hợp "
                        + "ngân sách.\n"
                        + "M hãy thử tăng ngân sách hoặc kiểm tra "
                        + "dữ liệu sản phẩm.",
                        "Không có sản phẩm phù hợp",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải sản phẩm từ database.\n"
                    + "Chi tiết: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // ===== THÊM SẢN PHẨM VÀO BẢNG =====
    private void addProductToTable(SanPhamEntity sp) {
        String description = sp.getMoTa();

        if (description == null || description.trim().isEmpty()) {
            description = sp.getTenL() == null
                    ? "Phụ kiện máy tính"
                    : sp.getTenL();
        }

        model.addRow(new Object[]{
            sp.getTenSP(),
            description,
            String.format("%,.0f", sp.getGiaBan())
        });

        displayedProducts.add(sp);
    }

    // ===== TÍNH TỔNG TIỀN =====
    private double calculateTotal() {
        double total = 0;

        for (SanPhamEntity sp : displayedProducts) {
            if (sp != null) {
                total += sp.getGiaBan();
            }
        }

        return total;
    }

    // ===== CẬP NHẬT TỔNG TIỀN VÀ TRẠNG THÁI NGÂN SÁCH =====
    private void updateTotal() {
        double total = calculateTotal();

        lblTotal.setText(
                "Tổng tiền: "
                + String.format("%,.0f", total)
                + " VNĐ"
        );

        if (currentBudget <= 0) {
            lblBudgetStatus.setText(
                    "Hãy nhập ngân sách để bắt đầu."
            );
            lblBudgetStatus.setForeground(TEXT_COLOR);
            return;
        }

        if (displayedProducts.isEmpty()) {
            lblBudgetStatus.setText(
                    "Chưa có sản phẩm trong cấu hình."
            );
            lblBudgetStatus.setForeground(TEXT_COLOR);
            return;
        }

        if (total > currentBudget) {
            double overBudget = total - currentBudget;

            lblBudgetStatus.setText(
                    "VƯỢT NGÂN SÁCH: "
                    + String.format("%,.0f", overBudget)
                    + " VNĐ"
            );
            lblBudgetStatus.setForeground(ERROR_COLOR);

        } else if (Double.compare(total, currentBudget) == 0) {
            lblBudgetStatus.setText(
                    "VỪA ĐỦ NGÂN SÁCH!"
            );
            lblBudgetStatus.setForeground(SUCCESS_COLOR);

        } else {
            double remaining = currentBudget - total;

            lblBudgetStatus.setText(
                    "Còn dư: "
                    + String.format("%,.0f", remaining)
                    + " VNĐ"
            );
            lblBudgetStatus.setForeground(SUCCESS_COLOR);
        }
    }

    // ===== ĐỔI PHỤ KIỆN =====
    private void changeProduct() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "M hãy chọn phụ kiện cần đổi trước nhé!",
                    "Chưa chọn sản phẩm",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (row >= displayedProducts.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Dữ liệu sản phẩm không khớp. "
                    + "Hãy tạo bộ phụ kiện lại.",
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (currentBudget <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Hãy tạo bộ phụ kiện với ngân sách hợp lệ trước nhé!"
            );
            return;
        }

        SanPhamEntity oldProduct = displayedProducts.get(row);

        try {
            SanPhamDAO dao = new SanPhamDAO();
            List<SanPhamEntity> allProducts = dao.selectAll();

            if (allProducts == null) {
                throw new IllegalStateException(
                        "Không lấy được danh sách sản phẩm."
                );
            }

            double total = calculateTotal();
            double oldPrice = oldProduct.getGiaBan();

            List<SanPhamEntity> candidates = new ArrayList<>();

            for (SanPhamEntity sp : allProducts) {
                if (sp == null || sp.getSl() <= 0) {
                    continue;
                }

                if (sp.getMaSP() == null
                        || sp.getMaSP().equals(oldProduct.getMaSP())) {
                    continue;
                }

                double price = sp.getGiaBan();

                if (Double.isNaN(price)
                        || Double.isInfinite(price)
                        || price <= 0) {
                    continue;
                }

                boolean alreadyUsed = false;

                for (int i = 0; i < displayedProducts.size(); i++) {
                    if (i == row) {
                        continue;
                    }

                    SanPhamEntity current = displayedProducts.get(i);

                    if (current.getMaSP() != null
                            && current.getMaSP().equals(sp.getMaSP())) {
                        alreadyUsed = true;
                        break;
                    }
                }

                if (alreadyUsed) {
                    continue;
                }

                double newTotal = total - oldPrice + price;

                // Không cho đổi nếu tổng tiền mới vượt ngân sách.
                if (newTotal <= currentBudget) {
                    candidates.add(sp);
                }
            }

            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy phụ kiện thay thế phù hợp "
                        + "ngân sách!\n"
                        + "M hãy thử tăng ngân sách.",
                        "Không có sản phẩm thay thế",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // Chọn ngẫu nhiên một phụ kiện thay thế hợp lệ.
            SanPhamEntity replacement = candidates.get(
                    (int) (Math.random() * candidates.size())
            );

            String description = replacement.getMoTa();

            if (description == null || description.trim().isEmpty()) {
                description = replacement.getTenL() == null
                        ? "Phụ kiện máy tính"
                        : replacement.getTenL();
            }

            model.setValueAt(replacement.getTenSP(), row, 0);
            model.setValueAt(description, row, 1);
            model.setValueAt(
                    String.format("%,.0f", replacement.getGiaBan()),
                    row,
                    2
            );

            displayedProducts.set(row, replacement);

            // Tính lại tổng tiền và trạng thái ngân sách.
            updateTotal();

            JOptionPane.showMessageDialog(
                    this,
                    "Đã đổi phụ kiện thành công!\n"
                    + "Tổng tiền mới: "
                    + String.format("%,.0f", calculateTotal())
                    + " VNĐ",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể đổi phụ kiện.\n"
                    + "Chi tiết: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // ===== THÊM VÀO GIỎ HÀNG =====
    private void addToCart() {
        if (displayedProducts.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Chưa có phụ kiện nào. Hãy tạo bộ phụ kiện trước!"
            );
            return;
        }

        double total = calculateTotal();

        if (total > currentBudget) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tổng tiền đang vượt ngân sách!\n"
                    + "Không thể tiếp tục.",
                    "Vượt ngân sách",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Bộ phụ kiện hiện có "
                + displayedProducts.size()
                + " sản phẩm.\n"
                + "Tổng tiền: "
                + String.format("%,.0f", total)
                + " VNĐ\n"
                + "Ngân sách: "
                + String.format("%,.0f", currentBudget)
                + " VNĐ\n\n"
                + "Chức năng thêm vào giỏ hàng cần tích hợp "
                + "với module giỏ hàng hiện tại của DAVISY."
        );
    }

    // ===== CHẠY CHƯƠNG TRÌNH =====
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new SetupBuildercode().setVisible(true);
        });
    }
}

