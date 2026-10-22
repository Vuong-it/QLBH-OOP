package com.strategy;

import com.entity.SetupCombo;

/** Tiet kiem: tong gia nho nhat. */
public class BudgetStrategy implements ComboStrategy {
    @Override
    public double calculateScore(SetupCombo combo, long budget) {
        return -combo.getTongTien();
    }
}
