package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;

/**
 * Represents a fire.
 */

@EntityInfo(width = 32, height = 64)
@EmitterInfo(maxParticles = 200, spawnAmount = 200, activateOnInit = true, particleMinTTL = 300, particleMaxTTL = 1200)
public class FireEmitter extends Emitter {
  /** One of the four colors used by the fire particles. */
  private static final Color DARK_ORANGE = new Color(208, 117, 29, 100);
  /** One of the four colors used by the fire particles. */
  private static final Color GOLD = new Color(246, 206, 72, 100);
  /** One of the four colors used by the fire particles. */
  private static final Color REDISH_BROWN = new Color(159, 70, 24, 100);
  /** One of the four colors used by the fire particles. */
  private static final Color YELLOW = new Color(251, 239, 169, 100);

  private static final int REDISH_BROWN_COUNT = 10;
  private static final int DARK_ORANGE_COUNT = 7;
  private static final int GOLD_COUNT = 5;
  private static final int YELLOW_COUNT = 2;
  private static final int NEW_PARTICLE_COUNT = REDISH_BROWN_COUNT + DARK_ORANGE_COUNT + GOLD_COUNT + YELLOW_COUNT;

  /**
   * Constructs a new Snow particle effect.
   *
   * @param originX
   *          The origin, on the X-axis, of the effect.
   * @param originY
   *          The origin, on the Y-axis, of the effect.
   */
  public FireEmitter(final int originX, final int originY) {
    super(originX, originY);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#getOrigin()
   */
  @Override
  public Point2D getOrigin() {
    return new Point2D.Double(this.getLocation().getX(), this.getLocation().getY() + this.getHeight());
  }

  /**
   * Creates a new Particle object.
   *
   * @param color
   *          the color
   * @param life
   *          The number of movements before the new Particle decays.
   * @param maxAxisMovement
   *          the max axis movement
   */
  public void newParticle(final Color color, final int life, final double maxAxisMovement) {
    final boolean randBool = Math.random() >= 0.5;

    final float xCoord = (float) (this.getWidth() * 0.5 + Math.random() * maxAxisMovement * (randBool ? -1f : 1f));
    final float dx = (float) (Math.random() * 0.2);
    final float dy = (float) (Math.random() * 2 * -1f);
    final float gravityY = 0.0015f * (randBool ? 1f : -1f);
    final float size = (float) (4 + Math.random() * 5);

    this.addParticle(new RectangleFillParticle(xCoord, 0, dx, dy, 0.0f, gravityY, size, size, life, color));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#createNewParticle()
   */
  @Override
  protected Particle createNewParticle() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#spawnParticle()
   */
  @Override
  protected void spawnParticle() {

    // only allow particle spawn if there is enough space to spawn particles in
    // all colors
    if (this.getParticles().size() > this.getMaxParticles() - NEW_PARTICLE_COUNT) {
      return;
    }

    for (byte i = 0; i < REDISH_BROWN_COUNT; i++) {
      this.newParticle(REDISH_BROWN, this.getParticleMaxTTL(), this.getWidth() / 4);
    }
    for (byte i = 0; i < DARK_ORANGE_COUNT; i++) {
      this.newParticle(DARK_ORANGE, this.getRandomParticleTTL(), this.getWidth() / 5);
    }
    for (byte i = 0; i < GOLD_COUNT; i++) {
      this.newParticle(GOLD, this.getRandomParticleTTL(), this.getWidth() / 6);
    }
    for (byte i = 0; i < YELLOW_COUNT; i++) {
      this.newParticle(YELLOW, this.getParticleMinTTL(), this.getWidth() / 7);
    }
  }
}