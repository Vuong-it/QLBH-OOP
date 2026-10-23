package com.strategy;

import com.entity.SetupCombo;

/** Strategy Pattern: diem cao hon co do uu tien lon hon. */
public abstract class ComboStrategy {
    public abstract double calculateScore(SetupCombo combo, long budget);
}
