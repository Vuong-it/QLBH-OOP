package com.service;

import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.strategy.BalancedStrategy;
import com.strategy.BudgetStrategy;
import com.strategy.ComboStrategy;
import com.strategy.PerformanceStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;

/** Thuat toan ghep combo. Hoat dong doc lap voi SQL va Java Swing. */
public class SetupBuilderService {
    private static final long MAX_COMBINATIONS = 100_000;

    /** Tra ve 0..3 combo khac nhau theo thu tu: tiet kiem, can bang, trai nghiem. */
    public List<SetupCombo> generateCombos(List<SetupProduct> products,
                                          long budget, List<String> categories) {
        if (budget <= 0) throw new IllegalArgumentException("Ngan sach phai lon hon 0");
        if (products == null || categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Danh sach san pham va nhom khong duoc rong");
        }
        List<String> requested = categories.stream()
                .map(c -> c == null ? "" : c.trim().toUpperCase(Locale.ROOT)).toList();
        if (requested.stream().anyMatch(String::isEmpty)
                || new LinkedHashSet<>(requested).size() != requested.size()) {
            throw new IllegalArgumentException("Ma nhom khong hop le hoac bi trung");
        }

        Map<String, SetupProduct> unique = new TreeMap<>();
        for (SetupProduct product : products) {
            if (product == null) continue;
            SetupProduct old = unique.putIfAbsent(product.getMaSP(), product);
            if (old != null && (!old.getMaLoai().equals(product.getMaLoai())
                    || old.getGiaBan() != product.getGiaBan() || old.getSoLuong() != product.getSoLuong()
                    || old.getDiemPhuHop() != product.getDiemPhuHop())) {
                throw new IllegalArgumentException("Dữ liệu trùng mã không nhất quán: " + product.getMaSP());
            }
        }
        List<List<SetupProduct>> groups = new ArrayList<>();
        long combinations = 1;
        for (String category : requested) {
            List<SetupProduct> candidates = unique.values().stream()
                    .filter(p -> p != null && p.getSoLuong() > 0
                            && p.getGiaBan() <= budget && category.equals(p.getMaLoai()))
                    .toList();
            if (candidates.isEmpty()) return List.of();
            if (combinations > MAX_COMBINATIONS / candidates.size()) {
                throw new IllegalArgumentException("Quá nhiều tổ hợp. Hãy chọn ít nhóm hoặc lọc mục đích/phong cách.");
            }
            combinations *= candidates.size();
            groups.add(candidates);
        }

        List<SetupCombo> valid = new ArrayList<>();
        backtrack(groups, 0, 0L, budget, new ArrayList<>(), valid);
        if (valid.isEmpty()) return List.of();

        List<SetupCombo> results = new ArrayList<>();
        Set<String> selectedSignatures = new HashSet<>();
        pickBest("Tiết kiệm", new BudgetStrategy(), valid, budget, selectedSignatures, results);
        pickBest("Cân bằng", new BalancedStrategy(), valid, budget, selectedSignatures, results);
        pickBest("Trải nghiệm", new PerformanceStrategy(), valid, budget, selectedSignatures, results);
        return List.copyOf(results);
    }

    /** Cat nhanh khi tong gia vuot ngan sach. */
    private void backtrack(List<List<SetupProduct>> groups, int index, long total,
                           long budget, List<SetupProduct> selected, List<SetupCombo> output) {
        if (Thread.currentThread().isInterrupted()) throw new IllegalStateException("Đã hủy tạo combo.");
        if (index == groups.size()) {
            output.add(new SetupCombo("Tam", selected));
            return;
        }
        for (SetupProduct p : groups.get(index)) {
            if (p.getGiaBan() > budget - total) continue;
            selected.add(p);
            backtrack(groups, index + 1, total + p.getGiaBan(), budget, selected, output);
            selected.remove(selected.size() - 1);
        }
    }

    private void pickBest(String label, ComboStrategy strategy, List<SetupCombo> valid,
                          long budget, Set<String> used, List<SetupCombo> results) {
        Comparator<SetupCombo> ranking = (a, b) -> {
            // Preserve exact VND ordering even beyond double's integer precision.
            if (strategy instanceof BudgetStrategy && a.getTongTien() != b.getTongTien())
                return Long.compare(a.getTongTien(), b.getTongTien());
            int cmp = Double.compare(strategy.calculateScore(b, budget),
                                     strategy.calculateScore(a, budget));
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.getTongDiem(), a.getTongDiem());
            if (cmp != 0) return cmp;
            cmp = Long.compare(a.getTongTien(), b.getTongTien());
            return cmp != 0 ? cmp : a.getSignature().compareTo(b.getSignature());
        };
        valid.stream().filter(c -> !used.contains(c.getSignature()))
                .min(ranking).ifPresent(best -> {
                    results.add(best.withLoaiCombo(label));
                    used.add(best.getSignature());
                });
    }

    /** Thay san pham cung loai, tinh lai tong tien, khong sua combo goc. */
    public SetupCombo replaceProduct(SetupCombo combo, SetupProduct replacement, long budget) {
        if (combo == null || replacement == null || budget <= 0) {
            throw new IllegalArgumentException("Tham so khong hop le");
        }
        if (replacement.getSoLuong() <= 0) {
            throw new IllegalArgumentException("San pham thay the da het hang");
        }
        List<SetupProduct> changed = new ArrayList<>(combo.getProducts());
        int position = -1;
        for (int i = 0; i < changed.size(); i++) {
            if (changed.get(i).getMaLoai().equals(replacement.getMaLoai())) {
                position = i;
                break;
            }
        }
        if (position == -1) {
            throw new IllegalArgumentException("Combo khong co nhom " + replacement.getMaLoai());
        }
        changed.set(position, replacement);
        SetupCombo result = new SetupCombo(combo.getLoaiCombo(), changed);
        if (result.getTongTien() > budget) {
            throw new IllegalArgumentException("San pham thay the lam vuot ngan sach");
        }
        return result;
    }
}
