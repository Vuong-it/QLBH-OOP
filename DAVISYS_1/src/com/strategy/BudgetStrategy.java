package com.strategy;

import com.entity.SetupCombo;

/** Tiet kiem: tong gia nho nhat. */
public final class BudgetStrategy extends ComboStrategy {
    @Override
    public double calculateScore(SetupCombo combo, long budget) {
        return -combo.getTongTien();
    }
}
