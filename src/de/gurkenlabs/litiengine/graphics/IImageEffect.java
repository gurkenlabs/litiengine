package de.gurkenlabs.litiengine.graphics;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.ITimeToLive;

public interface IImageEffect extends ITimeToLive {
  public BufferedImage apply(BufferedImage image);

  public String getName();
}
