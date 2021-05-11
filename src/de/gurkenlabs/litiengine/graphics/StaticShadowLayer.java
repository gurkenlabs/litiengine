package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.Environment;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class StaticShadowLayer extends ColorLayer {

  /**
   * Instantiates a new {@code StaticShadowLayer} instance.
   *
   * @param environment The environment to which this instance is assigned.
   * @param color The color of this instance.
   */
  public StaticShadowLayer(Environment environment, Color color) {
    super(environment, color);
  }

  @Override
  protected void renderSection(Graphics2D g, Rectangle2D section) {
    final Color color = this.getColor();
    g.setColor(color);

    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    final Area ar = new Area();
    for (final StaticShadow staticShadow : this.getEnvironment().getStaticShadows()) {
      if (!staticShadow.getBoundingBox().intersects(section)
          || staticShadow.getShadowType() == StaticShadowType.NONE) {
        continue;
      }

      final Area staticShadowArea = staticShadow.getArea();
      ar.add(staticShadowArea);
    }

    ar.transform(AffineTransform.getTranslateInstance(-section.getX(), -section.getY()));
    g.fill(ar);
  }

  @Override
  protected void clearSection(Graphics2D g, Rectangle2D section) {
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
    g.fill(section);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
  }
}
