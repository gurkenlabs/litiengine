package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class MouseSelectAreaRenderer implements IEditorRenderer {
  private final BasicStroke shapeStroke =
      new BasicStroke(1);
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
    if (rect == null
      || Editor.instance().getMapComponent().getTransformMode() != TransformMode.NONE) {
      return;
    }

    renderSelectArea(g, rect);
  }

  protected void renderSelectArea(Graphics2D g, Rectangle2D rect) {
    Rectangle2D screenRect = EditorRenderHelper.toScreen(rect);
    // draw mouse selection area
    g.setColor(this.fillColor);
    g.fill(screenRect);
    g.setColor(this.borderColor);
    g.setStroke(this.shapeStroke);
    g.draw(screenRect);
  }
}
