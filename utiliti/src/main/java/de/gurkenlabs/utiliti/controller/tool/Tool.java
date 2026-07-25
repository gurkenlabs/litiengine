package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import java.awt.Cursor;
import javax.swing.Icon;

public interface Tool {
  String getName();

  Icon getIcon();

  default Cursor getCursor() {
    return Cursor.getDefaultCursor();
  }

  default void activated() {
  }

  default void deactivated() {
  }

  default void mousePressed(ComponentMouseEvent event) {
  }

  default void mouseReleased(ComponentMouseEvent event) {
  }

  default void mouseDragged(ComponentMouseEvent event) {
  }

  default void mouseMoved(ComponentMouseEvent event) {
  }

  default boolean showInToolbar() {
    return true;
  }
}
