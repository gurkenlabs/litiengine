package com.litiengine.utiliti.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.litiengine.Game;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.components.MapComponent;
import com.litiengine.utiliti.Style;

public class MouseSelectAreaRenderer implements IEditorRenderer {
  private final BasicStroke shapeStroke = new BasicStroke(1 / Game.world().camera().getRenderScale());
  private final Color fillColor;
  private final Color borderColor;

  public MouseSelectAreaRenderer() {
    this(Style.COLOR_MOUSE_SELECTION_AREA_FILL, Style.COLOR_MOUSE_SELECTION_AREA_BORDER);
  }

  protected MouseSelectAreaRenderer(Color fillColor, Color borderColor) {
    this.fillColor = fillColor;
    this.borderColor = borderColor;
  }

  @Override
  public String getName() {
    return "MOUSE_SELECT_AREA";
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D rect = Editor.instance().getMapComponent().getMouseSelectArea(false);
    if (rect == null || Editor.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_EDIT) {
      return;
    }

    renderSelectArea(g, rect);
  }

  protected void renderSelectArea(Graphics2D g, Rectangle2D rect) {
    // draw mouse selection area
    g.setColor(this.fillColor);
    Game.graphics().renderShape(g, rect);
    g.setColor(this.borderColor);
    Game.graphics().renderOutline(g, rect, this.shapeStroke);
  }
}
