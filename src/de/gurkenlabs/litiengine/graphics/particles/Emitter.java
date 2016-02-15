/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.graphics.IRenderable;

/**
 * An abstract implementation for emitters that provide a particle effect.
 */
@CollisionInfo(collision = false)
public abstract class Emitter extends Entity implements IUpdateable, ITimeToLive, IRenderable {
  private static final Color DEFAULT_PARTICLE_COLOR = new Color(255, 255, 255, 150);
  private final List<Color> colors;

  private IGameLoop gameLoop;

  /** The activated. */
  private boolean activated;

  /** The activation tick. */
  private long activationTick;

  /** The alive time. */
  private long aliveTime;

  /** The last spawn. */
  private long lastSpawn;

  /** The max particles. */
  private final int maxParticles;

  private final int particleMaxTTL;

  private final int particleMinTTL;

  /** A collection of particles that make up the snow. */
  private final CopyOnWriteArrayList<Particle> particles;

  private final int particleUpdateDelay;

  /** The paused. */
  private boolean paused;

  /** The spawn amount. */
  private final int spawnAmount;

  /** The spawn rate. */
  private final int spawnRate;

  /** The time to live. */
  private final int timeToLive;

  /**
   * Basic constructor for an effect.
   *
   * @param originX
   *          The origin, on the X-axis, of the effect.
   * @param originY
   *          The origin, on the Y-axis, of the effect.
   */
  public Emitter(final double originX, final double originY) {
    this(new Point2D.Double(originX, originY));
  }

  public Emitter(final Point2D origin) {
    super();
    this.colors = new ArrayList<>();
    final EmitterInfo info = this.getClass().getAnnotation(EmitterInfo.class);

    this.maxParticles = info.maxParticles();
    this.spawnAmount = info.spawnAmount();
    this.spawnRate = info.spawnRate();
    this.timeToLive = info.emitterTTL();
    this.particleMinTTL = info.particleMinTTL();
    this.particleMaxTTL = info.particleMaxTTL();
    this.particleUpdateDelay = info.particleUpdateRate();
    this.particles = new CopyOnWriteArrayList<>();
    this.setLocation(origin);
    if (info.activateOnInit()) {
      this.activate(Game.getLoop());
    }
  }

  /**
   * Activate.
   */
  public void activate(IGameLoop gameLoop) {
    if (gameLoop == null || this.activated) {
      return;
    }

    this.gameLoop = gameLoop;
    this.activated = true;
    this.activationTick = this.gameLoop.getTicks();
    this.gameLoop.registerForUpdate(this);
  }

  /**
   * Sets all of the data of the specified particle to the new data provided.
   *
   * @param particle
   *          the particle
   */
  public void addParticle(final Particle particle) {
    this.particles.add(particle);
  }

  /**
   * Deactivate.
   */
  public void deactivate() {
    if (!this.activated) {
      return;
    }
    
    this.activated = false;
    this.getParticles().clear();
    this.aliveTime = 0;
    this.activationTick = 0;
    this.lastSpawn = 0;
    if (this.gameLoop != null) {
      this.gameLoop.unregisterFromUpdate(this);
    }
  }

  /**
   * Gets the alive time.
   *
   * @return the alive time
   */
  @Override
  public long getAliveTime() {
    return this.aliveTime;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(this.getLocation().getX(), this.getLocation().getY(), this.getWidth(), this.getHeight());
  }

  /**
   * Gets the max particles.
   *
   * @return the max particles
   */
  public int getMaxParticles() {
    return this.maxParticles;
  }

  /**
   * Gets the origin.
   *
   * @return the origin
   */
  public Point2D getOrigin() {
    return this.getLocation();
  }

  public int getParticleMaxTTL() {
    return this.particleMaxTTL;
  }

  public int getParticleMinTTL() {
    return this.particleMinTTL;
  }

  /**
   * Gets the particles.
   *
   * @return the particles
   */
  public List<Particle> getParticles() {
    return this.particles;
  }

  public int getParticleUpdateRate() {
    return this.particleUpdateDelay;
  }

  /**
   * Gets the spawn rate in milliseconds.
   *
   * @return the spawn rate
   */
  public int getSpawnRate() {
    return this.spawnRate;
  }

  /**
   * Gets the time to live.
   *
   * @return the time to live
   */
  @Override
  public int getTimeToLive() {
    return this.timeToLive;
  }

  /**
   * Checks if is finished.
   *
   * @return true, if is finished
   */
  public boolean isFinished() {
    // if a time to live is set and reached or ir the emitter has been started
    // and no particles are left
    return this.getTimeToLive() > 0 && this.timeToLiveReached() || this.activated && this.lastSpawn > 0 && this.getParticles().size() == 0;
  }

  /**
   * Checks if is paused.
   *
   * @return true, if is paused
   */
  public boolean isPaused() {
    return this.paused;
  }

  @Override
  public void render(final Graphics g) {
    this.particles.forEach(particle -> particle.render((Graphics2D) g, this.getOrigin()));
  }

  /**
   * Sets the paused.
   *
   * @param paused
   *          the new paused
   */
  public void setPaused(final boolean paused) {
    this.paused = paused;
  }

  /**
   * Time to live reached.
   *
   * @return true, if successful
   */
  @Override
  public boolean timeToLiveReached() {
    return this.activated && this.getTimeToLive() > 0 && this.getAliveTime() >= this.getTimeToLive();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update(final IGameLoop loop) {
    if (this.isPaused()) {
      return;
    }

    // clear particles if the effect time to life is reached
    if (this.timeToLiveReached()) {
      this.deactivate();
      return;
    }

    float updateRatio = (float) this.getParticleUpdateRate() / loop.getUpdateRate();
    this.getParticles().forEach(particle -> particle.update(loop, updateRatio));

    // remove dead particles
    for (final Object p : this.getParticles().stream().filter(particle -> this.particleCanBeRemoved(particle)).toArray()) {
      this.particles.remove(p);
    }

    this.aliveTime = loop.getDeltaTime(this.activationTick);

    if (loop.getDeltaTime(this.lastSpawn) >= this.getSpawnRate()) {
      this.spawnParticle();
    }
  }

  protected void addParticleColor(Color... colors) {
    for (Color color : colors) {
      if (!this.colors.contains(color)) {
        this.colors.add(color);
      }
    }
  }

  /**
   * Can take new particles.
   *
   * @return Whether-or-not the effect can hold any more particles.
   */
  protected boolean canTakeNewParticles() {
    return this.particles.size() < this.maxParticles;
  }

  /**
   * Creates the new particle.
   *
   * @return the particle
   */
  protected abstract Particle createNewParticle();

  protected Color getRandomParticleColor() {
    if (this.colors.size() == 0) {
      return DEFAULT_PARTICLE_COLOR;
    }

    return this.colors.get(new Random().nextInt(this.colors.size()));
  }

  protected int getRandomParticleTTL() {
    final int ttlDiff = this.getParticleMaxTTL() - this.getParticleMinTTL();
    if (ttlDiff <= 0) {
      return this.getParticleMaxTTL();
    }

    return new Random().nextInt(this.getParticleMaxTTL() - this.getParticleMinTTL()) + this.getParticleMinTTL();
  }

  /**
   * Particle can be removed.
   *
   * @param particle
   *          the particle
   * @return true, if successful
   */
  protected boolean particleCanBeRemoved(final Particle particle) {
    return particle.timeToLiveReached();
  }

  /**
   * Render particles of this effect. The particles are always rendered
   * relatively to this effects render location. A particle doesn't have an own
   * map location. It is always relative to the effect it is assigned to.
   *
   * @param g
   *          the g
   * @param p
   *          the p
   */
  /**
   * Spawn particle.
   */
  protected void spawnParticle() {
    for (short i = 0; i < this.spawnAmount; i++) {
      if (!this.canTakeNewParticles()) {
        return;
      }

      this.addParticle(this.createNewParticle());
    }
  }

}
