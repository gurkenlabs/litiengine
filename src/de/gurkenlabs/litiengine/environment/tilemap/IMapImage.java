/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMapImage.
 */
public interface IMapImage extends ICustomPropertyProvider {

  public String getAbsoluteSourcePath();

  /**
   * Gets the dimension.
   *
   * @return the dimension
   */
  public Dimension getDimension();

  /**
   * Gets the source.
   *
   * @return the source
   */
  public String getSource();

  /**
   * Gets the source file.
   *
   * @return the source file
   */
  public File getSourceFile();

  /**
   * Gets the transparent color.
   *
   * @return the transparent color
   */
  public Color getTransparentColor();
}
