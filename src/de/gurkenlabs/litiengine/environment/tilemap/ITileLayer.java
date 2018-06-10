package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * The Interface ITileLayer.
 */
public interface ITileLayer extends ILayer {

  @Deprecated
  default ITile getTileByLoctaion(Point2D location) {
    return getTileByLocation(location);
  }
  
  /**
   * Gets the tile by loctaion.
   *
   * @param location
   *          the location
   * @return the tile by loctaion
   */
  ITile getTileByLocation(Point2D location);

  ITile getTile(int x, int y);

  /**
   * Gets the tiles.
   *
   * @return the tiles
   */
  List<ITile> getTiles();
}
