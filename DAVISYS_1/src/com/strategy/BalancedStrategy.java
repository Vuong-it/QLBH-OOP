package com.strategy;

import com.entity.SetupCombo;

/** Can bang: 70% do phu hop va 30% muc tiet kiem so voi ngan sach. */
public class BalancedStrategy implements ComboStrategy {
    @Override
    public double calculateScore(SetupCombo combo, long budget) {
        if (budget <= 0) throw new IllegalArgumentException("budget phai > 0");
        double fit = combo.getDiemTrungBinh() / 5.0;
        double saving = 1.0 - ((double) combo.getTongTien() / budget);
        return 0.7 * fit + 0.3 * saving;
    }
}
