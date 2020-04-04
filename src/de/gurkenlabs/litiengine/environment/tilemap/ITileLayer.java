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

  /**
   * Gets the tile at the specified map grid location.
   * 
   * <p>
   * To retrieve map grid coordinates from a location on the map, use the <code>MapUtilities#getTile(Point2D)</code> method.
   * </p>
   * 
   * @param x
   *          The x-coordinate (on the map grid) to retrieve the tile.
   * @param y
   *          The y-coordinate (on the map grid) to retrieve the tile.
   * 
   * @return The tile at the specified grid location.
   * 
   * @see MapUtilities#getTile(Point2D)
   */
  ITile getTile(int x, int y);

  /**
   * Sets the id of the tile at the specified map grid location.
   * 
   * @param x
   *          The x-coordinate (on the map grid).
   * @param y
   *          The y-coordinate (on the map grid).
   * 
   * @param tile
   *          The tile that provides the tile id to be set on the tile at the specified location.
   * 
   * @see ITile#getGridId()
   */
  void setTile(int x, int y, ITile tile);

  /**
   * Sets the id of the tile at the specified map grid location.
   * 
   * @param x
   *          The x-coordinate (on the map grid).
   * @param y
   *          The y-coordinate (on the map grid).
   * 
   * @param gid
   *          The id to be set on the tile at the specified location.
   */
  void setTile(int x, int y, int gid);

  /**
   * Gets the tiles.
   *
   * @return the tiles
   */
  List<ITile> getTiles();
}
