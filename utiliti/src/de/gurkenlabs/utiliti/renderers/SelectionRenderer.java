package de.gurkenlabs.utiliti.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.handlers.Zoom;

public class SelectionRenderer implements IEditorRenderer {
  private Color colorSelectionBorder;
  private float selectionBorderBrightness = 0;
  private boolean selectionBorderBrightnessIncreasing = true;

  @Override
  public String getName() {
    return "SELECTION";
  }

  @Override
  public void render(Graphics2D g) {
    this.updateSelectionColor();

    for (IMapObject mapObject : Editor.instance().getMapComponent().getSelectedMapObjects()) {
      renderObjectId(g, mapObject);

      if (mapObject.equals(Editor.instance().getMapComponent().getFocusedMapObject())) {
        continue;
      }

      Stroke stroke = new BasicStroke(1 / Game.world().camera().getRenderScale());

      g.setColor(colorSelectionBorder);
      Game.graphics().renderOutline(g, mapObject.getBoundingBox(), stroke);
    }
  }

  private void updateSelectionColor() {
    if (this.selectionBorderBrightness <= 0.4) {
      this.selectionBorderBrightnessIncreasing = true;
    } else if (this.selectionBorderBrightness >= 0.9) {
      this.selectionBorderBrightnessIncreasing = false;
    }

    if (this.selectionBorderBrightnessIncreasing && this.selectionBorderBrightness < 0.9) {
      this.selectionBorderBrightness += 0.01;
    } else if (!selectionBorderBrightnessIncreasing && this.selectionBorderBrightness >= 0.4) {
      this.selectionBorderBrightness -= 0.01;
    }

    this.colorSelectionBorder = Color.getHSBColor(0, 0, this.selectionBorderBrightness);
  }

  private static void renderObjectId(Graphics2D g, IMapObject mapObject) {
    if (!Editor.preferences().renderMapIds()) {
      return;
    }

    Font previousFont = g.getFont();
    Font idFont = previousFont.deriveFont(Math.max(8f, (float) (10 * Math.sqrt(Game.world().camera().getRenderScale()))));
    if (Zoom.get() > 1) {
      idFont = idFont.deriveFont(Font.BOLD);
    }

    Point2D loc = Game.world().camera().getViewportLocation(new Point2D.Double(mapObject.getX() + mapObject.getWidth() / 2, mapObject.getY()));
    g.setColor(Style.COLOR_STATUS);

    g.setFont(idFont);
    String id = Integer.toString(mapObject.getId());

    double x = loc.getX() * Game.world().camera().getRenderScale() - g.getFontMetrics().stringWidth(id) / 2.0;
    double y = loc.getY() * Game.world().camera().getRenderScale() - (g.getFontMetrics().getHeight() * .30);

    if (Zoom.get() < 1) {
      TextRenderer.render(g, id, x, y);
    } else {
      TextRenderer.renderWithOutline(g, id, x, y, Style.COLOR_DARKBORDER, 5, true);
    }

    g.setFont(previousFont);
  }
}