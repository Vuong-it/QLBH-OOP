package com.dao;

import com.entity.SetupFilterProduct;
import com.utils.JdbcHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/** Loc truc tiep tu SANPHAM.MOTA theo dinh dang setup_product_descriptions.sql. */
public final class SetupFilterDAO {
    /** Diem toi thieu de san pham phu hop voi muc dich da chon. */
    public static final int MIN_PURPOSE_SCORE = 70;
    private static final Set<String> PURPOSES = Set.of(
            "GAMING", "HOC_TAP", "VAN_PHONG",
            "THIET_KE_DO_HOA", "LAM_VIEC_DA_NHIEM");
    private static final Set<String> STYLES = Set.of(
            "TOI_GIAN", "HIEN_DAI", "NANG_DONG", "SANG_TRONG");

    @FunctionalInterface
    public interface ConnectionProvider {
        Connection open() throws SQLException;
    }

    private final ConnectionProvider connections;

    public SetupFilterDAO() {
        this(() -> DriverManager.getConnection(
                JdbcHelper.dburl, JdbcHelper.user, JdbcHelper.pass));
    }

    public SetupFilterDAO(ConnectionProvider connections) {
        if (connections == null) {
            throw new IllegalArgumentException("ConnectionProvider khong duoc null.");
        }
        this.connections = connections;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vui long chon gia tri bo loc.");
        }
        String clean = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
        return clean;
    }

    public static String normalizePurpose(String value) {
        String code = normalize(value);
        if (!PURPOSES.contains(code)) {
            throw new IllegalArgumentException("Muc dich khong hop le: " + value);
        }
        return code;
    }

    public static String normalizeStyle(String value) {
        String code = normalize(value);
        if (!STYLES.contains(code)) {
            throw new IllegalArgumentException("Phong cach khong hop le: " + value);
        }
        return code;
    }
    
public List<SetupFilterProduct> getProducts(String mucDich, String phongCach) {
        return getProducts(mucDich, phongCach, null);
    }

    /** maLH null/blank: tat ca loai; hai bo loc muc dich/phong cach la bat buoc. */
    public List<SetupFilterProduct> getProducts(
            String mucDich, String phongCach, String maLH) {
        String purpose = normalizePurpose(mucDich);
        String style = normalizeStyle(phongCach);
        String category = maLH == null ? "" : maLH.trim();
        if (category.length() > 10) {
            throw new IllegalArgumentException("Ma loai hang toi da 10 ky tu.");
        }

        String sql = "SELECT S.MASP,S.TENSP,S.MALH,L.TENLH,"
                + "S.GIABAN,S.SOLUONG,S.HINH,S.MOTA "
                + "FROM dbo.SANPHAM S "
                + "JOIN dbo.LOAIHANG L ON L.MALH=S.MALH "
                + "WHERE S.SOLUONG>0 AND S.GIABAN>0 "
                + (category.isEmpty() ? "" : "AND S.MALH=? ");

        List<SetupFilterProduct> products = new ArrayList<>();
        try (Connection connection = connections.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!category.isEmpty()) {
                statement.setString(1, category);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String description = rs.getString("MOTA");
                    int score = purposeScore(description, purpose);
                    if (score < MIN_PURPOSE_SCORE || !hasStyle(description, style)) continue;
                    products.add(new SetupFilterProduct(
                            rs.getString("MASP"), rs.getString("TENSP"),
                            rs.getString("MALH"), rs.getString("TENLH"),
                            rs.getDouble("GIABAN"), rs.getInt("SOLUONG"),
                            purpose, score,
                            "Điểm đọc từ mô tả: " + score + "/100 (ngưỡng phù hợp: "
                                    + MIN_PURPOSE_SCORE + "/100).", rs.getString("HINH"),
                            style, "Phong cách đọc từ mô tả sản phẩm.", description));
                }
            }
            products.sort(Comparator.comparingInt(SetupFilterProduct::getDiemPhuHop)
                    .reversed().thenComparingDouble(SetupFilterProduct::getGiaBan)
                    .thenComparing(SetupFilterProduct::getMaSP));
            return products;
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Khong the lay san pham theo muc dich va phong cach. "
                    + "Kiem tra ket noi database DAVISYS.", ex);
        }
    }

    private static String plain(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

 /** Chi doc diem trong muc Nhu cau; khong doan tu ten hay dac diem. */
    public static int purposeScore(String description, String mucDich) {
        String purpose = normalizePurpose(mucDich).replace('_', ' ');
        String text = plain(description);
        Matcher section = Pattern.compile(
                "NHU CAU DEMO \\([^)]*\\): (.*?)\\. PHONG CACH DEMO:")
                .matcher(text);
        if (!section.find()) return 0;
        int found = -1;
        for (String entry : section.group(1).split(";")) {
            Matcher score = Pattern.compile("^" + Pattern.quote(purpose)
                    + " *: *([0-9]{1,3})$").matcher(entry.trim());
            if (score.matches()) {
                int value = Integer.parseInt(score.group(1));
                if (found != -1 || value > 100) return 0;
                found = value;
            }
        }
        return Math.max(0, found);
    }

    /** Chi nhan nhan phong cach chinh xac trong muc Phong cach demo. */
    public static boolean hasStyle(String description, String phongCach) {
        String style = normalizeStyle(phongCach).replace('_', ' ');
        String text = plain(description);
        Matcher section = Pattern.compile(
                "\\. PHONG CACH DEMO: (.*?)\\. DIEM VA PHONG CACH")
                .matcher(text);
        if (!section.find()) return false;
        for (String entry : section.group(1).split(";")) {
            if (entry.trim().equals(style)) return true;
        }
        return false;
    }

}
