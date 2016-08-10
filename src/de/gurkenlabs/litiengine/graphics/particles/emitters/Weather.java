/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.tiled.tmx.WeatherType;

/**
 * The Class WeatherEmitter.
 */
public abstract class Weather extends Emitter {

  /** The Constant WeatherEffectStartingX. */
  public static final int WeatherEffectStartingX = 0;

  /** The Constant WeatherEffectStartingY. */
  public static final int WeatherEffectStartingY = -50;

  private final WeatherType type;

  /** The screen dimensions. */
  private Dimension screenDimensions;

  /**
   * Instantiates a new weather emitter.
   */
  public Weather(final WeatherType type) {
    super((int) -Game.getScreenManager().getCamera().getCenterX(), (int) -Game.getScreenManager().getCamera().getCenterY() + WeatherEffectStartingY);
    this.type = type;
    this.screenDimensions = new Dimension((int) (Game.getScreenManager().getResolution().getWidth() / Game.getInfo().renderScale()), (int) (Game.getScreenManager().getResolution().getHeight() / Game.getInfo().renderScale()));
    Game.getScreenManager().onResolutionChanged(resolution -> this.resolutionChanged());
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return Game.getScreenManager().getCamera().getViewPort();
  }

  /**
   * Gets the screen dimensions.
   *
   * @return the screen dimensions
   */
  public Dimension getScreenDimensions() {
    return this.screenDimensions;
  }

  public WeatherType getType() {
    return this.type;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#particleCanBeRemoved(de.
   * gurkenlabs.liti.graphics.particles.Particle)
   */
  @Override
  protected boolean particleCanBeRemoved(final Particle particle) {
    // since our weather effects are top-right-to-bottom-left, we need to remove
    // particles that have reached the bottom left end
    final Point2D renderLocation = particle.getLocation(this.getOrigin());
    if (renderLocation.getX() < 0 || renderLocation.getY() > this.getScreenDimensions().height) {
      return true;
    }

    return super.particleCanBeRemoved(particle);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.screens.IResolutionObserver#resolutionChanged()
   */
  public void resolutionChanged() {
    // Update all particles to work properly with the new screen dimensions.
    final Dimension oldDimension = this.screenDimensions;
    this.screenDimensions = new Dimension((int) (Game.getScreenManager().getResolution().getWidth() / Game.getInfo().renderScale()), (int) (Game.getScreenManager().getResolution().getHeight() / Game.getInfo().renderScale()));
    for (final Particle p : this.getParticles()) {
      this.updateForNewScreenDimensions(p, oldDimension);
    }
  }

  /**
   * Updates, nearly, every variable of the effect to, hopefully, match-up with
   * the new screen dimensions that the effect is to be drawn onto.
   *
   * @param particle
   *          the particle
   * @param oldScreenDimension
   *          The current screen dimensions for the screen on which the particle
   *          is to be drawn.
   */
  private void updateForNewScreenDimensions(final Particle particle, final Dimension oldScreenDimension) {
    final short widthCurrent = (short) oldScreenDimension.width;
    final short heightCurrent = (short) oldScreenDimension.height;
    final short widthNew = (short) this.getScreenDimensions().width;
    final short heightNew = (short) this.getScreenDimensions().height;

    // Calculate the % difference between the widths and heights.
    final float widthDifference = widthNew / (float) widthCurrent;
    final float heightDifference = heightNew / (float) heightCurrent;

    // Change the effect's coordinates, velocity, gravitational pull, size, and
    // life.
    particle.setxCurrent(particle.getxCurrent() * widthDifference);
    particle.setyCurrent(particle.getyCurrent() * heightDifference);
    particle.setDx(particle.getDx() * widthDifference);
    particle.setDy(particle.getDy() * heightDifference);
    particle.setDeltaIncX(particle.getGravityX() * widthDifference);
    particle.setDeltaIncY(particle.getGravityY() * heightDifference);
  }
}
