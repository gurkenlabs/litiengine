package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

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

      float scale = Game.world().camera().getRenderScale();
      Stroke stroke = new BasicStroke(1.5f / scale);

      g.setColor(colorSelectionBorder);
      java.awt.geom.Rectangle2D bb = mapObject.getBoundingBox();
      double arc = 4.0 / scale;
      g.draw(new RoundRectangle2D.Double(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight(), arc, arc));
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

    this.colorSelectionBorder = new Color(
      Style.COLOR_ACCENT_BLUE.getRed(),
      Style.COLOR_ACCENT_BLUE.getGreen(),
      Style.COLOR_ACCENT_BLUE.getBlue(),
      (int)(this.selectionBorderBrightness * 255));
  }

  private static void renderObjectId(Graphics2D g, IMapObject mapObject) {
    if (!Editor.preferences().renderMapIds()) {
      return;
    }

    Font previousFont = Style.FONT_BOLD;
    Font idFont =
        Objects.requireNonNull(previousFont).deriveFont(
            Math.max(8f, (float) (10 * Math.sqrt(Game.world().camera().getRenderScale())))
                * Editor.preferences().getUiScale());

    Point2D loc =
        Game.world()
            .camera()
            .getViewportLocation(
                new Point2D.Double(mapObject.getX() + mapObject.getWidth() / 2, mapObject.getY()));
    g.setColor(Style.COLOR_STATUS);

    g.setFont(idFont);
    String id = Integer.toString(mapObject.getId());

    double x =
        loc.getX() * Game.world().camera().getRenderScale()
            - g.getFontMetrics().stringWidth(id) / 2.0;
    double y =
        loc.getY() * Game.world().camera().getRenderScale() - (g.getFontMetrics().getHeight());
    TextRenderer.render(g, id, x, y, true);

    g.setFont(previousFont);
  }
}
