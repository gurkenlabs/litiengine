package de.gurkenlabs.litiengine.graphics.emitters;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.CollisionInfo;
import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.particles.EllipseParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.LineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.PolygonParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.SpriteParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.TextParticle;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterLoader;
import de.gurkenlabs.litiengine.graphics.emitters.xml.ParticleParameter;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/** A standard implementation for emitters that provide a particle effect. */
@CollisionInfo(collision = false)
@EmitterInfo
@TmxType(MapObjectType.EMITTER)
public class Emitter extends Entity implements IUpdateable, ITimeToLive, IRenderable {

  private final Collection<EmitterFinishedListener> finishedListeners;
  private final CopyOnWriteArrayList<Particle> particles;

  private EmitterData emitterData;

  private boolean activateOnInit;
  private boolean activated;
  private boolean paused;
  private boolean stopped;

  private long activationTick;
  private long aliveTime;
  private long lastSpawn;

  private Map<RenderType, IRenderable> renderables;
  private Point2D origin;

  public Emitter() {
    this.finishedListeners = ConcurrentHashMap.newKeySet();
    this.particles = new CopyOnWriteArrayList<>();
    this.renderables = new ConcurrentHashMap<>();

    for (RenderType type : RenderType.values()) {
      if (type == RenderType.NONE) {
        continue;
      }

      this.renderables.put(type, g -> renderParticles(g, type));
    }

    this.emitterData = new EmitterData();
    this.emitterData.setRequiredQuality(EmitterData.DEFAULT_REQUIRED_QUALITY);

    final EmitterInfo info = this.getClass().getAnnotation(EmitterInfo.class);
    if (info != null) {
      this.emitterData.setParticleType(info.particleType());
      this.emitterData.setRequiredQuality(info.requiredQuality());
      this.emitterData.setMaxParticles(info.maxParticles());
      this.emitterData.setSpawnAmount(info.spawnAmount());
      this.emitterData.setSpawnRate(info.spawnRate());
      this.emitterData.setEmitterDuration(info.duration());
      this.emitterData.setParticleTTL(
          new ParticleParameter(info.particleMinTTL(), info.particleMaxTTL()));
      this.emitterData.setUpdateRate(info.particleUpdateRate());
      this.emitterData.setOriginAlign(info.originAlign());
      this.emitterData.setOriginValign(info.originValign());
      this.activateOnInit = info.activateOnInit();
    }
  }

  public Emitter(EmitterData emitterData) {
    this();
    setEmitterData(emitterData);
  }

  public Emitter(final Point2D origin, EmitterData emitterData) {
    this(origin);
    setEmitterData(emitterData);
  }

  public Emitter(final double x, final double y, EmitterData emitterData) {
    this(x, y);
    setEmitterData(emitterData);
  }

  public Emitter(final double x, final double y, final String emitterXml) {
    this(x, y);
    setEmitterData(emitterXml);
  }

  public Emitter(final Point2D origin, final String emitterXml) {
    this(origin);
    setEmitterData(emitterXml);
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

  @FunctionalInterface
  public interface EmitterFinishedListener extends EventListener {

    void finished(Emitter emitter);
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

  public EmitterData data() {
    return this.emitterData;
  }

  public Point2D getOrigin() {
    if (this.origin == null) {
      this.updateOrigin();

    }
    return this.origin;
  }

  protected void updateOrigin() {
    this.origin = new Point2D.Double(
        this.getX() + this.data().getOriginAlign().getValue(this.getWidth()),
        this.getY() + this.data().getOriginValign().getValue(this.getHeight()));
  }

  public IRenderable getRenderable(RenderType type) {
    if (type == RenderType.NONE) {
      return null;
    }

    return this.renderables.get(type);
  }

  /**
   * Gets the particles.
   *
   * @return the particles
   */
  public List<Particle> getParticles() {
    return this.particles;
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

  public void setEmitterData(final EmitterData emitterData) {
    if (emitterData == null) {
      return;
    }
    this.emitterData = emitterData;
  }

  public void setEmitterData(final String emitterXmlPath) {
    EmitterData loaded = EmitterLoader.load(emitterXmlPath);
    setEmitterData(loaded);
  }

  /**
   * Time to live reached.
   *
   * @return true, if successful
   */
  @Override
  public boolean timeToLiveReached() {
    return this.activated
        && this.getTimeToLive() > 0
        && this.getAliveTime() >= this.getTimeToLive();
  }

  public void togglePaused() {
    this.paused = !this.paused;
  }

  public void toggleStopped() {
    this.stopped = !this.stopped;
  }

  @Override
  public int getTimeToLive() {
    return this.data().getEmitterDuration();
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

    this.updateOrigin();

    final float updateRatio = (float) this.data().getUpdateRate() / Game.loop().getTickRate();
    for (final Particle p : this.getParticles().stream().collect(Collectors.toList())) {
      if (this.particleCanBeRemoved(p)) {
        // remove dead particles
        this.particles.remove(p);
        continue;
      }

      p.update(this.getOrigin(), updateRatio);
    }

    this.aliveTime = Game.time().since(this.activationTick);
    if ((this.data().getSpawnRate() == 0
        || Game.time().since(this.lastSpawn) >= this.data().getSpawnRate())) {
      this.lastSpawn = Game.time().now();
      this.spawnParticle();
    }
  }

  /**
   * Can take new particles.
   *
   * @return Whether-or-not the effect can hold any more particles.
   */
  protected boolean canTakeNewParticles() {
    return this.particles.size() < this.data().getMaxParticles();
  }

  /**
   * Creates the new particle.
   *
   * @return the particle
   */
  protected Particle createNewParticle() {

    float width = (float) this.data().getParticleWidth().get();
    float height = (float) this.data().getParticleHeight().get();

    Particle particle;
    switch (this.data().getParticleType()) {
      case ELLIPSE:
        particle = new EllipseParticle(width, height);
        break;
      case RECTANGLE:
        particle = new RectangleParticle(width, height);
        break;
      case TRIANGLE:
        particle = new PolygonParticle(width, height, 3);
        break;
      case DIAMOND:
        particle = new PolygonParticle(width, height, 4);
        break;
      case LINE:
        particle = new LineParticle(width, height);
        break;
      case TEXT:
        String text;
        if (this.data().getTexts().isEmpty()) {
          text = EmitterData.DEFAULT_TEXT;
        } else {
          text = Game.random().choose(this.data().getTexts());
        }
        particle = new TextParticle(text);
        break;
      case SPRITE:
        Spritesheet sprite = Resources.spritesheets().get(this.data().getSpritesheet());
        if (sprite == null || sprite.getTotalNumberOfSprites() <= 0) {
          return null;
        }
        particle = new SpriteParticle(sprite);
        ((SpriteParticle) particle).setAnimateSprite(this.data().isAnimatingSprite());
        ((SpriteParticle) particle).setLoopSprite(this.data().isLoopingSprite());
        break;
      default:
        particle = new RectangleParticle(width, height);
        break;
    }
    return particle.init(this.data());
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


  protected void spawnParticle() {
    for (short i = 0; i < this.data().getSpawnAmount(); i++) {
      if (!this.canTakeNewParticles()) {
        return;
      }

      Particle part = this.createNewParticle();
      if (part != null) {
        this.addParticle(part);
      }
    }
  }

  /**
   * Render particles of this effect. The particles are always rendered relatively to this effects render location. A
   * particle doesn't have an own map location. It is always relative to the effect it is assigned to.
   *
   * @param g
   *          The graphics object to draw on.
   * @param renderType
   *          The render type.
   */
  private void renderParticles(final Graphics2D g, final RenderType renderType) {
    if (Game.config().graphics().getGraphicQuality().getValue() < this.data().getRequiredQuality().getValue()) {
      return;
    }

    final Point2D origin = this.getOrigin();
    final Rectangle2D viewport =
        Game.screens() != null && Game.world().camera() != null
            ? Game.world().camera().getViewport()
            : null;
    for (Particle particle : this.particles) {
      if (((!particle.usesCustomRenderType() && renderType == RenderType.NONE)
          || (particle.usesCustomRenderType() && particle.getCustomRenderType() == renderType))
          && viewport != null
          && viewport.intersects(particle.getBoundingBox(origin))) {
        particle.render(g, origin);
      }
    }
  }
}
