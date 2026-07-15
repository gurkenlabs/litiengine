package de.gurkenlabs.utiliti.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Cursor;
import org.junit.jupiter.api.Test;

class CursorsTest {

  @Test
  void editorModesUseNativeCursorTypes() {
    assertEquals(Cursor.DEFAULT_CURSOR, Cursors.DEFAULT.getType());
    assertEquals(Cursor.CROSSHAIR_CURSOR, Cursors.ADD.getType());
    assertEquals(Cursor.MOVE_CURSOR, Cursors.MOVE.getType());
    assertEquals(Cursor.WAIT_CURSOR, Cursors.LOAD.getType());
    assertEquals(Cursor.E_RESIZE_CURSOR, Cursors.TRANS_HORIZONTAL.getType());
    assertEquals(Cursor.N_RESIZE_CURSOR, Cursors.TRANS_VERTICAL.getType());
    assertEquals(Cursor.NW_RESIZE_CURSOR, Cursors.TRANS_DIAGONAL_LEFT.getType());
    assertEquals(Cursor.NE_RESIZE_CURSOR, Cursors.TRANS_DIAGONAL_RIGHT.getType());
  }
}
