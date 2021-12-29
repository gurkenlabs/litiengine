package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface IMapOrientation {
  /**
   * Gets the name of this {@code IMapOrientation}.
   * 
   * @return The name of this orientation
   */
  public String getName();

  /**
   * Determines the size required for all tiles within the given map to be drawn into an image. Tiles larger than the
   * map's tile size may not fit within this size.
   *
   * @param map
   *          The {@code IMap} to measure
   * @return The required image size
   */
  public Dimension getSize(IMap map);

  /**
   * Determines the rendered location of a tile within the given {@code IMap}, given the saved coordinates of the tile.
   * The coordinates of the point returned are those of the bottom-left corner of the tile's image relative to the
   * top-left corner of the rectangle in which tiles in the given map are drawn. The point returned by this method will
   * not necessarily be contained inside the shape returned by {@code getShape}.
   *
   * @param x
   *          The saved X coordinate of the tile
   * @param y
   *          The saved Y coordinate of the tile
   * @param map
   *          The {@code IMap} that the tile is in
   * @return The location of a tile for the given map and coordinates
   */
  public Point getLocation(int x, int y, IMap map);

  /**
   * Determines the rendered location of a tile within the given {@code IMap}, given the saved coordinates of the tile.
   * The coordinates of the point returned are those of the bottom-left corner of the tile's image relative to the
   * top-left corner of the rectangle in which tiles in the given map are drawn. The point returned by this method will
   * not necessarily be contained inside the shape returned by {@code getShape}.
   *
   * @param tile
   *          The saved location of the tile
   * @param map
   *          The {@code IMap} that the tile is in
   * @return The location of a tile for the given map and coordinates
   */
  public Point getLocation(Point tile, IMap map);

  /**
   * Creates a {@code Shape} for the tile at the given coordinates. The shapes returned by this method should reflect the
   * intended shape of a tile in this orientation, and in general should not overlap.
   *
   * @param x
   *          The X coordinate of the tile
   * @param y
   *          The Y coordinate of the tile
   * @param map
   *          The {@code IMap} that the tile is in
   * @return The shape of the tile
   */
  public Shape getShape(int x, int y, IMap map);

  /**
   * Creates a {@code Shape} for the tile at the given coordinates. The shapes returned by this method should reflect the
   * intended shape of a tile in this orientation, and in general should not overlap.
   *
   * @param tile
   *          The location of the tile
   * @param map
   *          The {@code IMap} that the tile is in
   * @return The shape of the tile
   */
  public Shape getShape(Point tile, IMap map);

  /**
   * Determines the bounding box for the tile at the given coordinates. A call to this method is equivalent to calling
   * {@code orientation.getShape(x, y, map).getBounds2D()}.
   *
   * @param x
   *          The X coordinate of the tile
   * @param y
   *          The Y coordinate of the tile
   * @param map
   *          The {@code IMap} that the tile is in
   * @return The bounding box of the tile
   */
  public Rectangle2D getBounds(int x, int y, IMap map);

  /**
   * Determines the bounding box for the tile at the given coordinates. A call to this method is equivalent to calling
   * {@code orientation.getShape(tile, map).getBounds2D()}.
   *
   * @param tile
   *          The location of the tile
   * @param map
   *          The {@code IMap} that the tile is in
   * @return The bounding box of the tile
   */
  public Rectangle2D getBounds(Point tile, IMap map);

  /**
   * Returns the shape of the tile containing the given coordinates. A call to this method is equivalent to calling
   * {@code orientation.getShape(orientation.getTile(x, y, map), map)}.
   *
   * @param x
   *          The X coordinate to contain
   * @param y
   *          The Y coordinate to contain
   * @param map
   *          The {@code IMap} containing the tile
   * @return The shape of the tile containing the given point
   */
  public Shape getEnclosingTileShape(double x, double y, IMap map);

  /**
   * Returns the shape of the tile containing the given coordinates. A call to this method is equivalent to calling
   * {@code orientation.getShape(orientation.getTile(location, map), map)}.
   *
   * @param location
   *          The point to contain
   * @param map
   *          The {@code IMap} containing the tile
   * @return The shape of the tile containing the given point
   */
  public Shape getEnclosingTileShape(Point2D location, IMap map);

  /**
   * Returns the bounding box of the tile containing the given coordinates. A call to this method is equivalent to calling
   * {@code orientation.getShape(orientation.getTile(x, y, map), map).getBounds2D()}.
   *
   * @param x
   *          The X coordinate to contain
   * @param y
   *          The Y coordinate to contain
   * @param map
   *          The {@code IMap} containing the tile
   * @return The bounding box of the tile containing the given point
   */
  public Rectangle2D getEnclosingTileBounds(double x, double y, IMap map);

  /**
   * Returns the bounding box of the tile containing the given coordinates. A call to this method is equivalent to calling
   * {@code orientation.getShape(orientation.getTile(location, map), map).getBounds2D()}.
   *
   * @param location
   *          The point to contain
   * @param map
   *          The {@code IMap} containing the tile
   * @return The bounding box of the tile containing the given point
   */
  public Rectangle2D getEnclosingTileBounds(Point2D location, IMap map);

  /**
   * Determines the coordinates of the tile containing the given point, as determined by
   * {@link IMapOrientation#getShape(int, int, IMap)}.
   *
   * @param x
   *          The X coordinate to contain
   * @param y
   *          The Y coordinate to contain
   * @param map
   *          The {@code IMap} containing the tile
   * @return The tile coordinates of the tile containing the point.
   * @throws ArithmeticException
   *           if the tiles are packed too tightly to resolve
   */
  public Point getTile(double x, double y, IMap map);

  /**
   * Determines the coordinates of the tile containing the given point, as determined by
   * {@link IMapOrientation#getShape(int, int, IMap)}.
   *
   * @param location
   *          The point to contain
   * @param map
   *          The {@code IMap} containing the tile
   * @return The tile coordinates of the tile containing the point.
   * @throws ArithmeticException
   *           if the tiles are packed too tightly to resolve
   */
  public Point getTile(Point2D location, IMap map);
}
