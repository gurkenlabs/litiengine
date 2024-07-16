package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.util.Imaging;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RenderEngineTests {

  @Test
  public void testDrawText() {
    Graphics2D graphics = mock(Graphics2D.class);

    TextRenderer.render(graphics, "abc", 50.0, 100.0);
    verify(graphics).drawString("abc", 50f, 100f);
  }

  @Test
  public void testDrawTextWithLinebreaks() {
    Graphics2D graphics = mock(Graphics2D.class);

    when(graphics.getFontRenderContext())
      .thenReturn(new FontRenderContext(new AffineTransform(), true, true));
    ArgumentCaptor<GlyphVector> glyphVectorArgumentCaptor = ArgumentCaptor.forClass(GlyphVector.class);
    ArgumentCaptor<Float> yPointArgumentCaptor = ArgumentCaptor.forClass(Float.class);
    doNothing().when(graphics).drawGlyphVector(glyphVectorArgumentCaptor.capture(), anyFloat(), yPointArgumentCaptor.capture());

    List<String> text = List.of("a", "bc", "def");
    TextRenderer.renderWithLinebreaks(
      graphics,
      String.join(lineSeparator(), text),
      Align.LEFT, Valign.TOP,
      10.0, 20.0, 100.0, 200.0,
      true);

    verify(graphics, times(text.size())).drawGlyphVector(any(), eq(10f), anyFloat());

    List<GlyphVector> vectors = glyphVectorArgumentCaptor.getAllValues();
    List<Float> yPoints = yPointArgumentCaptor.getAllValues();

    assertEquals(vectors.size(), text.size());
    assertEquals(yPoints.size(), text.size());
    for (int i = 0; i < text.size(); i++) {
      assertEquals(vectors.get(i).getNumGlyphs(), i + 1);

      if (i > 0) {
        assertTrue(yPoints.get(i) > yPoints.get(i - 1));
      }
    }
  }

  @Test
  public void testRenderImage() {
    Graphics2D graphics = mock(Graphics2D.class);

    final Image img = Imaging.getCompatibleImage(5, 5);
    ImageRenderer.render(graphics, img, new Point2D.Double(10, 20));

    ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
    ArgumentCaptor<AffineTransform> transCaptor = ArgumentCaptor.forClass(AffineTransform.class);
    verify(graphics).drawImage(captor.capture(), transCaptor.capture(), any());

    assertEquals(img, captor.getValue());
    assertEquals(10, transCaptor.getValue().getTranslateX());
    assertEquals(20, transCaptor.getValue().getTranslateY());
  }
}
