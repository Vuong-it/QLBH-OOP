package com.service;

import java.math.BigDecimal;

/** SQL FLOAT boundary: accept only finite, positive, exactly representable integer VND. */
public final class SetupMoney {
    private static final double MAX_EXACT_DOUBLE_INTEGER = 9_007_199_254_740_991d;
    private SetupMoney() { }

    public static long fromDatabase(double amount) {
        if (!Double.isFinite(amount) || amount <= 0 || amount > MAX_EXACT_DOUBLE_INTEGER) {
            throw new IllegalArgumentException("Giá phải là số VNĐ nguyên, dương và trong giới hạn chính xác.");
        }
        try {
            return BigDecimal.valueOf(amount).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Giá có phần lẻ VNĐ; cần kiểm tra dữ liệu sản phẩm.", ex);
        }
    }

    public static long parseBudget(String input) {
        if (input == null || !input.trim().matches("[0-9]+")) {
            throw new IllegalArgumentException("Nhập ngân sách bằng số VNĐ nguyên, ví dụ 5000000.");
        }
        try {
            long amount = Long.parseLong(input.trim());
            if (amount <= 0) throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ngân sách phải lớn hơn 0 và không vượt giới hạn số nguyên.");
        }
    }
}
