package com.entity;

/** San pham da loc theo ca muc dich va phong cach. */
public final class SetupFilterProduct {
    private final String maSP, tenSP, maLH, tenLH, mucDich;
    private final String lyDo, hinh, phongCach, lyDoPhongCach, moTa;
    private final double giaBan;
    private final int soLuong, diemPhuHop;

    public SetupFilterProduct(String maSP, String tenSP, String maLH,
            String tenLH, double giaBan, int soLuong, String mucDich,
            int diemPhuHop, String lyDo, String hinh,
            String phongCach, String lyDoPhongCach) {
        this(maSP, tenSP, maLH, tenLH, giaBan, soLuong, mucDich,
                diemPhuHop, lyDo, hinh, phongCach, lyDoPhongCach, "");
    }

    public SetupFilterProduct(String maSP, String tenSP, String maLH,
            String tenLH, double giaBan, int soLuong, String mucDich,
            int diemPhuHop, String lyDo, String hinh,
            String phongCach, String lyDoPhongCach, String moTa) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.maLH = maLH;
        this.tenLH = tenLH;
        this.giaBan = giaBan;
        this.soLuong = soLuong;
        this.mucDich = mucDich;
        this.diemPhuHop = diemPhuHop;
        this.lyDo = lyDo;
        this.hinh = hinh;
        this.phongCach = phongCach;
        this.lyDoPhongCach = lyDoPhongCach;
        this.moTa = moTa;
    }

    public String getMoTa() { return moTa; }

    public String getMaSP() { return maSP; }
    public String getTenSP() { return tenSP; }
    public String getMaLH() { return maLH; }
    public String getTenLH() { return tenLH; }
    public double getGiaBan() { return giaBan; }
    public int getSoLuong() { return soLuong; }
    public String getMucDich() { return mucDich; }
    public int getDiemPhuHop() { return diemPhuHop; }
    public String getLyDo() { return lyDo; }
    public String getHinh() { return hinh; }
    public String getPhongCach() { return phongCach; }
    public String getLyDoPhongCach() { return lyDoPhongCach; }
}
