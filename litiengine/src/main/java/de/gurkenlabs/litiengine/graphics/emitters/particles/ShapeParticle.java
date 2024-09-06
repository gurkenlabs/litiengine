package de.gurkenlabs.litiengine.graphics.emitters.particles;

import de.gurkenlabs.litiengine.Game;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class ShapeParticle extends Particle {

  protected ShapeParticle(float width, float height) {
    super(width, height);
  }

  protected abstract Shape getShape(final Point2D emitterOrigin);

  @Override
  public Rectangle2D getBoundingBox(Point2D origin) {
    return getShape(origin).getBounds2D();
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    g.setColor(
      new Color(
        getColor().getRed() / 255f,
        getColor().getGreen() / 255f,
        getColor().getBlue() / 255f,
        getOpacity()));

    if (isOutlineOnly() || this instanceof LineParticle) {
      Game.graphics()
        .renderOutline(g, getShape(emitterOrigin), new BasicStroke(getOutlineThickness() / Game.graphics().getBaseRenderScale()), isAntiAliased());
    } else {
      Game.graphics().renderShape(g, getShape(emitterOrigin), isAntiAliased());
    }
  }
}
