package com.service;

import com.entity.SetupFilterProduct;
import com.entity.SetupProduct;

/** The metadata score /100 is rounded to nearest /5 (half up); its exact value stays in the reason. */
public final class SetupProductMapper {
    private SetupProductMapper() { }

    public static SetupProduct map(SetupFilterProduct row) {
        if (row == null || blank(row.getMaSP()) || blank(row.getTenSP()) || blank(row.getMaLH())
                || row.getSoLuong() < 0 || row.getDiemPhuHop() < 0 || row.getDiemPhuHop() > 100) {
            throw new IllegalArgumentException("Dữ liệu lọc sản phẩm không hợp lệ.");
        }
        int score = row.getMucDich() == null ? 1 : Math.max(1, (row.getDiemPhuHop() + 10) / 20);
        return new SetupProduct(row.getMaSP(), row.getTenSP(), row.getMaLH(), row.getTenLH(),
                SetupMoney.fromDatabase(row.getGiaBan()), row.getSoLuong(), score,
                row.getLyDo() + " " + row.getLyDoPhongCach());
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
