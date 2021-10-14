package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Point;
import java.awt.image.BufferedImage;

public interface ITile extends ICustomPropertyProvider {

  /**
   * Gets the grid id.
   *
   * @return the grid id
   */
  public int getGridId();

  public BufferedImage getImage();

  /**
   * Gets the tile coordinate.
   *
   * @return the tile coordinate
   */
  public Point getTileCoordinate();

  public ITilesetEntry getTilesetEntry();

  public boolean isFlippedHorizontally();

  public boolean isFlippedVertically();

  public boolean isFlippedDiagonally();
  
  public boolean isFlipped();

  /**
   * Tests for equality between two tiles. Two tiles are <i>equal</i>
   * if they have the same grid ID, flipped flags, and tileset entry.
   * @param anObject The tile to test equality for
   * @return Whether the provided tile is equal to this tile, or
   * {@code false} if {@code anObject} is not a tile
   */
  public boolean equals(Object anObject);

  /**
   * Computes a hash code for this tile. A tile's hash code is equal
   * to its stored grid ID, i.e. the gid bitmask, xor the tileset
   * entry's hash code.
   * @return The hash code for this tile
   */
  public int hashCode();
}
