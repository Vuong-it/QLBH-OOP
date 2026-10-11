package com.dao;

import com.entity.SetupProduct;
import com.utils.JdbcHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Transactional adapter for DAVISY's existing GIOHANG/GIOHANGTAM tables. */
public class SetupCartDAO {

    public Map<String, Double> addCombo(String maGH, List<SetupProduct> products) {
        if (maGH == null || maGH.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn khách hàng.");
        if (products == null || products.isEmpty()) throw new IllegalArgumentException("Combo chưa có sản phẩm.");

        // Merge duplicate product rows so the cart's (MAGH, MASP) key is only written once.
        Map<String, Integer> quantities = new LinkedHashMap<String, Integer>();
        for (SetupProduct product : products) {
            if (product == null || product.getMaSP() == null || product.getMaSP().trim().isEmpty())
                throw new IllegalArgumentException("Combo có mã sản phẩm không hợp lệ.");
            if (product.getSoLuong() <= 0) throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
            Integer previous = quantities.get(product.getMaSP().trim());
            long total = (previous == null ? 0L : previous.longValue()) + product.getSoLuong();
            if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Số lượng sản phẩm vượt giới hạn.");
            quantities.put(product.getMaSP().trim(), (int) total);
        }

        Connection connection = null;
        Map<String, Double> currentPrices = new LinkedHashMap<String, Double>();
        try {
            PreparedStatement starter = JdbcHelper.getStmt("SELECT 1");
            connection = starter.getConnection();
            starter.close();
            connection.setAutoCommit(false);
            try {
                validateCart(connection, maGH);
                for (Map.Entry<String, Integer> item : quantities.entrySet()) {
                    currentPrices.put(item.getKey(), addOne(connection, maGH, item.getKey(), item.getValue()));
                }
                connection.commit();
                return currentPrices;
            } catch (Exception ex) {
                try { connection.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể thêm combo vào giỏ hàng: " + usefulMessage(ex), ex);
        } finally {
            if (connection != null) {
                try { connection.setAutoCommit(true); } catch (SQLException ignored) { }
                try { connection.close(); } catch (SQLException ignored) { }
            }
        }
    }

    private void validateCart(Connection connection, String maGH) throws SQLException {
        String sql = "SELECT MAGH FROM GIOHANG WITH (UPDLOCK, HOLDLOCK) WHERE MAGH = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maGH.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Không tìm thấy giỏ hàng của khách hàng đã chọn.");
            }
        }
    }

    private double addOne(Connection connection, String maGH, String maSP, int amount) throws SQLException {
        String productSql = "SELECT GIABAN, SOLUONG FROM SANPHAM WITH (UPDLOCK, HOLDLOCK) WHERE MASP = ?";
        int stock;
        try (PreparedStatement statement = connection.prepareStatement(productSql)) {
            statement.setString(1, maSP);
            try (ResultSet product = statement.executeQuery()) {
                if (!product.next()) throw new IllegalArgumentException("Sản phẩm " + maSP + " không còn tồn tại.");
                double currentPrice = product.getDouble("GIABAN");
                stock = product.getInt("SOLUONG");
                if (product.wasNull() || !Double.isFinite(currentPrice) || currentPrice < 0 || stock < 0)
                    throw new IllegalArgumentException("Dữ liệu giá hoặc tồn kho của " + maSP + " không hợp lệ.");
                if (stock < amount) throw new IllegalArgumentException("Sản phẩm " + maSP + " chỉ còn " + stock + " trong kho.");
            }
        }

        String cartSql = "SELECT SOLUONG FROM GIOHANGTAM WITH (UPDLOCK, HOLDLOCK) WHERE MAGH = ? AND MASP = ?";
        Integer current = null;
        try (PreparedStatement statement = connection.prepareStatement(cartSql)) {
            statement.setString(1, maGH.trim());
            statement.setString(2, maSP);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) current = result.getInt("SOLUONG");
            }
        }
        if (current == null) {
            if (amount > stock) throw new IllegalArgumentException("Sản phẩm " + maSP + " không đủ tồn kho.");
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO GIOHANGTAM (MAGH, MASP, SOLUONG) VALUES (?, ?, ?)")) {
                statement.setString(1, maGH.trim()); statement.setString(2, maSP); statement.setInt(3, amount);
                if (statement.executeUpdate() != 1) throw new SQLException("Không thêm được sản phẩm " + maSP + " vào giỏ.");
            }
        } else {
            long next = (long) current + amount;
            if (current <= 0 || next > Integer.MAX_VALUE) throw new IllegalArgumentException("Số lượng hiện tại trong giỏ không hợp lệ cho " + maSP + ".");
            if (next > stock) throw new IllegalArgumentException("Sản phẩm " + maSP + " chỉ còn " + stock + " trong kho, giỏ hiện có " + current + ".");
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE GIOHANGTAM SET SOLUONG = ? WHERE MAGH = ? AND MASP = ?")) {
                statement.setInt(1, (int) next); statement.setString(2, maGH.trim()); statement.setString(3, maSP);
                if (statement.executeUpdate() != 1) throw new SQLException("Không cập nhật được sản phẩm " + maSP + " trong giỏ.");
            }
        }
        return currentPrice(connection, maSP);
    }

    private double currentPrice(Connection connection, String maSP) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT GIABAN FROM SANPHAM WHERE MASP = ?")) {
            statement.setString(1, maSP);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Sản phẩm " + maSP + " không còn tồn tại.");
                double price = result.getDouble(1);
                if (result.wasNull() || !Double.isFinite(price) || price < 0)
                    throw new IllegalArgumentException("Giá bán của " + maSP + " không hợp lệ.");
                return price;
            }
        }
    }

    private String usefulMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.trim().isEmpty() ? ex.getClass().getSimpleName() : message;
    }
}
