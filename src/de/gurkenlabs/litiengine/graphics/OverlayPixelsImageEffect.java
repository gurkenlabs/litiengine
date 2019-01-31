package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.util.Imaging;

public class OverlayPixelsImageEffect extends ImageEffect {
  private final Color color;

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
