package de.gurkenlabs.utiliti.view.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;

import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.Test;

class GridRendererTest {

  @Test
  void orthogonalMapDoesNotRequireHexSideLength() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);

    assertDoesNotThrow(() -> GridRenderer.GridCacheKey.from(map, java.awt.Color.WHITE, 1f));
  }

  @Test
  void visibleRangeIncludesOnlyViewportTilesAndOneTileExpansion() {
    GridRenderer.TileRange range =
        GridRenderer.calculateVisibleTileRange(
            new Rectangle2D.Double(32, 16, 32, 16), 100, 50, 16, 8);

    assertEquals(new GridRenderer.TileRange(1, 1, 4, 4), range);
  }

  @Test
  void visibleRangeDoesNotIncludeTileStartingAtExclusiveViewportEdge() {
    GridRenderer.TileRange range =
        GridRenderer.calculateVisibleTileRange(
            new Rectangle2D.Double(32, 24, 16, 8), 100, 50, 16, 8);

    assertEquals(new GridRenderer.TileRange(1, 2, 3, 4), range);
  }

  @Test
  void visibleRangeIsClampedToMapEdges() {
    GridRenderer.TileRange range =
        GridRenderer.calculateVisibleTileRange(
            new Rectangle2D.Double(-20, -20, 30, 30), 10, 10, 16, 8);

    assertEquals(new GridRenderer.TileRange(0, 0, 1, 2), range);
  }

  @Test
  void visibleRangeIsEmptyOutsideMap() {
    GridRenderer.TileRange range =
        GridRenderer.calculateVisibleTileRange(
            new Rectangle2D.Double(200, 100, 20, 20), 10, 10, 16, 8);

    assertTrue(range.isEmpty());
  }

  @Test
  void visibleRangeIsEmptyForInvalidDimensions() {
    GridRenderer.TileRange range =
        GridRenderer.calculateVisibleTileRange(
            new Rectangle2D.Double(0, 0, 20, 20), 10, 10, 0, 8);

    assertTrue(range.isEmpty());
  }
}
