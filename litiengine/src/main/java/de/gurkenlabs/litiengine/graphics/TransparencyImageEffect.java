package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.util.Imaging;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TransparencyImageEffect extends ImageEffect {
  private final float alpha;

  /**
   * Initializes a new instance of the {@code TransparencyImageEffect}.
   *
   * @param ttl The time to live of this effect.
   * @param alpha the constant alpha to be multiplied with the alpha of the source. alpha must be a floating point number in the inclusive range [0.0,
   *              1.0].
   */
  public TransparencyImageEffect(final int ttl, final float alpha) {
    super(ttl, "Transparency_" + alpha);
    this.alpha = alpha;
  }

  @Override
  public BufferedImage apply(final BufferedImage image) {
    final BufferedImage bimage =
      Imaging.getCompatibleImage(image.getWidth(null), image.getHeight(null));

    // Draw the image on to the buffered image
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(Imaging.setAlpha(image, this.alpha), 0, 0, null);
    bGr.dispose();

    return bimage;
  }
}
