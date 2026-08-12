package de.gurkenlabs.utiliti.model;

import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/** Central registry for configurable application menu shortcuts. */
public final class KeyBindings {
  private static final String ACTION_PROPERTY = "utiliti.keyBindingAction";

  public enum Command {
    NEW_PROJECT("menu_file_new", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
    OPEN_PROJECT("menu_file_open", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
    SAVE_PROJECT("menu_file_save", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
    EXIT("menu_exit", KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK),
    UNDO("menu_edit_undo", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
    REDO("menu_edit_redo", KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
    CUT("menu_edit_cut", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK),
    COPY("menu_edit_copy", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
    PASTE("menu_edit_paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
    DELETE("menu_edit_delete", KeyEvent.VK_DELETE, 0),
    SELECT_ALL("menu_edit_selectAll", KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
    DESELECT("menu_edit_deselect", KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
    QUICK_SEARCH("menu_edit_quickSearch", KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK),
    SHOW_GRID("menu_view_showGrid", KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK),
    SHOW_COLLISION("menu_view_showCollisionBoxes", KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK),
    SHOW_CUSTOM_OBJECTS("menu_view_showCustomMapObjects", KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK),
    SHOW_MAP_IDS("menu_view_showMapIds", KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK),
    ZOOM_IN("menu_view_zoomIn", KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK),
    ZOOM_OUT("menu_view_zoomOut", KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),
    CENTER_FOCUS("menu_view_center", KeyEvent.VK_SPACE, 0),
    CENTER_MAP("menu_view_centermap", KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK),
    MAP_NEW("menu_map_new", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
    INSPECTOR_BACK("keymap_inspector_back", KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK),
    INSPECTOR_FORWARD("keymap_inspector_forward", KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK),
    ADD_PROP("menu_add_prop", KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK),
    ADD_CREATURE("menu_add_creature", KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK),
    ADD_COLLISION("menu_add_collisionbox", KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK),
    ADD_TRIGGER("menu_add_trigger", KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK),
    ADD_SPAWNPOINT("menu_add_spawnpoint", KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK),
    ADD_AREA("menu_add_area", KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK),
    ADD_LIGHT("menu_add_light", KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK),
    ADD_SHADOW("menu_add_shadow", KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK),
    ADD_EMITTER("menu_add_emitter", KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK),
    ADD_SOUND("menu_add_soundsource", KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK),
    MAP_SNAPSHOT("menu_map_snapshot", KeyEvent.VK_PRINTSCREEN, InputEvent.SHIFT_DOWN_MASK),
    EXPORT_SPRITES("menu_export_spriteSheets", KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK),
    SCRIPT_SAVE("menu_script_save", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
    SCRIPT_FORMAT("menu_script_format", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
    SCRIPT_COMPILE("menu_script_compile", KeyEvent.VK_F9, InputEvent.CTRL_DOWN_MASK),
    SCRIPT_RELOAD("menu_script_reload", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
    SCRIPT_OPEN_IDE("menu_script_openIde", KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
    RUN_PROJECT("menu_run_project", KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK),
    DEBUG_PROJECT("menu_debug_project", KeyEvent.VK_F9, InputEvent.SHIFT_DOWN_MASK),
    STOP_PROJECT("menu_stop_project", KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK);

    private final String resourceKey;
    private final KeyStroke defaultKeyStroke;

    Command(String resourceKey, int keyCode, int modifiers) {
      this.resourceKey = resourceKey;
      this.defaultKeyStroke = KeyStroke.getKeyStroke(
          keyCode, platformModifiers(modifiers, System.getProperty("os.name", "")));
    }

    public enum CommandGroup {
      MAP,
      SCRIPT
    }

    public CommandGroup group() {
      if (this.name().startsWith("SCRIPT_")) {
        return CommandGroup.SCRIPT;
      }
      return CommandGroup.MAP;
    }

    public String resourceKey() {
      return this.resourceKey;
    }

    public KeyStroke defaultKeyStroke() {
      return this.defaultKeyStroke;
    }
  }

  private KeyBindings() {
  }

  static int platformModifiers(int modifiers, String osName) {
    if (osName == null || !osName.toLowerCase(java.util.Locale.ROOT).contains("mac")
        || (modifiers & InputEvent.CTRL_DOWN_MASK) == 0) {
      return modifiers;
    }
    return modifiers & ~InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;
  }

  public static void bind(JMenuItem menuItem, Command command) {
    menuItem.putClientProperty(ACTION_PROPERTY, command);
    menuItem.setAccelerator(get(command));
  }

  public static KeyStroke get(Command command) {
    return snapshot().get(command);
  }

  public static Map<Command, KeyStroke> snapshot() {
    return resolve(Editor.preferences().getKeyBindings());
  }

  static EnumMap<Command, KeyStroke> resolve(String serialized) {
    EnumMap<Command, KeyStroke> bindings = defaults();
    if (serialized == null || serialized.isBlank()) {
      return bindings;
    }

    for (String entry : serialized.split(";")) {
      int separator = entry.indexOf('=');
      if (separator < 1) {
        continue;
      }
      try {
        Command command = Command.valueOf(entry.substring(0, separator));
        String value = entry.substring(separator + 1);
        if (value.isEmpty()) {
          bindings.put(command, null);
        } else {
          KeyStroke keyStroke = KeyStroke.getKeyStroke(value);
          if (keyStroke != null) {
            bindings.put(command, keyStroke);
          }
        }
      } catch (IllegalArgumentException ignored) {
        // Ignore entries from commands that no longer exist.
      }
    }
    return bindings;
  }

  public static void save(Map<Command, KeyStroke> bindings) {
    Editor.preferences().setKeyBindings(serialize(bindings));
  }

  static String serialize(Map<Command, KeyStroke> bindings) {
    StringBuilder serialized = new StringBuilder();
    for (Command command : Command.values()) {
      KeyStroke keyStroke = bindings.get(command);
      if (java.util.Objects.equals(keyStroke, command.defaultKeyStroke())) {
        continue;
      }
      if (!serialized.isEmpty()) {
        serialized.append(';');
      }
      serialized.append(command.name()).append('=');
      if (keyStroke != null) {
        serialized.append(keyStroke);
      }
    }
    return serialized.toString();
  }

  public static void refresh(Component component) {
    if (component instanceof JMenuItem item
        && item.getClientProperty(ACTION_PROPERTY) instanceof Command command) {
      item.setAccelerator(get(command));
    }
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        refresh(child);
      }
    }
  }

  public static EnumMap<Command, KeyStroke> defaults() {
    EnumMap<Command, KeyStroke> bindings = new EnumMap<>(Command.class);
    for (Command command : Command.values()) {
      bindings.put(command, command.defaultKeyStroke());
    }
    return bindings;
  }

  public static String format(KeyStroke keyStroke) {
    if (keyStroke == null) {
      return "";
    }
    String modifiers = InputEvent.getModifiersExText(keyStroke.getModifiers());
    String key = keyStroke.getKeyCode() != 0
        ? KeyEvent.getKeyText(keyStroke.getKeyCode())
        : Character.toString(keyStroke.getKeyChar()).toUpperCase(java.util.Locale.ROOT);
    return modifiers.isEmpty() ? key : modifiers + "+" + key;
  }
}
