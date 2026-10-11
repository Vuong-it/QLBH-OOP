package com.entity;

import java.util.Objects;

/** San pham dau vao cua thuat toan (da duoc DAO gan diem cho muc dich dang chon). */
public final class SetupProduct {
    private final String maSP;
    private final String tenSP;
    private final String maLoai;
    private final long giaBan;
    private final int soLuong;
    private final int diemPhuHop;
    private final String lyDo;

    public SetupProduct(String maSP, String tenSP, String maLoai,
                        long giaBan, int soLuong, int diemPhuHop, String lyDo) {
        this.maSP = Objects.requireNonNull(maSP, "maSP").trim();
        this.tenSP = Objects.requireNonNull(tenSP, "tenSP").trim();
        this.maLoai = Objects.requireNonNull(maLoai, "maLoai").trim().toUpperCase();
        if (this.maSP.isEmpty() || this.tenSP.isEmpty() || this.maLoai.isEmpty()) {
            throw new IllegalArgumentException("Ma san pham, ten san pham va ma loai khong duoc rong");
        }
        if (giaBan <= 0 || soLuong < 0 || diemPhuHop < 1 || diemPhuHop > 5) {
            throw new IllegalArgumentException("Gia > 0, so luong >= 0, diem trong [1,5]");
        }
        this.giaBan = giaBan;
        this.soLuong = soLuong;
        this.diemPhuHop = diemPhuHop;
        this.lyDo = lyDo == null ? "" : lyDo;
    }

    public String getMaSP() { return maSP; }
    public String getTenSP() { return tenSP; }
    public String getMaLoai() { return maLoai; }
    public long getGiaBan() { return giaBan; }
    public int getSoLuong() { return soLuong; }
    public int getDiemPhuHop() { return diemPhuHop; }
    public String getLyDo() { return lyDo; }

    @Override
    public String toString() {
        return tenSP + " (" + maSP + ", " + giaBan + " VND, diem " + diemPhuHop + "/5)";
    }
}
