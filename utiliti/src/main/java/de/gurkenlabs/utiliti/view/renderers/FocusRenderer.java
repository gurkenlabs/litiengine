package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

public class FocusRenderer implements IEditorRenderer {

  @Override
  public String getName() {
    return "FOCUS";
  }

  @Override
  public void render(Graphics2D g) {
    // render the focus and the transform rects
    final Rectangle2D focus = Editor.instance().getMapComponent().getFocusBounds();
    final IMapObject focusedMapObject = Editor.instance().getMapComponent().getFocusedMapObject();
    if (focus != null && focusedMapObject != null) {
      final float strokeSize =
          (float) Math.max(1, Math.log(Game.world().camera().getRenderScale()) * 4);
      final float dashPhaseBlack =
          (float) ((Game.time().now() / 15f)
              * Math.max(1, Math.sqrt(Game.world().camera().getRenderScale())));
      final float dashPhaseWhite = dashPhaseBlack + strokeSize;
      Stroke stroke =
          new BasicStroke(
              1,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_MITER,
              strokeSize,
              new float[] {strokeSize, strokeSize},
              dashPhaseBlack);

      g.setColor(Color.BLACK);

      Game.graphics().renderOutline(g, focus, stroke);

      Stroke whiteStroke =
          new BasicStroke(
              1,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_MITER,
              strokeSize,
              new float[] {strokeSize, strokeSize},
              dashPhaseWhite);
      g.setColor(Color.WHITE);
      Game.graphics().renderOutline(g, focus, whiteStroke);

      // render transform rects
      if (Editor.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_MOVE) {
        Stroke transStroke = new BasicStroke(1);
        for (Rectangle2D trans : Transform.getAnchors()) {
          g.setColor(Style.COLOR_TRANSFORM_RECT_FILL);
          Game.graphics().renderShape(g, trans);
          g.setColor(Color.BLACK);
          Game.graphics().renderOutline(g, trans, transStroke);
        }
      }
    }
  }
}
