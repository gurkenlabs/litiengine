package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.environment.Environment;

public class StaticShadowLayer extends ColorLayer {
  public StaticShadowLayer(Environment env, int alpha, Color color) {
    super(env, color, alpha);
  }

  @Override
  protected void renderSection(Graphics2D g, Rectangle2D section) {
    final Color colorWithAlpha = this.getColorWithAlpha();
    g.setColor(colorWithAlpha);

    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    final Area ar = new Area();
    for (final StaticShadow staticShadow : this.getEnvironment().getStaticShadows()) {
      if (!staticShadow.getBoundingBox().intersects(section) || staticShadow.getShadowType() == StaticShadowType.NONE) {
        continue;
      }

      final Area staticShadowArea = staticShadow.getArea();
      ar.add(staticShadowArea);
    }

    ar.transform(AffineTransform.getTranslateInstance(-section.getX(), -section.getY()));
    g.fill(ar);
  }
}
