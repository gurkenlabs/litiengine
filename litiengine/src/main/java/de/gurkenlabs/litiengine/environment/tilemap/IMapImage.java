package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;

public interface IMapImage extends ICustomPropertyProvider {

  URL getAbsoluteSourcePath(); // XXX merge with getSource

  /**
   * Gets the width.
   *
   * @return the width
   */
  int getWidth();

  /**
   * Gets the height.
   *
   * @return the height
   */
  int getHeight();

  /**
   * Gets the dimension.
   *
   * @return the dimension
   */
  Dimension getDimension();

  /**
   * Gets the source.
   *
   * @return the source
   */
  String getSource();

  /**
   * Gets the transparent color.
   *
   * @return the transparent color
   */
  Color getTransparentColor();

  /**
   * Tests for equality between two map images. Two map images are <i>equal</i> if they have the same absolute source path and the same transparent
   * color.
   *
   * @param anObject The map image to test for equality with
   * @return Whether this map image is equal to the provided map image, or {@code false} if {@code anObject} is not a map image
   */
  boolean equals(Object anObject);

  void setTransparentColor(Color color);

  void setSource(String source); // TODO change this to a URL

  void setAbsoluteSourcePath(URL absolutePath); // XXX merge with setSource

  void setWidth(int width);

  void setHeight(int height);
}
