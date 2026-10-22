package com.strategy;

import com.entity.SetupCombo;

public interface ComboStrategy {
    double calculateScore(SetupCombo combo, long budget);
}