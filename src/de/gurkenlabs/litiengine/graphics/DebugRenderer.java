package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.Collision;

public class DebugRenderer {
  private static List<Consumer<MapDebugArgs>> mapDebugConsumer;

  private static List<EntityDebugRenderedListener> entityDebugListeners;

  static {
    mapDebugConsumer = new CopyOnWriteArrayList<>();
    entityDebugListeners = new CopyOnWriteArrayList<>();
  }

  private DebugRenderer() {
  }

  public static void addEntityDebugListener(EntityDebugRenderedListener listener) {
    entityDebugListeners.add(listener);
  }

  public static void removeEntityDebugListener(EntityDebugRenderedListener listener) {
    entityDebugListeners.remove(listener);
  }

  public static void onMapDebugRendered(Consumer<MapDebugArgs> cons) {
    mapDebugConsumer.add(cons);
  }

  public static void renderEntityDebugInfo(final Graphics2D g, final IEntity entity) {
    if (!Game.config().debug().isDebugEnabled()) {
      return;
    }

    if (Game.config().debug().renderEntityNames()) {
      drawMapId(g, entity);
    }

    if (Game.config().debug().renderHitBoxes() && entity instanceof ICombatEntity) {
      g.setColor(Color.RED);
      RenderEngine.renderOutline(g, ((ICombatEntity) entity).getHitBox());
    }

    if (Game.config().debug().renderBoundingBoxes()) {
      g.setColor(Color.RED);
      RenderEngine.renderOutline(g, entity.getBoundingBox());
    }

    if (Game.config().debug().renderCollisionBoxes() && entity instanceof ICollisionEntity) {
      final ICollisionEntity collisionEntity = (ICollisionEntity) entity;
      g.setColor(collisionEntity.hasCollision() ? Color.RED : Color.ORANGE);
      RenderEngine.renderOutline(g, collisionEntity.getCollisionBox());
    }

    for (EntityDebugRenderedListener listener : entityDebugListeners) {
      listener.entityRendered(g, entity);
    }
  }

  public static void renderMapDebugInfo(final Graphics2D g, final IMap map) {
    // draw collision boxes from shape layer
    if (Game.config().debug().renderCollisionBoxes()) {
      final BasicStroke shapeStroke = new BasicStroke(1 / Game.world().camera().getRenderScale());
      for (final Rectangle2D shape : Game.physics().getCollisionBoxes(Collision.STATIC)) {
        g.setColor(Color.RED);
        RenderEngine.renderOutline(g, shape, shapeStroke);
      }
    }

    if (Game.config().debug().showTilesMetric()) {
      // draw mouse tile info
      drawTileBoundingBox(g, map, Input.mouse().getMapLocation());
    }

    final MapDebugArgs args = new MapDebugArgs(map, g);
    for (Consumer<MapDebugArgs> cons : mapDebugConsumer) {
      cons.accept(args);
    }
  }

  /**
   * Draw name.
   *
   * @param g
   *          the g
   * @param entity
   *          the entity
   */
  private static void drawMapId(final Graphics2D g, final IEntity entity) {
    g.setColor(Color.RED);
    g.setFont(g.getFont().deriveFont(Font.PLAIN, 4f));
    final double x = Game.world().camera().getViewportDimensionCenter(entity).getX() + 10;
    final double y = Game.world().camera().getViewportDimensionCenter(entity).getY();
    TextRenderer.render(g, Integer.toString(entity.getMapId()), x, y);
    final String locationString = new DecimalFormat("##.##").format(entity.getX()) + ";" + new DecimalFormat("##.##").format(entity.getY());
    TextRenderer.render(g, locationString, x, y + 5.0);
  }

  private static void drawTileBoundingBox(final Graphics2D g, final IMap map, final Point2D location) {
    final Rectangle2D playerTile = map.getOrientation().getEnclosingTileShape(location, map).getBounds2D();

    // draw rect
    g.setColor(Color.CYAN);
    RenderEngine.renderOutline(g, playerTile);

    // draw coords
    final Point tileLocation = map.getOrientation().getTile(location, map);
    final String locationText = tileLocation.x + ", " + tileLocation.y;
    g.setFont(g.getFont().deriveFont(3f));
    final FontMetrics fm = g.getFontMetrics();
    final Point2D relative = Game.world().camera().getViewportLocation(playerTile.getX(), playerTile.getY());
    TextRenderer.render(g, locationText, (float) (relative.getX() + playerTile.getWidth() + 3), (float) (relative.getY() + fm.getHeight()));

    final List<ITile> tiles = MapUtilities.getTilesByPixelLocation(map, location);
    final StringBuilder sb = new StringBuilder();
    for (final ITile tile : tiles) {
      sb.append("[gid: " + tile.getGridId() + "] ");
    }

    TextRenderer.render(g, sb.toString(), (float) (relative.getX() + playerTile.getWidth() + 3), (float) (relative.getY() + fm.getHeight() * 2 + 2));
  }
}
