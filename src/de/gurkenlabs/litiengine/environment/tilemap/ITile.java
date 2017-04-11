/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Point;

// TODO: Auto-generated Javadoc
/**
 * The Interface ITile.
 */
public interface ITile extends ICustomPropertyProvider {

  public int getId();
  
  public ITerrain[] getTerrain();
  
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
  
  public ITileAnimation getAnimation();
}
