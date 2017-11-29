package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;

public final class MapUtilities {
  private static Map<String, ITileAnimation> animations;
  private static Map<String, Boolean> hasAnimation;
  private static Map<String, ITileset> tilesets = new ConcurrentHashMap<>();

  static {
    animations = new ConcurrentHashMap<>();
    hasAnimation = new ConcurrentHashMap<>();
    tilesets = new ConcurrentHashMap<>();
  }

  private MapUtilities() {
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

  public static Point2D getCenterMapLocation() {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return new Point2D.Double();
    }

    return getCenterMapLocation(Game.getEnvironment().getMap());
  }

  public static Point2D getCenterMapLocation(IMap map) {
    return new Point2D.Double(map.getSizeInPixels().width / 2.0, map.getSizeInPixels().height / 2.0);
  }

  public static Rectangle2D getTileBoundingBox(final Point2D mapLocation) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return new Rectangle2D.Double();
    }

    return getTileBoundingBox(Game.getEnvironment().getMap(), mapLocation);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point2D mapLocation) {
    Point location = getTile(map, mapLocation);
    return new Rectangle2D.Double(location.x * map.getTileSize().getWidth(), location.y * map.getTileSize().getHeight(), map.getTileSize().getWidth(), map.getTileSize().getHeight());
  }

  public static Rectangle2D getTileBoundingBox(final Point tile) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return new Rectangle2D.Double();
    }

    return getTileBoundingBox(Game.getEnvironment().getMap(), tile);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point tile) {
    if (map == null || tile == null) {
      return null;
    }
    return new Rectangle2D.Double(tile.x * map.getTileSize().getWidth(), tile.y * map.getTileSize().getHeight(), map.getTileSize().getWidth(), map.getTileSize().getHeight());
  }

  public static Point getTile(final Point2D mapLocation) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return new Point();
    }

    return getTile(Game.getEnvironment().getMap(), mapLocation);
  }

  public static Point getTile(final IMap map, final Point2D mapLocation) {
    return new Point((int) (mapLocation.getX() / map.getTileSize().getWidth()), (int) (mapLocation.getY() / map.getTileSize().getHeight()));
  }

  public static Point2D getMapLocation(final IMap map, final Point tileLocation) {
    return new Point2D.Double(tileLocation.x * map.getTileSize().getWidth(), tileLocation.y * map.getTileSize().getHeight());
  }

  /**
   * Gets the tiles by pixel location.
   *
   * @param location
   *          the location
   * @return the tiles by pixel location
   */
  public static List<ITile> getTilesByPixelLocation(final IMap map, final Point2D location) {
    final List<ITile> tilesAtLocation = new ArrayList<>();
    if (map.getTileLayers() == null || map.getTileLayers().isEmpty()) {
      return tilesAtLocation;
    }

    final Point tileLocation = getTile(map, location);
    for (final ITileLayer layer : map.getTileLayers()) {
      final ITile tile = layer.getTile(tileLocation.x, tileLocation.y);
      if (tile != null) {
        tilesAtLocation.add(tile);
      }
    }

    return tilesAtLocation;
  }

  public static ITile getTopMostTile(final Point2D location) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.getEnvironment().getMap(), location);
  }

  public static ITile getTopMostTile(final IMap map, final Point2D location) {
    if (map.getTileLayers() == null || map.getTileLayers().isEmpty()) {
      return null;
    }

    return getTopMostTile(getTile(map, location));
  }

  public static ITile getTopMostTile(final Point point) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.getEnvironment().getMap(), point);
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

  public static ITerrain[] getTerrain(final IMap map, final int gId) {
    for (final ITileset tileset : map.getTilesets()) {
      if (tileset.containsTile(gId)) {
        return tileset.getTerrain(gId);
      }
    }

    return new ITerrain[4];
  }

  public static boolean hasAnimation(final IMap map, final ITile tile) {

    final ITileset tileset = MapUtilities.findTileSet(map, tile);
    if (tileset == null || tileset.getFirstGridId() > tile.getGridId()) {
      return false;
    }

    // get the grid id relative to the sprite sheet since we use a 0 based
    // approach to calculate the position
    int index = tile.getGridId() - tileset.getFirstGridId();

    final ITileAnimation animation = MapUtilities.getAnimation(map, index);
    return animation != null && !animation.getFrames().isEmpty();
  }

  public static ITileAnimation getAnimation(final IMap map, final int gId) {

    String cacheKey = map.getFileName() + "[" + gId + "]";
    if (hasAnimation.containsKey(cacheKey) && !hasAnimation.get(cacheKey)) {
      return null;
    }

    if (animations.containsKey(cacheKey)) {
      return animations.get(cacheKey);
    }

    for (final ITileset tileset : map.getTilesets()) {
      if (tileset.containsTile(gId)) {
        ITileAnimation anim = tileset.getAnimation(gId);
        boolean animation = false;
        if (anim != null) {
          animations.put(cacheKey, anim);
          animation = true;
        }

        hasAnimation.put(cacheKey, animation);

        return anim;
      }
    }

    return null;
  }

  /**
   * Searches for the tile set that contains the specified tile, identified by
   * the grid id.
   *
   * @param map
   *          the map
   * @param tile
   *          the tile
   * @return the tileset
   */
  public static ITileset findTileSet(final IMap map, final ITile tile) {
    if (map == null || tile == null) {
      return null;
    }

    String cacheKey = map.getFileName() + "[" + tile.getGridId() + "]";
    if (tilesets.containsKey(cacheKey)) {
      return tilesets.get(cacheKey);
    }

    ITileset match = null;

    for (final ITileset tileset : map.getTilesets()) {
      if (tileset.containsTile(tile)) {
        match = tileset;
        break;
      }
    }

    if (match != null) {
      tilesets.put(cacheKey, match);
    }

    return match;
  }

  public static Path2D convertPolylineToPath(final IMapObject mapObject) {
    if (mapObject == null || mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().isEmpty()) {
      return null;
    }

    Path2D path = new Path2D.Float();
    path.moveTo(mapObject.getLocation().getX(), mapObject.getLocation().getY());
    for (int i = 1; i < mapObject.getPolyline().getPoints().size(); i++) {
      Point2D point = mapObject.getPolyline().getPoints().get(i);
      path.lineTo(mapObject.getLocation().getX() + point.getX(), mapObject.getLocation().getY() + point.getY());
    }

    return path;
  }

  public static List<Point2D> convertPolylineToPointList(final IMapObject mapObject) {
    List<Point2D> points = new ArrayList<>();
    if (mapObject == null || mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().isEmpty()) {
      return points;
    }

    for (int i = 1; i < mapObject.getPolyline().getPoints().size(); i++) {
      Point2D point = mapObject.getPolyline().getPoints().get(i);
      points.add(new Point2D.Double(mapObject.getLocation().getX() + point.getX(), mapObject.getLocation().getY() + point.getY()));
    }

    return points;
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
}
