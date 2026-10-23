package com.service;

import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.strategy.BalancedStrategy;
import com.strategy.BudgetStrategy;
import com.strategy.PerformanceStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupBuilderServiceTest {
    private final SetupBuilderService service = new SetupBuilderService();
    private static SetupProduct p(String code, String category, long price, int stock, int score) {
        return new SetupProduct(code, "Product " + code, category, price, stock, score, "Test");
    }
    private List<SetupProduct> sample() {
        return List.of(p("B1", "BP", 1000000, 20, 5), p("B2", "BP", 400000, 20, 4),
            p("B3", "BP", 550000, 20, 4), p("T1", "TN", 650000, 20, 5),
            p("T2", "TN", 120000, 20, 3), p("T0", "TN", 1200000, 0, 3),
            p("G1", "GD", 50000, 20, 3), p("G2", "GD", 120000, 20, 5));
    }
    @Test public void sourceAlgorithmScenarioProducesThreeDistinctCompleteCombos() {
        List<SetupCombo> combos = service.generateCombos(sample(), 1800000, List.of("BP", "TN", "GD"));
        assertEquals(3, combos.size()); assertEquals(570000, combos.get(0).getTongTien());
        assertEquals(3, combos.stream().map(SetupCombo::getSignature).distinct().count());
        for (SetupCombo combo : combos) {
            assertTrue(combo.getTongTien() <= 1800000); assertEquals(3, combo.getProducts().size());
            assertEquals(3, combo.getProducts().stream().map(SetupProduct::getMaLoai).distinct().count());
            assertTrue(combo.getProducts().stream().allMatch(x -> x.getSoLuong() > 0));
        }
    }
    @Test public void rankingsMatchStrategyDefinitions() {
        SetupCombo cheap = new SetupCombo("", List.of(p("A", "X", 100, 1, 1)));
        SetupCombo good = new SetupCombo("", List.of(p("B", "X", 200, 1, 5)));
        assertTrue(new BudgetStrategy().calculateScore(cheap, 500) > new BudgetStrategy().calculateScore(good, 500));
        assertTrue(new BalancedStrategy().calculateScore(good, 500) > new BalancedStrategy().calculateScore(cheap, 500));
        assertTrue(new PerformanceStrategy().calculateScore(good, 500) > new PerformanceStrategy().calculateScore(cheap, 500));
    }
    @Test public void excludesOutOfStockAndRespectsCategoryAndBudget() {
        List<SetupCombo> combos = service.generateCombos(List.of(p("A", "X", 300, 2, 3),
            p("B", "Y", 500, 4, 5), p("C", "X", 1, 0, 5)), 800, List.of("Y"));
        assertEquals(1, combos.size()); assertEquals("B", combos.get(0).getProducts().get(0).getMaSP());
        assertTrue(service.generateCombos(sample(), 300000, List.of("BP", "TN", "GD")).isEmpty());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsZeroBudget() { service.generateCombos(sample(), 0, List.of("BP")); }
    @Test(expected=IllegalArgumentException.class) public void rejectsNegativeBudget() { service.generateCombos(sample(), -1, List.of("BP")); }
    @Test(expected=IllegalArgumentException.class) public void rejectsNoCategories() { service.generateCombos(sample(), 1000, List.of()); }
    @Test(expected=IllegalArgumentException.class) public void rejectsNullInputs() { service.generateCombos(null, 1000, List.of("BP")); }
    @Test(expected=IllegalArgumentException.class) public void rejectsDuplicateCategoryAfterNormalization() { service.generateCombos(sample(), 1000, List.of("bp", " BP ")); }
    @Test(expected=IllegalArgumentException.class) public void rejectsBlankCategory() { service.generateCombos(sample(), 1000, List.of(" ")); }
    @Test public void emptyProductsOrMissingCategoryReturnsNoCombo() {
        assertTrue(service.generateCombos(List.of(), 1000, List.of("X")).isEmpty());
        assertTrue(service.generateCombos(sample(), 1000, List.of("MISSING")).isEmpty());
    }
    @Test public void duplicateRowsDoNotDuplicateSignatures() {
        SetupProduct product = p("A", "X", 100, 2, 5);
        assertEquals(1, service.generateCombos(List.of(product, product), 1000, List.of("X")).size());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsConflictingDuplicateCodes() {
        service.generateCombos(List.of(p("A", "X", 100, 1, 5), p("A", "Y", 100, 1, 5)), 1000, List.of("X", "Y"));
    }
    @Test public void preservesProductCodeCaseForDatabaseLookup() { assertEquals("lower", p("lower", "X", 100, 1, 5).getMaSP()); }
    @Test public void tiesAreIndependentOfInputOrder() {
        List<SetupProduct> rows = new ArrayList<>(List.of(p("C", "X", 100, 1, 5), p("A", "X", 100, 1, 5), p("B", "X", 100, 1, 5)));
        List<String> first = service.generateCombos(rows, 500, List.of("X")).stream().map(SetupCombo::getSignature).toList();
        Collections.reverse(rows);
        assertEquals(first, service.generateCombos(rows, 500, List.of("X")).stream().map(SetupCombo::getSignature).toList());
        assertEquals(List.of("A", "B", "C"), first);
    }
    @Test public void largeMoneyDoesNotOverflowBudgetPruning() {
        assertTrue(service.generateCombos(List.of(p("A", "X", Long.MAX_VALUE, 1, 5), p("B", "Y", 1, 1, 5)),
                Long.MAX_VALUE, List.of("X", "Y")).isEmpty());
    }
    @Test public void budgetRankingKeepsOneVndDifferenceBeyondDoublePrecision() {
        List<SetupCombo> combos = service.generateCombos(List.of(p("A", "X", 9007199254740992L, 1, 1),
            p("B", "X", 9007199254740993L, 1, 5)), Long.MAX_VALUE, List.of("X"));
        assertEquals("A", combos.get(0).getSignature());
    }
    @Test public void signaturesEscapeProductCodeSeparators() {
        SetupCombo left = new SetupCombo("", List.of(p("A|B", "X", 1, 1, 1), p("C", "Y", 1, 1, 1)));
        SetupCombo right = new SetupCombo("", List.of(p("A", "X", 1, 1, 1), p("B|C", "Y", 1, 1, 1)));
        assertNotEquals(left.getSignature(), right.getSignature());
    }
    @Test(expected=ArithmeticException.class) public void rejectsOverflowingCombo() {
        new SetupCombo("", List.of(p("A", "X", Long.MAX_VALUE, 1, 5), p("B", "Y", 1, 1, 5)));
    }
    @Test public void replacementPreservesOriginalAndRecomputesTotals() {
        SetupCombo original = service.generateCombos(sample(), 1800000, List.of("BP", "TN", "GD")).get(0);
        SetupCombo changed = service.replaceProduct(original, sample().get(0), 1800000);
        assertEquals(570000, original.getTongTien()); assertEquals(1170000, changed.getTongTien());
    }
    @Test(expected=IllegalArgumentException.class) public void replacementMustHaveExistingCategory() {
        service.replaceProduct(new SetupCombo("", List.of(p("A", "X", 100, 1, 3))), p("B", "Y", 100, 1, 5), 1000);
    }
    @Test(expected=IllegalArgumentException.class) public void replacementMustFitBudget() {
        service.replaceProduct(new SetupCombo("", List.of(p("A", "X", 100, 1, 3))), p("B", "X", 1001, 1, 5), 1000);
    }
    @Test(expected=IllegalArgumentException.class) public void replacementMustBeInStock() {
        service.replaceProduct(new SetupCombo("", List.of(p("A", "X", 100, 1, 3))), p("B", "X", 100, 0, 5), 1000);
    }
    @Test(expected=UnsupportedOperationException.class) public void comboSnapshotCannotBeMutated() {
        new SetupCombo("", List.of(p("A", "X", 100, 1, 3))).getProducts().clear();
    }
    @Test(expected=IllegalArgumentException.class) public void comboCannotRepeatCategory() {
        new SetupCombo("", List.of(p("A", "X", 100, 1, 3), p("B", "X", 100, 1, 3)));
    }
    @Test(expected=IllegalArgumentException.class) public void comboCannotRepeatCodeAcrossCategories() {
        new SetupCombo("", List.of(p("A", "X", 100, 1, 3), p("A", "Y", 100, 1, 3)));
    }
    @Test(expected=IllegalArgumentException.class) public void limitsSearchRatherThanFreezingUI() {
        List<SetupProduct> rows = new ArrayList<>();
        for (int group=0; group<6; group++) for (int i=0; i<10; i++) rows.add(p(group+"-"+i, "G"+group, 1, 1, 3));
        service.generateCombos(rows, 100, List.of("G0", "G1", "G2", "G3", "G4", "G5"));
    }
}
