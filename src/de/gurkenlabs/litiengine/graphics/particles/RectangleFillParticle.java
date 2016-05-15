/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.util.logging.Stopwatch;

/**
 * Represents a particle in 2D space.
 */
public class RectangleFillParticle extends Particle {

  public RectangleFillParticle(final float xCurrent, final float yCurrent, final float dx, final float dy, final float deltaIncX, final float deltaIncY, final float width, final float height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, deltaIncX, deltaIncY, width, height, life, color);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    g.fill(new Rectangle2D.Float((float) renderLocation.getX(), (float) renderLocation.getY(), this.getWidth(), this.getHeight()));
  }
}