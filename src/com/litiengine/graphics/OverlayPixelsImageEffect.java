package com.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.litiengine.util.Imaging;

public class OverlayPixelsImageEffect extends ImageEffect {
  private final Color color;

  /**
   * Initializes a new instance of the {@code OverlayPixelsImageEffect}.
   *
   * @param ttl
   *          The time to live of this effect.
   * @param color
   *          The color of this effect.
   */
  public OverlayPixelsImageEffect(final int ttl, final Color color) {
    super(ttl, "OverlayPixels" + color.getRed() + "" + color.getGreen() + "" + color.getBlue());
    this.color = color;
  }

  @Override
  public BufferedImage apply(final BufferedImage image) {
    final BufferedImage bimage = Imaging.getCompatibleImage(image.getWidth(null), image.getHeight(null));

    // Draw the image on to the buffered image
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(image, 0, 0, null);
    bGr.drawImage(Imaging.flashVisiblePixels(image, this.color), 0, 0, null);
    bGr.dispose();

    return bimage;
  }

  public Color getColor() {
    return this.color;
  }
}
