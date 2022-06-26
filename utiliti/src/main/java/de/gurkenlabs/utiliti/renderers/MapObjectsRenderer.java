package de.gurkenlabs.utiliti.renderers;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.MapComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class MapObjectsRenderer implements IEditorRenderer {
  private static final int MAX_NAME_DISPLAY_LENGTH = 50;

  @Override
  public String getName() {
    return "MAPOBJECTS";
  }

  @Override
  public void render(Graphics2D g) {
    if (!Editor.preferences().renderBoundingBoxes()) {
      return;
    }

    if (MapComponent.mapIsNull()) {
      return;
    }

    final List<IMapObjectLayer> layers = Game.world().environment().getMap().getMapObjectLayers();
    // render all entities
    for (final IMapObjectLayer layer : layers) {
      if (layer == null || !layer.isVisible()) {
        continue;
      }

      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        MapObjectType type = MapObjectType.get(mapObject.getType());
        final BasicStroke shapeStroke =
            new BasicStroke(1f / Game.world().camera().getRenderScale());
        if (type == null) {
          if (Editor.preferences().renderCustomMapObjects()) {
            renderUnsupportedMapObject(g, mapObject, shapeStroke);
          }

          continue;
        }

        // render spawn points
        if (type == MapObjectType.SPAWNPOINT) {
          g.setColor(Style.COLOR_SPAWNPOINT);
          Game.graphics()
              .renderShape(
                  g,
                  new Rectangle2D.Double(
                      mapObject.getBoundingBox().getCenterX() - 1,
                      mapObject.getBoundingBox().getCenterY() - 1,
                      2,
                      2));
        }

        if (type != MapObjectType.COLLISIONBOX) {
          Color colorBoundingBoxFill;
          if (layer.getColor() != null) {
            colorBoundingBoxFill =
                new Color(
                    layer.getColor().getRed(),
                    layer.getColor().getGreen(),
                    layer.getColor().getBlue(),
                    15);
          } else {
            colorBoundingBoxFill = Style.COLOR_DEFAULT_BOUNDING_BOX_FILL;
          }

          renderBoundingBox(g, mapObject, colorBoundingBoxFill, shapeStroke);
        }

        renderCollisionBox(g, mapObject, shapeStroke);
      }
    }
  }

  private static void renderUnsupportedMapObject(
      Graphics2D g, IMapObject mapObject, BasicStroke shapeStroke) {
    Color color =
        mapObject.getLayer().getColor() == null
            ? Style.COLOR_UNSUPPORTED
            : mapObject.getLayer().getColor();
    g.setColor(color);
    Point2D start =
        new Point2D.Double(mapObject.getLocation().getX(), mapObject.getLocation().getY());
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

      // found the path for the rat
      final Path2D path = MapUtilities.convertPolyshapeToPath(mapObject);
      if (path == null) {
        return;
      }

      Game.graphics().renderOutline(g, path, shapeStroke);
    } else if (mapObject.isPolygon()) {
      if (mapObject.getPolygon() == null || mapObject.getPolygon().getPoints().isEmpty()) {
        return;
      }

      // found the path for the rat
      final Path2D path = MapUtilities.convertPolyshapeToPath(mapObject);
      if (path == null) {
        return;
      }

      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      Game.graphics().renderShape(g, path);
      g.setColor(Style.COLOR_UNSUPPORTED);
      Game.graphics().renderOutline(g, path, shapeStroke);
    } else if (mapObject.isEllipse()) {
      if (mapObject.getEllipse() == null) {
        return;
      }
      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      Game.graphics().renderShape(g, mapObject.getEllipse());

      g.setColor(Style.COLOR_UNSUPPORTED);
      Game.graphics().renderOutline(g, mapObject.getEllipse(), shapeStroke);
    } else {
      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      Game.graphics().renderShape(g, mapObject.getBoundingBox());
      g.setColor(Style.COLOR_UNSUPPORTED);
      Game.graphics().renderOutline(g, mapObject.getBoundingBox(), shapeStroke);
    }
  }

  private static void renderBoundingBox(
      Graphics2D g, IMapObject mapObject, Color colorBoundingBoxFill, BasicStroke shapeStroke) {
    MapObjectType type = MapObjectType.get(mapObject.getType());
    Color fillColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      fillColor = Style.COLOR_TRIGGER_FILL;
    } else if (type == MapObjectType.STATICSHADOW) {
      fillColor = Style.COLOR_SHADOW_FILL;
    }

    // render bounding boxes
    g.setColor(fillColor);

    // don't fill rect for lightsource because it is important to judge
    // the color
    if (type != MapObjectType.LIGHTSOURCE) {
      Game.graphics().renderShape(g, mapObject.getBoundingBox());
    }

    Color borderColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      borderColor = Style.COLOR_TRIGGER_BORDER;
    } else if (type == MapObjectType.LIGHTSOURCE) {
      final String mapObjectColor = mapObject.getStringValue(MapObjectProperty.LIGHT_COLOR);
      if (mapObjectColor != null && !mapObjectColor.isEmpty()) {
        Color lightColor = ColorHelper.decode(mapObjectColor);
        borderColor =
            new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 255);
      }
    } else if (type == MapObjectType.STATICSHADOW) {
      borderColor = Style.COLOR_SHADOW_BORDER;
    } else if (type == MapObjectType.SPAWNPOINT) {
      borderColor = Style.COLOR_SPAWNPOINT;
    } else {
      borderColor =
          new Color(
              colorBoundingBoxFill.getRed(),
              colorBoundingBoxFill.getGreen(),
              colorBoundingBoxFill.getBlue(),
              150);
    }

    g.setColor(borderColor);

    Game.graphics().renderOutline(g, mapObject.getBoundingBox(), shapeStroke);
    if (type == MapObjectType.SOUNDSOURCE) {
      final int range = mapObject.getIntValue(MapObjectProperty.SOUND_RANGE);
      final float[] dash1 = {10.0f};
      final BasicStroke dashed =
          new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
      Game.graphics()
          .renderOutline(
              g,
              new Ellipse2D.Double(
                  mapObject.getBoundingBox().getCenterX() - range,
                  mapObject.getBoundingBox().getCenterY() - range,
                  range * 2d,
                  range * 2d),
              dashed);
    }

    if (Editor.preferences().renderNames()) {
      renderName(g, mapObject);
    }
  }

  private static void renderCollisionBox(
      Graphics2D g, IMapObject mapObject, BasicStroke shapeStroke) {
    // render collision boxes
    boolean collision = mapObject.getBoolValue(MapObjectProperty.COLLISION, false);
    float collisionBoxWidth = mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_WIDTH, -1);
    float collisionBoxHeight = mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_HEIGHT, -1);
    final Align align = Align.get(mapObject.getStringValue(MapObjectProperty.COLLISION_ALIGN));
    final Valign valign = Valign.get(mapObject.getStringValue(MapObjectProperty.COLLISION_VALIGN));

    if (MapObjectType.get(mapObject.getType()) == MapObjectType.COLLISIONBOX) {
      collisionBoxWidth = mapObject.getWidth();
      collisionBoxHeight = mapObject.getHeight();
      collision = true;
    }

    if (collisionBoxWidth != -1 && collisionBoxHeight != -1) {

      g.setColor(Style.COLOR_COLLISION_FILL);
      Rectangle2D collisionBox =
          CollisionEntity.getCollisionBox(
              mapObject.getLocation(),
              mapObject.getWidth(),
              mapObject.getHeight(),
              collisionBoxWidth,
              collisionBoxHeight,
              align,
              valign);

      Game.graphics().renderShape(g, collisionBox);
      g.setColor(collision ? Style.COLOR_COLLISION_BORDER : Style.COLOR_NOCOLLISION_BORDER);

      Stroke collisionStroke =
          collision
              ? shapeStroke
              : new BasicStroke(
                  1 / Game.world().camera().getRenderScale(),
                  BasicStroke.CAP_ROUND,
                  BasicStroke.JOIN_BEVEL,
                  0,
                  new float[] {1f},
                  0);
      Game.graphics().renderOutline(g, collisionBox, collisionStroke);
    }
  }

  private static void renderName(Graphics2D g, IMapObject mapObject) {
    g.setFont(Style.getDefaultFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));
    FontMetrics fm = g.getFontMetrics();

    String objectName = mapObject.getName();
    if (objectName != null && !objectName.isEmpty()) {
      objectName = truncateName(objectName);
      final int PADDING = 2;
      double stringWidth = fm.stringWidth(objectName) / Game.world().camera().getRenderScale();
      double stringHeight = fm.getHeight() * .5 / Game.world().camera().getRenderScale();
      double x = mapObject.getX() + ((mapObject.getWidth() - stringWidth) / 2.0) - PADDING;
      double y = mapObject.getY() + mapObject.getHeight() + stringHeight;
      double width = stringWidth + PADDING * 2;
      double height = stringHeight + PADDING * 2;
      RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 2, 2);
      g.setColor(new Color(0, 0, 0, 200));
      Game.graphics().renderShape(g, rect, true);

      g.setColor(Color.WHITE);
      Game.graphics().renderText(g, objectName, x + PADDING, rect.getMaxY() - PADDING, true);
    }
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
