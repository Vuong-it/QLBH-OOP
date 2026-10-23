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
            view[0] = new SetupBuilderPanel(new SetupBuilderFacade(new SetupFilterDAO(db::open), new SetupBuilderService()),
                    new SetupCartDAO(db::open), "staff", () -> {
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
            assertEquals(1, ((JComboBox<?>) field(panel, "comboBox")).getItemCount());
            try {
                panel.setSize(1200, 650); layout(panel);
                BufferedImage picture = new BufferedImage(1200, 650, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = picture.createGraphics(); panel.printAll(graphics); graphics.dispose();
                ImageIO.write(picture, "png", new File("build-integration/setup-preview.png"));
            } catch (Exception ex) { throw new AssertionError(ex); }
            ((JButton) field(panel, "addButton")).doClick();
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
        });
        awaitIdle(panel);
        assertEquals(1, db.commits); assertEquals(1, refreshes.get()); assertFalse(db.queriedOnEdt);
        assertEquals(Integer.valueOf(1), db.cartQuantities.get("A")); assertEquals(Integer.valueOf(1), db.cartQuantities.get("B"));
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(0, ((JComboBox<?>) field(panel, "comboBox")).getItemCount());
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
            ((JButton) field(panel, "buildButton")).doClick();
        });
        awaitIdle(panel);
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) field(panel, "budgetField")).setText("1");
            assertEquals(0, ((JComboBox<?>) field(panel, "comboBox")).getItemCount());
            assertFalse(((JButton) field(panel, "addButton")).isEnabled());
        });
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
        try { Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target); }
        catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) if (child instanceof Container) layout((Container) child);
    }
}
