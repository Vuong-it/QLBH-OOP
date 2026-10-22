
package com.service;

import com.entity.SetupCombo;
import com.entity.SetupProduct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupBuilderServiceTest {

    private final SetupBuilderService service =
            new SetupBuilderService();

    private List<SetupProduct> sampleProducts() {
        return List.of(
            new SetupProduct("SP0003", "Ban phim RAZER",
                    "BP", 1_000_000, 20, 5, "Gaming"),
            new SetupProduct("SP0005", "Ban phim G15",
                    "BP", 400_000, 20, 4, "Gaming"),
            new SetupProduct("SP0006", "Ban phim G16",
                    "BP", 550_000, 20, 4, "Gaming"),
            new SetupProduct("SP0011", "Tai nghe G3",
                    "TN", 650_000, 20, 5, "Gaming"),
            new SetupProduct("SP0007", "Tai nghe P5P",
                    "TN", 120_000, 20, 3, "Gaming"),
            new SetupProduct("SP0001", "Tai nghe het hang",
                    "TN", 1_200_000, 0, 3, "Het hang"),
            new SetupProduct("SP0008", "Gia do nhom",
                    "GD", 50_000, 20, 3, "Gaming"),
            new SetupProduct("SP0009", "Gia do nhua",
                    "GD", 120_000, 20, 5, "Gaming")
        );
    }

    @Test
    public void testGenerateThreeCombos() {
        List<SetupCombo> combos = service.generateCombos(
                sampleProducts(),
                1_800_000L,
                List.of("BP", "TN", "GD")
        );

        assertEquals(3, combos.size());
        assertEquals(570_000L, combos.get(0).getTongTien());
        assertEquals(1_170_000L, combos.get(1).getTongTien());
        assertEquals(1_770_000L, combos.get(2).getTongTien());
    }

    @Test
    public void testCombosWithinBudget() {
        long budget = 1_800_000L;

        List<SetupCombo> combos = service.generateCombos(
                sampleProducts(), budget,
                List.of("BP", "TN", "GD")
        );

        for (SetupCombo combo : combos) {
            assertTrue(combo.getTongTien() <= budget);
            assertEquals(3, combo.getProducts().size());
        }
    }

    @Test
    public void testNoDuplicateCombos() {
        List<SetupCombo> combos = service.generateCombos(
                sampleProducts(), 1_800_000L,
                List.of("BP", "TN", "GD")
        );

        Set<String> signatures = new HashSet<>();

        for (SetupCombo combo : combos) {
            assertTrue(signatures.add(combo.getSignature()));
        }
    }

    @Test
    public void testOutOfStockProductsExcluded() {
        List<SetupCombo> combos = service.generateCombos(
                sampleProducts(), 1_800_000L,
                List.of("BP", "TN", "GD")
        );

        for (SetupCombo combo : combos) {
            for (SetupProduct product : combo.getProducts()) {
                assertTrue(product.getSoLuong() > 0);
            }
        }
    }

    @Test
    public void testLowBudgetReturnsEmpty() {
        List<SetupCombo> combos = service.generateCombos(
                sampleProducts(), 300_000L,
                List.of("BP", "TN", "GD")
        );

        assertTrue(combos.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBudgetRejected() {
        service.generateCombos(
                sampleProducts(), -1L,
                List.of("BP", "TN", "GD")
        );
    }

    @Test
    public void testReplaceProduct() {
        List<SetupProduct> products = sampleProducts();
        long budget = 1_800_000L;

        SetupCombo original = service.generateCombos(
                products, budget,
                List.of("BP", "TN", "GD")
        ).get(0);

        SetupCombo replaced = service.replaceProduct(
                original, products.get(0), budget
        );

        assertTrue(replaced.getTongTien()
                > original.getTongTien());
        assertTrue(replaced.getTongTien() <= budget);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectOverBudgetReplacement() {
        List<SetupProduct> products = sampleProducts();

        SetupCombo original = service.generateCombos(
                products, 1_800_000L,
                List.of("BP", "TN", "GD")
        ).get(0);

        SetupProduct expensive = new SetupProduct(
                "SP9999", "Ban phim qua dat",
                "BP", 3_000_000, 1, 5, "Gaming"
        );

        service.replaceProduct(
                original, expensive, 1_800_000L
        );
    }
        @Test(expected = IllegalArgumentException.class)
    public void testRejectOutOfStockReplacement() {
        List<SetupProduct> products = sampleProducts();

        SetupCombo original = service.generateCombos(
                products, 1_800_000L,
                List.of("BP", "TN", "GD")
        ).get(0);

        SetupProduct outOfStock = new SetupProduct(
                "SP9998", "Tai nghe het hang",
                "TN", 200_000L, 0, 5, "Gaming"
        );

        service.replaceProduct(
                original, outOfStock, 1_800_000L
        );
    }
}
