package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.util.image.ImageProcessing;

public class OverlayPixelsImageEffect extends ImageEffect {
  private final Color color;

  public OverlayPixelsImageEffect(final int ttl, final Color color) {
    super(ttl);
    this.color = color;
  }

  @Override
  public BufferedImage apply(final BufferedImage image) {
    final BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(image, 0, 0, null);
    bGr.drawImage(ImageProcessing.flashVisiblePixels(image, this.color), 0, 0, null);
    bGr.dispose();

    return bimage;
  }
}
