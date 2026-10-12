package com.service;

import com.entity.SetupFilterProduct;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupMoneyTest {
    @Test public void acceptsExactIntegerVnd() { assertEquals(1000000, SetupMoney.fromDatabase(1000000)); }
    @Test public void rejectsInvalidDatabasePricesWithoutRounding() {
        for (double price : new double[]{0, -1, 1.5, Double.NaN, Double.POSITIVE_INFINITY, 9_007_199_254_740_992d}) {
            try { SetupMoney.fromDatabase(price); fail("Accepted " + price); } catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void parsesBudgetWithoutDoubleLoss() { assertEquals(Long.MAX_VALUE, SetupMoney.parseBudget("9223372036854775807")); }
    @Test public void rejectsInvalidBudgets() {
        for (String input : new String[]{null, "", "0", "-1", "NaN", "Infinity", "100.5", "1e3", "9223372036854775808"}) {
            try { SetupMoney.parseBudget(input); fail("Accepted " + input); } catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void mapperKeepsCodePriceStockAndExplicitScoreScale() {
        var product = SetupProductMapper.map(row("A", "X", 100, 90));
        assertEquals("A", product.getMaSP()); assertEquals("X", product.getMaLoai());
        assertEquals(100, product.getGiaBan()); assertEquals(8, product.getSoLuong()); assertEquals(5, product.getDiemPhuHop());
    }
    @Test(expected=IllegalArgumentException.class) public void mapperRejectsMissingCategory() { SetupProductMapper.map(row("A", null, 100, 90)); }
    @Test(expected=IllegalArgumentException.class) public void mapperRejectsFractionalPrice() { SetupProductMapper.map(row("A", "X", 100.25, 90)); }
    @Test(expected=IllegalArgumentException.class) public void mapperRejectsMissingRow() { SetupProductMapper.map(null); }
    @Test(expected=IllegalArgumentException.class) public void mapperRejectsInvalidScore() { SetupProductMapper.map(row("A", "X", 100, 101)); }
    private SetupFilterProduct row(String code, String category, double price, int score) {
        return new SetupFilterProduct(code, "Product", category, "Category", price, 8, "GAMING", score, "Reason", "", null, "");
    }
}
