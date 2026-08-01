package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class FocusRenderer implements IEditorRenderer {
  private static final Color SOUND_FOCUS_FILL = new Color(
    Style.COLOR_ACCENT_CYAN.getRed(),
    Style.COLOR_ACCENT_CYAN.getGreen(),
    Style.COLOR_ACCENT_CYAN.getBlue(),
    18);
  private static final Stroke SOUND_FOCUS_HALO_STROKE = new BasicStroke(
    6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
    new float[] {12.0f, 8.0f}, 0.0f);
  private static final Stroke SOUND_FOCUS_STROKE = new BasicStroke(
    3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
    new float[] {12.0f, 8.0f}, 0.0f);

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
      if (MapObjectType.get(focusedMapObject.getType()) == MapObjectType.SOUNDSOURCE) {
        renderSoundFocus(
          g,
          focus,
          screenFocus,
          focusedMapObject.getIntValue(MapObjectProperty.SOUND_RANGE, 0));
      }

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

  private static void renderSoundFocus(
    Graphics2D g, Rectangle2D focus, Rectangle2D screenFocus, int soundRange) {
    Color oldColor = g.getColor();
    Stroke oldStroke = g.getStroke();
    Object oldAntialiasing = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (soundRange > 0) {
        Rectangle2D screenRange = EditorRenderHelper.toScreen(
          MapObjectsRenderer.soundRangeBounds(focus, soundRange));
        Ellipse2D range = new Ellipse2D.Double(
          screenRange.getX(),
          screenRange.getY(),
          screenRange.getWidth(),
          screenRange.getHeight());
        g.setColor(SOUND_FOCUS_FILL);
        g.fill(range);
        g.setColor(Style.COLOR_SOUND_RANGE_HALO);
        g.setStroke(SOUND_FOCUS_HALO_STROKE);
        g.draw(range);
        g.setColor(Style.COLOR_ACCENT_CYAN);
        g.setStroke(SOUND_FOCUS_STROKE);
        g.draw(range);
      }

      renderSoundOrigin(g, screenFocus.getCenterX(), screenFocus.getCenterY());
    } finally {
      g.setColor(oldColor);
      g.setStroke(oldStroke);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
    }
  }

  private static void renderSoundOrigin(Graphics2D g, double centerX, double centerY) {
    double outerRadius = 11;
    double innerRadius = 7;
    g.setColor(new Color(12, 18, 24, 220));
    g.fill(new Ellipse2D.Double(
      centerX - outerRadius,
      centerY - outerRadius,
      outerRadius * 2,
      outerRadius * 2));
    g.setColor(Style.COLOR_ACCENT_CYAN);
    g.setStroke(new BasicStroke(2.5f));
    g.draw(new Ellipse2D.Double(
      centerX - innerRadius,
      centerY - innerRadius,
      innerRadius * 2,
      innerRadius * 2));
    g.draw(new Line2D.Double(centerX - 15, centerY, centerX - 8, centerY));
    g.draw(new Line2D.Double(centerX + 8, centerY, centerX + 15, centerY));
    g.draw(new Line2D.Double(centerX, centerY - 15, centerX, centerY - 8));
    g.draw(new Line2D.Double(centerX, centerY + 8, centerX, centerY + 15));
    g.setColor(Color.WHITE);
    g.fill(new Ellipse2D.Double(centerX - 2, centerY - 2, 4, 4));
  }
}
