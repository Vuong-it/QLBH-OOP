package com.dao;

import com.entity.SetupProduct;
import com.service.SetupMoney;
import com.utils.JdbcHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Adapter to the existing cart. One combo product means ONE unit, never its stock count. */
public class SetupCartDAO {
    @FunctionalInterface
    public interface ConnectionProvider { Connection open() throws SQLException; }
    private final ConnectionProvider connections;

    public SetupCartDAO() { this(JdbcHelper::openConnection); }
    public SetupCartDAO(ConnectionProvider connections) { this.connections = Objects.requireNonNull(connections); }

    public static final class CartContext {
        public final String cartId, customerId, phone, customerName, username;
        public CartContext(String cartId, String customerId, String phone, String customerName, String username) {
            this.cartId = cartId; this.customerId = customerId; this.phone = phone;
            this.customerName = customerName; this.username = username;
        }
        @Override public String toString() { return customerName + " – " + phone + " (" + cartId + ")"; }
    }

    /** Read actual cart keys instead of assuming a customer's phone is always MAGH. */
    public List<CartContext> getCarts(String username) {
        requireText(username, "Cần đăng nhập để chọn giỏ hàng.");
        String sql = "SELECT G.MAGH,G.MAKH,K.DIENTHOAI,K.HOTEN FROM dbo.GIOHANG G "
                + "JOIN dbo.KHACHHANG K ON K.MAKH=G.MAKH "
                + "JOIN dbo.TAIKHOAN T ON T.TENDN=G.TENDN "
                + "WHERE G.TENDN=? AND T.TRANGTHAI=1 ORDER BY K.HOTEN,G.MAGH";
        List<CartContext> carts = new ArrayList<>();
        try (Connection connection = connections.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) carts.add(new CartContext(rs.getString("MAGH"), rs.getString("MAKH"),
                        rs.getString("DIENTHOAI"), rs.getString("HOTEN"), username.trim()));
            }
            return List.copyOf(carts);
        } catch (SQLException ex) { throw new IllegalStateException("Không tải được giỏ hàng khách hàng.", ex); }
    }

    public Map<String, Long> addCombo(CartContext cart, List<SetupProduct> products) {
        if (cart == null) throw new IllegalArgumentException("Vui lòng chọn giỏ hàng khách hàng.");
        requireText(cart.cartId, "Mã giỏ hàng không hợp lệ.");
        requireText(cart.customerId, "Khách hàng không hợp lệ.");
        requireText(cart.username, "Cần đăng nhập để thêm combo.");
        if (products == null || products.isEmpty()) throw new IllegalArgumentException("Combo chưa có sản phẩm.");
        Map<String, Integer> quantities = new TreeMap<>();
        Map<String, Long> expectedPrices = new TreeMap<>();
        for (SetupProduct product : products) {
            if (product == null || product.getSoLuong() <= 0) throw new IllegalArgumentException("Combo có sản phẩm hết hàng.");
            quantities.merge(product.getMaSP(), 1, Math::addExact);
            Long old = expectedPrices.putIfAbsent(product.getMaSP(), product.getGiaBan());
            if (old != null && old.longValue() != product.getGiaBan())
                throw new IllegalArgumentException("Giá trùng mã không nhất quán.");
        }

        try (Connection connection = connections.open()) {
            if (!connection.getAutoCommit()) throw new SQLException("ConnectionProvider phải cấp kết nối riêng.");
            connection.setAutoCommit(false);
            try {
                validateCart(connection, cart);
                Map<String, Long> currentPrices = new TreeMap<>();
                for (Map.Entry<String, Integer> item : quantities.entrySet()) {
                    currentPrices.put(item.getKey(), addOne(connection, cart.cartId.trim(), item.getKey(),
                            item.getValue(), expectedPrices.get(item.getKey())));
                }
                connection.commit();
                return Map.copyOf(currentPrices);
            } catch (Exception ex) {
                try { connection.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể thêm combo vào giỏ: " + usefulMessage(ex), ex);
        }
    }

    private void validateCart(Connection connection, CartContext cart) throws SQLException {
        String sql = "SELECT G.MAGH FROM dbo.GIOHANG G WITH (UPDLOCK,HOLDLOCK) "
                + "JOIN dbo.KHACHHANG K WITH (HOLDLOCK) ON K.MAKH=G.MAKH "
                + "JOIN dbo.TAIKHOAN T WITH (HOLDLOCK) ON T.TENDN=G.TENDN "
                + "WHERE G.MAGH=? AND G.MAKH=? AND G.TENDN=? AND T.TRANGTHAI=1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cart.cartId.trim()); statement.setString(2, cart.customerId.trim());
            statement.setString(3, cart.username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Giỏ hàng/khách hàng/tài khoản đã thay đổi. Hãy tải lại.");
            }
        }
    }

    private long addOne(Connection connection, String cartId, String code, int amount, long expectedPrice) throws SQLException {
        int stock;
        long price;
        String sql = "SELECT GIABAN,SOLUONG FROM dbo.SANPHAM WITH (UPDLOCK,HOLDLOCK) WHERE MASP=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Sản phẩm đã bị xóa: " + code);
                double rawPrice = rs.getDouble("GIABAN");
                if (rs.wasNull()) throw new IllegalArgumentException("Giá trống: " + code);
                price = SetupMoney.fromDatabase(rawPrice);
                stock = rs.getInt("SOLUONG");
                if (rs.wasNull() || stock <= 0) throw new IllegalArgumentException("Sản phẩm hết hàng: " + code);
                if (price != expectedPrice) throw new IllegalArgumentException("Giá đã thay đổi: " + code + ". Hãy tạo lại combo.");
            }
        }

        Integer current = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT SOLUONG FROM dbo.GIOHANGTAM WITH (UPDLOCK,HOLDLOCK) WHERE MAGH=? AND MASP=?")) {
            statement.setString(1, cartId); statement.setString(2, code);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    current = rs.getInt("SOLUONG");
                    if (rs.wasNull() || current <= 0) throw new IllegalArgumentException("Số lượng giỏ không hợp lệ: " + code);
                }
            }
        }
        long next = (current == null ? 0L : current.longValue()) + amount;
        if (next > stock || next > Integer.MAX_VALUE) throw new IllegalArgumentException("Không đủ tồn kho: " + code);
        String writeSql = current == null ? "INSERT INTO dbo.GIOHANGTAM (MAGH,MASP,SOLUONG) VALUES (?,?,?)"
                : "UPDATE dbo.GIOHANGTAM SET SOLUONG=? WHERE MAGH=? AND MASP=?";
        try (PreparedStatement statement = connection.prepareStatement(writeSql)) {
            if (current == null) {
                statement.setString(1, cartId); statement.setString(2, code); statement.setInt(3, (int) next);
            } else {
                statement.setInt(1, (int) next); statement.setString(2, cartId); statement.setString(3, code);
            }
            if (statement.executeUpdate() != 1) throw new SQLException("Không ghi được sản phẩm " + code);
        }
        return price;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
    private static String usefulMessage(Exception ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
