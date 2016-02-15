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

  public RectangleFillParticle(float xCurrent, float yCurrent, float dx, float dy, float deltaIncX, float deltaIncY, float width, float height, int life, Color color) {
    super(xCurrent, yCurrent, dx, dy, deltaIncX, deltaIncY, width, height, life, color);
  }

  @Override
  public void render(Graphics2D g, Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    g.fill(new Rectangle2D.Double(renderLocation.getX(), renderLocation.getY(), this.getWidth(), this.getHeight()));
  }
}