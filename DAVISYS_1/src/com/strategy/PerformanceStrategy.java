package com.strategy;

import com.entity.SetupCombo;

/** Trai nghiem: tong diem phu hop cao nhat. */
public final class PerformanceStrategy extends ComboStrategy {
    @Override
    public double calculateScore(SetupCombo combo, long budget) {
        return combo.getTongDiem();
    }
}
