package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class TileCollisionEditorPanelTest {

  @Test
  void rectangleCreationNormalizesAndClampsBounds() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 12);

    editor.createRectangleForTest(20, 10, 4, -2);

    IMapObject shape = entry.getCollisionInfo().getMapObjects().getFirst();
    assertEquals(4, shape.getX());
    assertEquals(0, shape.getY());
    assertEquals(12, shape.getWidth());
    assertEquals(10, shape.getHeight());
    assertEquals(1, shape.getId());
  }

  @Test
  void rectangleSnapstoWholePixels() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 16);

    editor.createRectangleForTest(1.3f, 2.7f, 9.6f, 11.4f);

    IMapObject shape = entry.getCollisionInfo().getMapObjects().getFirst();
    assertEquals(1, shape.getX());
    assertEquals(3, shape.getY());
    assertEquals(8, shape.getWidth());
    assertEquals(9, shape.getHeight());
  }

  @Test
  void zeroAreaRectangleIsIgnored() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 16);

    editor.createRectangleForTest(4, 4, 4, 12);

    assertNull(entry.getCollisionInfo());
  }

  @Test
  void selectionUsesTopmostShapeAndDeletingLastShapeClearsLayer() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 16);
    editor.createRectangleForTest(0, 0, 12, 12);
    editor.createRectangleForTest(4, 4, 8, 8);

    editor.selectShapeForTest(6, 6);
    editor.deleteSelectedShapeForTest();

    assertEquals(1, entry.getCollisionInfo().getMapObjects().size());
    assertEquals(12, entry.getCollisionInfo().getMapObjects().getFirst().getWidth());
    editor.selectShapeForTest(6, 6);
    editor.deleteSelectedShapeForTest();
    assertNull(entry.getCollisionInfo());
  }

  @Test
  void mutationsAreCommittedThroughTheProvidedHandler() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = new TileCollisionEditorPanel();
    boolean[] committed = {false};
    editor.bind(entry, image(16, 16), 16, 16, change -> {
      committed[0] = true;
      change.run();
    }, () -> {});

    editor.createRectangleForTest(1, 2, 3, 4);

    assertTrue(committed[0]);
    assertNotNull(entry.getCollisionInfo());
    assertFalse(entry.getCollisionInfo().getMapObjects().isEmpty());
  }

  @Test
  void dragMoveSnapsToPixelGrid() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 16);
    editor.createRectangleForTest(2, 2, 8, 8);
    editor.selectShapeForTest(5, 5);

    editor.dragShapeForTest(3.7f, 2.3f);

    IMapObject shape = entry.getCollisionInfo().getMapObjects().getFirst();
    assertEquals(6, shape.getX());
    assertEquals(4, shape.getY());
    assertEquals(6, shape.getWidth());
    assertEquals(6, shape.getHeight());
  }

  @Test
  void dragMoveClampsToTileBounds() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 16);
    editor.createRectangleForTest(10, 10, 16, 16);
    editor.selectShapeForTest(13, 13);

    editor.dragShapeForTest(20, 20);

    IMapObject shape = entry.getCollisionInfo().getMapObjects().getFirst();
    assertEquals(10, shape.getX());
    assertEquals(10, shape.getY());
    assertEquals(6, shape.getWidth());
    assertEquals(6, shape.getHeight());
  }

  @Test
  void dragMoveNegativeDirection() {
    TilesetEntry entry = new TilesetEntry();
    TileCollisionEditorPanel editor = editor(entry, 16, 16);
    editor.createRectangleForTest(8, 8, 14, 14);
    editor.selectShapeForTest(11, 11);

    editor.dragShapeForTest(-4, -3);

    IMapObject shape = entry.getCollisionInfo().getMapObjects().getFirst();
    assertEquals(4, shape.getX());
    assertEquals(5, shape.getY());
    assertEquals(6, shape.getWidth());
    assertEquals(6, shape.getHeight());
  }

  private static TileCollisionEditorPanel editor(TilesetEntry entry, int width, int height) {
    TileCollisionEditorPanel editor = new TileCollisionEditorPanel();
    editor.bind(entry, image(width, height), width, height, Runnable::run, () -> {});
    return editor;
  }

  private static BufferedImage image(int width, int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }
}
