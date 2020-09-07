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

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EntityRenderEvent;
import de.gurkenlabs.litiengine.entities.EntityRenderedListener;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.Collision;

/**
 * The {@code DebugRenderer} class implements default debug rendering and exposes extension points to reder your own
 * debug information via callbacks.
 */
public final class DebugRenderer {
  private static List<MapRenderedListener> mapDebugListener;
  private static List<EntityRenderedListener> entityDebugListeners;

  static {
    mapDebugListener = new CopyOnWriteArrayList<>();
    entityDebugListeners = new CopyOnWriteArrayList<>();
  }

  private DebugRenderer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Add the specified entity rendered listener to attach custom debug rendering after the default debug information for an entity has been rendered.
   * 
   * @param listener
   *          The listener to add.
   */
  public static void addEntityDebugListener(EntityRenderedListener listener) {
    entityDebugListeners.add(listener);
  }

  /**
   * Removes the specified entity rendered listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public static void removeEntityDebugListener(EntityRenderedListener listener) {
    entityDebugListeners.remove(listener);
  }

  /**
   * Add the specified map rendered listener to attach custom debug rendering after layers of the type {@code GROUND} have beend rendered.
   * 
   * @param listener
   *          The listener to add.
   * 
   * @see RenderType#GROUND
   * @see Environment#render(Graphics2D)
   */
  public static void addMapRenderedListener(MapRenderedListener listener) {
    mapDebugListener.add(listener);
  }

  /**
   * Removes the specified map rendered listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public static void removeMapRenderedListener(MapRenderedListener listener) {
    mapDebugListener.remove(listener);
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
      Game.graphics().renderOutline(g, ((ICombatEntity) entity).getHitBox());
    }

    if (Game.config().debug().renderBoundingBoxes()) {
      g.setColor(Color.RED);
      Game.graphics().renderOutline(g, entity.getBoundingBox());
    }

    if (Game.config().debug().renderCollisionBoxes() && entity instanceof ICollisionEntity) {
      final ICollisionEntity collisionEntity = (ICollisionEntity) entity;
      g.setColor(collisionEntity.hasCollision() ? Color.RED : Color.ORANGE);
      Game.graphics().renderOutline(g, collisionEntity.getCollisionBox());
    }

    final EntityRenderEvent event = new EntityRenderEvent(g, entity);
    for (EntityRenderedListener listener : entityDebugListeners) {
      listener.rendered(event);
    }
  }

  public static void renderMapDebugInfo(final Graphics2D g, final IMap map) {
    if (!Game.config().debug().isDebugEnabled()) {
      return;
    }
    
    // draw collision boxes from shape layer
    if (Game.config().debug().renderCollisionBoxes()) {
      final BasicStroke shapeStroke = new BasicStroke(1 / Game.world().camera().getRenderScale());
      for (final Rectangle2D shape : Game.physics().getCollisionBoxes(Collision.STATIC)) {
        g.setColor(Color.RED);
        Game.graphics().renderOutline(g, shape, shapeStroke);
      }
    }

    if (Game.config().debug().showTilesMetric()) {
      // draw mouse tile info
      drawTileBoundingBox(g, map, Input.mouse().getMapLocation());
    }

    final MapRenderedEvent event = new MapRenderedEvent(g, map);
    for (MapRenderedListener cons : mapDebugListener) {
      cons.rendered(event);
    }
  }

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
    Game.graphics().renderOutline(g, playerTile);

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
