package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.gurkenlabs.litiengine.util.ImageProcessing;

public class RenderEngineTests {

  @Test
  public void testDrawText() {
    Graphics2D graphics = mock(Graphics2D.class);

    RenderEngine.drawText(graphics, "abc", 50.0, 100.0);
    verify(graphics).drawString("abc", 50f, 100f);
  }

  @Test
  public void testRenderImage() {
    Graphics2D graphics = mock(Graphics2D.class);

    final Image img = ImageProcessing.getCompatibleImage(5, 5);
    ImageRenderer.renderImage(graphics, img, new Point2D.Double(10, 20));

    ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
    ArgumentCaptor<AffineTransform> transCaptor = ArgumentCaptor.forClass(AffineTransform.class);
    verify(graphics).drawImage(captor.capture(), transCaptor.capture(), any());

    assertEquals(img, captor.getValue());
    assertEquals(10, transCaptor.getValue().getTranslateX());
    assertEquals(20, transCaptor.getValue().getTranslateY());
  }
}
