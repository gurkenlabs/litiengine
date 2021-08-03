package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.MathUtilities;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public final class MapUtilities {
  private MapUtilities() {
    throw new UnsupportedOperationException();
  }

  public static Rectangle2D getBounds(IMapObject... objects) {
    double x = Double.MAX_VALUE;
    double y = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;
    for (IMapObject object : objects) {
      final Rectangle2D bounds = object.getBoundingBox();
      x = Math.min(bounds.getX(), x);
      y = Math.min(bounds.getY(), y);
      maxX = Math.max(bounds.getX(), maxX);
      maxY = Math.max(bounds.getY(), maxY);
    }

    return new Rectangle2D.Double(x, y, maxX - x, maxY - y);
  }

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

  public static Rectangle2D getTileBoundingBox(final IMap map, final Rectangle2D box) {
    final int minX = (int) MathUtilities.clamp(box.getX(), 0, map.getSizeInPixels().width - 1);
    final int minY = (int) MathUtilities.clamp(box.getY(), 0, map.getSizeInPixels().height - 1);
    final int maxX = (int) MathUtilities.clamp(box.getMaxX(), 0, map.getSizeInPixels().width - 1);
    final int maxY = (int) MathUtilities.clamp(box.getMaxY(), 0, map.getSizeInPixels().height - 1);
    final Point minTilePoint = map.getOrientation().getTile(minX, minY, map);
    final Point maxTilePoint = map.getOrientation().getTile(maxX, maxY, map);
    int minTileX =
        map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
            ? minTilePoint.x
            : MathUtilities.clamp(minTilePoint.x, 0, map.getWidth() - 1);
    int minTileY =
        map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
            ? minTilePoint.y
            : MathUtilities.clamp(minTilePoint.y, 0, map.getHeight() - 1);
    int maxTileX =
        map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
            ? maxTilePoint.x
            : MathUtilities.clamp(maxTilePoint.x, 0, map.getWidth() - 1);
    int maxTileY =
        map.getOrientation().getName().equals(MapOrientations.ISOMETRIC.getName())
            ? maxTilePoint.y
            : MathUtilities.clamp(maxTilePoint.y, 0, map.getWidth() - 1);
    final Rectangle2D minTileBounds =
        map.getOrientation()
            .getBounds(
                new Point(
                    MathUtilities.clamp(minTileX, 0, map.getWidth() - 1),
                    MathUtilities.clamp(minTileY, 0, map.getHeight() - 1)),
                map);
    final Rectangle2D maxTileBounds =
        map.getOrientation()
            .getBounds(
                new Point(
                    MathUtilities.clamp(maxTileX, 0, map.getWidth() - 1),
                    MathUtilities.clamp(maxTileY, 0, map.getHeight() - 1)),
                map);

    return new Rectangle2D.Double(
        minTileBounds.getX(),
        minTileBounds.getY(),
        maxTileBounds.getMaxX() - minTileBounds.getX(),
        maxTileBounds.getMaxY() - minTileBounds.getY());
  }

  /**
   * Get the corresponding tile for a given pixel map location. This is an overload taking the Map
   * from the current environment to calculate a tile location.
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
   * @param map The map on which to calculate the tile location.
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
   * @param staggerIndex the staggerIndex property of the map. Every second row (or column,
   *     depending on the {@link StaggerAxis} of the map is staggered half a tile.
   * @param index the index of the current row or column for which we want to determine if it's
   *     staggered or not.
   * @return a boolean representing if the row or column with the given index is staggered.
   */
  public static boolean isStaggeredRowOrColumn(StaggerIndex staggerIndex, int index) {
    return (staggerIndex == StaggerIndex.ODD && MathUtilities.isOddNumber(index))
        || (staggerIndex == StaggerIndex.EVEN && !MathUtilities.isOddNumber(index));
  }

  public static Point2D getMapLocation(final IMap map, final Point tileLocation) {
    return new Point2D.Double(
        tileLocation.x * map.getTileSize().getWidth(),
        tileLocation.y * map.getTileSize().getHeight());
  }

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

  public static ITile getTopMostTile(final Point2D location) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.world().environment().getMap(), location);
  }

  public static ITile getTopMostTile(final IMap map, final Point2D location) {
    if (map.getTileLayers() == null || map.getTileLayers().isEmpty()) {
      return null;
    }

    return getTopMostTile(map.getOrientation().getTile(location, map));
  }

  public static ITile getTopMostTile(final Point point) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.world().environment().getMap(), point);
  }

  public static ITile getTopMostTile(final IMap map, final Point point) {
    final Point tileLocation = point;

    ITile tile = null;
    for (final ITileLayer layer : map.getTileLayers()) {
      ITile tileOfLayer = layer.getTile(tileLocation.x, tileLocation.y);
      if (tileOfLayer != null && tileOfLayer.getGridId() != 0) {
        tile = tileOfLayer;
      }
    }

    return tile;
  }

  /**
   * Searches for the tile set that contains the specified tile, identified by the grid id.
   *
   * @param map the map
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

  public static Path2D convertPolyshapeToPath(final IMapObject mapObject) {
    if (mapObject == null || (!mapObject.isPolygon() && !mapObject.isPolyline())) {
      return null;
    }

    List<Point2D> points =
        mapObject.getPolyline() != null
            ? mapObject.getPolyline().getPoints()
            : mapObject.getPolygon().getPoints();
    if (points.isEmpty()) {
      return null;
    }

    Path2D path = new Path2D.Float();
    path.moveTo(mapObject.getLocation().getX(), mapObject.getLocation().getY());
    for (int i = 1; i < points.size(); i++) {
      Point2D point = points.get(i);
      path.lineTo(
          mapObject.getLocation().getX() + point.getX(),
          mapObject.getLocation().getY() + point.getY());
    }

    if (mapObject.isPolygon()) {
      path.closePath();
    }

    return path;
  }

  public static List<Point2D> getAbsolutePolyshapePoints(final IMapObject mapObject) {
    if (mapObject.isPolygon()) {
      return mapObject.getPolygon().getAbsolutePoints(mapObject.getLocation());
    }

    if (mapObject.isPolyline()) {
      return mapObject.getPolyline().getAbsolutePoints(mapObject.getLocation());
    }

    return new ArrayList<>();
  }

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

  public static Rectangle2D getTileBoundingBox(final Point2D mapLocation) {
    return getTileBoundingBox(getCurrentMap(), mapLocation);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point2D mapLocation) {
    if (map == null) {
      return new Rectangle2D.Double();
    }

    return map.getOrientation().getEnclosingTileBounds(mapLocation, map);
  }

  public static Rectangle2D getTileBoundingBox(final int x, final int y) {
    return getTileBoundingBox(getCurrentMap(), x, y);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final int x, final int y) {
    return getTileBoundingBox(map, new Point(x, y));
  }

  public static Rectangle2D getTileBoundingBox(final Point tile) {
    return getTileBoundingBox(getCurrentMap(), tile);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point tile) {
    if (map == null) {
      return new Rectangle2D.Double();
    }

    return map.getOrientation().getBounds(tile, map);
  }

  private static final IMap getCurrentMap() {
    if (Game.world().environment() == null) {
      return null;
    }

    return Game.world().environment().getMap();
  }
}
