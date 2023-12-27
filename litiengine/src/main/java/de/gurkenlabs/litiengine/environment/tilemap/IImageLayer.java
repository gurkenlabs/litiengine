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
  Color getTransparentColor();

  /**
   * Gets a value indicating whether the image should be rendered repeatedly horizontally.
   *
   * @return True if the image should be repeated horizontally; Otherwise false.
   */
  boolean repeatHorizontally();


  /**
   * Gets a value indicating whether the image should be rendered repeatedly vertically.
   *
   * @return True if the image should be repeated vertically; Otherwise false.
   */
  boolean repeatVertically();
}
