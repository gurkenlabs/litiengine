/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface ITileLayer.
 */
public interface ITileLayer extends ILayer {

  /**
   * Gets the tile by loctaion.
   *
   * @param location
   *          the location
   * @return the tile by loctaion
   */
  public ITile getTileByLoctaion(Point2D location);
  
  public ITile getTile(int x, int y);

  /**
   * Gets the tiles.
   *
   * @return the tiles
   */
  public List<ITile> getTiles();
}
