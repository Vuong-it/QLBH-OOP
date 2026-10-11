package com.entity;

import java.util.ArrayList;
import java.util.List;

public class SetupCombo {
    private List<SetupProduct> products = new ArrayList<SetupProduct>();
    private double tongTien;
    private double tongDiem;
    private String loaiCombo;

    public SetupCombo() { }
    public SetupCombo(List<SetupProduct> products, double tongTien, double tongDiem, String loaiCombo) {
        setProducts(products);
        this.tongTien = tongTien;
        this.tongDiem = tongDiem;
        this.loaiCombo = loaiCombo;
    }
    public List<SetupProduct> getProducts() { return products; }
    public void setProducts(List<SetupProduct> products) {
        this.products = products == null ? new ArrayList<SetupProduct>() : new ArrayList<SetupProduct>(products);
    }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public double getTongDiem() { return tongDiem; }
    public void setTongDiem(double tongDiem) { this.tongDiem = tongDiem; }
    public String getLoaiCombo() { return loaiCombo; }
    public void setLoaiCombo(String loaiCombo) { this.loaiCombo = loaiCombo; }
}
