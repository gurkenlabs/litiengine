package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.UserPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class MapObjectsRenderer implements IEditorRenderer {

  private static final int MAX_NAME_DISPLAY_LENGTH = 50;
  private static final float MIN_NAME_RENDER_SCALE = 0.35f;

  @Override
  public String getName() {
    return "MAPOBJECTS";
  }

  @Override
  public void render(Graphics2D g) {
    final UserPreferences preferences = Editor.preferences();
    final boolean renderBoundingBoxes = preferences.renderBoundingBoxes();
    final boolean renderCustomMapObjects = preferences.renderCustomMapObjects();
    final ICamera camera = Game.world().camera();
    final float renderScale = camera.getRenderScale();
    final boolean renderNames = preferences.renderNames() && renderScale >= MIN_NAME_RENDER_SCALE;
    if (!renderBoundingBoxes) {
      return;
    }

    final Environment environment = Game.world().environment();
    if (environment == null) {
      return;
    }

    final IMap map = environment.getMap();
    if (map == null) {
      return;
    }

    final Rectangle2D viewport = camera.getViewport();
    final BasicStroke boundingBoxStroke = new BasicStroke(0.5f * renderScale);
    final BasicStroke polylineStroke = new BasicStroke(renderScale);
    final BasicStroke soundRangeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f);
    final Stroke noCollisionStroke = new BasicStroke(1 / renderScale, BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_BEVEL, 0, new float[] {1f}, 0);
    final Font nameFont = renderNames ? Style.getDefaultFont().deriveFont(11f) : null;
    final FontMetrics nameFontMetrics = renderNames ? g.getFontMetrics(nameFont) : null;

    final List<IMapObjectLayer> layers = map.getMapObjectLayers();
    for (final IMapObjectLayer layer : layers) {
      if (layer == null || !MapComponent.isLayerEffectivelyVisible(map, layer)) {
        continue;
      }

      final Color layerColor = layer.getColor();
      final Color boundingBoxFill = layerColor == null
        ? Style.COLOR_DEFAULT_BOUNDING_BOX_FILL
        : new Color(layerColor.getRed(), layerColor.getGreen(), layerColor.getBlue(), 25);
      final Color boundingBoxBorder = new Color(boundingBoxFill.getRed(), boundingBoxFill.getGreen(),
        boundingBoxFill.getBlue(), 150);
      final Color unsupportedColor = layerColor == null ? Style.COLOR_UNSUPPORTED : layerColor;

      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        final double objectX = mapObject.getX();
        final double objectY = mapObject.getY();
        final double objectWidth = mapObject.getWidth();
        final double objectHeight = mapObject.getHeight();
        final boolean baseBoundsVisible = intersects(
          viewport, objectX, objectY, objectWidth, objectHeight);
        Rectangle2D collisionBox = null;
        boolean collisionBoxResolved = false;
        int soundRange = 0;
        boolean soundRangeResolved = false;
        final String typeName;
        if (!baseBoundsVisible) {
          typeName = mapObject.getType();
          // Tiled polyshapes can extend beyond the object's width and height.
          if (!mapObject.isPolyline() && !mapObject.isPolygon()) {
            if (MapObjectType.SOUNDSOURCE.name().equals(typeName)) {
              soundRange = mapObject.getIntValue(MapObjectProperty.SOUND_RANGE, 0);
              soundRangeResolved = true;
            } else if (hasCollisionOverlay(typeName)) {
              collisionBox = getCollisionBox(mapObject, MapObjectType.COLLISIONBOX.name().equals(typeName));
              collisionBoxResolved = true;
            }

            if (!isVisibleInViewport(
                viewport, objectX, objectY, objectWidth, objectHeight, collisionBox, soundRange)) {
              continue;
            }
          }
        } else {
          typeName = mapObject.getType();
        }

        final MapObjectType type = MapObjectType.get(typeName);
        final BasicStroke shapeStroke = mapObject.isPolyline() ? polylineStroke : boundingBoxStroke;
        final Rectangle2D bounds = mapObject.getBoundingBox();
        if (type == null) {
          if (renderCustomMapObjects) {
            renderUnsupportedMapObject(g, mapObject, bounds, unsupportedColor, shapeStroke);
          }

          continue;
        }

        if (type == MapObjectType.SPAWNPOINT) {
          g.setColor(Style.COLOR_SPAWNPOINT);
          Game.graphics().renderShape(g, new Rectangle2D.Double(bounds.getCenterX() - 1,
            bounds.getCenterY() - 1, 2, 2));
        }

        if (type == MapObjectType.SOUNDSOURCE && !soundRangeResolved) {
          soundRange = mapObject.getIntValue(MapObjectProperty.SOUND_RANGE, 0);
        }

        if (type != MapObjectType.COLLISIONBOX) {
          boolean renderBaseShape = baseBoundsVisible || mapObject.isPolyline() || mapObject.isPolygon();
          renderBoundingBox(g, mapObject, type, bounds, boundingBoxFill, boundingBoxBorder,
            shapeStroke, soundRangeStroke, soundRange, renderNames, nameFont, nameFontMetrics,
            viewport, renderScale, renderBaseShape);
        }

        if (hasCollisionOverlay(type)) {
          if (!collisionBoxResolved) {
            collisionBox = getCollisionBox(mapObject, type == MapObjectType.COLLISIONBOX);
          }
          if (collisionBox != null) {
            renderCollisionBox(g, mapObject, type, collisionBox, shapeStroke, noCollisionStroke);
          }
        }
      }
    }
  }

  static boolean isVisibleInViewport(Rectangle2D viewport, Rectangle2D baseBounds,
    Rectangle2D collisionBounds, double soundRange) {
    if (viewport == null || baseBounds == null) {
      return true;
    }

    return isVisibleInViewport(
      viewport,
      baseBounds.getX(),
      baseBounds.getY(),
      baseBounds.getWidth(),
      baseBounds.getHeight(),
      collisionBounds,
      soundRange);
  }

  private static boolean isVisibleInViewport(Rectangle2D viewport, double x, double y,
    double width, double height, Rectangle2D collisionBounds, double soundRange) {
    if (viewport == null) {
      return true;
    }

    if (intersects(viewport, x, y, width, height)) {
      return true;
    }

    if (soundRange > 0) {
      double centerX = x + width / 2.0;
      double centerY = y + height / 2.0;
      if (intersects(
          viewport,
          centerX - soundRange,
          centerY - soundRange,
          soundRange * 2,
          soundRange * 2)) {
        return true;
      }
    }

    return collisionBounds != null && intersects(viewport, collisionBounds);
  }

  private static boolean intersects(Rectangle2D viewport, Rectangle2D bounds) {
    return intersects(viewport, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
  }

  private static boolean intersects(
      Rectangle2D viewport, double x, double y, double width, double height) {
    return !viewport.isEmpty()
      && x <= viewport.getMaxX()
      && x + width >= viewport.getX()
      && y <= viewport.getMaxY()
      && y + height >= viewport.getY();
  }

  private static boolean hasCollisionOverlay(String typeName) {
    return MapObjectType.PROP.name().equals(typeName)
      || MapObjectType.COLLISIONBOX.name().equals(typeName)
      || MapObjectType.CREATURE.name().equals(typeName);
  }

  private static boolean hasCollisionOverlay(MapObjectType type) {
    return type == MapObjectType.PROP
      || type == MapObjectType.COLLISIONBOX
      || type == MapObjectType.CREATURE;
  }

  private static Rectangle2D getCollisionBox(IMapObject mapObject, boolean collisionBoxObject) {
    final float collisionBoxWidth = collisionBoxObject
      ? mapObject.getWidth()
      : mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_WIDTH, -1);
    final float collisionBoxHeight = collisionBoxObject
      ? mapObject.getHeight()
      : mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_HEIGHT, -1);
    if (collisionBoxWidth == -1 || collisionBoxHeight == -1) {
      return null;
    }

    final Align align = mapObject.getEnumValue(MapObjectProperty.COLLISION_ALIGN, Align.class, Align.CENTER);
    final Valign valign = mapObject.getEnumValue(MapObjectProperty.COLLISION_VALIGN, Valign.class, Valign.DOWN);
    final Point2D location = mapObject.getLocation();
    return CollisionEntity.getCollisionBox(location, mapObject.getWidth(), mapObject.getHeight(),
      collisionBoxWidth, collisionBoxHeight, align, valign);
  }

  private static void renderUnsupportedMapObject(Graphics2D g, IMapObject mapObject,
    Rectangle2D bounds, Color color, BasicStroke shapeStroke) {
    g.setColor(color);
    final Point2D start = mapObject.getLocation();
    StringBuilder info = new StringBuilder("#");
    info.append(mapObject.getId());
    if (mapObject.getName() != null && !mapObject.getName().isEmpty()) {
      info.append("(");
      info.append(mapObject.getName());
      info.append(")");
    }

    Game.graphics().renderText(g, info.toString(), start.getX(), start.getY() - 5);
    Game.graphics().renderShape(g, new Ellipse2D.Double(start.getX() - 1, start.getY() - 1, 3, 3));

    if (mapObject.isPolyline()) {
      if (mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().isEmpty()) {
        return;
      }

      final Path2D path = MapUtilities.convertPolyshapeToPath(mapObject);
      if (path == null) {
        return;
      }

      Game.graphics().renderOutline(g, path, shapeStroke);
    } else if (mapObject.isPolygon()) {
      if (mapObject.getPolygon() == null || mapObject.getPolygon().getPoints().isEmpty()) {
        return;
      }

      final Path2D path = MapUtilities.convertPolyshapeToPath(mapObject);
      if (path == null) {
        return;
      }

      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      Game.graphics().renderShape(g, path);
      g.setColor(Style.COLOR_UNSUPPORTED);
      Game.graphics().renderOutline(g, path, shapeStroke);
    } else if (mapObject.isEllipse()) {
      final Ellipse2D ellipse = mapObject.getEllipse();
      if (ellipse == null) {
        return;
      }
      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      Game.graphics().renderShape(g, ellipse);

      g.setColor(Style.COLOR_UNSUPPORTED);
      Game.graphics().renderOutline(g, ellipse, shapeStroke);
    } else {
      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      Game.graphics().renderShape(g, bounds);
      g.setColor(Style.COLOR_UNSUPPORTED);
      Game.graphics().renderOutline(g, bounds, shapeStroke);
    }
  }

  // TODO rename to renderShape, support points and draw polygon points too.
  private static void renderBoundingBox(Graphics2D g, IMapObject mapObject, MapObjectType type,
    Rectangle2D baseBounds, Color colorBoundingBoxFill, Color defaultBorderColor,
    BasicStroke shapeStroke, BasicStroke soundRangeStroke, int soundRange, boolean renderNames,
    Font nameFont, FontMetrics nameFontMetrics, Rectangle2D viewport, float renderScale,
    boolean renderBaseShape) {
    Color fillColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      fillColor = Style.COLOR_TRIGGER_FILL;
    } else if (type == MapObjectType.STATICSHADOW) {
      fillColor = Style.COLOR_SHADOW_FILL;
    }

    Color borderColor;
    if (type == MapObjectType.TRIGGER) {
      borderColor = Style.COLOR_TRIGGER_BORDER;
    } else if (type == MapObjectType.LIGHTSOURCE) {
      borderColor = colorBoundingBoxFill;
      if (mapObject.hasCustomProperty(MapObjectProperty.LIGHT_COLOR)) {
        final Color mapObjectColor = mapObject.getColorValue(MapObjectProperty.LIGHT_COLOR);
        if (mapObjectColor != null) {
          borderColor = new Color(mapObjectColor.getRed(), mapObjectColor.getGreen(),
            mapObjectColor.getBlue(), 255);
        }
      }
    } else if (type == MapObjectType.STATICSHADOW) {
      borderColor = Style.COLOR_SHADOW_BORDER;
    } else if (type == MapObjectType.SPAWNPOINT) {
      borderColor = Style.COLOR_SPAWNPOINT;
    } else {
      borderColor = defaultBorderColor;
    }

    if (renderBaseShape) {
      g.setColor(borderColor);
      Shape bounds = baseBounds;
      if (mapObject.isEllipse()) {
        bounds = mapObject.getEllipse();
      } else if (mapObject.isPolyline() || mapObject.isPolygon()) {
        bounds = MapUtilities.convertPolyshapeToPath(mapObject);
      }
      Game.graphics().renderOutline(g, bounds, shapeStroke, true);

      g.setColor(fillColor);
      if (type != MapObjectType.LIGHTSOURCE && !mapObject.isPolyline()) {
        Game.graphics().renderShape(g, bounds, true);
      }
    }

    if (type == MapObjectType.SOUNDSOURCE) {
      Game.graphics().renderOutline(g,
        new Ellipse2D.Double(baseBounds.getCenterX() - soundRange,
          baseBounds.getCenterY() - soundRange, soundRange * 2d, soundRange * 2d),
        soundRangeStroke,
        true);
    }

    if (renderNames && renderBaseShape) {
      renderName(g, mapObject, baseBounds, nameFont, nameFontMetrics, viewport, renderScale);
    }
  }

  private static void renderCollisionBox(Graphics2D g, IMapObject mapObject, MapObjectType type,
    Rectangle2D collisionBox, BasicStroke shapeStroke, Stroke noCollisionStroke) {
    final boolean collision = type == MapObjectType.COLLISIONBOX
      || mapObject.getBoolValue(MapObjectProperty.COLLISION, false);

    g.setColor(Style.COLOR_COLLISION_FILL);
    Game.graphics().renderShape(g, collisionBox);
    g.setColor(collision ? Style.COLOR_COLLISION_BORDER : Style.COLOR_NOCOLLISION_BORDER);
    Game.graphics().renderOutline(g, collisionBox, collision ? shapeStroke : noCollisionStroke);
  }

  private static void renderName(Graphics2D g, IMapObject mapObject, Rectangle2D bounds,
    Font font, FontMetrics fontMetrics, Rectangle2D viewport, float renderScale) {
    String objectName = mapObject.getName();
    if (objectName == null || objectName.isEmpty()) {
      return;
    }
    objectName = truncateName(objectName);
    g.setFont(font);
    final double padding = fontMetrics.getHeight() / 3d;
    final double textWidth = fontMetrics.stringWidth(objectName);
    final double screenCenterX = (bounds.getCenterX() - viewport.getX()) * renderScale;
    final double screenBottomY = (bounds.getMaxY() - viewport.getY()) * renderScale;

    final double textScreenX = screenCenterX - textWidth / 2d;
    final double textScreenY = screenBottomY + padding + fontMetrics.getHeight() / 2d
      + fontMetrics.getAscent();

    RoundRectangle2D rect = new RoundRectangle2D.Double(
      textScreenX - padding,
      textScreenY - fontMetrics.getAscent() - padding,
      textWidth + padding * 2.5,
      fontMetrics.getHeight() + padding * 2,
      3, 3);

    g.setColor(Style.COLOR_DARKBORDER);
    g.fill(rect);
    g.setColor(Style.COLOR_STATUS);
    TextRenderer.render(g, objectName, textScreenX, textScreenY, true);
  }

  private static String truncateName(String value) {
    if (value == null || value.length() < 4) {
      return value;
    }

    if (value.length() < MAX_NAME_DISPLAY_LENGTH) {
      return value;
    }

    return value.substring(0, MAX_NAME_DISPLAY_LENGTH - 1) + "...";
  }
}
