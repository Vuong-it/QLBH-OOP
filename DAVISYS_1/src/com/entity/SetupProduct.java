package com.entity;

/** Product shown by the Smart Setup Builder. Cart pricing and stock are always re-read. */
public class SetupProduct {
    private String maSP;
    private String tenSP;
    private String maLoai;
    private double giaBan;
    private int soLuong = 1;
    private double diemPhuHop;
    private String lyDo;

    public SetupProduct() { }

    public SetupProduct(String maSP, String tenSP, String maLoai, double giaBan,
            int soLuong, double diemPhuHop, String lyDo) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.maLoai = maLoai;
        this.giaBan = giaBan;
        this.soLuong = soLuong;
        this.diemPhuHop = diemPhuHop;
        this.lyDo = lyDo;
    }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String maLoai) { this.maLoai = maLoai; }
    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDiemPhuHop() { return diemPhuHop; }
    public void setDiemPhuHop(double diemPhuHop) { this.diemPhuHop = diemPhuHop; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
}
