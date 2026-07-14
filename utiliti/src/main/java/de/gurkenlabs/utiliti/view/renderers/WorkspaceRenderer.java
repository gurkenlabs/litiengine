package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

/** Draws the editor workspace and separates finite map content from the surrounding canvas. */
public final class WorkspaceRenderer {
  private static final BasicStroke[] SHADOW_STROKES = {
    new BasicStroke(10),
    new BasicStroke(8),
    new BasicStroke(6),
    new BasicStroke(4),
    new BasicStroke(2)
  };
  private static final Color[] SHADOW_COLORS = {
    new Color(0, 0, 0, 14),
    new Color(0, 0, 0, 18),
    new Color(0, 0, 0, 22),
    new Color(0, 0, 0, 26),
    new Color(0, 0, 0, 30)
  };
  private static int gradientHeight = -1;
  private static Color gradientTop;
  private static Color gradientBottom;
  private static Paint workspaceGradient;

  private WorkspaceRenderer() {}

  public static void renderBackground(Graphics2D graphics) {
    int width = Game.window().getRenderComponent().getWidth();
    int height = Game.window().getRenderComponent().getHeight();
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setPaint(workspaceGradient(height));
      g.fillRect(0, 0, width, height);

      Rectangle2D mapBounds = getMapScreenBounds();
      if (mapBounds == null) {
        return;
      }
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      for (int index = 0; index < SHADOW_STROKES.length; index++) {
        g.setColor(SHADOW_COLORS[index]);
        g.setStroke(SHADOW_STROKES[index]);
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

  private static Paint workspaceGradient(int height) {
    Color top = Style.workspaceTop();
    Color bottom = Style.workspaceBottom();
    int resolvedHeight = Math.max(1, height);
    if (workspaceGradient == null
        || gradientHeight != resolvedHeight
        || !top.equals(gradientTop)
        || !bottom.equals(gradientBottom)) {
      gradientHeight = resolvedHeight;
      gradientTop = top;
      gradientBottom = bottom;
      workspaceGradient = new GradientPaint(0, 0, top, 0, resolvedHeight, bottom);
    }
    return workspaceGradient;
  }
}
