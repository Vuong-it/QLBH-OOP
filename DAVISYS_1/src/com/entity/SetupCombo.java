package com.entity;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/** Mot bo san pham hop le: moi ma loai chi co mot san pham. */
public final class SetupCombo {
    private final String loaiCombo;
    private final List<SetupProduct> products;
    private final long tongTien;
    private final int tongDiem;

    public SetupCombo(String loaiCombo, List<SetupProduct> products) {
        this.loaiCombo = Objects.requireNonNull(loaiCombo, "loaiCombo");
        this.products = List.copyOf(Objects.requireNonNull(products, "products"));
        if (this.products.isEmpty()) throw new IllegalArgumentException("Combo phai co san pham");
        Set<String> categories = new HashSet<>();
        Set<String> codes = new HashSet<>();
        long total = 0;
        int score = 0;
        for (SetupProduct p : this.products) {
            if (!categories.add(p.getMaLoai())) {
                throw new IllegalArgumentException("Combo trung ma loai: " + p.getMaLoai());
            }
            if (!codes.add(p.getMaSP()) || p.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Sản phẩm trùng mã hoặc hết hàng: " + p.getMaSP());
            }
            total = Math.addExact(total, p.getGiaBan());
            score = Math.addExact(score, p.getDiemPhuHop());
        }
        this.tongTien = total;
        this.tongDiem = score;
    }

    public String getLoaiCombo() { return loaiCombo; }
    public List<SetupProduct> getProducts() { return products; }

    public long getTongTien() {
        return tongTien;
    }

    public int getTongDiem() {
        return tongDiem;
    }

    public double getDiemTrungBinh() {
        return (double) getTongDiem() / products.size();
    }

    /** Khoa nhan dang de khong de xuat lai cung mot bo san pham. */
    public String getSignature() {
        return products.stream().map(SetupProduct::getMaSP).sorted()
                .map(code -> code.replace("\\", "\\\\").replace("|", "\\|"))
                .collect(Collectors.joining("|"));
    }

    public SetupCombo withLoaiCombo(String loaiMoi) {
        return new SetupCombo(loaiMoi, products);
    }
}
