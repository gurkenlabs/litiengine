package de.gurkenlabs.utiliti.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.MapComponent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
        || Editor.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_CREATE) {
      return;
    }

    this.renderSelectArea(g, rect);

    g.setFont(g.getFont().deriveFont(Font.BOLD));

    FontMetrics fm = g.getFontMetrics();

    String width = rect.getWidth() + "";
    String height = rect.getHeight() + "";
    Game.graphics()
        .renderText(
            g,
            width,
            rect.getX() + rect.getWidth() / 2.0 - fm.stringWidth(width) / 2.0,
            rect.getY() - 5);
    Game.graphics()
        .renderText(
            g,
            height,
            rect.getX() - (fm.stringWidth(height) + 3),
            rect.getY() + rect.getHeight() / 2);
  }
}
