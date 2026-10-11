package com.test;

import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.service.SetupBuilderService;
import java.util.List;

/** Main doc lap de test khong can SSMS hay UI. */
public final class SetupBuilderTest {
    public static void main(String[] args) {
        // Gia la du lieu mau tu DAVISY; diem phu hop la gia lap cho Gaming.
        List<SetupProduct> products = List.of(
            new SetupProduct("SP0003", "Ban phim RAZER", "BP", 1_000_000, 20, 5, "Gaming"),
            new SetupProduct("SP0005", "Ban phim co G15", "BP", 400_000, 20, 4, "Gaming"),
            new SetupProduct("SP0006", "Ban phim G16", "BP", 550_000, 20, 4, "Gaming"),
            new SetupProduct("SP0011", "Tai nghe G3", "TN", 650_000, 20, 5, "Gaming"),
            new SetupProduct("SP0007", "Tai nghe P5P", "TN", 120_000, 20, 3, "Gaming"),
            new SetupProduct("SP0001", "Tai nghe AirPod Pro", "TN", 1_200_000, 0, 3, "Het hang"),
            new SetupProduct("SP0008", "Gia do bang nhom", "GD", 50_000, 20, 3, "Gaming"),
            new SetupProduct("SP0009", "Gia do bang nhua", "GD", 120_000, 20, 5, "Gaming")
        );
        SetupBuilderService service = new SetupBuilderService();
        long budget = 1_800_000;
        List<SetupCombo> combos = service.generateCombos(products, budget, List.of("BP", "TN", "GD"));
        if (combos.size() != 3) throw new AssertionError("Can co 3 combo rieng biet");
        for (SetupCombo combo : combos) {
            if (combo.getTongTien() > budget) throw new AssertionError("Vuot ngan sach");
            if (combo.getProducts().size() != 3) throw new AssertionError("Thieu nhom san pham");
            System.out.printf("%n%s: %,d VND, diem %d/15%n",
                    combo.getLoaiCombo(), combo.getTongTien(), combo.getTongDiem());
            for (SetupProduct p : combo.getProducts()) System.out.println("  - " + p);
        }
        if (combos.stream().map(SetupCombo::getSignature).distinct().count() != combos.size()) {
            throw new AssertionError("Co 2 combo trung nhau");
        }
        if (combos.stream().flatMap(c -> c.getProducts().stream())
                .anyMatch(p -> p.getSoLuong() == 0)) {
            throw new AssertionError("San pham het hang bi dua vao combo");
        }
        if (combos.get(0).getTongTien() != 570_000) {
            throw new AssertionError("Combo tiet kiem phai la 570000 VND");
        }
        if (!service.generateCombos(products, 300_000, List.of("BP", "TN", "GD")).isEmpty()) {
            throw new AssertionError("Ngan sach qua thap phai tra danh sach rong");
        }
        SetupCombo replaced = service.replaceProduct(combos.get(0), products.get(0), budget);
        if (replaced.getTongTien() <= combos.get(0).getTongTien()) {
            throw new AssertionError("Tong gia chua duoc tinh lai sau khi thay");
        }
        expectInvalid(() -> service.generateCombos(products, -1, List.of("BP", "TN", "GD")));
        expectInvalid(() -> service.replaceProduct(combos.get(0),
                new SetupProduct("SP9999", "Hang gia lap", "BP", 3_000_000, 1, 5, "Test"), budget));
        expectInvalid(() -> service.replaceProduct(combos.get(0),
                new SetupProduct("SP9998", "Hang het", "BP", 200_000, 0, 5, "Test"), budget));
        System.out.println("\nDA PASS: tao 3 combo, loc ngan sach, loc het hang, "
                + "khong trung combo, thay san pham, kiem tra loi.");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Phai bao loi IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Dat yeu cau: du lieu khong hop le duoc tu choi.
        }
    }
}
