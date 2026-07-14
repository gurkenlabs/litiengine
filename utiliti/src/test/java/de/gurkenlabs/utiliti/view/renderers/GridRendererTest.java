package de.gurkenlabs.utiliti.view.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.Test;

class GridRendererTest {

  @Test
  void orthogonalMapDoesNotRequireHexSideLength() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);

    assertDoesNotThrow(() -> GridRenderer.GridCacheKey.from(map));
  }

  @Test
  void gridDetailIncludesMinorLinesAtMinimumProjectedSpacing() {
    assertEquals(GridRenderer.GridDetail.ALL, GridRenderer.gridDetail(4.0));
  }

  @Test
  void gridDetailIncludesOnlyMajorLinesWhenMinorLinesAreTooDense() {
    assertEquals(GridRenderer.GridDetail.MAJOR_ONLY, GridRenderer.gridDetail(3.999));
    assertEquals(GridRenderer.GridDetail.MAJOR_ONLY, GridRenderer.gridDetail(1.0));
  }

  @Test
  void gridDetailSkipsGridWhenMajorLinesAreTooDense() {
    assertEquals(GridRenderer.GridDetail.NONE, GridRenderer.gridDetail(0.999));
    assertEquals(GridRenderer.GridDetail.NONE, GridRenderer.gridDetail(0));
    assertEquals(GridRenderer.GridDetail.NONE, GridRenderer.gridDetail(Double.NaN));
  }

  @Test
  void orientedGridSkipsDisconnectedMajorOnlyOutlines() {
    assertTrue(GridRenderer.rendersOrientedGrid(GridRenderer.GridDetail.ALL));
    assertFalse(GridRenderer.rendersOrientedGrid(GridRenderer.GridDetail.MAJOR_ONLY));
    assertFalse(GridRenderer.rendersOrientedGrid(GridRenderer.GridDetail.NONE));
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
