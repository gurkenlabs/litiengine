/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
    g.fillRect((int)renderLocation.getX(), (int)renderLocation.getY(), (int)this.getWidth(), (int) this.getHeight());
  }
}