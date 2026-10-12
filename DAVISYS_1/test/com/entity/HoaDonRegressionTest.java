package com.entity;
import org.junit.Test;
import static org.junit.Assert.*;
public class HoaDonRegressionTest {
    @Test public void mainDiscountFixRemainsIntact() {
        HoaDonEntity invoice = new HoaDonEntity(); invoice.setTongTien(100000); invoice.setPhanTramGG(10); invoice.setTichDiem(20);
        assertEquals(30000f, invoice.getTienGiam(), 0f); assertEquals(70000f, invoice.getThanhTien(), 0f);
        invoice.setTichDiem(1000);
        assertEquals(100000f, invoice.getTienGiam(), 0f); assertEquals(0f, invoice.getThanhTien(), 0f);
    }
}
