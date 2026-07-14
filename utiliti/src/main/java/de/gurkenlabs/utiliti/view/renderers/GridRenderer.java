package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerIndex;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GridRenderer implements IEditorRenderer {

  private static final int MAX_GRID_ALPHA = 52;
  private static final int MAJOR_GRID_INTERVAL = 4;
  private static final float MIN_LINE_WIDTH = 0.25f;
  private static final float MAJOR_LINE_WIDTH_FACTOR = 1.75f;

  private final Map<GridCacheKey, GridGeometry> gridCache = new ConcurrentHashMap<>();

  @Override
  public String getName() {
    return "GRID";
  }

  @Override
  public void render(Graphics2D graphics) {
    ICamera camera = Game.world().camera();
    if (!Editor.preferences().showGrid()
        || camera.getRenderScale() < 1
        || Game.world().environment() == null) {
      return;
    }

    IMap map = Game.world().environment().getMap();
    if (map == null || map.getOrientation() == null) {
      return;
    }

    TileRange visibleTiles = calculateVisibleTileRange(map, camera.getViewport());
    if (visibleTiles.isEmpty()) {
      return;
    }

    Color preferenceColor = Editor.preferences().getGridColor();
    float preferenceLineWidth = Editor.preferences().getGridLineWidth();
    GridCacheKey cacheKey = GridCacheKey.from(map, preferenceColor, preferenceLineWidth);
    GridGeometry geometry = gridCache.computeIfAbsent(cacheKey, ignored -> GridGeometry.from(map));

    float lineWidth =
        Float.isFinite(preferenceLineWidth)
            ? Math.max(MIN_LINE_WIDTH, preferenceLineWidth)
            : MIN_LINE_WIDTH;
    Color renderColor = withLimitedAlpha(preferenceColor);
    double renderScale = camera.getRenderScale();
    AffineTransform mapToScreen = new AffineTransform();
    mapToScreen.scale(renderScale, renderScale);
    mapToScreen.translate(camera.getPixelOffsetX(), camera.getPixelOffsetY());

    Graphics2D gridGraphics = (Graphics2D) graphics.create();
    try {
      gridGraphics.clip(mapToScreen.createTransformedShape(map.getBounds()));
      gridGraphics.setColor(renderColor);

      if (isOrthogonal(map)) {
        renderOrthogonalGrid(gridGraphics, map, visibleTiles, mapToScreen, lineWidth);
      } else {
        renderOrientedGrid(gridGraphics, map, visibleTiles, mapToScreen, geometry, lineWidth);
      }
    } finally {
      gridGraphics.dispose();
    }
  }

  public void clearCache() {
    gridCache.clear();
  }

  static TileRange calculateVisibleTileRange(
      Rectangle2D viewport,
      int mapWidth,
      int mapHeight,
      int tileWidth,
      int tileHeight) {
    if (viewport == null
        || viewport.isEmpty()
        || mapWidth <= 0
        || mapHeight <= 0
        || tileWidth <= 0
        || tileHeight <= 0) {
      return TileRange.empty();
    }

    Rectangle2D mapBounds =
        new Rectangle2D.Double(
            0, 0, (double) mapWidth * tileWidth, (double) mapHeight * tileHeight);
    Rectangle2D visibleBounds = viewport.createIntersection(mapBounds);
    if (visibleBounds.isEmpty()) {
      return TileRange.empty();
    }

    int minX = (int) Math.floor(visibleBounds.getMinX() / tileWidth);
    int minY = (int) Math.floor(visibleBounds.getMinY() / tileHeight);
    int maxX = (int) Math.floor(Math.nextDown(visibleBounds.getMaxX()) / tileWidth);
    int maxY = (int) Math.floor(Math.nextDown(visibleBounds.getMaxY()) / tileHeight);
    return expandedAndClampedRange(minX, minY, maxX, maxY, mapWidth, mapHeight);
  }

  private static TileRange calculateVisibleTileRange(IMap map, Rectangle2D viewport) {
    if (isOrthogonal(map)) {
      return calculateVisibleTileRange(
          viewport,
          map.getWidth(),
          map.getHeight(),
          map.getTileWidth(),
          map.getTileHeight());
    }

    if (viewport == null
        || viewport.isEmpty()
        || map.getWidth() <= 0
        || map.getHeight() <= 0
        || map.getTileWidth() <= 0
        || map.getTileHeight() <= 0) {
      return TileRange.empty();
    }

    Rectangle2D visibleBounds = viewport.createIntersection(map.getBounds());
    if (visibleBounds.isEmpty()) {
      return TileRange.empty();
    }

    double minX = visibleBounds.getMinX();
    double minY = visibleBounds.getMinY();
    double maxX = Math.nextDown(visibleBounds.getMaxX());
    double maxY = Math.nextDown(visibleBounds.getMaxY());
    double midX = (minX + maxX) / 2.0;
    double midY = (minY + maxY) / 2.0;
    double[] sampleXs = {minX, midX, maxX};
    double[] sampleYs = {minY, midY, maxY};

    int minTileX = Integer.MAX_VALUE;
    int minTileY = Integer.MAX_VALUE;
    int maxTileX = Integer.MIN_VALUE;
    int maxTileY = Integer.MIN_VALUE;
    for (double sampleX : sampleXs) {
      for (double sampleY : sampleYs) {
        Point tile = map.getOrientation().getTile(sampleX, sampleY, map);
        minTileX = Math.min(minTileX, tile.x);
        minTileY = Math.min(minTileY, tile.y);
        maxTileX = Math.max(maxTileX, tile.x);
        maxTileY = Math.max(maxTileY, tile.y);
      }
    }

    return expandedAndClampedRange(
        minTileX, minTileY, maxTileX, maxTileY, map.getWidth(), map.getHeight());
  }

  private static TileRange expandedAndClampedRange(
      int minX, int minY, int maxX, int maxY, int mapWidth, int mapHeight) {
    if (mapWidth <= 0 || mapHeight <= 0 || minX > maxX || minY > maxY) {
      return TileRange.empty();
    }

    return new TileRange(
        Math.clamp(minX - 1, 0, mapWidth - 1),
        Math.clamp(minY - 1, 0, mapHeight - 1),
        Math.clamp(maxX + 1, 0, mapWidth - 1),
        Math.clamp(maxY + 1, 0, mapHeight - 1));
  }

  private static void renderOrthogonalGrid(
      Graphics2D graphics,
      IMap map,
      TileRange range,
      AffineTransform mapToScreen,
      float lineWidth) {
    graphics.setStroke(new BasicStroke(lineWidth));
    renderOrthogonalLines(graphics, map, range, mapToScreen, false);

    graphics.setStroke(new BasicStroke(lineWidth * MAJOR_LINE_WIDTH_FACTOR));
    renderOrthogonalLines(graphics, map, range, mapToScreen, true);
  }

  private static void renderOrthogonalLines(
      Graphics2D graphics,
      IMap map,
      TileRange range,
      AffineTransform mapToScreen,
      boolean major) {
    double minMapY = (double) range.minY() * map.getTileHeight();
    double maxMapY = (double) (range.maxY() + 1) * map.getTileHeight();
    for (int x = range.minX(); x <= range.maxX() + 1; x++) {
      if (isMajorLine(x) != major) {
        continue;
      }
      double mapX = (double) x * map.getTileWidth();
      graphics.draw(
          mapToScreen.createTransformedShape(
              new Line2D.Double(mapX, minMapY, mapX, maxMapY)));
    }

    double minMapX = (double) range.minX() * map.getTileWidth();
    double maxMapX = (double) (range.maxX() + 1) * map.getTileWidth();
    for (int y = range.minY(); y <= range.maxY() + 1; y++) {
      if (isMajorLine(y) != major) {
        continue;
      }
      double mapY = (double) y * map.getTileHeight();
      graphics.draw(
          mapToScreen.createTransformedShape(
              new Line2D.Double(minMapX, mapY, maxMapX, mapY)));
    }
  }

  private static void renderOrientedGrid(
      Graphics2D graphics,
      IMap map,
      TileRange range,
      AffineTransform mapToScreen,
      GridGeometry geometry,
      float lineWidth) {
    graphics.setStroke(new BasicStroke(lineWidth));
    for (int x = range.minX(); x <= range.maxX(); x++) {
      for (int y = range.minY(); y <= range.maxY(); y++) {
        Point tileLocation = map.getOrientation().getLocation(x, y, map);
        AffineTransform tileToScreen = new AffineTransform(mapToScreen);
        tileToScreen.translate(tileLocation.x, tileLocation.y);
        graphics.draw(tileToScreen.createTransformedShape(geometry.relativeTileShape()));
      }
    }
  }

  private static boolean isMajorLine(int tileBoundary) {
    return tileBoundary % MAJOR_GRID_INTERVAL == 0;
  }

  private static boolean isOrthogonal(IMap map) {
    return MapOrientations.ORTHOGONAL.getName().equals(map.getOrientation().getName());
  }

  private static Color withLimitedAlpha(Color color) {
    if (color == null) {
      return new Color(255, 255, 255, 65);
    }

    return new Color(
        color.getRed(), color.getGreen(), color.getBlue(), Math.min(color.getAlpha(), MAX_GRID_ALPHA));
  }

  record TileRange(int minX, int minY, int maxX, int maxY) {
    static TileRange empty() {
      return new TileRange(0, 0, -1, -1);
    }

    boolean isEmpty() {
      return maxX < minX || maxY < minY;
    }
  }

  record GridCacheKey(
      String mapName,
      int mapWidth,
      int mapHeight,
      int tileWidth,
      int tileHeight,
      IMapOrientation orientation,
      String orientationName,
      StaggerAxis staggerAxis,
      StaggerIndex staggerIndex,
      int hexSideLength,
      int color,
      float lineWidth) {

    static GridCacheKey from(IMap map, Color color, float lineWidth) {
      return new GridCacheKey(
          map.getName(),
          map.getWidth(),
          map.getHeight(),
          map.getTileWidth(),
          map.getTileHeight(),
          map.getOrientation(),
          map.getOrientation().getName(),
          map.getStaggerAxis(),
          map.getStaggerIndex(),
          hexSideLength(map),
          color != null ? color.getRGB() : 0,
          lineWidth);
    }

    private static int hexSideLength(IMap map) {
      if (!MapOrientations.HEXAGONAL.getName().equals(map.getOrientation().getName())) {
        return 0;
      }
      return map.getHexSideLength();
    }
  }

  private record GridGeometry(Shape relativeTileShape) {
    private static GridGeometry from(IMap map) {
      Point tileLocation = map.getOrientation().getLocation(0, 0, map);
      AffineTransform relativeTransform =
          AffineTransform.getTranslateInstance(-tileLocation.x, -tileLocation.y);
      Shape relativeShape =
          relativeTransform.createTransformedShape(map.getOrientation().getShape(0, 0, map));
      return new GridGeometry(new Path2D.Double(relativeShape));
    }
  }
}
