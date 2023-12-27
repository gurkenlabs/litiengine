package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;

public interface IImageLayer extends ICustomPropertyProvider, ILayer {

  /**
   * Gets the image.
   *
   * @return the image
   */
  public IMapImage getImage();

  /**
   * Gets the transparent color.
   *
   * @return the transparent color
   */
  public Color getTransparentColor();
}
