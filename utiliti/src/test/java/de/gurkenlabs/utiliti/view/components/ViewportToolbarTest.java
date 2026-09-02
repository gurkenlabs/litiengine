package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.ProjectLaunchPhase;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.KeyBindings;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class ViewportToolbarTest {

  @Test
  void toolbarUsesSpacingWithoutAFullWidthOutline() {
    ViewportToolbar toolbar = new ViewportToolbar(new JComboBox<>());

    assertInstanceOf(EmptyBorder.class, toolbar.getBorder());
    assertEquals(0, toolbar.getInsets().left);
    assertEquals(0, toolbar.getInsets().right);
  }

  @Test
  void projectControlsExposeOnlyActionsValidForTheLaunchPhase() {
    ViewportToolbar toolbar = new ViewportToolbar(new JComboBox<>());
    AbstractButton run = findButton(toolbar, "Run Project (Shift+F10)");
    AbstractButton debug = findButton(toolbar, "Debug Project (Shift+F9)");
    AbstractButton stop = findButton(toolbar, "Stop Project ("
        + KeyBindings.format(KeyBindings.get(KeyBindings.Command.STOP_PROJECT)) + ")");
    ViewportToolbar.LaunchStatusIndicator status =
        findComponent(toolbar, ViewportToolbar.LaunchStatusIndicator.class);

    toolbar.updateRunState(true, false, ProjectLaunchPhase.BUILDING);
    assertFalse(run.isVisible());
    assertFalse(debug.isVisible());
    assertTrue(stop.isVisible());
    assertTrue(stop.isEnabled());
    assertTrue(status.isVisible());

    toolbar.updateRunState(true, false, ProjectLaunchPhase.STOPPING);
    assertFalse(stop.isEnabled());
    assertEquals("Stopping...", status.phaseText());

    toolbar.updateRunState(true, true, ProjectLaunchPhase.RUNNING);
    assertTrue(run.isVisible());
    assertTrue(debug.isVisible());
    assertFalse(run.isEnabled());
    assertFalse(debug.isEnabled());
    assertTrue(stop.isEnabled());
    assertFalse(status.isVisible());

    toolbar.updateRunState(true, false, ProjectLaunchPhase.IDLE);
    assertTrue(run.isEnabled());
    assertTrue(debug.isEnabled());
    assertFalse(stop.isEnabled());
  }

  @Test
  void launchStatusUsesCompactPhaseTextAndHidesWhenLaunchFinishes() {
    ViewportToolbar.LaunchStatusIndicator indicator =
        new ViewportToolbar.LaunchStatusIndicator();

    indicator.setPhase(ProjectLaunchPhase.BUILDING, true);

    assertTrue(indicator.isVisible());
    assertEquals("Building...", indicator.phaseText());
    assertEquals("Building...", indicator.getToolTipText());
    int buildingWidth = indicator.getPreferredSize().width;
    assertTrue(buildingWidth > 0);

    indicator.setPhase(ProjectLaunchPhase.ATTACHING_DEBUGGER, true);
    assertEquals("Attaching debugger...", indicator.phaseText());
    assertTrue(indicator.getPreferredSize().width > buildingWidth);

    indicator.setPhase(ProjectLaunchPhase.RUNNING, false);

    assertFalse(indicator.isVisible());
    assertFalse(indicator.isAnimationRunning());
  }

  @Test
  void largeHistoryPopupIsScrollableAndRetainsEveryEntry() {
    List<UndoManager.HistoryEntry> history = history(44);
    AtomicInteger appliedOperations = new AtomicInteger();

    JPopupMenu popup =
        ViewportToolbar.createHistoryPopup(history, true, appliedOperations::set);

    JScrollPane scroll = assertInstanceOf(JScrollPane.class, popup.getComponent(0));
    JList<?> list = assertInstanceOf(JList.class, scroll.getViewport().getView());
    assertEquals(44, list.getModel().getSize());
    assertEquals(ViewportToolbar.MAX_HISTORY_VISIBLE_ROWS, list.getVisibleRowCount());
    assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, scroll.getVerticalScrollBarPolicy());

    list.setSelectedIndex(24);
    list.getActionMap()
        .get("applyHistory")
        .actionPerformed(new ActionEvent(list, ActionEvent.ACTION_PERFORMED, "applyHistory"));
    assertEquals(25, appliedOperations.get());
  }

  @Test
  void shortHistoryPopupUsesOnlyTheRowsItNeeds() {
    JPopupMenu popup =
        ViewportToolbar.createHistoryPopup(history(3), false, ignored -> {});

    JScrollPane scroll = assertInstanceOf(JScrollPane.class, popup.getComponent(0));
    JList<?> list = assertInstanceOf(JList.class, scroll.getViewport().getView());
    assertEquals(3, list.getVisibleRowCount());
  }

  @Test
  void emptyHistoryPopupKeepsDisabledMessage() {
    JPopupMenu popup =
        ViewportToolbar.createHistoryPopup(List.of(), true, ignored -> {});

    JMenuItem message = assertInstanceOf(JMenuItem.class, popup.getComponent(0));
    assertFalse(message.isEnabled());
  }

  @Test
  void advancedScriptActionsLiveInTheOverflowMenu() {
    JComboBox<Object> mapSelector = new JComboBox<>();
    ViewportToolbar toolbar = new ViewportToolbar(mapSelector);

    toolbar.setScriptMode(true);

    assertTrue(mapSelector.getParent().isVisible());
    AbstractButton overflow = findButton(toolbar, "More script actions");
    assertTrue(overflow.isVisible());
    JPopupMenu menu = toolbar.createScriptActionsMenu();
    assertEquals("Format code", ((JMenuItem) menu.getComponent(0)).getText());
    assertEquals("Build", ((JMenuItem) menu.getComponent(1)).getText());
    assertEquals("Reload from disk", ((JMenuItem) menu.getComponent(2)).getText());
    assertEquals("Configure game scripts...", ((JMenuItem) menu.getComponent(4)).getText());
  }

  @Test
  void narrowToolbarCompactsLabelsWithoutOverlappingRightControls() {
    ViewportToolbar toolbar = new ViewportToolbar(new JComboBox<>());
    toolbar.setSize(900, toolbar.getPreferredSize().height);

    toolbar.doLayout();

    assertTrue(toolbar.isCompactLayout());
    assertTrue(toolbar.isOverflowLayout());
    Component left = toolbar.getComponent(0);
    Component right = toolbar.getComponent(1);
    assertTrue(left.getX() + left.getWidth() <= right.getX());
    AbstractButton cut = findButton(toolbar, "Cut ("
        + KeyBindings.format(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X,
            java.awt.event.InputEvent.CTRL_DOWN_MASK)) + ")");
    assertEquals(null, cut.getText());
    assertTrue(cut.getToolTipText().startsWith("Cut"));

    toolbar.setSize(2400, toolbar.getHeight());
    toolbar.doLayout();

    assertFalse(toolbar.isCompactLayout());
    assertFalse(toolbar.isOverflowLayout());
    assertEquals("Cut", cut.getText());
  }

  @Test
  void narrowMapToolbarKeepsSecondaryCommandsInOverflowMenu() {
    ViewportToolbar toolbar = new ViewportToolbar(new JComboBox<>());
    toolbar.setSize(640, toolbar.getPreferredSize().height);

    toolbar.doLayout();

    assertTrue(toolbar.isOverflowLayout());
    AbstractButton overflow = findButton(toolbar, "More map actions");
    assertTrue(overflow.isVisible());
    Component left = toolbar.getComponent(0);
    Component right = toolbar.getComponent(1);
    assertTrue(left.getX() + left.getWidth() <= right.getX());
    assertTrue(left.getPreferredSize().width <= left.getWidth());
    assertTrue(right.getPreferredSize().width <= right.getWidth());
    JPopupMenu menu = toolbar.createMapActionsMenu();
    assertEquals("Undo", ((JMenuItem) menu.getComponent(0)).getText());
    assertEquals("Undo history", ((JMenuItem) menu.getComponent(1)).getText());
    assertEquals("Redo", ((JMenuItem) menu.getComponent(2)).getText());
    assertEquals("Redo history", ((JMenuItem) menu.getComponent(3)).getText());
    assertEquals("Add", ((JMenuItem) menu.getComponent(5)).getText());
    assertEquals("Cut", ((JMenuItem) menu.getComponent(7)).getText());
    assertEquals("Grid", ((JMenuItem) menu.getComponent(12)).getText());
  }

  @Test
  void scriptModeDoesNotReserveHiddenMapControlsOrShowMapOverflow() {
    ViewportToolbar toolbar = new ViewportToolbar(new JComboBox<>());
    toolbar.setScriptMode(true);
    toolbar.setSize(700, toolbar.getPreferredSize().height);

    toolbar.doLayout();

    assertFalse(toolbar.isOverflowLayout());
    assertFalse(findButton(toolbar, Resources.strings().get("toolbar_moreMapActions")).getParent().isVisible());
  }

  private static List<UndoManager.HistoryEntry> history(int size) {
    List<UndoManager.HistoryEntry> history = new ArrayList<>(size);
    for (int index = 1; index <= size; index++) {
      history.add(new UndoManager.HistoryEntry("Change object " + index, 1));
    }
    return history;
  }

  private static AbstractButton findButton(Container root, String tooltip) {
    for (Component component : root.getComponents()) {
      if (component instanceof AbstractButton button
          && tooltip.equals(button.getToolTipText())) return button;
      if (component instanceof Container container) {
        AbstractButton found = findButtonOrNull(container, tooltip);
        if (found != null) return found;
      }
    }
    throw new AssertionError("Button not found: " + tooltip);
  }

  private static AbstractButton findButtonOrNull(Container root, String tooltip) {
    for (Component component : root.getComponents()) {
      if (component instanceof AbstractButton button
          && tooltip.equals(button.getToolTipText())) return button;
      if (component instanceof Container container) {
        AbstractButton found = findButtonOrNull(container, tooltip);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static <T extends Component> T findComponent(Container root, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) return type.cast(component);
      if (component instanceof Container container) {
        T found = findComponentOrNull(container, type);
        if (found != null) return found;
      }
    }
    throw new AssertionError("Component not found: " + type.getSimpleName());
  }

  private static <T extends Component> T findComponentOrNull(Container root, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) return type.cast(component);
      if (component instanceof Container container) {
        T found = findComponentOrNull(container, type);
        if (found != null) return found;
      }
    }
    return null;
  }
}
