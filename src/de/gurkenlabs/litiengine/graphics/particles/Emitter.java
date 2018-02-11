package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.particles.Particle.ParticleRenderType;

/**
 * An abstract implementation for emitters that provide a particle effect.
 */
@CollisionInfo(collision = false)
public abstract class Emitter extends Entity implements IUpdateable, ITimeToLive, IRenderable {
  public static final Color DEFAULT_PARTICLE_COLOR = new Color(255, 255, 255, 150);
  public static final int DEFAULT_UPDATERATE = 30;
  public static final int DEFAULT_SPAWNAMOUNT = 1;
  public static final int DEFAULT_MAXPARTICLES = 100;

  private static final Random RANDOM = new Random();

  private final List<Consumer<Emitter>> finishedConsumer;

  /** The activated. */
  private boolean activated;

  private final boolean activateOnInit;

  /** The activation tick. */
  private long activationTick;

  /** The alive time. */
  private long aliveTime;

  private final List<Color> colors;

  /** The last spawn. */
  private long lastSpawn;

  /** The max particles. */
  private int maxParticles;

  private int particleMaxTTL;

  private int particleMinTTL;

  private final CopyOnWriteArrayList<Particle> particles;

  private int particleUpdateDelay;

  /** The paused. */
  private boolean paused;

  /** The spawn amount. */
  private int spawnAmount;

  /** The spawn rate. */
  private int spawnRate;

  /** The time to live. */
  private int timeToLive;

  private IRenderable groundRenderable;

  private IRenderable overlayRenderable;

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
    this.finishedConsumer = new CopyOnWriteArrayList<>();
    this.particles = new CopyOnWriteArrayList<>();
    this.groundRenderable = g -> renderParticles(g, ParticleRenderType.GROUND);
    this.overlayRenderable = g -> renderParticles(g, ParticleRenderType.OVERLAY);
    this.setLocation(origin);

    final EmitterInfo info = this.getClass().getAnnotation(EmitterInfo.class);

    if (info != null) {
      this.maxParticles = info.maxParticles();
      this.spawnAmount = info.spawnAmount();
      this.spawnRate = info.spawnRate();
      this.timeToLive = info.emitterTTL();
      this.particleMinTTL = info.particleMinTTL();
      this.particleMaxTTL = info.particleMaxTTL();
      this.particleUpdateDelay = info.particleUpdateRate();
      this.activateOnInit = info.activateOnInit();
    } else {
      this.maxParticles = Emitter.DEFAULT_MAXPARTICLES;
      this.spawnAmount = Emitter.DEFAULT_SPAWNAMOUNT;
      this.spawnRate = 0;
      this.timeToLive = 0;
      this.particleMinTTL = 0;
      this.particleMaxTTL = 0;
      this.particleUpdateDelay = Emitter.DEFAULT_UPDATERATE;
      this.activateOnInit = true;
    }
  }

  /**
   * Activate.
   */
  public void activate() {
    if (this.activated) {
      return;
    }

    this.activated = true;
    this.activationTick = Game.getLoop().getTicks();
    Game.getLoop().attach(this);
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
    Game.getLoop().detach(this);
  }

  public void delete() {
    this.deactivate();
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().remove(this);
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

  public List<Color> getColors() {
    return this.colors;
  }

  public IRenderable getGroundRenderable() {
    return this.groundRenderable;
  }

  public IRenderable getOverlayRenderable() {
    return this.overlayRenderable;
  }

  /**
   * Gets the max particles.
   *
   * @return the max particles
   */
  public int getMaxParticles() {
    return this.maxParticles;
  }

  public int getParticleMaxTTL() {
    return this.particleMaxTTL;
  }

  public int getParticleMinTTL() {
    return this.particleMinTTL;
  }

  public void getParticleMinTTL(final int minTTL) {
    this.particleMinTTL = minTTL;
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

  public int getSpawnAmount() {
    return this.spawnAmount;
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

  public boolean isActivateOnInit() {
    return this.activateOnInit;
  }

  /**
   * Checks if is finished.
   *
   * @return true, if is finished
   */
  public boolean isFinished() {
    // if a time to live is set and reached or ir the emitter has been started
    // and no particles are left
    return this.getTimeToLive() > 0 && this.timeToLiveReached() || this.activated && this.lastSpawn > 0 && this.getParticles().isEmpty();
  }

  /**
   * Checks if is paused.
   *
   * @return true, if is paused
   */
  public boolean isPaused() {
    return this.paused;
  }

  public void onFinished(Consumer<Emitter> cons) {
    this.finishedConsumer.add(cons);
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getScreenManager() != null && Game.getCamera() != null && !Game.getCamera().getViewPort().intersects(this.getBoundingBox())) {
      return;
    }

    this.renderParticles(g, ParticleRenderType.EMITTER);

    if (Game.getConfiguration().debug().renderHitBoxes()) {
      DebugRenderer.renderEntityDebugInfo(g, this);
    }
  }

  public void setColors(final Color... colors) {
    this.colors.clear();
    this.colors.addAll(Arrays.asList(colors));
  }

  public void setMaxParticles(final int maxPart) {
    this.maxParticles = maxPart;
  }

  public void setParticleMaxTTL(final int maxTTL) {
    this.particleMaxTTL = maxTTL;
  }

  public void setParticleMinTTL(final int minTTL) {
    this.particleMinTTL = minTTL;
  }

  public void setParticleUpdateRate(final int delay) {
    this.particleUpdateDelay = delay;
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

  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
  }

  public void setTimeToLive(final int ttl) {
    this.timeToLive = ttl;
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

  public void togglePaused() {
    this.paused = !this.paused;
  }

  @Override
  public void update() {
    if (this.isPaused()) {
      return;
    }

    // clear particles if the effect time to life is reached
    if (this.isFinished()) {
      for (Consumer<Emitter> cons : this.finishedConsumer) {
        cons.accept(this);
      }

      this.delete();
      return;
    }

    final float updateRatio = (float) this.getParticleUpdateRate() / Game.getLoop().getUpdateRate();
    for (final Particle p : this.getParticles().stream().collect(Collectors.toList())) {
      if (this.particleCanBeRemoved(p)) {
        // remove dead particles
        this.particles.remove(p);
        continue;
      }

      p.update(this.getLocation(), updateRatio);
    }

    this.aliveTime = Game.getLoop().getDeltaTime(this.activationTick);

    if ((this.getSpawnRate() == 0 || Game.getLoop().getDeltaTime(this.lastSpawn) >= this.getSpawnRate())) {
      this.spawnParticle();
    }
  }

  protected void addParticleColor(final Color... colors) {
    for (final Color color : colors) {
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
    if (this.colors.isEmpty()) {
      return DEFAULT_PARTICLE_COLOR;
    }

    return this.colors.get(RANDOM.nextInt(this.colors.size()));
  }

  protected int getRandomParticleTTL() {
    if (this.getParticleMaxTTL() == 0) {
      return this.getParticleMinTTL();
    }

    final int ttlDiff = this.getParticleMaxTTL() - this.getParticleMinTTL();
    if (ttlDiff <= 0) {
      return this.getParticleMaxTTL();
    }

    return RANDOM.nextInt(this.getParticleMaxTTL() - this.getParticleMinTTL()) + this.getParticleMinTTL();
  }

  protected int getRandomParticleX() {
    return RANDOM.nextInt((int) this.getWidth());
  }

  protected int getRandomParticleY() {
    return RANDOM.nextInt((int) this.getHeight());
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
    for (short i = 0; i < this.getSpawnAmount(); i++) {
      if (!this.canTakeNewParticles()) {
        return;
      }

      Particle part = this.createNewParticle();
      if (part != null) {
        this.addParticle(part);
      }
    }
  }

  private void renderParticles(final Graphics2D g, final ParticleRenderType renderType) {
    final Point2D origin = this.getLocation();
    this.particles.forEach(particle -> {
      if (particle.getParticleRenderType() == renderType) {
        particle.render(g, origin);
      }
    });
  }
}
