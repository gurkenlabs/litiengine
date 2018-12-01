package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Point;

/**
 * The Interface ITile.
 */
public interface ITile extends ICustomPropertyProvider {

  /**
   * Gets the grid id.
   *
   * @return the grid id
   */
  public int getGridId();

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
}
