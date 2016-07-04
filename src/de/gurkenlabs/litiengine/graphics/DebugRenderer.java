package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;

public class DebugRenderer {
  public static void renderEntityDebugInfo(final Graphics2D g, final IEntity entity) {
    if (!Game.getConfiguration().DEBUG.isDebugEnabled()) {
      return;
    }

    if (Game.getConfiguration().DEBUG.renderEntityNames()) {
      drawMapId(g, entity);
    }

    if (Game.getConfiguration().DEBUG.renderHitBoxes() && entity instanceof ICombatEntity) {
      g.setColor(Color.RED);
      RenderEngine.drawShape(g, ((ICombatEntity)entity).getHitBox());
    }
    
    if (Game.getConfiguration().DEBUG.renderBoundingBoxes()) {
      g.setColor(Color.RED);
      RenderEngine.drawShape(g, entity.getBoundingBox());
    }

    if (Game.getConfiguration().DEBUG.renderCollisionBoxes() && entity instanceof ICollisionEntity) {
      final ICollisionEntity collisionEntity = (ICollisionEntity) entity;
      if (collisionEntity.hasCollision()) {
        g.setColor(Color.RED);
        RenderEngine.drawShape(g, collisionEntity.getCollisionBox());
      }
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
    final int x = (int) Game.getScreenManager().getCamera().getViewPortDimensionCenter(entity).getX() + 10;
    final int y = (int) Game.getScreenManager().getCamera().getViewPortDimensionCenter(entity).getY();
    RenderEngine.drawText(g, entity.getMapId() + "", x, y);
    final String locationString = "[x:" + new DecimalFormat("##.##").format(entity.getLocation().getX()) + ";y:" + new DecimalFormat("##.##").format(entity.getLocation().getY()) + "]";
    RenderEngine.drawText(g, locationString, x, y + 5);
  }
}
