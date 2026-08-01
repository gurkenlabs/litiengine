package de.gurkenlabs.utiliti.view.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
  void soundRangeUsesCenteredBounds() {
    Rectangle2D baseBounds = new Rectangle2D.Double(20, 30, 10, 20);

    Rectangle2D range = MapObjectsRenderer.soundRangeBounds(baseBounds, 40);

    assertEquals(-15, range.getX());
    assertEquals(0, range.getY());
    assertEquals(80, range.getWidth());
    assertEquals(80, range.getHeight());
  }

  @Test
  void soundRangeStrokeIsClearlyVisible() {
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      MapObjectsRenderer.renderSoundRangeScreen(
        graphics, new Rectangle2D.Double(10, 10, 80, 80));
    } finally {
      graphics.dispose();
    }

    int maxAlpha = 0;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        maxAlpha = Math.max(maxAlpha, image.getRGB(x, y) >>> 24);
      }
    }
    assertTrue(maxAlpha >= 200);
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
