package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;

import de.gurkenlabs.litiengine.environment.IEnvironment;

public class StaticShadowLayer extends ColorLayer {
  public StaticShadowLayer(IEnvironment env, int alpha, Color color) {
    super(env, color, alpha);
  }

  @Override
  protected void renderLayer(Graphics2D g) {
    final Color colorWithAlpha = this.getColorWithAlpha();
    g.setColor(colorWithAlpha);

    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    final Area ar = new Area();
    for (final StaticShadow staticShadow : this.getEnvironment().getStaticShadows()) {
      if (staticShadow.getShadowType() == StaticShadowType.NONE) {
        continue;
      }

      final Area staticShadowArea = staticShadow.getArea();
      ar.add(staticShadowArea);
    }

    g.fill(ar);
  }

  @Override
  protected String getCacheKey() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getColor());
    sb.append(this.getAlpha());

    for (final StaticShadow shadow : this.getEnvironment().getStaticShadows()) {
      sb.append(shadow.getShadowType());
      sb.append(shadow.getLocation());
      sb.append(shadow.getWidth());
      sb.append(shadow.getHeight());
    }

    sb.append(this.getEnvironment().getMap().getSizeInPixels());

    final int key = sb.toString().hashCode();
    return "staticshadow-" + this.getEnvironment().getMap().getFileName() + "-" + Integer.toString(key);
  }
}
