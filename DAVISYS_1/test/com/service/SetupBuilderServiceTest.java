package com.service;

import com.dao.SanPhamDAO;
import com.entity.SanPhamEntity;
import com.entity.SetupCombo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupBuilderServiceTest {
    @Test
    public void buildsComboWithinBudgetAndOnlyUsesInStockProducts() {
        SetupBuilderService service = service(
                product("A", "A", 300, 2), product("B", "B", 500, 4), product("C", "C", 100, 0));

        SetupCombo combo = service.buildCombo(800, null);

        assertEquals(2, combo.getProducts().size());
        assertEquals(800d, combo.getTongTien(), 0.001d);
        assertFalse(contains(combo, "C"));
    }

    @Test
    public void categoryPreferenceRestrictsRecommendationsWhenAvailable() {
        SetupBuilderService service = service(
                product("A", "A", 300, 2), product("B", "B", 500, 4));

        SetupCombo combo = service.buildCombo(1000, "B");

        assertEquals(1, combo.getProducts().size());
        assertEquals("B", combo.getProducts().get(0).getMaLoai());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveBudget() {
        service(product("A", "A", 300, 2)).buildCombo(0, null);
    }

    private SetupBuilderService service(final SanPhamEntity... products) {
        return new SetupBuilderService(new SanPhamDAO() {
            @Override public List<SanPhamEntity> selectAll() { return new ArrayList<SanPhamEntity>(Arrays.asList(products)); }
        });
    }

    private SanPhamEntity product(String code, String category, float price, int stock) {
        SanPhamEntity result = new SanPhamEntity();
        result.setMaSP(code);
        result.setTenSP("Product " + code);
        result.setMaLH(category);
        result.setGiaBan(price);
        result.setSl(stock);
        return result;
    }

    private boolean contains(SetupCombo combo, String code) {
        for (int i = 0; i < combo.getProducts().size(); i++)
            if (code.equals(combo.getProducts().get(i).getMaSP())) return true;
        return false;
    }
}
