package de.gurkenlabs.utiliti.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

class TilesetRenderingMathTest {

  @Test
  void testViewportClippingCalculation() {
    int cell = 32;
    int columns = 10;
    int totalTiles = 100; // 10 rows
    int totalRows = (int) Math.ceil(totalTiles / (double) columns);

    // Visible clip region covering tiles from x: 32..128, y: 64..160
    Rectangle clip = new Rectangle(32, 64, 96, 96);

    int minCol = Math.max(0, clip.x / cell);
    int maxCol = Math.min(columns - 1, (clip.x + clip.width) / cell);
    int minRow = Math.max(0, clip.y / cell);
    int maxRow = Math.min(totalRows - 1, (clip.y + clip.height) / cell);

    assertEquals(1, minCol);
    assertEquals(4, maxCol);
    assertEquals(2, minRow);
    assertEquals(5, maxRow);
  }

  @Test
  void testGridVisibilityThreshold() {
    // Cell size >= 16 shows grid lines and detailed overlays
    assertTrue(shouldShowGrid(32));
    assertTrue(shouldShowGrid(16));

    // Cell size < 16 suppresses per-cell grid lines for whole-image zoom rendering
    assertFalse(shouldShowGrid(12));
    assertFalse(shouldShowGrid(8));
  }

  private static boolean shouldShowGrid(int cellSize) {
    return cellSize >= 16;
  }
}
