package de.gurkenlabs.utiliti.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.handlers.Transform;

public class FocusRenderer implements IEditorRenderer {

  @Override
  public String getName() {
    return "FOCUS";
  }

  @Override
  public void render(Graphics2D g) {
    // render the focus and the transform rects
    final Rectangle2D focus = EditorScreen.instance().getMapComponent().getFocusBounds();
    final IMapObject focusedMapObject = EditorScreen.instance().getMapComponent().getFocusedMapObject();
    if (focus != null && focusedMapObject != null) {
      Stroke stroke = new BasicStroke(1 / Game.world().camera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4, new float[] { 1f, 1f }, Game.time().now() / 15);

      g.setColor(Color.BLACK);

      RenderEngine.renderOutline(g, focus, stroke);

      Stroke whiteStroke = new BasicStroke(1 / Game.world().camera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4, new float[] { 1f, 1f }, Game.time().now() / 15.0f - 1f);
      g.setColor(Color.WHITE);
      RenderEngine.renderOutline(g, focus, whiteStroke);

      // render transform rects
      if (EditorScreen.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_MOVE) {
        Stroke transStroke = new BasicStroke(1 / Game.world().camera().getRenderScale());
        for (Rectangle2D trans : Transform.getAnchors()) {
          g.setColor(Style.COLOR_TRANSFORM_RECT_FILL);
          RenderEngine.renderShape(g, trans);
          g.setColor(Color.BLACK);
          RenderEngine.renderOutline(g, trans, transStroke);
        }
      }
    }
  }
}