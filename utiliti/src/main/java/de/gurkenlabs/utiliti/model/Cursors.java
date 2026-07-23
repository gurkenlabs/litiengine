package de.gurkenlabs.utiliti.model;

import de.gurkenlabs.litiengine.Game;
import java.awt.Component;
import java.awt.Cursor;

/**
 * Native cursor definitions used by the editor Canvas.
 */
public final class Cursors {
  public static final Cursor DEFAULT = Cursor.getDefaultCursor();
  public static final Cursor ADD = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  public static final Cursor MOVE = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  public static final Cursor LOAD = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  public static final Cursor TRANS_HORIZONTAL = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
  public static final Cursor TRANS_VERTICAL = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
  public static final Cursor TRANS_DIAGONAL_LEFT = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
  public static final Cursor TRANS_DIAGONAL_RIGHT = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);

  public static void initialize() {
    Game.window().cursor().setVisible(false);
    apply(DEFAULT);
  }

  public static void apply(Cursor cursor) {
    Component renderComponent = Game.window().getRenderComponent();
    Cursor resolved = cursor != null ? cursor : DEFAULT;
    if (!resolved.equals(renderComponent.getCursor())) {
      renderComponent.setCursor(resolved);
    }
  }

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private Cursors() {
  }
}
