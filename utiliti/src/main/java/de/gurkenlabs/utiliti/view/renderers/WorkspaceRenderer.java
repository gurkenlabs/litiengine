package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

/** Draws the editor workspace and separates finite map content from the surrounding canvas. */
public final class WorkspaceRenderer {
  private WorkspaceRenderer() {}

  public static void renderBackground(Graphics2D graphics) {
    int width = Game.window().getRenderComponent().getWidth();
    int height = Game.window().getRenderComponent().getHeight();
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setPaint(new GradientPaint(0, 0, Style.workspaceTop(), 0, Math.max(1, height), Style.workspaceBottom()));
      g.fillRect(0, 0, width, height);

      Rectangle2D mapBounds = getMapScreenBounds();
      if (mapBounds == null) {
        return;
      }
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      for (int offset = 10; offset >= 2; offset -= 2) {
        int alpha = Math.max(8, 34 - offset * 2);
        g.setColor(new Color(0, 0, 0, alpha));
        g.setStroke(new BasicStroke(offset));
        g.draw(mapBounds);
      }
      IMap map = Game.world().environment().getMap();
      g.setColor(map.getBackgroundColor() != null ? map.getBackgroundColor() : Style.mapBacking());
      g.fill(mapBounds);
    } finally {
      g.dispose();
    }
  }

  public static void renderMapBounds(Graphics2D graphics) {
    Rectangle2D mapBounds = getMapScreenBounds();
    if (mapBounds == null) {
      return;
    }
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(Style.mapBorder());
      g.setStroke(new BasicStroke(1f));
      g.draw(mapBounds);
    } finally {
      g.dispose();
    }
  }

  static Rectangle2D getMapScreenBounds() {
    IMap map = currentMap();
    return map != null ? EditorRenderHelper.toScreen(map.getBounds()) : null;
  }

  private static IMap currentMap() {
    return Game.world() != null && Game.world().environment() != null
        ? Game.world().environment().getMap()
        : null;
  }
}
