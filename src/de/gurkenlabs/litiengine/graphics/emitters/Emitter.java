package de.gurkenlabs.litiengine.graphics.emitters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.CollisionInfo;
import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;

/**
 * An abstract implementation for emitters that provide a particle effect.
 */
@CollisionInfo(collision = false)
@EmitterInfo
@TmxType(MapObjectType.EMITTER)
public abstract class Emitter extends Entity implements IUpdateable, ITimeToLive, IRenderable {

  private final Collection<EmitterFinishedListener> finishedListeners;
  private final CopyOnWriteArrayList<Particle> particles;
  private final List<Color> colors;

  private Quality requiredQuality;

  private boolean activateOnInit;
  private boolean activated;
  private boolean paused;
  private boolean stopped;

  private long activationTick;
  private long aliveTime;
  private long lastSpawn;
  private int maxParticles;
  private int particleMaxTTL;
  private int particleMinTTL;
  private int particleUpdateDelay;
  private int spawnAmount;
  private int spawnRate;
  private int duration;
  private Valign originValign;
  private Align originAlign;

  private Map<RenderType, IRenderable> renderables;

  public Emitter() {
    this.colors = new ArrayList<>();
    this.finishedListeners = ConcurrentHashMap.newKeySet();
    this.particles = new CopyOnWriteArrayList<>();
    this.renderables = new ConcurrentHashMap<>();

    for (RenderType type : RenderType.values()) {
      if (type == RenderType.NONE) {
        continue;
      }

      this.renderables.put(type, g -> renderParticles(g, type));
    }

    final EmitterInfo info = this.getClass().getAnnotation(EmitterInfo.class);
    if (info != null) {
      this.requiredQuality = info.requiredQuality();
      this.maxParticles = info.maxParticles();
      this.spawnAmount = info.spawnAmount();
      this.spawnRate = info.spawnRate();
      this.duration = info.duration();
      this.particleMinTTL = info.particleMinTTL();
      this.particleMaxTTL = info.particleMaxTTL();
      this.particleUpdateDelay = info.particleUpdateRate();
      this.activateOnInit = info.activateOnInit();
      this.originAlign = info.originAlign();
      this.originValign = info.originVAlign();
    }
  }

  public Emitter(final double originX, final double originY) {
    this(new Point2D.Double(originX, originY));
  }

  public Emitter(final Point2D origin) {
    this();
    this.setLocation(origin);
  }

  public void activate() {
    if (this.activated) {
      return;
    }

    this.activated = true;
    this.activationTick = Game.time().now();
    Game.loop().attach(this);
  }

  /**
   * Adds a particle to this Emitter's list of Particles.
   *
   * @param particle
   *          the particle
   */
  public void addParticle(final Particle particle) {
    if (this.isStopped()) {
      return;
    }
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
    Game.loop().detach(this);
  }

  public void delete() {
    this.deactivate();
    if (Game.world().environment() != null) {
      Game.world().environment().remove(this);
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

  public Point2D getOrigin() {
    return new Point2D.Double(this.getX() + this.getOriginAlign().getValue(this.getWidth()), this.getY() + this.getOriginValign().getValue(this.getHeight()));
  }

  public Align getOriginAlign() {
    return this.originAlign;
  }

  public Valign getOriginValign() {
    return this.originValign;
  }

  public IRenderable getRenderable(RenderType type) {
    if (type == RenderType.NONE) {
      return null;
    }

    return this.renderables.get(type);
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

  public Quality getRequiredQuality() {
    return this.requiredQuality;
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
    return this.duration;
  }

  public boolean isActivateOnInit() {
    return this.activateOnInit;
  }

  public boolean isActivated() {
    return this.activated;
  }

  /**
   * Checks if the emitter duration is reached.
   *
   * @return true, if the emitter is finished
   */
  public boolean isFinished() {
    return this.getTimeToLive() > 0 && this.timeToLiveReached();
  }

  /**
   * Checks if is paused.
   *
   * @return true, if is paused
   */
  public boolean isPaused() {
    return this.paused;
  }

  public boolean isStopped() {
    return this.stopped;
  }

  public void onFinished(EmitterFinishedListener listener) {
    this.finishedListeners.add(listener);
  }

  public void removeFinishedListener(EmitterFinishedListener listener) {
    this.finishedListeners.remove(listener);
  }

  @Override
  public void render(final Graphics2D g) {
    this.renderParticles(g, RenderType.NONE);
  }

  public void setColors(final Color... colors) {
    this.colors.clear();
    this.colors.addAll(Arrays.asList(colors));
  }

  public void setMaxParticles(final int maxPart) {
    this.maxParticles = maxPart;
  }

  public void setOriginAlign(Align align) {
    this.originAlign = align;
  }

  public void setOriginValign(Valign valign) {
    this.originValign = valign;
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

  public void setStopped(final boolean stopped) {
    this.stopped = stopped;
  }

  public void setRequiredQuality(Quality requiredQuality) {
    this.requiredQuality = requiredQuality;
  }

  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
  }

  public void setDuration(final int ttl) {
    this.duration = ttl;
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

  public void toggleStopped() {
    this.stopped = !this.stopped;
  }

  @Override
  public void update() {
    if (this.isPaused()) {
      return;
    }

    // clear particles if the effect time to life is reached
    if (this.isFinished()) {
      for (EmitterFinishedListener listener : this.finishedListeners) {
        listener.finished(this);
      }

      this.delete();
      return;
    }

    final float updateRatio = (float) this.getParticleUpdateRate() / Game.loop().getTickRate();
    for (final Particle p : this.getParticles().stream().collect(Collectors.toList())) {
      if (this.particleCanBeRemoved(p)) {
        // remove dead particles
        this.particles.remove(p);
        continue;
      }

      p.update(this.getOrigin(), updateRatio);
    }

    this.aliveTime = Game.time().since(this.activationTick);
    if ((this.getSpawnRate() == 0 || Game.time().since(this.lastSpawn) >= this.getSpawnRate())) {
      this.lastSpawn = Game.time().now();
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
      return EmitterData.DEFAULT_COLOR;
    }

    return this.colors.get(ThreadLocalRandom.current().nextInt(this.colors.size()));
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

  private void renderParticles(final Graphics2D g, final RenderType renderType) {
    if (Game.config().graphics().getGraphicQuality().getValue() < this.getRequiredQuality().getValue()) {
      return;
    }

    if (Game.screens() != null && Game.world().camera() != null && !Game.world().camera().getViewport().intersects(this.getBoundingBox())) {
      return;
    }

    final Point2D origin = this.getOrigin();
    for (Particle particle : this.particles) {
      if (!particle.usesCustomRenderType() && renderType == RenderType.NONE
          || particle.usesCustomRenderType() && particle.getCustomRenderType() == renderType) {
        particle.render(g, origin);
      }
    }
  }

  @FunctionalInterface
  public interface EmitterFinishedListener extends EventListener {
    void finished(Emitter emitter);
  }
}
