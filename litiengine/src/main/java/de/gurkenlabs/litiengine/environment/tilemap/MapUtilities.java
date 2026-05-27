package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.MathUtilities;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Static utility methods for working with TMX-style maps, map objects, tiles and tile coordinates. Most overloads default to the map of the currently
 * loaded environment.
 */
public final class MapUtilities {

  private MapUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Computes the combined axis-aligned bounding box of the supplied map objects.
   *
   * @param objects the map objects whose bounding boxes are merged
   * @return the union bounding box
   */
  public static Rectangle2D getBounds(IMapObject... objects) {
    double x = Double.MAX_VALUE;
    double y = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;
    for (IMapObject object : objects) {
      final Rectangle2D bounds = object.getBoundingBox();
      x = Math.min(bounds.getX(), x);
      y = Math.min(bounds.getY(), y);
      maxX = Math.max(bounds.getX() + bounds.getWidth(), maxX);
      maxY = Math.max(bounds.getY() + bounds.getHeight(), maxY);
    }

    return new Rectangle2D.Double(x, y, maxX - x, maxY - y);
  }

  /**
   * Returns the maximum {@link IMapObject#getId() id} of any map object on the given map. Returns {@code 0} if the map is {@code null} or contains no
   * map object layers.
   *
   * @param map the map to inspect
   * @return the maximum map object id
   */
  public static int getMaxMapId(final IMap map) {
    int maxId = 0;
    if (map == null || map.getMapObjectLayers() == null) {
      return maxId;
    }

    for (IMapObjectLayer objectLayer : map.getMapObjectLayers()) {
      if (objectLayer == null || objectLayer.getMapObjects() == null) {
        continue;
      }

      for (IMapObject mapObject : objectLayer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        if (mapObject.getId() > maxId) {
          maxId = mapObject.getId();
        }
      }
    }

    return maxId;
  }

  /**
   * Snaps the given pixel-space rectangle to the bounding box of the tiles it covers on the given map.
   *
   * @param map the map providing tile size and orientation
   * @param box the pixel-space rectangle to snap
   * @return the bounding box that covers all enclosed tiles
   */
  public static Rectangle2D getTileBoundingBox(final IMap map, final Rectangle2D box) {
    final int minX = (int) Math.clamp(box.getX(), 0, map.getSizeInPixels().getWidth() - 1);
    final int minY = (int) Math.clamp(box.getY(), 0, map.getSizeInPixels().getHeight() - 1);
    final int maxX = (int) Math.clamp(box.getMaxX(), 0, map.getSizeInPixels().getWidth() - 1);
    final int maxY = (int) Math.clamp(box.getMaxY(), 0, map.getSizeInPixels().getHeight() - 1);
    final Point minTilePoint = map.getOrientation().getTile(minX, minY, map);
    final Point maxTilePoint = map.getOrientation().getTile(maxX, maxY, map);
    int minTileX = map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
      ? minTilePoint.x
      : Math.clamp(minTilePoint.x, 0, map.getWidth() - 1);
    int minTileY = map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
      ? minTilePoint.y
      : Math.clamp(minTilePoint.y, 0, map.getHeight() - 1);
    int maxTileX = map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
      ? maxTilePoint.x
      : Math.clamp(maxTilePoint.x, 0, map.getWidth() - 1);
    int maxTileY = map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
      ? maxTilePoint.y
      : Math.clamp(maxTilePoint.y, 0, map.getWidth() - 1);
    final Rectangle2D minTileBounds = map.getOrientation().getBounds(
      new Point(Math.clamp(minTileX, 0, map.getWidth() - 1),
        Math.clamp(minTileY, 0, map.getHeight() - 1)), map);
    final Rectangle2D maxTileBounds = map.getOrientation().getBounds(
      new Point(Math.clamp(maxTileX, 0, map.getWidth() - 1),
        Math.clamp(maxTileY, 0, map.getHeight() - 1)), map);

    return new Rectangle2D.Double(
      minTileBounds.getX(), minTileBounds.getY(),
      maxTileBounds.getMaxX() - minTileBounds.getX(),
      maxTileBounds.getMaxY() - minTileBounds.getY());
  }

  /**
   * Get the corresponding tile for a given pixel map location. This is an overload taking the Map from the current environment to calculate a tile
   * location.
   *
   * @param mapLocation the pixel map location.
   * @return The x / y tile coordinate for the given location.
   * @see MapUtilities#getTile(IMap, Point2D)
   */
  public static Point getTile(final Point2D mapLocation) {
    if (Game.world().environment() == null) {
      return new Point(-1, -1);
    }
    return getTile(Game.world().environment().getMap(), mapLocation);
  }

  /**
   * Get the corresponding tile for a given pixel map location.
   *
   * @param map         The map on which to calculate the tile location.
   * @param mapLocation the pixel map location.
   * @return The x / y tile coordinate for the given mapLocation.
   */
  public static Point getTile(IMap map, final Point2D mapLocation) {
    if (map == null) {
      return new Point(-1, -1);
    }
    return map.getOrientation().getTile(mapLocation, map);
  }

  /**
   * Check if the row or column with the given index is staggered.
   *
   * @param staggerIndex the staggerIndex property of the map. Every second row (or column, depending on the {@link StaggerAxis} of the map is
   *                     staggered half a tile.
   * @param index        the index of the current row or column for which we want to determine if it's staggered or not.
   * @return a boolean representing if the row or column with the given index is staggered.
   */
  public static boolean isStaggeredRowOrColumn(StaggerIndex staggerIndex, int index) {
    return (staggerIndex == StaggerIndex.ODD && MathUtilities.isOddNumber(index))
      || (staggerIndex == StaggerIndex.EVEN && !MathUtilities.isOddNumber(index));
  }

  /**
   * Converts a tile coordinate to its pixel-space top-left location on the given map.
   *
   * @param map          the map providing tile size
   * @param tileLocation the tile coordinate
   * @return the pixel-space location of the tile's top-left corner
   */
  public static Point2D getMapLocation(final IMap map, final Point tileLocation) {
    return new Point2D.Double(tileLocation.x * map.getTileSize().getWidth(),
      tileLocation.y * map.getTileSize().getHeight());
  }

  /**
   * Returns all tiles from any tile layer on the given map that contain the supplied pixel-space location.
   *
   * @param map      the map to inspect
   * @param location the pixel-space location
   * @return the matching tiles, ordered by layer (bottom to top)
   */
  public static List<ITile> getTilesByPixelLocation(final IMap map, final Point2D location) {
    final List<ITile> tilesAtLocation = new ArrayList<>();
    if (map.getTileLayers() == null || map.getTileLayers().isEmpty()) {
      return tilesAtLocation;
    }

    final Point tileLocation = map.getOrientation().getTile(location, map);
    for (final ITileLayer layer : map.getTileLayers()) {
      final ITile tile = layer.getTile(tileLocation.x, tileLocation.y);
      if (tile != null) {
        tilesAtLocation.add(tile);
      }
    }

    return tilesAtLocation;
  }

  /**
   * Returns the top-most non-empty tile at the given pixel-space location on the current environment's map.
   *
   * @param location the pixel-space location
   * @return the top-most tile, or {@code null} if no environment or tile is found
   */
  public static ITile getTopMostTile(final Point2D location) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.world().environment().getMap(), location);
  }

  /**
   * Returns the top-most non-empty tile at the given pixel-space location on the supplied map.
   *
   * @param map      the map to inspect
   * @param location the pixel-space location
   * @return the top-most tile, or {@code null} if no tile is found
   */
  public static ITile getTopMostTile(final IMap map, final Point2D location) {
    if (map.getTileLayers() == null || map.getTileLayers().isEmpty()) {
      return null;
    }

    return getTopMostTile(map.getOrientation().getTile(location, map));
  }

  /**
   * Returns the top-most non-empty tile at the given tile coordinate on the current environment's map.
   *
   * @param point the tile coordinate
   * @return the top-most tile, or {@code null} if no environment or tile is found
   */
  public static ITile getTopMostTile(final Point point) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.world().environment().getMap(), point);
  }

  /**
   * Returns the top-most non-empty tile at the given tile coordinate on the supplied map.
   *
   * @param map   the map to inspect
   * @param point the tile coordinate
   * @return the top-most tile, or {@code null} if no tile is found
   */
  public static ITile getTopMostTile(final IMap map, final Point point) {
    final Point tileLocation = point;

    ITile tile = null;

    for (int i = map.getTileLayers().size() - 1; i >= 0; i--) {
      ITile tileOfLayer = map.getTileLayers().get(i).getTile(tileLocation.x, tileLocation.y);
      if (tileOfLayer != null && tileOfLayer.getGridId() != 0) {
        tile = tileOfLayer;
        break;
      }
    }

    return tile;
  }

  /**
   * Searches for the tile set that contains the specified tile, identified by the grid id.
   *
   * @param map  the map
   * @param tile the tile
   * @return the tileset
   */
  public static ITileset findTileSet(final IMap map, final ITile tile) {
    if (map == null || tile == null) {
      return null;
    }

    ITileset match = null;

    for (final ITileset tileset : map.getTilesets()) {
      if (tileset.containsTile(tile)) {
        match = tileset;
        break;
      }
    }

    return match;
  }

  /**
   * Converts a polyline or polygon map object to a {@link Path2D} in absolute (map-space) coordinates.
   *
   * @param mapObject the polyline or polygon map object
   * @return the resulting path, or {@code null} if the object is not a polyline/polygon or contains no points
   */
  public static Path2D convertPolyshapeToPath(final IMapObject mapObject) {
    if (mapObject == null || (!mapObject.isPolygon() && !mapObject.isPolyline())) {
      return null;
    }

    List<Point2D> points = mapObject.getPolyline() != null ? mapObject.getPolyline().getPoints()
      : mapObject.getPolygon().getPoints();
    if (points.isEmpty()) {
      return null;
    }

    Path2D path = new Path2D.Float();
    path.moveTo(mapObject.getLocation().getX(), mapObject.getLocation().getY());
    for (int i = 1; i < points.size(); i++) {
      Point2D point = points.get(i);
      path.lineTo(mapObject.getLocation().getX() + point.getX(),
        mapObject.getLocation().getY() + point.getY());
    }

    if (mapObject.isPolygon()) {
      path.closePath();
    }

    return path;
  }

  /**
   * Returns the points of a polyline or polygon map object translated to absolute (map-space) coordinates.
   *
   * @param mapObject the polyline or polygon map object
   * @return the absolute points, or an empty list if the object is not a polyline/polygon
   */
  public static List<Point2D> getAbsolutePolyshapePoints(final IMapObject mapObject) {
    if (mapObject.isPolygon()) {
      return mapObject.getPolygon().getAbsolutePoints(mapObject.getLocation());
    }

    if (mapObject.isPolyline()) {
      return mapObject.getPolyline().getAbsolutePoints(mapObject.getLocation());
    }

    return new ArrayList<>();
  }

  /**
   * Searches the supplied map for a map object with the given id.
   *
   * @param map the map to search
   * @param id  the id to look up
   * @return the matching map object, or {@code null} if none exists
   */
  public static IMapObject findMapObject(final IMap map, final int id) {
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      for (IMapObject obj : layer.getMapObjects()) {
        if (obj.getId() == id) {
          return obj;
        }
      }
    }

    return null;
  }

  /**
   * Returns the bounding box of the tile that contains the given pixel-space location on the current environment's map.
   *
   * @param mapLocation the pixel-space location
   * @return the enclosing tile's bounding box
   */
  public static Rectangle2D getTileBoundingBox(final Point2D mapLocation) {
    return getTileBoundingBox(getCurrentMap(), mapLocation);
  }

  /**
   * Returns the bounding box of the tile that contains the given pixel-space location on the supplied map.
   *
   * @param map         the map providing tile size and orientation
   * @param mapLocation the pixel-space location
   * @return the enclosing tile's bounding box, or an empty rectangle if {@code map} is {@code null}
   */
  public static Rectangle2D getTileBoundingBox(final IMap map, final Point2D mapLocation) {
    if (map == null) {
      return new Rectangle2D.Double();
    }

    return map.getOrientation().getEnclosingTileBounds(mapLocation, map);
  }

  /**
   * Returns the bounding box of the tile at the given tile coordinate on the current environment's map.
   *
   * @param x the tile x coordinate
   * @param y the tile y coordinate
   * @return the tile's bounding box
   */
  public static Rectangle2D getTileBoundingBox(final int x, final int y) {
    return getTileBoundingBox(getCurrentMap(), x, y);
  }

  /**
   * Returns the bounding box of the tile at the given tile coordinate on the supplied map.
   *
   * @param map the map providing tile size and orientation
   * @param x   the tile x coordinate
   * @param y   the tile y coordinate
   * @return the tile's bounding box
   */
  public static Rectangle2D getTileBoundingBox(final IMap map, final int x, final int y) {
    return getTileBoundingBox(map, new Point(x, y));
  }

  /**
   * Returns the bounding box of the tile at the given tile coordinate on the current environment's map.
   *
   * @param tile the tile coordinate
   * @return the tile's bounding box
   */
  public static Rectangle2D getTileBoundingBox(final Point tile) {
    return getTileBoundingBox(getCurrentMap(), tile);
  }

  /**
   * Returns the bounding box of the tile at the given tile coordinate on the supplied map.
   *
   * @param map  the map providing tile size and orientation
   * @param tile the tile coordinate
   * @return the tile's bounding box, or an empty rectangle if {@code map} is {@code null}
   */
  public static Rectangle2D getTileBoundingBox(final IMap map, final Point tile) {
    if (map == null) {
      return new Rectangle2D.Double();
    }

    return map.getOrientation().getBounds(tile, map);
  }

  private static IMap getCurrentMap() {
    if (Game.world().environment() == null) {
      return null;
    }

    return Game.world().environment().getMap();
  }
}
