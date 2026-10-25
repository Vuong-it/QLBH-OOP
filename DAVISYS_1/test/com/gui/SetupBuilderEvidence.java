package com.gui;

import com.dao.SetupCartDAO;
import com.dao.SetupFilterDAO;
import com.entity.SetupCombo;
import com.entity.SetupProduct;
import com.service.SetupBuilderFacade;
import com.service.SetupBuilderService;
import com.support.FakeSetupDatabase;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Reproducible offscreen UI evidence using the demo catalog and in-memory JDBC, never SQL Server. */
public final class SetupBuilderEvidence {
    private static Path output;
    private static boolean sqlDemo;
    private static class EvidencePanel extends SetupBuilderPanel {
        EvidencePanel(SetupBuilderFacade facade) { super(facade, sqlDemo ? "demo_admin" : "staff", null); }
        @Override protected void showError(String text) { throw new AssertionError(text); }
        @Override protected SetupProduct chooseReplacement(List<SetupProduct> choices) {
            JOptionPane dialog = new JOptionPane("Chọn phụ kiện cùng loại, trong ngân sách:", JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
            dialog.setWantsInput(true); dialog.setSelectionValues(choices.toArray()); dialog.setInitialSelectionValue(choices.get(0));
            snapshot(dialog, "03-doi-phu-kien.png", 850, 220);
            @SuppressWarnings("unchecked") List<SetupCombo> combos = (List<SetupCombo>) field(this, "combos");
            SetupCombo selected = (SetupCombo) field(this, "combo");
            SetupBuilderService algorithm = new SetupBuilderService();
            long budget = (Long) field(this, "currentBudget");
            return choices.stream().filter(p -> {
                String signature = algorithm.replaceProduct(selected, p, budget).getSignature();
                return combos.stream().noneMatch(c -> c.getSignature().equals(signature));
            }).findFirst().orElseThrow(() -> new AssertionError("No distinct replacement for demo"));
        }
    }
    public static void main(String[] args) throws Exception {
        output = Paths.get(args[0]); Files.createDirectories(output);
        sqlDemo = args.length > 1 && args[1].equals("--sql-demo");
        if (sqlDemo && !System.getProperty("davisy.jdbc.url", "").contains("databaseName=DAVISYS_DEMO_SB60;"))
            throw new IllegalArgumentException("Evidence writes are restricted to DAVISYS_DEMO_SB60");
        FakeSetupDatabase db = new FakeSetupDatabase();
        for (String line : Files.readAllLines(Paths.get("../database/demo/products.tsv"), StandardCharsets.UTF_8).subList(1, 61)) {
            String[] c = line.split("\t", -1);
            if (Integer.parseInt(c[4]) <= 0) continue; // Match the real SELECT stock predicate.
            Map<String,Object> row = FakeSetupDatabase.product(c[0], c[2], Double.valueOf(c[6]), Integer.valueOf(c[4]), c[9]);
            row.put("TENSP", c[1]); db.products.add(row);
        }
        EvidencePanel[] panels = {null};
        SwingUtilities.invokeAndWait(() -> {
            for (Object key : UIManager.getDefaults().keySet().toArray()) {
                if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)
                    UIManager.put(key, new javax.swing.plaf.FontUIResource("Arial", Font.PLAIN, 14));
            }
            panels[0] = new EvidencePanel(sqlDemo ? new SetupBuilderFacade() : new SetupBuilderFacade(new SetupFilterDAO(db::open), new SetupBuilderService(), new SetupCartDAO(db::open)));
            panels[0].refreshOptions();
        });
        EvidencePanel panel = panels[0]; idle(panel);
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) field(panel, "budgetField")).setText("1500000");
            ((JComboBox<?>) field(panel, "purposeBox")).setSelectedItem("Học tập");
            ((JComboBox<?>) field(panel, "styleBox")).setSelectedItem("Tối giản");
            JList<?> groups = (JList<?>) field(panel, "categoryList");
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < groups.getModel().getSize(); i++)
                if (Set.of("BP", "CH", "TN").contains(groups.getModel().getElementAt(i))) indices.add(i);
            groups.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
            ((JComboBox<?>) field(panel, "customerBox")).setSelectedIndex(1);
            snapshot(panel, "01-tieu-chi.png", 1360, 760);
            ((JButton) field(panel, "buildButton")).doClick();
        });
        idle(panel);
        SwingUtilities.invokeAndWait(() -> {
            snapshot(panel, "02-ket-qua.png", 1360, 760);
            System.out.println("Hoc tap: " + ((JLabel) field(panel, "summaryLabel")).getText());
            panel.setDarkMode(true); snapshot(panel, "05-dark-mode.png", 1360, 760); panel.setDarkMode(false);
            JTable table = (JTable) field(panel, "table"); table.setRowSelectionInterval(0, 0);
            ((JButton) field(panel, "replaceButton")).doClick();
            snapshot(panel, "04-sau-khi-doi.png", 1360, 760);
            System.out.println("Sau doi: " + ((JLabel) field(panel, "summaryLabel")).getText());
            if (args.length < 3 || !args[2].equals("--read-only")) ((JButton) field(panel, "addButton")).doClick();
        });
        idle(panel);
        if (args.length < 3 || !args[2].equals("--read-only")) SwingUtilities.invokeAndWait(() -> snapshot(panel, "06-them-gio.png", 1360, 760));
        if (!sqlDemo) System.out.println("Fixture committed quantities: " + new TreeMap<>(db.cartQuantities));
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) field(panel, "budgetField")).setText("3000000");
            ((JComboBox<?>) field(panel, "purposeBox")).setSelectedItem("Gaming");
            ((JComboBox<?>) field(panel, "styleBox")).setSelectedItem("Hiện đại");
            ((JButton) field(panel, "buildButton")).doClick();
        });
        idle(panel);
        SwingUtilities.invokeAndWait(() -> {
            System.out.println("Gaming: " + ((JLabel) field(panel, "summaryLabel")).getText());
            snapshot(panel, "07-gaming.png", 1360, 760);
        });
    }
    private static void snapshot(JComponent component, String name, int width, int height) {
        try {
            JPanel root = new JPanel(new BorderLayout()); root.add(component);
            JLabel disclosure = new JLabel(sqlDemo ? "Giao diện Swing • SQL Server thật • DAVISYS_DEMO_SB60 • Tài khoản và khách hàng demo"
                    : "Ảnh giao diện kiểm thử • Catalog SB60 • JDBC giả lập • Chưa xác nhận trên SQL Server thật", SwingConstants.CENTER);
            disclosure.setOpaque(true); disclosure.setBackground(new Color(255, 245, 210)); disclosure.setForeground(Color.BLACK);
            disclosure.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); root.add(disclosure, BorderLayout.SOUTH);
            root.setSize(width, height); layout(root);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics(); root.printAll(graphics); graphics.dispose();
            ImageIO.write(image, "png", output.resolve(name).toFile());
        } catch (Exception e) { throw new AssertionError(e); }
    }
    private static void layout(Container c) { c.doLayout(); for (Component child : c.getComponents()) if (child instanceof Container) layout((Container) child); }
    private static Object field(Object panel, String name) {
        try { Field f = SetupBuilderPanel.class.getDeclaredField(name); f.setAccessible(true); return f.get(panel); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    private static void idle(SetupBuilderPanel panel) throws Exception {
        for (int i = 0; i < 500; i++) {
            boolean[] busy = {true}; SwingUtilities.invokeAndWait(() -> busy[0] = (Boolean) field(panel, "busy"));
            if (!busy[0]) return; Thread.sleep(20);
        }
        throw new AssertionError("UI worker timeout");
    }
}
