package com.gui;

import com.dao.SetupCartDAO;
import com.dao.SetupFilterDAO;
import com.service.SetupBuilderFacade;
import com.service.SetupBuilderService;
import com.support.FakeSetupDatabase;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import com.entity.SetupProduct;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupBuilderPanelTest {
    @Test public void panelGeneratesSelectsAndCommitsOffEdtThenRefreshesOnEdt() throws Exception {
        FakeSetupDatabase db = new FakeSetupDatabase();
        db.products.add(FakeSetupDatabase.product("A", "BP", 100d, 10, "No metadata"));
        db.products.add(FakeSetupDatabase.product("B", "TN", 200d, 10, "No metadata"));
        AtomicInteger refreshes = new AtomicInteger();
        SetupBuilderPanel[] view = {null};
        SwingUtilities.invokeAndWait(() -> {
            view[0] = new SetupBuilderPanel(new SetupBuilderFacade(new SetupFilterDAO(db::open), new SetupBuilderService(),
                    new SetupCartDAO(db::open)), "staff", () -> {
                        assertTrue(SwingUtilities.isEventDispatchThread()); assertEquals(1, db.commits); refreshes.incrementAndGet();
                    });
            view[0].refreshOptions();
        });
        SetupBuilderPanel panel = view[0]; awaitIdle(panel);
        SwingUtilities.invokeAndWait(() -> {
            ((JList<?>) field(panel, "categoryList")).setSelectedIndices(new int[]{0,1});
            ((JTextField) field(panel, "budgetField")).setText("300");
            ((JComboBox<?>) field(panel, "customerBox")).setSelectedIndex(1);
            ((JButton) field(panel, "buildButton")).doClick();
        });
        awaitIdle(panel);
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(1, ((JTabbedPane) field(panel, "comboTabs")).getTabCount());
            try {
                panel.setSize(1200, 650); layout(panel);
                BufferedImage picture = new BufferedImage(1200, 650, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = picture.createGraphics(); panel.printAll(graphics); graphics.dispose();
                File output = new File("build-integration/setup-preview.png");
                output.getParentFile().mkdirs();
                ImageIO.write(picture, "png", output);
            } catch (Exception ex) { throw new AssertionError(ex); }
            ((JButton) field(panel, "addButton")).doClick();
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
        });
        awaitIdle(panel);
        assertEquals(1, db.commits); assertEquals(1, refreshes.get()); assertFalse(db.queriedOnEdt);
        assertEquals(Integer.valueOf(1), db.cartQuantities.get("A")); assertEquals(Integer.valueOf(1), db.cartQuantities.get("B"));
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(0, ((JTabbedPane) field(panel, "comboTabs")).getTabCount());
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
            ((JButton) field(panel, "buildButton")).doClick();
        });
        awaitIdle(panel);
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) field(panel, "budgetField")).setText("1");
            assertEquals(0, ((JTabbedPane) field(panel, "comboTabs")).getTabCount());
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
        });
    }

    @Test public void invalidBudgetAndMissingCategoriesDoNotQueryDatabase() throws Exception {
        FakeSetupDatabase db = database(); TestPanel panel = open(db);
        int queries = db.queries.size();
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) field(panel, "budgetField")).setText("0");
            ((JButton) field(panel, "buildButton")).doClick();
            assertNotNull(panel.error);
            panel.error = null;
            ((JTextField) field(panel, "budgetField")).setText("500");
            ((JButton) field(panel, "buildButton")).doClick();
            assertTrue(panel.error.contains("nhóm"));
        });
        assertEquals(queries, db.queries.size());
    }

    @Test public void tabsAndNumericPriceSortAndThemePreserveSelection() throws Exception {
        TestPanel panel = open(database()); generate(panel, "1000");
        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane tabs = (JTabbedPane) field(panel, "comboTabs");
            assertEquals(3, tabs.getTabCount());
            tabs.setSelectedIndex(2);
            Object combo = field(panel, "combo");
            JTable table = (JTable) field(panel, "table");
            assertEquals(Long.class, table.getColumnClass(3));
            table.getRowSorter().toggleSortOrder(3);
            assertTrue((Long) table.getValueAt(0, 3) <= (Long) table.getValueAt(1, 3));
            panel.setDarkMode(true); assertSame(combo, field(panel, "combo"));
            assertTrue(table.getForeground().getRed() > table.getBackground().getRed());
            panel.setDarkMode(false); assertSame(combo, field(panel, "combo"));
            assertTrue(((JLabel) field(panel, "summaryLabel")).getText().contains("Điểm TB"));
        });
    }

    @Test public void emptyResultDisablesActionsAndExplainsRecovery() throws Exception {
        TestPanel panel = open(database()); generate(panel, "1");
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(0, ((JTabbedPane) field(panel, "comboTabs")).getTabCount());
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
            assertFalse(((JButton) field(panel, "replaceButton")).isEnabled());
            assertTrue(((JLabel) field(panel, "statusLabel")).getText().contains("tăng ngân sách"));
        });
    }

    @Test public void replacementUsesSortedRowAndOnlyAffordableSameCategoryProducts() throws Exception {
        TestPanel panel = open(database()); generate(panel, "1200");
        SwingUtilities.invokeAndWait(() -> {
            JTable table = (JTable) field(panel, "table");
            table.getRowSorter().toggleSortOrder(3);
            table.getRowSorter().toggleSortOrder(3);
            table.setRowSelectionInterval(0, 0); // TN=200 precedes BP=100 after sorting.
            panel.choiceCode = "T3";
            ((JButton) field(panel, "replaceButton")).doClick();
            assertNull(panel.error);
            assertTrue(panel.choices.stream().allMatch(p -> p.getMaLoai().equals("TN") && p.getGiaBan() <= 1100));
            assertTrue(panel.choices.stream().noneMatch(p -> p.getMaSP().equals("TOO_EXPENSIVE")));
            assertTrue(((com.entity.SetupCombo) field(panel, "combo")).getProducts().stream().anyMatch(p -> p.getMaSP().equals("T3")));
        });
    }

    @Test public void cartFailureRollsBackAndDoesNotRefreshThenAllowsRetry() throws Exception {
        FakeSetupDatabase db = database(); TestPanel panel = open(db); generate(panel, "1000");
        db.failCode = "T1";
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
            ((JComboBox<?>) field(panel, "customerBox")).setSelectedIndex(1);
            ((JButton) field(panel, "addButton")).doClick();
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
        });
        awaitIdle(panel);
        assertEquals(0, db.commits); assertEquals(1, db.rollbacks); assertTrue(db.cartQuantities.isEmpty());
        assertEquals(0, panel.refreshes.get()); assertNotNull(panel.error); assertFalse(db.queriedOnEdt);
        db.failCode = null;
        SwingUtilities.invokeAndWait(() -> ((JButton) field(panel, "addButton")).doClick());
        awaitIdle(panel); assertEquals(1, db.commits); assertEquals(1, panel.refreshes.get());
    }

    @Test public void failedReloadInvalidatesOldRecommendationAndRestoresControls() throws Exception {
        FakeSetupDatabase db = database(); TestPanel panel = open(db); generate(panel, "1000");
        panel.failReads.set(true);
        SwingUtilities.invokeAndWait(panel::refreshOptions);
        awaitIdle(panel);
        assertNotNull(panel.error);
        SwingUtilities.invokeAndWait(() -> {
            assertNull(field(panel, "combo"));
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
            assertTrue(((JButton) field(panel, "loadButton")).isEnabled());
        });
    }

    private static FakeSetupDatabase database() {
        FakeSetupDatabase db = new FakeSetupDatabase();
        db.products.add(FakeSetupDatabase.product("B1", "BP", 100d, 10, ""));
        db.products.add(FakeSetupDatabase.product("B2", "BP", 300d, 10, ""));
        db.products.add(FakeSetupDatabase.product("T1", "TN", 200d, 10, ""));
        db.products.add(FakeSetupDatabase.product("T2", "TN", 400d, 10, ""));
        db.products.add(FakeSetupDatabase.product("T3", "TN", 800d, 10, ""));
        db.products.add(FakeSetupDatabase.product("TOO_EXPENSIVE", "TN", 1300d, 10, ""));
        return db;
    }
    private static TestPanel open(FakeSetupDatabase db) throws Exception {
        TestPanel[] panel = {null};
        SwingUtilities.invokeAndWait(() -> { panel[0] = new TestPanel(db); panel[0].refreshOptions(); });
        awaitIdle(panel[0]); return panel[0];
    }
    private static void generate(SetupBuilderPanel panel, String budget) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ((JList<?>) field(panel, "categoryList")).setSelectedIndices(new int[]{0, 1});
            ((JTextField) field(panel, "budgetField")).setText(budget);
            ((JButton) field(panel, "buildButton")).doClick();
        });
        awaitIdle(panel);
    }
    private static final class TestPanel extends SetupBuilderPanel {
        String error, choiceCode;
        List<SetupProduct> choices;
        final AtomicInteger refreshes;
        final java.util.concurrent.atomic.AtomicBoolean failReads;
        TestPanel(FakeSetupDatabase db) { this(db, new AtomicInteger(), new java.util.concurrent.atomic.AtomicBoolean()); }
        TestPanel(FakeSetupDatabase db, AtomicInteger refreshes, java.util.concurrent.atomic.AtomicBoolean failReads) {
            super(new SetupBuilderFacade(new SetupFilterDAO(() -> {
                if (failReads.get()) throw new java.sql.SQLException("Test connection unavailable");
                return db.open();
            }), new SetupBuilderService(), new SetupCartDAO(db::open)),
                    "staff", refreshes::incrementAndGet);
            this.refreshes = refreshes;
            this.failReads = failReads;
        }
        @Override protected void showError(String text) { error = text; }
        @Override protected SetupProduct chooseReplacement(List<SetupProduct> choices) {
            this.choices = choices;
            return choices.stream().filter(p -> p.getMaSP().equals(choiceCode)).findFirst().orElse(null);
        }
    }

    private static void awaitIdle(SetupBuilderPanel panel) throws Exception {
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < deadline) {
            boolean[] busy = {true}; SwingUtilities.invokeAndWait(() -> busy[0] = (Boolean) field(panel, "busy"));
            if (!busy[0]) return;
            Thread.sleep(20);
        }
        fail("SwingWorker did not complete");
    }
    private static Object field(Object target, String name) {
        try { Field field = SetupBuilderPanel.class.getDeclaredField(name); field.setAccessible(true); return field.get(target); }
        catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) if (child instanceof Container) layout((Container) child);
    }
}
