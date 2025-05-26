package de.gurkenlabs.litiengine.graphics.emitters.particles;

import de.gurkenlabs.litiengine.Game;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Represents a particle with a specific shape.
 *
 * <p>This abstract class defines the behavior and rendering logic for particles
 * that are represented by geometric shapes. Subclasses must implement the {@link #getShape(Point2D)} method to define the specific shape of the
 * particle.
 */
public abstract class ShapeParticle extends Particle {

  /**
   * Creates a new shape particle with the specified dimensions.
   *
   * @param width  the width of the particle
   * @param height the height of the particle
   */
  protected ShapeParticle(float width, float height) {
    super(width, height);
  }

  /**
   * Retrieves the shape of the particle based on the emitter's origin.
   *
   * <p>This method must be implemented by subclasses to define the specific
   * shape of the particle.
   *
   * @param emitterOrigin the origin point of the emitter
   * @return the shape of the particle
   */
  protected abstract Shape getShape(final Point2D emitterOrigin);

  /**
   * Retrieves the bounding box of the particle.
   *
   * <p>The bounding box is calculated based on the particle's shape and the
   * specified origin point.
   *
   * @param origin the origin point of the emitter
   * @return the bounding box of the particle as a {@link Rectangle2D}
   */
  @Override
  public Rectangle2D getBoundingBox(Point2D origin) {
    return getShape(origin).getBounds2D();
  }

  /**
   * Renders the particle on the specified graphics context.
   *
   * <p>The rendering behavior depends on whether the particle is outline-only
   * or a specific subclass such as {@link LineParticle}. The particle's color, opacity, and anti-aliasing settings are applied during rendering.
   *
   * @param g             the graphics context to render on
   * @param emitterOrigin the origin point of the emitter
   */
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
