package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.Test;

class ColorLayerTests {

  @Test
  void updateSectionExpandsFractionalBoundsToWholePixels() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getBounds()).thenReturn(new Rectangle2D.Double(0, 0, 100, 100));
    Environment environment = mock(Environment.class);
    when(environment.getMap()).thenReturn(map);
    TestColorLayer layer = new TestColorLayer(environment);

    layer.updateSection(new Rectangle2D.Double(10.25, 20.75, 5.1, 7.1));

    Rectangle expected = new Rectangle(10, 20, 6, 8);
    assertEquals(expected, layer.clearedSection);
    assertEquals(expected, layer.renderedSection);
  }

  private static class TestColorLayer extends ColorLayer {
    private Rectangle2D clearedSection;
    private Rectangle2D renderedSection;

    private TestColorLayer(Environment environment) {
      super(environment, Color.BLACK);
    }

    @Override
    protected void renderSection(Graphics2D g, Rectangle2D section) {
      renderedSection = section;
    }

    @Override
    protected void clearSection(Graphics2D g, Rectangle2D section) {
      clearedSection = section;
    }
  }
}
