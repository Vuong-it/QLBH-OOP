package com.service;

import com.dao.SetupFilterDAO;
import com.entity.SetupCombo;
import com.entity.SetupProduct;
import java.util.List;
import java.util.Objects;

/** DAO adapter around the SQL-independent strategy service. */
public final class SetupBuilderFacade {
    private final SetupFilterDAO filter;
    private final SetupBuilderService algorithm;

    public SetupBuilderFacade() { this(new SetupFilterDAO(), new SetupBuilderService()); }
    public SetupBuilderFacade(SetupFilterDAO filter, SetupBuilderService algorithm) {
        this.filter = Objects.requireNonNull(filter);
        this.algorithm = Objects.requireNonNull(algorithm);
    }

    public List<SetupProduct> getProducts(String purpose, String style) {
        return filter.getProducts(purpose, style).stream().map(SetupProductMapper::map).toList();
    }

    public List<SetupCombo> generateCombos(List<SetupProduct> products, long budget, List<String> categories) {
        return algorithm.generateCombos(products, budget, categories);
    }

    public SetupCombo replaceProduct(SetupCombo combo, SetupProduct replacement, long budget) {
        return algorithm.replaceProduct(combo, replacement, budget);
    }
}
