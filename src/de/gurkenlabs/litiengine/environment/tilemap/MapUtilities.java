package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public final class MapUtilities {
  private static final Map<String, ITileAnimation> animations;
  private static final Map<String, Boolean> hasAnimation;

  static {
    animations = new ConcurrentHashMap<>();
    hasAnimation = new ConcurrentHashMap<>();
  }

  private MapUtilities() {
    throw new UnsupportedOperationException();
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
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return new Point2D.Double();
    }

    return getCenterMapLocation(Game.world().environment().getMap());
  }

  public static Point2D getCenterMapLocation(IMap map) {
    return new Point2D.Double(map.getSizeInPixels().width / 2.0, map.getSizeInPixels().height / 2.0);
  }

  public static Rectangle2D getTileBoundingBox(final Point2D mapLocation) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return new Rectangle2D.Double();
    }

    return getTileBoundingBox(Game.world().environment().getMap(), mapLocation);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point2D mapLocation) {
    Point location = getTile(map, mapLocation);
    return new Rectangle2D.Double(location.x * map.getTileSize().getWidth(), location.y * map.getTileSize().getHeight(), map.getTileSize().getWidth(), map.getTileSize().getHeight());
  }

  public static Rectangle2D getTileBoundingBox(final int x, final int y) {
    return getTileBoundingBox(new Point(x, y));
  }

  public static Rectangle2D getTileBoundingBox(final Point tile) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return new Rectangle2D.Double();
    }
    return getTileBoundingBox(Game.world().environment().getMap(), tile);
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point tile) {
    if (map == null || tile == null) {
      return null;
    }
    return map.getTileShape(tile.x, tile.y).getBounds2D();
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Rectangle2D box) {
    final int minX = (int) MathUtilities.clamp(box.getX(), 0, map.getSizeInPixels().width - 1);
    final int minY = (int) MathUtilities.clamp(box.getY(), 0, map.getSizeInPixels().height - 1);
    final int maxX = (int) MathUtilities.clamp(box.getMaxX(), 0, map.getSizeInPixels().width - 1);
    final int maxY = (int) MathUtilities.clamp(box.getMaxY(), 0, map.getSizeInPixels().height - 1);
    final Point minTilePoint = getTile(map, minX, minY);
    final Point maxTilePoint = getTile(map, maxX, maxY);
    final Rectangle2D minTileBounds = map.getTileShape(MathUtilities.clamp(minTilePoint.x, 0, map.getWidth() - 1), MathUtilities.clamp(minTilePoint.y, 0, map.getHeight() - 1)).getBounds2D();
    final Rectangle2D maxTileBounds = map.getTileShape(MathUtilities.clamp(maxTilePoint.x, 0, map.getWidth() - 1), MathUtilities.clamp(maxTilePoint.y, 0, map.getHeight() - 1)).getBounds2D();

    return new Rectangle2D.Double(minTileBounds.getX(), minTileBounds.getY(), maxTileBounds.getMaxX() - minTileBounds.getX(), maxTileBounds.getMaxY() - minTileBounds.getY());
  }

  public static Point getTile(final Point2D mapLocation) {
    if (Game.world().environment() == null) {
      return new Point(-1, -1);
    }
    return getTile(Game.world().environment().getMap(), mapLocation);
  }

  public static Point getTile(final IMap map, final Point2D mapLocation) {
    return getTile(map, mapLocation.getX(), mapLocation.getY());
  }

  public static Point getTile(final IMap map, final double x, final double y) {
    //standard behaviour for rectangular Tiles: search on a grid with the tile dimensions
    int jumpWidth = map.getTileWidth();
    int jumpHeight = map.getTileHeight();
    //if we're less than 1 tile left or up the map, get -1 instead of 0 as tile coordinate.
    int xCoord = x < 0 && -x < jumpWidth ? -1 : (int) (x / jumpWidth);
    int yCoord = y < 0 && -y < jumpHeight ? -1 : (int) (y / jumpHeight);

    if (map.getOrientation() != MapOrientation.HEXAGONAL) {
      return new Point(xCoord, yCoord);
    }
    //for hex maps, we must adjust our jump size for cropping the subImages since tiles are not aligned orthogonally.

    StaggerAxis staggerAxis = map.getStaggerAxis();
    StaggerIndex staggerIndex = map.getStaggerIndex();
    //the t parameter describes the distance between one end of the flat hex side to the bounding box.
    int s = map.getHexSideLength();
    int t = staggerAxis == StaggerAxis.X ? (map.getTileWidth() - s) / 2 : (map.getTileHeight() - s) / 2;
    int r = staggerAxis == StaggerAxis.X ? map.getTileHeight() / 2 : map.getTileWidth() / 2;
    //Since we require to get Tiles outside of the map as well, we need to construct an infinite hex grid on which we can determine
    //tile indices. This follows the hex grid click detection from http://www.quarkphysics.ca/scripsi/hexgrid/ 

    jumpWidth = staggerAxis == StaggerAxis.X ? t + s : map.getTileWidth();
    jumpHeight = staggerAxis == StaggerAxis.X ? map.getTileHeight() : t + s;
    xCoord = x < 0 ? (int) (x / jumpWidth) - 1 : (int) (x / jumpWidth);
    yCoord = y < 0 ? (int) (y / jumpHeight) - 1 : (int) (y / jumpHeight);
    if (staggerAxis == StaggerAxis.X && isStaggeredRowOrColumn(staggerIndex, xCoord)) {
      yCoord = (int) ((y - jumpHeight / 2) / jumpHeight);
      yCoord = y < jumpHeight / 2 ? yCoord - 1 : yCoord;
    } else if (staggerAxis == StaggerAxis.Y && isStaggeredRowOrColumn(staggerIndex, yCoord)) {
      xCoord = (int) ((x - jumpWidth / 2) / jumpWidth);
      xCoord = x < jumpWidth / 2 ? xCoord - 1 : xCoord;
    }
    return assessHexStaggering(staggerAxis, staggerIndex, new Point(xCoord, yCoord), s, t, r, jumpWidth, jumpHeight, x, y);
  }

  /**
   * Check if the row or column with the given index is staggered.
   * 
   * @param staggerIndex
   *          the staggerIndex property of the map. Every second row (or column, depending on the {@link StaggerAxis} of the map is staggered half a
   *          tile.
   * @param index
   *          the index of the current row or column for which we want to determine if it's staggered or not.
   * @return a boolean representing if the row or column with the given index is staggered.
   */
  public static boolean isStaggeredRowOrColumn(StaggerIndex staggerIndex, int index) {
    return (staggerIndex == StaggerIndex.ODD && MathUtilities.isOddNumber(index)) || (staggerIndex == StaggerIndex.EVEN && !MathUtilities.isOddNumber(index));
  }

  private static Point assessHexStaggering(StaggerAxis staggerAxis, StaggerIndex staggerIndex, Point tileLocation, int s, int t, int r, int jumpWidth, int jumpHeight, double mouseX, double mouseY) {
    int xIndex = tileLocation.x;
    int yIndex = tileLocation.y;
    int x = isStaggeredRowOrColumn(staggerIndex, yIndex) && staggerAxis == StaggerAxis.Y ? xIndex * jumpWidth + r : xIndex * jumpWidth;
    int y = isStaggeredRowOrColumn(staggerIndex, xIndex) && staggerAxis == StaggerAxis.X ? yIndex * jumpHeight + r : yIndex * jumpHeight;
    Polygon hex = GeometricUtilities.getHex(x, y, staggerAxis, s, r, t);
    //we don't need any further computation if the mouse is already inside the hex
    if (hex.contains(mouseX, mouseY)) {
      return new Point(xIndex, yIndex);
    } else if (mouseY < hex.getBounds2D().getY() + hex.getBounds2D().getHeight() / 2) { //is the mouse in the upper left triangle outside the hex -> switch to the hex left and above the current hex
      if (staggerAxis == StaggerAxis.X) {
        yIndex = isStaggeredRowOrColumn(staggerIndex, xIndex) ? yIndex : yIndex - 1;
        xIndex -= 1;
      }
      if (staggerAxis == StaggerAxis.Y) {
        xIndex = isStaggeredRowOrColumn(staggerIndex, yIndex) ? xIndex : xIndex - 1;
        yIndex -= 1;
      }
    } else if (mouseY >= hex.getBounds2D().getY() + hex.getBounds2D().getHeight() / 2) { //is the mouse in the lower left triangle outside the hex-> switch to the hex left and below the current hex
      if (staggerAxis == StaggerAxis.X) {
        yIndex = isStaggeredRowOrColumn(staggerIndex, xIndex) ? yIndex + 1 : yIndex;
        xIndex -= 1;
      }
      if (staggerAxis == StaggerAxis.Y) {
        xIndex = isStaggeredRowOrColumn(staggerIndex, yIndex) ? xIndex + 1 : xIndex;
        yIndex -= 1;
      }
    }
    return new Point(xIndex, yIndex);
  }

  public static Point2D getMapLocation(final IMap map, final Point tileLocation) {
    return new Point2D.Double(tileLocation.x * map.getTileSize().getWidth(), tileLocation.y * map.getTileSize().getHeight());
  }

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
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }

    return getTopMostTile(Game.world().environment().getMap(), location);
  }

  public static ITile getTopMostTile(final IMap map, final Point2D location) {
    if (map.getTileLayers() == null || map.getTileLayers().isEmpty()) {
      return null;
    }

    return getTopMostTile(getTile(map, location));
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

    String cacheKey = map.getName() + "[" + gId + "]";
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

    ITileset match = null;

    for (final ITileset tileset : map.getTilesets()) {
      if (tileset.containsTile(tile)) {
        match = tileset;
        break;
      }
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
