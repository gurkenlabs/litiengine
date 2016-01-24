package de.gurkenlabs.litiengine.graphics;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.core.ITimeToLive;

public interface IImageEffect extends ITimeToLive {
  public BufferedImage apply(BufferedImage image);
}
