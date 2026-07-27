package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
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
    if (focus != null && focusedMapObject != null
        && MapComponent.isLayerEffectivelyVisible(
            Game.world().environment().getMap(), focusedMapObject.getLayer())) {
      float renderScale = Game.world().camera().getRenderScale();
      Rectangle2D screenFocus = EditorRenderHelper.toScreen(focus);

      // High-contrast glowing outline for small zoom levels
      if (renderScale < 0.6f) {
        g.setColor(new Color(255, 220, 0, 200));
        g.setStroke(new BasicStroke(3.0f));
        g.draw(new Rectangle2D.Double(screenFocus.getX() - 2, screenFocus.getY() - 2, screenFocus.getWidth() + 4, screenFocus.getHeight() + 4));
      }

      final float strokeSize = (float) Math.max(3, Math.log(renderScale) * 4);
      final float dashPhaseBlack =
          (float) ((Game.time().now() / 15f)
              * Math.max(1, Math.sqrt(renderScale)));
      final float dashPhaseWhite = dashPhaseBlack + strokeSize;
      float lineWidth = renderScale < 0.5f ? 2.5f : 1.5f;

      Stroke stroke =
          new BasicStroke(
              lineWidth,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_MITER,
              strokeSize,
              new float[] {strokeSize, strokeSize},
              dashPhaseBlack);

      g.setColor(Color.BLACK);
      g.setStroke(stroke);
      g.draw(screenFocus);

      Stroke whiteStroke =
          new BasicStroke(
              lineWidth,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_MITER,
              strokeSize,
              new float[] {strokeSize, strokeSize},
              dashPhaseWhite);
      g.setColor(Color.WHITE);
      g.setStroke(whiteStroke);
      g.draw(screenFocus);

      // render transform rects (not when in MOVE mode)
      if (Editor.instance().getMapComponent().getTransformMode() != TransformMode.MOVE) {
        Stroke transStroke = new BasicStroke(1.5f);
        for (Rectangle2D trans : Transform.getAnchors()) {
          Rectangle2D screenTrans = EditorRenderHelper.toScreen(trans);
          double anchorSize = Math.max(screenTrans.getWidth(), 7.0);
          double anchorX = screenTrans.getCenterX() - anchorSize / 2.0;
          double anchorY = screenTrans.getCenterY() - anchorSize / 2.0;
          Rectangle2D drawAnchor = new Rectangle2D.Double(anchorX, anchorY, anchorSize, anchorSize);

          g.setColor(Style.COLOR_TRANSFORM_RECT_FILL);
          g.fill(drawAnchor);
          g.setColor(Color.BLACK);
          g.setStroke(transStroke);
          g.draw(drawAnchor);
        }
      }
    }
  }
}
