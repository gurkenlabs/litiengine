/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;

// TODO: Auto-generated Javadoc
/**
 * The Interface IImageLayer.
 */
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
