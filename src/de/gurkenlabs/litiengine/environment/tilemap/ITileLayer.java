package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;
import java.util.List;

public interface ITileLayer extends ILayer {

  /**
   * Gets the tile by location.
   *
   * @param location
   *          the location
   * @return the tile by location
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
