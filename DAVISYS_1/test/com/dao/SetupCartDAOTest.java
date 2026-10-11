package com.dao;

import com.entity.SetupProduct;
import java.util.Collections;
import org.junit.Test;

public class SetupCartDAOTest {
    private final SetupCartDAO dao = new SetupCartDAO();

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyComboBeforeOpeningConnection() {
        dao.addCombo("0900000000", Collections.<SetupProduct>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidProductCodeBeforeOpeningConnection() {
        SetupProduct product = new SetupProduct(null, "Invalid", "X", 100, 1, 0, "");
        dao.addCombo("0900000000", Collections.singletonList(product));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidQuantityBeforeOpeningConnection() {
        SetupProduct product = new SetupProduct("SP01", "Invalid", "X", 100, 0, 0, "");
        dao.addCombo("0900000000", Collections.singletonList(product));
    }
}
