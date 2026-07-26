package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ImageRendererTests {

  @Test
  void renderResetsTheReusableTransform() {
    Graphics2D graphics = mock(Graphics2D.class);
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    ImageRenderer.renderScaled(graphics, image, 10, 20, 2, 3);
    ImageRenderer.render(graphics, image, 30, 40);

    ArgumentCaptor<AffineTransform> transform = ArgumentCaptor.forClass(AffineTransform.class);
    verify(graphics, times(2)).drawImage(any(BufferedImage.class), transform.capture(), any());

    AffineTransform finalTransform = transform.getAllValues().get(1);
    assertEquals(1, finalTransform.getScaleX());
    assertEquals(1, finalTransform.getScaleY());
    assertEquals(30, finalTransform.getTranslateX());
    assertEquals(40, finalTransform.getTranslateY());
  }
}
