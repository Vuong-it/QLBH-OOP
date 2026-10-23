package com.dao;

import com.dao.SetupCartDAO.CartContext;
import com.service.SetupMoney;
import com.utils.JdbcHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checkout a single owned cart atomically, using current stock and prices. */
public final class CheckoutDAO {
    private final SetupCartDAO.ConnectionProvider connections;
    public CheckoutDAO() { this(JdbcHelper::openConnection); }
    public CheckoutDAO(SetupCartDAO.ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections);
    }
    public record Receipt(String invoiceId, long total, int lines) { }
    private record Line(String code, String name, String brand, String brandName,
            String category, String categoryName, java.sql.Date received,
            double cost, long price, int quantity) { }

    public Receipt checkout(CartContext cart, long expectedTotal) {
        if (cart == null || cart.cartId == null || cart.customerId == null || cart.username == null)
            throw new IllegalArgumentException("Vui lòng chọn giỏ hàng của tài khoản đăng nhập.");
        try (Connection c = connections.open()) {
            if (!c.getAutoCommit()) throw new SQLException("Checkout requires a dedicated connection.");
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT G.MAGH FROM dbo.GIOHANG G WITH (UPDLOCK,HOLDLOCK) "
                        + "JOIN dbo.TAIKHOAN T WITH (HOLDLOCK) ON T.TENDN=G.TENDN "
                        + "WHERE G.MAGH=? AND G.MAKH=? AND G.TENDN=? AND T.TRANGTHAI=1")) {
                    s.setString(1, cart.cartId); s.setString(2, cart.customerId); s.setString(3, cart.username);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) throw new IllegalArgumentException("Giỏ hàng hoặc tài khoản đã thay đổi. Hãy tải lại.");
                    }
                }
                List<Line> lines = new ArrayList<>();
                long total = 0;
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT P.MASP,P.TENSP,P.MAHANG,H.TENHANG,P.MALH,L.TENLH,P.NGAYNHAP,"
                        + "P.GIANHAP,P.GIABAN,P.SOLUONG AS STOCK,G.SOLUONG AS QTY "
                        + "FROM dbo.GIOHANGTAM G WITH (UPDLOCK,HOLDLOCK) "
                        + "JOIN dbo.SANPHAM P WITH (UPDLOCK,HOLDLOCK) ON P.MASP=G.MASP "
                        + "JOIN dbo.HANG H ON H.MAHANG=P.MAHANG JOIN dbo.LOAIHANG L ON L.MALH=P.MALH "
                        + "WHERE G.MAGH=? ORDER BY P.MASP")) {
                    s.setString(1, cart.cartId);
                    try (ResultSet r = s.executeQuery()) {
                        while (r.next()) {
                            int quantity = r.getInt("QTY");
                            if (r.wasNull() || quantity <= 0 || quantity > r.getInt("STOCK"))
                                throw new IllegalArgumentException("Không đủ tồn kho: " + r.getString("MASP"));
                            double raw = r.getDouble("GIABAN");
                            if (r.wasNull()) throw new IllegalArgumentException("Giá sản phẩm trống.");
                            long price = SetupMoney.fromDatabase(raw);
                            if (!legacyMoneyIsExact(price))
                                throw new IllegalArgumentException("Giá vượt độ chính xác hóa đơn hiện tại: " + r.getString("MASP"));
                            total = Math.addExact(total, Math.multiplyExact(price, quantity));
                            lines.add(new Line(r.getString("MASP"), r.getString("TENSP"), r.getString("MAHANG"),
                                    r.getString("TENHANG"), r.getString("MALH"), r.getString("TENLH"),
                                    r.getDate("NGAYNHAP"), r.getDouble("GIANHAP"), price, quantity));
                        }
                    }
                }
                if (lines.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống.");
                try (PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM dbo.GIOHANGTAM WHERE MAGH=?")) {
                    s.setString(1, cart.cartId);
                    try (ResultSet r = s.executeQuery()) {
                        r.next(); if (r.getInt(1) != lines.size()) throw new IllegalStateException("Giỏ chứa sản phẩm không còn hợp lệ.");
                    }
                }
                if (total != expectedTotal)
                    throw new IllegalArgumentException("Giá hoặc số lượng đã thay đổi. Hãy tải lại giỏ trước khi xác nhận.");
                // Legacy invoice columns/readers use float. Refuse silent rounding at that boundary.
                if (!legacyMoneyIsExact(total))
                    throw new IllegalArgumentException("Tổng tiền vượt độ chính xác hóa đơn hiện tại.");
                String invoice = nextId(c, "HOADON", "MAHD", "HD");
                java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
                try (PreparedStatement s = c.prepareStatement("INSERT dbo.HOADON "
                        + "(MAHD,TENDN,MAKH,MAGH,NGAYLAP,TONGTIEN,PHANTRAMGG,TRUTIENTICHDIEM,THANHTIEN) "
                        + "VALUES(?,?,?,?,?,?,0,0,?)")) {
                    s.setString(1, invoice); s.setString(2, cart.username); s.setString(3, cart.customerId);
                    s.setString(4, cart.cartId); s.setDate(5, today); s.setLong(6, total); s.setLong(7, total);
                    if (s.executeUpdate() != 1) throw new SQLException("Không ghi được hóa đơn.");
                }
                for (Line line : lines) {
                    String detail = nextId(c, "CHITIETHOADON", "MACTHD", "CTHD");
                    try (PreparedStatement s = c.prepareStatement("INSERT dbo.CHITIETHOADON "
                            + "(MACTHD,MAHD,MASP,MAHANG,MALH,NGAYLAP,TENSP,TENHANG,TENLH,NGAYNHAP,GIANHAP,GIABAN,SOLUONG) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                        s.setString(1, detail); s.setString(2, invoice); s.setString(3, line.code);
                        s.setString(4, line.brand); s.setString(5, line.category); s.setDate(6, today);
                        s.setString(7, line.name); s.setString(8, line.brandName); s.setString(9, line.categoryName);
                        s.setDate(10, line.received); s.setDouble(11, line.cost); s.setLong(12, line.price);
                        s.setInt(13, line.quantity); if (s.executeUpdate() != 1) throw new SQLException("Không ghi được chi tiết hóa đơn.");
                    }
                    try (PreparedStatement s = c.prepareStatement(
                            "UPDATE dbo.SANPHAM SET SOLUONG=SOLUONG-? WHERE MASP=? AND SOLUONG>=?")) {
                        s.setInt(1, line.quantity); s.setString(2, line.code); s.setInt(3, line.quantity);
                        if (s.executeUpdate() != 1) throw new SQLException("Tồn kho đã thay đổi: " + line.code);
                    }
                }
                try (PreparedStatement s = c.prepareStatement("DELETE dbo.GIOHANGTAM WHERE MAGH=?")) {
                    s.setString(1, cart.cartId);
                    if (s.executeUpdate() != lines.size()) throw new SQLException("Giỏ hàng đã thay đổi.");
                }
                try (PreparedStatement s = c.prepareStatement("UPDATE dbo.KHACHHANG SET TICHDIEM=TICHDIEM+? WHERE MAKH=?")) {
                    s.setInt(1, Math.toIntExact(total / 100000)); s.setString(2, cart.customerId);
                    if (s.executeUpdate() != 1) throw new SQLException("Khách hàng không còn tồn tại.");
                }
                c.commit();
                return new Receipt(invoice, total, lines.size());
            } catch (Exception ex) {
                try { c.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể xác nhận đơn hàng: " + ex.getMessage(), ex);
        }
    }

    private static boolean legacyMoneyIsExact(long amount) {
        return new java.math.BigDecimal((double)(float)amount)
                .compareTo(java.math.BigDecimal.valueOf(amount)) == 0;
    }
    private static String nextId(Connection c, String table, String column, String prefix) throws SQLException {
        // Called only with the constants above. Table locks serialize legacy numeric ID allocation.
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(bigint,SUBSTRING(" + column + "," + (prefix.length()+1)
                + ",10))),0)+1 FROM dbo." + table + " WITH (TABLOCKX,HOLDLOCK) WHERE " + column + " LIKE ?";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, prefix + "%");
            try (ResultSet r = s.executeQuery()) {
                r.next(); String id = prefix + String.format(java.util.Locale.ROOT, "%02d", r.getLong(1));
                if (id.length() > 10) throw new SQLException("Hết dung lượng mã hóa đơn.");
                return id;
            }
        }
    }
}
