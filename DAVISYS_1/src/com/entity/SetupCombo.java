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

    public SetupCombo(String loaiCombo, List<SetupProduct> products) {
        this.loaiCombo = Objects.requireNonNull(loaiCombo, "loaiCombo");
        this.products = List.copyOf(Objects.requireNonNull(products, "products"));
        if (this.products.isEmpty()) throw new IllegalArgumentException("Combo phai co san pham");
        Set<String> categories = new HashSet<>();
        for (SetupProduct p : this.products) {
            if (!categories.add(p.getMaLoai())) {
                throw new IllegalArgumentException("Combo trung ma loai: " + p.getMaLoai());
            }
        }
    }

    public String getLoaiCombo() { return loaiCombo; }
    public List<SetupProduct> getProducts() { return products; }

    public long getTongTien() {
        long result = 0;
        for (SetupProduct product : products) {
            result = Math.addExact(result, product.getGiaBan());
        }
        return result;
    }

    public int getTongDiem() {
        return products.stream().mapToInt(SetupProduct::getDiemPhuHop).sum();
    }

    public double getDiemTrungBinh() {
        return (double) getTongDiem() / products.size();
    }

    /** Khoa nhan dang de khong de xuat lai cung mot bo san pham. */
    public String getSignature() {
        return products.stream().map(SetupProduct::getMaSP).sorted()
                .collect(Collectors.joining("|"));
    }

    public SetupCombo withLoaiCombo(String loaiMoi) {
        return new SetupCombo(loaiMoi, products);
    }
}
