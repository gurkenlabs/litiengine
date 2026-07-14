package de.gurkenlabs.utiliti.view.renderers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.Test;

class MapObjectsRendererTest {

  private static final Rectangle2D VIEWPORT = new Rectangle2D.Double(0, 0, 100, 100);

  @Test
  void offscreenBaseBoundsAreCulled() {
    Rectangle2D baseBounds = new Rectangle2D.Double(120, 20, 10, 10);

    assertFalse(MapObjectsRenderer.isVisibleInViewport(VIEWPORT, baseBounds, null, 0));
  }

  @Test
  void soundRangeCanMakeOffscreenObjectVisible() {
    Rectangle2D baseBounds = new Rectangle2D.Double(120, 20, 10, 10);

    assertTrue(MapObjectsRenderer.isVisibleInViewport(VIEWPORT, baseBounds, null, 30));
  }

  @Test
  void collisionExtentCanMakeOffscreenObjectVisible() {
    Rectangle2D baseBounds = new Rectangle2D.Double(120, 20, 10, 10);
    Rectangle2D collisionBounds = new Rectangle2D.Double(90, 20, 40, 10);

    assertTrue(MapObjectsRenderer.isVisibleInViewport(VIEWPORT, baseBounds, collisionBounds, 0));
  }

  @Test
  void zeroSizeObjectInsideViewportIsVisible() {
    Rectangle2D pointBounds = new Rectangle2D.Double(50, 50, 0, 0);

    assertTrue(MapObjectsRenderer.isVisibleInViewport(VIEWPORT, pointBounds, null, 0));
  }
}
