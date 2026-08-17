package de.gurkenlabs.utiliti.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class KeyBindingsTest {
  @Test
  void resolvesDefaultsAndOverrides() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.resolve(
        "SAVE_PROJECT=alt pressed S;DELETE=");

    assertEquals(Command.NEW_PROJECT.defaultKeyStroke(), bindings.get(Command.NEW_PROJECT));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK),
        bindings.get(Command.SAVE_PROJECT));
    assertNull(bindings.get(Command.DELETE));
  }

  @Test
  void serializesOnlyChangesFromDefaults() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.defaults();
    bindings.put(Command.SAVE_PROJECT, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
    bindings.put(Command.DELETE, null);

    String serialized = KeyBindings.serialize(bindings);

    assertTrue(serialized.contains("SAVE_PROJECT=alt pressed S"));
    assertTrue(serialized.contains("DELETE="));
    assertEquals(bindings, KeyBindings.resolve(serialized));
  }

  @Test
  void formatsShortcutsForDisplay() {
    assertEquals("Ctrl+S", KeyBindings.format(
        KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)));
    assertEquals("", KeyBindings.format(null));
  }

  @Test
  void includesInspectorNavigationDefaults() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.defaults();

    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK),
        bindings.get(Command.INSPECTOR_BACK));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK),
        bindings.get(Command.INSPECTOR_FORWARD));
  }

  @Test
  void invalidSerializedShortcutKeepsDefault() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.resolve("SAVE_PROJECT=not a keystroke");

    assertEquals(Command.SAVE_PROJECT.defaultKeyStroke(), bindings.get(Command.SAVE_PROJECT));
  }

  @Test
  void includesQuickSearchDefault() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.defaults();

    assertEquals(Command.QUICK_SEARCH.defaultKeyStroke(), bindings.get(Command.QUICK_SEARCH));
  }

  @Test
  void includesProjectLifecycleDefaults() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.defaults();

    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK),
        bindings.get(Command.RUN_PROJECT));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.SHIFT_DOWN_MASK),
        bindings.get(Command.DEBUG_PROJECT));
    assertEquals(Command.STOP_PROJECT.defaultKeyStroke(), bindings.get(Command.STOP_PROJECT));
  }

  @Test
  void usesCommandInsteadOfControlForMacDefaults() {
    assertEquals(
        InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
        KeyBindings.platformModifiers(
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, "Mac OS X"));
    assertEquals(
        InputEvent.CTRL_DOWN_MASK,
        KeyBindings.platformModifiers(InputEvent.CTRL_DOWN_MASK, "Linux"));
  }

  @Test
  void includesWorkspaceSwitchingDefaults() {
    EnumMap<Command, KeyStroke> bindings = KeyBindings.defaults();

    int ctrlOrCmd = KeyBindings.platformModifiers(InputEvent.CTRL_DOWN_MASK, System.getProperty("os.name", ""));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ctrlOrCmd),
        bindings.get(Command.SWITCH_WORKSPACE_MODE));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_M, ctrlOrCmd | InputEvent.SHIFT_DOWN_MASK),
        bindings.get(Command.SWITCH_MAP_MODE));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrlOrCmd | InputEvent.SHIFT_DOWN_MASK),
        bindings.get(Command.SWITCH_SCRIPT_MODE));
  }
}
