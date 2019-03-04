package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

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

  /**
   * Tests for equality between two map images. Two map images
   * are <i>equal</i> if they have the same absolute source path
   * and the same transparent color.
   * @param anObject The map image to test for equality with
   * @return Whether this map image is equal to the provided map
   *  image, or {@code false} if {@code anObject} is not a map
   *  image
   */
  public boolean equals(Object anObject);

  /**
   * Computes a hash code for this map image. The hash code for
   * a map image is equal to the hash code of its absolute source
   * path xor the hash code of its transparent color.
   * @return The hash code for this map image
   */
  public int hashCode();

  public void setTransparentColor(Color color);

  public void setSource(String source);

  public void setAbsoluteSourcePath(String absolutePath);

  public void setWidth(int width);

  public void setHeight(int height);
}
