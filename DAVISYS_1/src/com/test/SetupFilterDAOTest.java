package com.test;

import com.dao.SetupFilterDAO;
import com.entity.SetupFilterProduct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Chay Run File sau khi SQL da thanh cong, su dung ket noi that cua du an. */
public final class SetupFilterDAOTest {
    public static void main(String[] args) {
        String[] purposes = {"Gaming", "Văn phòng", "Học tập",
            "Thiết kế đồ họa", "Làm việc đa nhiệm"};
        String[] styles = {"Tối giản", "Hiện đại", "Năng động", "Sang trọng"};
        SetupFilterDAO dao = new SetupFilterDAO();
        for (String purpose : purposes) {
            for (String style : styles) {
                List<SetupFilterProduct> products = dao.getProducts(purpose, style);
                require(!products.isEmpty(), "Khong co du lieu demo cho " + purpose + " / " + style);
                Set<String> seen = new HashSet<>();
                for (SetupFilterProduct p : products) {
                    require(p.getMoTa() != null && !p.getMoTa().isBlank(), "Thieu mo ta: " + p.getMaSP());
                    require(seen.add(p.getMaSP()), "Trung ma san pham: " + p.getMaSP());
                    require(p.getSoLuong() > 0 && p.getGiaBan() > 0 && p.getDiemPhuHop() > 0,
                            "San pham khong du dieu kien: " + p.getMaSP());
                    require(p.getMucDich().equals(SetupFilterDAO.normalizePurpose(purpose)), "Sai muc dich");
                    require(p.getPhongCach().equals(SetupFilterDAO.normalizeStyle(style)), "Sai phong cach");
                }
                System.out.printf("%s / %s: %d san pham%n", purpose, style, products.size());
            }
        }
        for (SetupFilterProduct p : dao.getProducts("Học tập", "Tối giản", "BP")) {
            require(p.getMaLH().equals("BP"), "Sai loai hang");
        }
        require(dao.getProducts("Gaming", "Năng động", "NO_SUCH").isEmpty(),
                "Loai khong ton tai phai tra danh sach rong");
        System.out.println("PASS: 20 cap bo loc, ton kho, diem, loai hang va khong trung san pham.");
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }
}
