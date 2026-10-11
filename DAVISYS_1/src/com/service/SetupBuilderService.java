package com.service;

import com.dao.SanPhamDAO;
import com.entity.SanPhamEntity;
import com.entity.SetupCombo;
import com.entity.SetupProduct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lightweight recommender used until a dedicated scoring DAO/service is supplied. */
public class SetupBuilderService {
    private final SanPhamDAO productDAO;

    public SetupBuilderService() { this(new SanPhamDAO()); }
    public SetupBuilderService(SanPhamDAO productDAO) { this.productDAO = productDAO; }

    public List<SanPhamEntity> getAvailableProducts() {
        List<SanPhamEntity> available = new ArrayList<SanPhamEntity>();
        for (SanPhamEntity product : productDAO.selectAll()) {
            if (product != null && product.getMaSP() != null && product.getSl() > 0
                    && Double.isFinite(product.getGiaBan()) && product.getGiaBan() > 0) available.add(product);
        }
        return available;
    }

    public SetupCombo buildCombo(double budget, String preferredCategory) {
        if (!Double.isFinite(budget) || budget <= 0) throw new IllegalArgumentException("Ngân sách phải lớn hơn 0.");
        List<SanPhamEntity> candidates = getAvailableProducts();
        if (preferredCategory != null && !preferredCategory.trim().isEmpty()) {
            List<SanPhamEntity> preferred = new ArrayList<SanPhamEntity>();
            for (SanPhamEntity product : candidates) {
                if (preferredCategory.equalsIgnoreCase(product.getMaLH())) preferred.add(product);
            }
            if (!preferred.isEmpty()) candidates = preferred;
        }
        Collections.sort(candidates, new Comparator<SanPhamEntity>() {
            public int compare(SanPhamEntity left, SanPhamEntity right) {
                int byCategory = safe(left.getMaLH()).compareToIgnoreCase(safe(right.getMaLH()));
                if (byCategory != 0) return byCategory;
                return Double.compare(left.getGiaBan(), right.getGiaBan());
            }
        });

        Map<String, SanPhamEntity> chosen = new LinkedHashMap<String, SanPhamEntity>();
        Map<String, Integer> categories = new LinkedHashMap<String, Integer>();
        double total = 0;
        // First add at most one affordable product from each product type, then fill remaining budget.
        for (SanPhamEntity product : candidates) {
            String category = safe(product.getMaLH());
            if (!categories.containsKey(category) && total + product.getGiaBan() <= budget) {
                chosen.put(product.getMaSP(), product);
                categories.put(category, 1);
                total += product.getGiaBan();
            }
        }
        for (SanPhamEntity product : candidates) {
            if (chosen.containsKey(product.getMaSP())) continue;
            if (total + product.getGiaBan() <= budget) {
                chosen.put(product.getMaSP(), product);
                total += product.getGiaBan();
            }
        }

        List<SetupProduct> result = new ArrayList<SetupProduct>();
        double score = 0;
        for (SanPhamEntity product : chosen.values()) {
            double fit = product.getSl() > 0 ? 1.0 : 0.0;
            result.add(new SetupProduct(product.getMaSP(), product.getTenSP(), product.getMaLH(),
                    product.getGiaBan(), 1, fit, "Còn hàng, phù hợp ngân sách"));
            score += fit;
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Không có sản phẩm còn hàng phù hợp ngân sách.");
        return new SetupCombo(result, total, score, preferredCategory == null || preferredCategory.trim().isEmpty()
                ? "Tự chọn theo ngân sách" : preferredCategory);
    }

    public SetupCombo recalculate(List<SetupProduct> products, String type) {
        if (products == null || products.isEmpty()) throw new IllegalArgumentException("Combo chưa có sản phẩm.");
        double total = 0, score = 0;
        for (SetupProduct product : products) {
            if (product == null || product.getSoLuong() <= 0 || !Double.isFinite(product.getGiaBan()) || product.getGiaBan() < 0)
                throw new IllegalArgumentException("Dữ liệu sản phẩm trong combo không hợp lệ.");
            total += product.getGiaBan() * product.getSoLuong();
            score += product.getDiemPhuHop();
        }
        return new SetupCombo(products, total, score, type);
    }

    private String safe(String value) { return value == null ? "" : value; }
}
