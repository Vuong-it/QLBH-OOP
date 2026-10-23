package com.dao;

import com.support.FakeSetupDatabase;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupFilterDAOTest {
    private FakeSetupDatabase data() {
        FakeSetupDatabase db = new FakeSetupDatabase();
        db.products.add(FakeSetupDatabase.product("A", "BP", 100d, 10, DescriptionFilterTest.DESCRIPTION));
        db.products.add(FakeSetupDatabase.product("B", "TN", 200d, 20, "Ordinary description"));
        return db;
    }
    @Test public void optionalFiltersSupportExistingDescriptionsWithoutPretendingPurposeFit() {
        FakeSetupDatabase db = data(); var products = new SetupFilterDAO(db::open).getProducts(null, null);
        assertEquals(2, products.size()); assertNull(products.get(0).getMucDich()); assertEquals(0, products.get(0).getDiemPhuHop());
        assertEquals(100d, products.get(0).getGiaBan(), 0d); assertEquals("B", products.get(1).getMaSP());
        assertEquals(200d, products.get(1).getGiaBan(), 0d);
    }
    @Test public void strictMetadataFiltersAndCategoryParameterAreHonored() {
        FakeSetupDatabase db = data(); SetupFilterDAO dao = new SetupFilterDAO(db::open);
        assertEquals(1, dao.getProducts("Học tập", "Tối giản", "BP").size());
        assertTrue(dao.getProducts("Gaming", "Hiện đại").isEmpty());
        assertTrue(dao.getProducts("Học tập", "Sang trọng").isEmpty());
        assertTrue(dao.getProducts("Học tập", "Tối giản", "MISSING").isEmpty());
    }
    @Test public void mapsMultipleRowsWithoutContamination() {
        var products = new SetupFilterDAO(data()::open).getProducts(null, null);
        assertEquals("A", products.get(0).getMaSP());
        assertEquals("BP", products.get(0).getMaLH());
        assertEquals("B", products.get(1).getMaSP());
        assertEquals("TN", products.get(1).getMaLH());
        assertEquals(20, products.get(1).getSoLuong());
    }
    @Test(expected=IllegalStateException.class) public void nullPriceCannotBeReadAsZero() {
        FakeSetupDatabase db = data(); db.products.get(0).put("GIABAN", null); new SetupFilterDAO(db::open).getProducts(null, null);
    }
    @Test(expected=IllegalStateException.class) public void nullStockCannotBeReadAsZero() {
        FakeSetupDatabase db = data(); db.products.get(0).put("SOLUONG", null); new SetupFilterDAO(db::open).getProducts(null, null);
    }
    @Test public void resourcesCloseOnSuccessAndFailure() {
        FakeSetupDatabase db = data(); new SetupFilterDAO(db::open).getProducts(null, null);
        db.products.get(0).put("SOLUONG", null);
        try { new SetupFilterDAO(db::open).getProducts(null, null); fail(); } catch (IllegalStateException expected) { }
        assertEquals(db.opened, db.closed); assertEquals(db.statementsOpened, db.statementsClosed); assertEquals(db.resultsOpened, db.resultsClosed);
    }
}
