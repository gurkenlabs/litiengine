/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.Particle;

// TODO: Auto-generated Javadoc
/**
 * The Class RainEmitter.
 */
@EmitterInfo(maxParticles = 3000, spawnAmount = 50, particleMinTTL = 20000, particleMaxTTL = 30000)
public class RainEmitter extends WeatherEmitter {

  /** The Constant LIGHT_BLUE. */
  private static final Color LIGHT_BLUE = new Color(45, 166, 189, 100);

  /** The last camera focus. */
  private Point2D lastCameraFocus;

  /**
   * Creates a new Particle object.
   *
   * @return the particle
   */
  @Override
  public Particle createNewParticle() {
    final float xCoord = (float) (Math.random() * (this.getScreenDimensions().width * 2 + Game.getScreenManager().getCamera().getFocus().getX()) + this.getScreenDimensions().width);
    final float yCoord = (float) Game.getScreenManager().getCamera().getFocus().getY() + WeatherEmitter.WeatherEffectStartingY;
    final float delta = (float) (Math.random() * 2);
    final float dx = -delta;
    final float dy = delta;
    final float gravityX = -0.1f;
    final float gravityY = 0.1f;
    final byte size = (byte) (Math.random() * 20);
    final int life = this.getRandomParticleTTL();

    return new LeftLineParticle(xCoord, yCoord, dx, dy, gravityX, gravityY, (byte) (size / 2.5), size, life, LIGHT_BLUE);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#update()
   */
  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    if (this.lastCameraFocus != null) {
      final double cameraDeltaX = this.lastCameraFocus.getX() - Game.getScreenManager().getCamera().getFocus().getX();
      final double cameraDeltaY = this.lastCameraFocus.getY() - Game.getScreenManager().getCamera().getFocus().getY();
      this.getParticles().forEach(particle -> particle.setxCurrent(particle.getxCurrent() - (float) cameraDeltaX));
      this.getParticles().forEach(particle -> particle.setyCurrent(particle.getyCurrent() - (float) cameraDeltaY));
    }

    this.lastCameraFocus = Game.getScreenManager().getCamera().getFocus();
  }
}
