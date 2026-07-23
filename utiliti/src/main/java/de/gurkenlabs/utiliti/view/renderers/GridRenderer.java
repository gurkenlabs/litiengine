package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class GridRenderer implements IEditorRenderer {

  private static final int MAX_GRID_ALPHA = 52;
  private static final int MAJOR_GRID_INTERVAL = 4;
  private static final float MIN_LINE_WIDTH = 0.25f;
  private static final float MAJOR_LINE_WIDTH_FACTOR = 1.75f;
  private static final double MIN_PROJECTED_GRID_SPACING = 4.0;

  private GridCacheKey cachedGeometryKey;
  private GridGeometry cachedGeometry;

  @Override
  public String getName() {
    return "GRID";
  }

  @Override
  public void render(Graphics2D graphics) {
    ICamera camera = Game.world().camera();
    if (!Editor.preferences().showGrid()
        || Game.world().environment() == null) {
      return;
    }

    IMap map = Game.world().environment().getMap();
    if (map == null || map.getOrientation() == null) {
      return;
    }

    double renderScale = camera.getRenderScale();
    GridDetail horizontalDetail = gridDetail(map.getTileHeight() * renderScale);
    GridDetail verticalDetail = gridDetail(map.getTileWidth() * renderScale);
    boolean orthogonal = isOrthogonal(map);
    GridDetail orientedDetail =
        gridDetail(Math.min(map.getTileWidth(), map.getTileHeight()) * renderScale);
    if (orthogonal
        ? horizontalDetail == GridDetail.NONE && verticalDetail == GridDetail.NONE
        : !rendersOrientedGrid(orientedDetail)) {
      return;
    }

    TileRange visibleTiles = calculateVisibleTileRange(map, camera.getViewport());
    if (visibleTiles.isEmpty()) {
      return;
    }

    Color preferenceColor = Editor.preferences().getGridColor();
    float preferenceLineWidth = Editor.preferences().getGridLineWidth();
    float lineWidth =
        Float.isFinite(preferenceLineWidth)
            ? Math.max(MIN_LINE_WIDTH, preferenceLineWidth)
            : MIN_LINE_WIDTH;
    Color renderColor = withLimitedAlpha(preferenceColor);
    AffineTransform mapToScreen = new AffineTransform();
    mapToScreen.scale(renderScale, renderScale);
    mapToScreen.translate(camera.getPixelOffsetX(), camera.getPixelOffsetY());

    Graphics2D gridGraphics = (Graphics2D) graphics.create();
    try {
      gridGraphics.clip(mapToScreen.createTransformedShape(map.getBounds()));
      gridGraphics.setColor(renderColor);

      if (orthogonal) {
        renderOrthogonalGrid(
            gridGraphics,
            map,
            visibleTiles,
            mapToScreen,
            horizontalDetail,
            verticalDetail,
            lineWidth);
      } else {
        renderOrientedGrid(
            gridGraphics,
            map,
            visibleTiles,
            mapToScreen,
            geometryFor(map),
            lineWidth);
      }
    } finally {
      gridGraphics.dispose();
    }
  }

  public synchronized void clearCache() {
    cachedGeometryKey = null;
    cachedGeometry = null;
  }

  private synchronized GridGeometry geometryFor(IMap map) {
    GridCacheKey key = GridCacheKey.from(map);
    if (!key.equals(cachedGeometryKey)) {
      cachedGeometry = GridGeometry.from(map);
      cachedGeometryKey = key;
    }

    return cachedGeometry;
  }

  static GridDetail gridDetail(double projectedTileSpacing) {
    if (!Double.isFinite(projectedTileSpacing) || projectedTileSpacing <= 0) {
      return GridDetail.NONE;
    }
    if (projectedTileSpacing >= MIN_PROJECTED_GRID_SPACING) {
      return GridDetail.ALL;
    }
    if (projectedTileSpacing * MAJOR_GRID_INTERVAL >= MIN_PROJECTED_GRID_SPACING) {
      return GridDetail.MAJOR_ONLY;
    }
    return GridDetail.NONE;
  }

  static boolean rendersOrientedGrid(GridDetail detail) {
    return detail == GridDetail.ALL;
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
      GridDetail horizontalDetail,
      GridDetail verticalDetail,
      float lineWidth) {
    Path2D.Double minorLines = new Path2D.Double();
    Path2D.Double majorLines = new Path2D.Double();
    boolean hasMinorLines = false;
    boolean hasMajorLines = false;

    double minScreenY = mapToScreenY(mapToScreen, (double) range.minY() * map.getTileHeight());
    double maxScreenY =
        mapToScreenY(mapToScreen, (double) (range.maxY() + 1) * map.getTileHeight());
    if (verticalDetail != GridDetail.NONE) {
      for (int x = range.minX(); x <= range.maxX() + 1; x++) {
        boolean major = isMajorLine(x);
        if (!major && verticalDetail != GridDetail.ALL) {
          continue;
        }
        double screenX = mapToScreenX(mapToScreen, (double) x * map.getTileWidth());
        Path2D.Double path = major ? majorLines : minorLines;
        path.moveTo(screenX, minScreenY);
        path.lineTo(screenX, maxScreenY);
        hasMajorLines |= major;
        hasMinorLines |= !major;
      }
    }

    double minScreenX = mapToScreenX(mapToScreen, (double) range.minX() * map.getTileWidth());
    double maxScreenX =
        mapToScreenX(mapToScreen, (double) (range.maxX() + 1) * map.getTileWidth());
    if (horizontalDetail != GridDetail.NONE) {
      for (int y = range.minY(); y <= range.maxY() + 1; y++) {
        boolean major = isMajorLine(y);
        if (!major && horizontalDetail != GridDetail.ALL) {
          continue;
        }
        double screenY = mapToScreenY(mapToScreen, (double) y * map.getTileHeight());
        Path2D.Double path = major ? majorLines : minorLines;
        path.moveTo(minScreenX, screenY);
        path.lineTo(maxScreenX, screenY);
        hasMajorLines |= major;
        hasMinorLines |= !major;
      }
    }

    if (hasMinorLines) {
      graphics.setStroke(new BasicStroke(lineWidth));
      graphics.draw(minorLines);
    }
    if (hasMajorLines) {
      graphics.setStroke(new BasicStroke(lineWidth * MAJOR_LINE_WIDTH_FACTOR));
      graphics.draw(majorLines);
    }
  }

  private static void renderOrientedGrid(
      Graphics2D graphics,
      IMap map,
      TileRange range,
      AffineTransform mapToScreen,
      GridGeometry geometry,
      float lineWidth) {
    Path2D.Double tileOutlines = new Path2D.Double();
    graphics.setStroke(new BasicStroke(lineWidth));
    for (int x = range.minX(); x <= range.maxX(); x++) {
      for (int y = range.minY(); y <= range.maxY(); y++) {
        Point tileLocation = map.getOrientation().getLocation(x, y, map);
        geometry.appendTranslated(tileOutlines, tileLocation, mapToScreen);
      }
    }
    graphics.draw(tileOutlines);
  }

  private static double mapToScreenX(AffineTransform mapToScreen, double mapX) {
    return mapX * mapToScreen.getScaleX() + mapToScreen.getTranslateX();
  }

  private static double mapToScreenY(AffineTransform mapToScreen, double mapY) {
    return mapY * mapToScreen.getScaleY() + mapToScreen.getTranslateY();
  }

  private static boolean isMajorLine(int tileBoundary) {
    return tileBoundary % MAJOR_GRID_INTERVAL == 0;
  }

  private static boolean isOrthogonal(IMap map) {
    return MapOrientations.ORTHOGONAL.getName().equals(map.getOrientation().getName());
  }

  private static Color withLimitedAlpha(Color color) {
    if (color == null) {
      return new Color(255, 255, 255, MAX_GRID_ALPHA);
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

  enum GridDetail {
    NONE,
    MAJOR_ONLY,
    ALL
  }

  record GridCacheKey(
      int tileWidth,
      int tileHeight,
      IMapOrientation orientation,
      StaggerAxis staggerAxis,
      int hexSideLength) {

    static GridCacheKey from(IMap map) {
      return new GridCacheKey(
          map.getTileWidth(),
          map.getTileHeight(),
          map.getOrientation(),
          staggerAxis(map),
          hexSideLength(map));
    }

    private static StaggerAxis staggerAxis(IMap map) {
      if (!MapOrientations.HEXAGONAL.getName().equals(map.getOrientation().getName())) {
        return null;
      }
      return map.getStaggerAxis();
    }

    private static int hexSideLength(IMap map) {
      if (!MapOrientations.HEXAGONAL.getName().equals(map.getOrientation().getName())) {
        return 0;
      }
      return map.getHexSideLength();
    }
  }

  private record GridGeometry(byte[] segmentTypes, double[] coordinates) {
    private static GridGeometry from(IMap map) {
      Point tileLocation = map.getOrientation().getLocation(0, 0, map);
      PathIterator iterator = map.getOrientation().getShape(0, 0, map).getPathIterator(null);
      byte[] segmentTypes = new byte[8];
      double[] coordinates = new double[segmentTypes.length * 6];
      double[] segmentCoordinates = new double[6];
      int segmentCount = 0;
      while (!iterator.isDone()) {
        if (segmentCount == segmentTypes.length) {
          segmentTypes = Arrays.copyOf(segmentTypes, segmentTypes.length * 2);
          coordinates = Arrays.copyOf(coordinates, segmentTypes.length * 6);
        }

        int segmentType = iterator.currentSegment(segmentCoordinates);
        segmentTypes[segmentCount] = (byte) segmentType;
        int coordinateCount = coordinateCount(segmentType);
        int coordinateOffset = segmentCount * 6;
        for (int i = 0; i < coordinateCount; i += 2) {
          coordinates[coordinateOffset + i] = segmentCoordinates[i] - tileLocation.x;
          coordinates[coordinateOffset + i + 1] = segmentCoordinates[i + 1] - tileLocation.y;
        }
        segmentCount++;
        iterator.next();
      }

      return new GridGeometry(
          Arrays.copyOf(segmentTypes, segmentCount),
          Arrays.copyOf(coordinates, segmentCount * 6));
    }

    private void appendTranslated(
        Path2D.Double path, Point tileLocation, AffineTransform mapToScreen) {
      double scaleX = mapToScreen.getScaleX();
      double scaleY = mapToScreen.getScaleY();
      double screenX = mapToScreenX(mapToScreen, tileLocation.x);
      double screenY = mapToScreenY(mapToScreen, tileLocation.y);
      for (int i = 0; i < segmentTypes.length; i++) {
        int offset = i * 6;
        switch (segmentTypes[i]) {
          case PathIterator.SEG_MOVETO ->
              path.moveTo(
                  screenX + coordinates[offset] * scaleX,
                  screenY + coordinates[offset + 1] * scaleY);
          case PathIterator.SEG_LINETO ->
              path.lineTo(
                  screenX + coordinates[offset] * scaleX,
                  screenY + coordinates[offset + 1] * scaleY);
          case PathIterator.SEG_QUADTO ->
              path.quadTo(
                  screenX + coordinates[offset] * scaleX,
                  screenY + coordinates[offset + 1] * scaleY,
                  screenX + coordinates[offset + 2] * scaleX,
                  screenY + coordinates[offset + 3] * scaleY);
          case PathIterator.SEG_CUBICTO ->
              path.curveTo(
                  screenX + coordinates[offset] * scaleX,
                  screenY + coordinates[offset + 1] * scaleY,
                  screenX + coordinates[offset + 2] * scaleX,
                  screenY + coordinates[offset + 3] * scaleY,
                  screenX + coordinates[offset + 4] * scaleX,
                  screenY + coordinates[offset + 5] * scaleY);
          case PathIterator.SEG_CLOSE -> path.closePath();
          default -> throw new IllegalStateException("Unsupported path segment: " + segmentTypes[i]);
        }
      }
    }

    private static int coordinateCount(int segmentType) {
      return switch (segmentType) {
        case PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> 2;
        case PathIterator.SEG_QUADTO -> 4;
        case PathIterator.SEG_CUBICTO -> 6;
        case PathIterator.SEG_CLOSE -> 0;
        default -> throw new IllegalStateException("Unsupported path segment: " + segmentType);
      };
    }
  }
}
