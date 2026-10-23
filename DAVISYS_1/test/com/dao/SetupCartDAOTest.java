package com.dao;

import com.dao.SetupCartDAO.CartContext;
import com.entity.SetupProduct;
import com.support.FakeSetupDatabase;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupCartDAOTest {
    private FakeSetupDatabase data() {
        FakeSetupDatabase db = new FakeSetupDatabase();
        db.products.add(FakeSetupDatabase.product("A", "BP", 100d, 10, ""));
        db.products.add(FakeSetupDatabase.product("B", "TN", 200d, 10, ""));
        return db;
    }
    private CartContext context() { return new CartContext("GH-REAL", "KH01", "0900000000", "Customer", "staff"); }
    private SetupProduct p(String code, long price, int stock) { return new SetupProduct(code, code, code, price, stock, 3, ""); }
    private List<SetupProduct> combo() { return List.of(p("A", 100, 10), p("B", 200, 10)); }
    private void expectFailure(SetupCartDAO dao, List<SetupProduct> rows) {
        try { dao.addCombo(context(), rows); fail("Expected failure"); } catch (IllegalStateException expected) { }
    }
    @Test public void addsOneUnitPerProductAndCommitsOnceUsingActualCartId() {
        FakeSetupDatabase db = data(); SetupCartDAO dao = new SetupCartDAO(db::open);
        assertEquals("GH-REAL", dao.getCarts("staff").get(0).cartId);
        var prices = dao.addCombo(context(), combo());
        assertEquals(Long.valueOf(100), prices.get("A")); assertEquals(Integer.valueOf(1), db.cartQuantities.get("A"));
        assertEquals(Integer.valueOf(1), db.cartQuantities.get("B")); assertEquals(1, db.commits); assertEquals(0, db.rollbacks);
    }
    @Test public void repeatedCodesMergeWithExistingCartQuantity() {
        FakeSetupDatabase db = data(); db.cartQuantities.put("A", 3);
        new SetupCartDAO(db::open).addCombo(context(), List.of(p("A", 100, 10), p("A", 100, 10)));
        assertEquals(Integer.valueOf(5), db.cartQuantities.get("A")); assertEquals(1, db.writes);
    }
    @Test public void middleRowFailureRollsBackEarlierWrites() {
        FakeSetupDatabase db = data(); db.cartQuantities.put("A", 2); db.failCode = "B";
        expectFailure(new SetupCartDAO(db::open), combo());
        assertEquals(1, db.writes); assertEquals(Integer.valueOf(2), db.cartQuantities.get("A"));
        assertFalse(db.cartQuantities.containsKey("B")); assertEquals(1, db.rollbacks); assertEquals(0, db.commits);
    }
    @Test public void changedPriceRollsBackWholeCombo() {
        FakeSetupDatabase db = data(); db.products.get(1).put("GIABAN", 201d);
        expectFailure(new SetupCartDAO(db::open), combo()); assertTrue(db.cartQuantities.isEmpty()); assertEquals(1, db.rollbacks);
    }
    @Test public void rechecksStockIncludingExistingQuantity() {
        FakeSetupDatabase db = data(); db.cartQuantities.put("B", 10);
        expectFailure(new SetupCartDAO(db::open), combo()); assertEquals(1, db.cartQuantities.size()); assertEquals(0, db.commits);
    }
    @Test public void removedProductRollsBackWholeCombo() {
        FakeSetupDatabase db = data(); db.products.remove(1);
        expectFailure(new SetupCartDAO(db::open), combo()); assertTrue(db.cartQuantities.isEmpty());
    }
    @Test public void zeroStockOrInvalidCurrentPriceStopsCommit() {
        for (Object price : new Object[]{null, -1d, Double.NaN, Double.POSITIVE_INFINITY, 100.5d}) {
            FakeSetupDatabase db = data(); db.products.get(0).put("GIABAN", price);
            expectFailure(new SetupCartDAO(db::open), combo()); assertEquals(0, db.writes); assertEquals(0, db.commits);
        }
        FakeSetupDatabase db = data(); db.products.get(0).put("SOLUONG", 0);
        expectFailure(new SetupCartDAO(db::open), combo()); assertEquals(0, db.commits);
    }
    @Test public void missingCartCustomerOrInactiveAccountStopsWrites() {
        FakeSetupDatabase db = data(); db.validCart = false;
        expectFailure(new SetupCartDAO(db::open), combo()); assertEquals(0, db.writes); assertEquals(1, db.rollbacks);
    }
    @Test public void badContextAndEmptyOrOutOfStockComboFailBeforeConnection() {
        FakeSetupDatabase db = data(); SetupCartDAO dao = new SetupCartDAO(db::open);
        for (List<SetupProduct> rows : List.of(List.<SetupProduct>of(), List.of(p("A", 100, 0)))) {
            try { dao.addCombo(context(), rows); fail(); } catch (IllegalArgumentException expected) { }
        }
        try { dao.addCombo(null, combo()); fail(); } catch (IllegalArgumentException expected) { }
        try { dao.addCombo(new CartContext("", "KH01", "", "", "staff"), combo()); fail(); } catch (IllegalArgumentException expected) { }
        assertEquals(0, db.opened);
    }
    @Test public void closesAllResourcesWhenTransactionFails() {
        FakeSetupDatabase db = data(); db.failCode = "B"; expectFailure(new SetupCartDAO(db::open), combo());
        assertEquals(db.opened, db.closed); assertEquals(db.statementsOpened, db.statementsClosed); assertEquals(db.resultsOpened, db.resultsClosed);
        assertTrue(db.queries.stream().filter(q -> q.contains("SELECT") && !q.startsWith("SELECT G.MAGH,G.MAKH")).allMatch(q -> q.contains("HOLDLOCK")));
    }
}
