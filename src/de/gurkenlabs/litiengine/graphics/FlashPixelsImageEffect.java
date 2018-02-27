package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.util.ImageProcessing;

public class FlashPixelsImageEffect extends ImageEffect {
  private final Color color;

  public FlashPixelsImageEffect(final int ttl, final Color color) {
    super(ttl, "FlashPixelsImageEffect");
    this.color = color;
  }

  @Override
  public BufferedImage apply(final BufferedImage image) {
    return ImageProcessing.flashVisiblePixels(image, this.color);
  }
}
