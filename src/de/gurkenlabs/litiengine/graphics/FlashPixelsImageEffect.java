package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.core.IGameLoop;
import de.gurkenlabs.util.image.ImageProcessing;

public class FlashPixelsImageEffect extends ImageEffect {
  private final Color color;

  public FlashPixelsImageEffect(final IGameLoop gameLoop, final int ttl, final Color color) {
    super(gameLoop, ttl);
    this.color = color;
  }

  @Override
  public BufferedImage apply(final BufferedImage image) {
    return ImageProcessing.flashVisiblePixels(image, this.color);
  }
}
