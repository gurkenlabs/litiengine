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

    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
        bindings.get(Command.NEW_PROJECT));
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
}
