package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.utiliti.controller.debug.ScriptDebugSnapshot;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebuggerBackend;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ScriptDebuggerPanelTest {
  @Test
  void snapshotSelectsPreferredScriptFrameAndFiltersFrameworkFrames() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      ScriptDebuggerPanel panel = new ScriptDebuggerPanel();
      ScriptDebugSnapshot.Frame engine = frame("de.gurkenlabs.litiengine.Game", "loop", List.of());
      ScriptDebugSnapshot.Frame script = frame("example.CreatureScript", "onLoaded",
          List.of(
              new ScriptDebugSnapshot.Variable("health", "int", "100"),
              new ScriptDebugSnapshot.Variable("globals", "ScriptGlobals", "ScriptGlobals #1", "globals-ref")));
      ScriptDebugSnapshot.Frame jdk = frame("java.util.ArrayList", "forEach", List.of());
      AtomicInteger selections = new AtomicInteger();
      AtomicReference<ScriptDebugSnapshot.Frame> selected = new AtomicReference<>();
      panel.onFrameSelected(frame -> {
        selections.incrementAndGet();
        selected.set(frame);
      });

      panel.showSnapshot(new ScriptDebugSnapshot("main", List.of(engine, script, jdk)), script);

      JList<?> stack = find(panel, JList.class);
      JTable variables = find(panel, JTable.class);
      assertEquals(1, stack.getModel().getSize());
      assertSame(script, stack.getSelectedValue());
      assertSame(script, selected.get());
      assertEquals(1, selections.get());
      assertEquals(2, variables.getRowCount());
      assertEquals("health", variables.getValueAt(0, 0).toString());

      AtomicReference<ScriptDebugSnapshot.Variable> expanded = new AtomicReference<>();
      panel.onExpandVariable(expanded::set);
      clickVariable(variables, 1);
      assertEquals("globals-ref", expanded.get().reference());
      panel.showVariableChildren("globals-ref",
          List.of(new ScriptDebugSnapshot.Variable("[score]", "int", "42")));
      assertEquals(3, variables.getRowCount());
      assertEquals("[score]", variables.getValueAt(2, 0).toString());
      clickVariable(variables, 1);
      assertEquals(2, variables.getRowCount());

      JToggleButton allFrames = (JToggleButton) findAccessibleButton(panel, "Show engine and JDK frames");
      allFrames.doClick();
      assertEquals(3, stack.getModel().getSize());
      assertSame(script, stack.getSelectedValue());
    });
  }

  @Test
  void controlsFollowDebuggerState() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      ScriptDebuggerPanel panel = new ScriptDebuggerPanel();
      AbstractButton resume = findAccessibleButton(panel, "Continue (F5)");
      AbstractButton pause = findAccessibleButton(panel, "Pause");
      AbstractButton stepOver = findAccessibleButton(panel, "Step Over (F10)");

      panel.updateState(ScriptDebuggerBackend.State.RUNNING, "Project is running");
      assertFalse(resume.isEnabled());
      assertTrue(pause.isEnabled());
      assertFalse(stepOver.isEnabled());

      panel.updateState(ScriptDebuggerBackend.State.PAUSED, "Paused at breakpoint");
      assertTrue(resume.isEnabled());
      assertFalse(pause.isEnabled());
      assertTrue(stepOver.isEnabled());
    });
  }

  private static ScriptDebugSnapshot.Frame frame(
      String className, String method, List<ScriptDebugSnapshot.Variable> variables) {
    return new ScriptDebugSnapshot.Frame(className, method, "Script.java", 12, variables);
  }

  private static void clickVariable(JTable table, int row) {
    Rectangle cell = table.getCellRect(row, 0, true);
    table.dispatchEvent(new MouseEvent(table, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0,
        cell.x + 4, cell.y + cell.height / 2, 1, false));
  }

  private static AbstractButton findAccessibleButton(Container root, String name) {
    for (Component component : root.getComponents()) {
      if (component instanceof AbstractButton button
          && name.equals(button.getAccessibleContext().getAccessibleName())) return button;
      if (component instanceof Container container) {
        AbstractButton found = findAccessibleButtonOrNull(container, name);
        if (found != null) return found;
      }
    }
    throw new AssertionError("Button not found: " + name);
  }

  private static AbstractButton findAccessibleButtonOrNull(Container root, String name) {
    for (Component component : root.getComponents()) {
      if (component instanceof AbstractButton button
          && name.equals(button.getAccessibleContext().getAccessibleName())) return button;
      if (component instanceof Container container) {
        AbstractButton found = findAccessibleButtonOrNull(container, name);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static <T extends Component> T findButton(Container root, String text, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component) && component instanceof AbstractButton button
          && text.equals(button.getText())) return type.cast(component);
      if (component instanceof Container container) {
        T found = findButtonOrNull(container, text, type);
        if (found != null) return found;
      }
    }
    throw new AssertionError("Button not found: " + text);
  }

  private static <T extends Component> T findButtonOrNull(Container root, String text, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component) && component instanceof AbstractButton button
          && text.equals(button.getText())) return type.cast(component);
      if (component instanceof Container container) {
        T found = findButtonOrNull(container, text, type);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static <T extends Component> T find(Container root, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) return type.cast(component);
      if (component instanceof Container container) {
        T found = findOrNull(container, type);
        if (found != null) return found;
      }
    }
    throw new AssertionError("Component not found: " + type.getSimpleName());
  }

  private static <T extends Component> T findOrNull(Container root, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) return type.cast(component);
      if (component instanceof Container container) {
        T found = findOrNull(container, type);
        if (found != null) return found;
      }
    }
    return null;
  }
}
