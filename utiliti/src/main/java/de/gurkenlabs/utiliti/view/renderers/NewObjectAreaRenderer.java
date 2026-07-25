package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class NewObjectAreaRenderer extends MouseSelectAreaRenderer {

  public NewObjectAreaRenderer() {
    super(Style.COLOR_NEWOBJECT_FILL, Style.COLOR_NEWOBJECT_BORDER);
  }

  @Override
  public String getName() {
    return "NEW_OBJECT_AREA";
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D rect = Editor.instance().getMapComponent().getMouseSelectArea(true);
    if (rect == null
      || Editor.instance().getMapComponent().getTransformMode() != TransformMode.CREATE) {
      return;
    }

    this.renderSelectArea(g, rect);

    g.setFont(g.getFont().deriveFont(Font.BOLD));

    FontMetrics fm = g.getFontMetrics();

    String width = rect.getWidth() + "";
    String height = rect.getHeight() + "";
    Point2D start = Game.world().camera().getViewportLocation(rect.getX(), rect.getY());
    double scale = Game.world().camera().getRenderScale();
    double x = start.getX() * scale;
    double y = start.getY() * scale;
    double w = rect.getWidth() * scale;
    double h = rect.getHeight() * scale;

    g.drawString(width, (float) (x + w / 2.0 - fm.stringWidth(width) / 2.0), (float) (y - 5));
    g.drawString(height, (float) (x - fm.stringWidth(height) - 3), (float) (y + h / 2.0));
  }
}
