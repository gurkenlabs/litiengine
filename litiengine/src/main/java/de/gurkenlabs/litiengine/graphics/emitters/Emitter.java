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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an emitter that provides particle effects in the game.
 *
 * <p>The {@code Emitter} class extends {@link Entity} and implements the
 * {@link IUpdateable}, {@link ITimeToLive}, and {@link IRenderable} interfaces. It manages the lifecycle, rendering, and behavior of particles,
 * allowing for dynamic visual effects.
 */
@CollisionInfo(collision = false) @EmitterInfo @TmxType(MapObjectType.EMITTER) public class Emitter extends Entity
  implements IUpdateable, ITimeToLive, IRenderable {

  private final Collection<EmitterFinishedListener> finishedListeners;
  private final CopyOnWriteArrayList<Particle> particles;
  private final Map<RenderType, IRenderable> renderables;
  private EmitterData emitterData;
  private boolean activateOnInit;
  private boolean activated;
  private boolean paused;
  private boolean stopped;
  private long activationTick;
  private long aliveTime;
  private long lastSpawn;
  private Point2D origin;

  /**
   * Constructs a new {@code Emitter} instance.
   *
   * <p>Initializes the emitter with default settings, including particle collections,
   * renderable mappings, and emitter data. If the {@link EmitterInfo} annotation is present on the class, its values are used to configure the
   * emitter's properties.
   */
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
      this.emitterData.setParticleTTL(new ParticleParameter(info.particleMinTTL(), info.particleMaxTTL()));
      this.emitterData.setUpdateRate(info.particleUpdateRate());
      this.emitterData.setOriginAlign(info.originAlign());
      this.emitterData.setOriginValign(info.originValign());
      this.activateOnInit = info.activateOnInit();
    }
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified emitter data.
   *
   * <p>Initializes the emitter using the provided {@link EmitterData}, which
   * contains configuration details such as particle type, spawn rate, and emitter duration.
   *
   * @param emitterData the data used to configure this emitter
   */
  public Emitter(EmitterData emitterData) {
    this();
    setEmitterData(emitterData);
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified origin and emitter data.
   *
   * <p>Initializes the emitter at the given origin point and configures it using the provided
   * {@link EmitterData}, which contains details such as particle type, spawn rate, and emitter duration.
   *
   * @param origin      the origin point where the emitter is located
   * @param emitterData the data used to configure this emitter
   */
  public Emitter(final Point2D origin, EmitterData emitterData) {
    this(origin);
    setEmitterData(emitterData);
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified coordinates and emitter data.
   *
   * <p>Initializes the emitter at the given x and y coordinates and configures it using the provided
   * {@link EmitterData}, which contains details such as particle type, spawn rate, and emitter duration.
   *
   * @param x           the x-coordinate of the emitter's origin
   * @param y           the y-coordinate of the emitter's origin
   * @param emitterData the data used to configure this emitter
   */
  public Emitter(final double x, final double y, EmitterData emitterData) {
    this(x, y);
    setEmitterData(emitterData);
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified coordinates and emitter XML configuration.
   *
   * <p>Initializes the emitter at the given x and y coordinates and configures it using the provided
   * XML file, which contains emitter settings such as particle type, spawn rate, and emitter duration.
   *
   * @param x          the x-coordinate of the emitter's origin
   * @param y          the y-coordinate of the emitter's origin
   * @param emitterXml the path to the XML file used to configure this emitter
   */
  public Emitter(final double x, final double y, final String emitterXml) {
    this(x, y);
    setEmitterData(emitterXml);
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified origin and emitter XML configuration.
   *
   * <p>Initializes the emitter at the given origin point and configures it using the provided
   * XML file, which contains emitter settings such as particle type, spawn rate, and emitter duration.
   *
   * @param origin     the origin point where the emitter is located
   * @param emitterXml the path to the XML file used to configure this emitter
   */
  public Emitter(final Point2D origin, final String emitterXml) {
    this(origin);
    setEmitterData(emitterXml);
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified origin coordinates.
   *
   * <p>Initializes the emitter at the given x and y coordinates, represented as a {@link Point2D.Double}.
   *
   * @param originX the x-coordinate of the emitter's origin
   * @param originY the y-coordinate of the emitter's origin
   */
  public Emitter(final double originX, final double originY) {
    this(new Point2D.Double(originX, originY));
  }

  /**
   * Constructs a new {@code Emitter} instance with the specified origin.
   *
   * <p>Initializes the emitter at the given origin point, setting its location
   * and preparing it for further configuration or activation.
   *
   * @param origin the origin point where the emitter is located
   */
  public Emitter(final Point2D origin) {
    this();
    this.setLocation(origin);
  }

  /**
   * Activates the emitter.
   *
   * <p>Marks the emitter as activated, sets the activation tick to the current game time,
   * and attaches the emitter to the game loop for updates. If the emitter is already activated, this method does nothing.
   */
  public void activate() {
    if (this.activated) {
      return;
    }

    this.activated = true;
    this.activationTick = Game.time().now();
    Game.loop().attach(this);
  }

  /**
   * Adds a particle to this emitter's list of particles.
   *
   * <p>If the emitter is stopped, this method does nothing. Otherwise, the specified particle
   * is added to the internal particle collection for rendering and updates.
   *
   * @param particle the particle to be added to the emitter
   */
  public void addParticle(final Particle particle) {
    if (this.isStopped()) {
      return;
    }
    this.particles.add(particle);
  }

  /**
   * Deactivates the emitter.
   *
   * <p>This method stops the emitter's activity and resets its state. It clears all particles,
   * resets the alive time, activation tick, and last spawn time, and detaches the emitter from the game loop. If the emitter is not currently
   * activated, this method does nothing.
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

  /**
   * Deletes this emitter from the game world.
   *
   * <p>Deactivates the emitter, clears its particles, and removes it from the current environment
   * if one exists. This method ensures that the emitter is no longer active or present in the game.
   */
  public void delete() {
    this.deactivate();
    if (Game.world().environment() != null) {
      Game.world().environment().remove(this);
    }
  }


  /**
   * Retrieves the total alive time of the emitter.
   *
   * <p>This method returns the amount of time, in milliseconds, that the emitter
   * has been active since its activation.
   *
   * @return the alive time of the emitter in milliseconds
   */
  @Override
  public long getAliveTime() {
    return this.aliveTime;
  }

  /**
   * Retrieves the emitter data associated with this emitter.
   *
   * <p>The emitter data contains configuration details such as particle type,
   * spawn rate, and other properties that define the emitter's behavior.
   *
   * @return the {@link EmitterData} object for this emitter
   */
  public EmitterData data() {
    return this.emitterData;
  }

  /**
   * Retrieves the origin point of the emitter.
   *
   * <p>If the origin has not been calculated yet, this method updates the origin
   * based on the emitter's current position and alignment settings.
   *
   * @return the origin point of the emitter as a {@link Point2D} object
   */
  public Point2D getOrigin() {
    if (this.origin == null) {
      this.updateOrigin();
    }
    return this.origin;
  }

  /**
   * Updates the origin point of the emitter.
   *
   * <p>This method recalculates the origin based on the emitter's current position,
   * width, height, and alignment settings defined in the emitter data.
   */
  protected void updateOrigin() {
    this.origin = new Point2D.Double(
      getX() + data().getOriginAlign().getValue(getWidth()),
      getY() + data().getOriginValign().getValue(getHeight())
    );
  }

  /**
   * Retrieves the renderable object associated with the specified render type.
   *
   * <p>If the provided render type is {@link RenderType#NONE}, this method returns {@code null}.
   * Otherwise, it returns the {@link IRenderable} instance mapped to the given render type.
   *
   * @param type the render type for which the renderable object is requested
   * @return the renderable object associated with the specified render type, or {@code null} if the type is {@link RenderType#NONE}
   */
  public IRenderable getRenderable(RenderType type) {
    if (type == RenderType.NONE) {
      return null;
    }

    return this.renderables.get(type);
  }

  /**
   * Retrieves the list of particles managed by this emitter.
   *
   * <p>Returns a list containing all particles currently associated with this emitter.
   * These particles are used for rendering and updates during the emitter's lifecycle.
   *
   * @return a list of particles managed by this emitter
   */
  public List<Particle> getParticles() {
    return this.particles;
  }

  /**
   * Checks if the emitter is set to activate on initialization.
   *
   * <p>Returns a boolean indicating whether the emitter should automatically activate
   * when it is initialized.
   *
   * @return {@code true} if the emitter activates on initialization, {@code false} otherwise
   */
  public boolean isActivateOnInit() {
    return this.activateOnInit;
  }

  /**
   * Checks if the emitter is currently activated.
   *
   * <p>Returns a boolean indicating whether the emitter is in an active state.
   *
   * @return {@code true} if the emitter is activated, {@code false} otherwise
   */
  public boolean isActivated() {
    return this.activated;
  }

  /**
   * Determines if the emitter has finished its duration.
   *
   * <p>This method checks whether the emitter's time-to-live (TTL) is greater than zero
   * and if the TTL has been reached. If both conditions are true, the emitter is considered finished.
   *
   * @return {@code true} if the emitter's duration is complete, {@code false} otherwise
   */
  public boolean isFinished() {
    return this.getTimeToLive() > 0 && this.timeToLiveReached();
  }

  /**
   * Checks whether the emitter is currently paused.
   *
   * <p>Returns a boolean indicating if the emitter is in a paused state.
   *
   * @return {@code true} if the emitter is paused, {@code false} otherwise
   */
  public boolean isPaused() {
    return this.paused;
  }

  /**
   * Sets the paused state of the emitter.
   *
   * <p>This method updates the emitter's paused status to the specified value.
   * When paused, the emitter stops updating its particles and other behaviors.
   *
   * @param paused {@code true} to pause the emitter, {@code false} to resume it
   */
  public void setPaused(final boolean paused) {
    this.paused = paused;
  }

  /**
   * Checks if the emitter is currently stopped.
   *
   * <p>Returns a boolean indicating whether the emitter is in a stopped state.
   *
   * @return {@code true} if the emitter is stopped, {@code false} otherwise
   */
  public boolean isStopped() {
    return this.stopped;
  }

  /**
   * Sets the stopped state of the emitter.
   *
   * <p>This method updates the emitter's stopped status to the specified value.
   * When stopped, the emitter ceases all activity and updates.
   *
   * @param stopped {@code true} to stop the emitter, {@code false} to resume it
   */
  public void setStopped(final boolean stopped) {
    this.stopped = stopped;
  }

  /**
   * Registers a listener to be notified when the emitter finishes.
   *
   * <p>The specified {@link EmitterFinishedListener} is added to the list of listeners
   * that will be invoked when the emitter completes its lifecycle.
   *
   * @param listener the listener to be notified when the emitter finishes
   */
  public void onFinished(EmitterFinishedListener listener) {
    this.finishedListeners.add(listener);
  }

  /**
   * Removes a previously registered finished listener.
   *
   * <p>The specified {@link EmitterFinishedListener} is removed from the list of listeners
   * that are notified when the emitter finishes.
   *
   * @param listener the listener to be removed
   */
  public void removeFinishedListener(EmitterFinishedListener listener) {
    this.finishedListeners.remove(listener);
  }

  @Override public void render(final Graphics2D g) {
    this.renderParticles(g, RenderType.NONE);
  }

  /**
   * Sets the emitter data for this emitter.
   *
   * <p>This method updates the emitter's configuration using the provided {@link EmitterData}.
   * If the provided data is null, the method does nothing.
   *
   * @param emitterData the data used to configure this emitter
   */
  public void setEmitterData(final EmitterData emitterData) {
    if (emitterData == null) {
      return;
    }
    this.emitterData = emitterData;
  }

  /**
   * Sets the emitter data for this emitter using an XML configuration file.
   *
   * <p>This method loads the emitter data from the specified XML file and updates the emitter's
   * configuration accordingly.
   *
   * @param emitterXmlPath the path to the XML file containing the emitter data
   */
  public void setEmitterData(final String emitterXmlPath) {
    EmitterData loaded = EmitterLoader.load(emitterXmlPath);
    setEmitterData(loaded);
  }

  /**
   * Time to live reached.
   *
   * @return true, if successful
   */
  @Override public boolean timeToLiveReached() {
    return this.activated && this.getTimeToLive() > 0 && this.getAliveTime() >= this.getTimeToLive();
  }

  /**
   * Toggles the paused state of the emitter.
   *
   * <p>This method switches the emitter's paused status between {@code true} and {@code false}.
   * When paused, the emitter stops updating its particles and other behaviors.
   */
  public void togglePaused() {
    this.paused = !this.paused;
  }

  /**
   * Toggles the stopped state of the emitter.
   *
   * <p>This method switches the emitter's stopped status between {@code true} and {@code false}.
   * When stopped, the emitter ceases all activity and updates.
   */
  public void toggleStopped() {
    this.stopped = !this.stopped;
  }

  @Override public int getTimeToLive() {
    return this.data().getEmitterDuration();
  }

  @Override public void update() {
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
    for (final Particle p : new ArrayList<>(this.getParticles())) {
      if (this.particleCanBeRemoved(p)) {
        // remove dead particles
        this.particles.remove(p);
        continue;
      }

      p.update(this.getOrigin(), updateRatio);
    }

    this.aliveTime = Game.time().since(this.activationTick);
    if ((this.data().getSpawnRate() == 0 || Game.time().since(this.lastSpawn) >= this.data().getSpawnRate())) {
      this.lastSpawn = Game.time().now();
      this.spawnParticle();
    }
  }


  /**
   * Checks if the emitter can accept new particles.
   *
   * <p>This method determines whether the current number of particles is less than
   * the maximum allowed particles as defined in the emitter data configuration.
   *
   * @return {@code true} if the emitter can accept new particles, {@code false} otherwise
   */
  protected boolean canTakeNewParticles() {
    return this.particles.size() < this.data().getMaxParticles();
  }

  /**
   * Creates a new particle based on the emitter's configuration.
   *
   * <p>This method generates a particle of the type specified in the emitter's data.
   * The particle's dimensions are determined by the configured width and height. If the particle type is {@code SPRITE}, the method ensures the
   * spritesheet is valid before creating the particle. If no specific type is defined, a rectangle particle is created by default.
   *
   * @return the newly created particle, or {@code null} if the particle type is {@code SPRITE} and the spritesheet is invalid
   */
  protected Particle createNewParticle() {

    float width = (float) data().getParticleWidth().get();
    float height = (float) data().getParticleHeight().get();

    switch (data().getParticleType()) {
      case ELLIPSE -> {
        return new EllipseParticle(width, height).init(data());
      }
      case TRIANGLE -> {
        return new PolygonParticle(width, height, 3).init(data());
      }
      case DIAMOND -> {
        return new PolygonParticle(width, height, 4).init(data());
      }
      case LINE -> {
        return new LineParticle(width, height).init(data());
      }
      case TEXT -> {
        String text = data().getTexts().isEmpty() ? EmitterData.DEFAULT_TEXT : Game.random().choose(data().getTexts());
        return new TextParticle(text).init(data());
      }
      case SPRITE -> {
        Spritesheet sprite = Resources.spritesheets().get(data().getSpritesheet());
        if (sprite == null || sprite.getTotalNumberOfSprites() <= 0) {
          return null;
        }
        return new SpriteParticle(sprite).setAnimateSprite(data().isAnimatingSprite()).setLoopSprite(data().isLoopingSprite()).init(data());
      }
      default -> {
        return new RectangleParticle(width, height).init(data());
      }
    }
  }

  /**
   * Determines if a particle can be removed from the emitter.
   *
   * <p>This method checks whether the specified particle has reached its time-to-live (TTL).
   * If the particle's TTL is reached, it is considered eligible for removal.
   *
   * @param particle the particle to check
   * @return {@code true} if the particle's TTL is reached, {@code false} otherwise
   */
  protected boolean particleCanBeRemoved(final Particle particle) {
    return particle.timeToLiveReached();
  }

  /**
   * Spawns new particles for the emitter.
   *
   * <p>This method generates a number of particles based on the emitter's spawn amount
   * configuration. It ensures that the emitter does not exceed its maximum allowed particles. If a new particle is successfully created, it is added
   * to the emitter's particle list.
   */
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
   * Render particles of this effect. The particles are always rendered relatively to this effects render location. A particle doesn't have an own map
   * location. It is always relative to the effect it is assigned to.
   *
   * @param g          The graphics object to draw on.
   * @param renderType The render type.
   */
  private void renderParticles(final Graphics2D g, final RenderType renderType) {
    if (Game.config().graphics().getGraphicQuality().getValue() < this.data().getRequiredQuality().getValue()) {
      return;
    }

    final Rectangle2D viewport = Game.screens() != null && Game.world().camera() != null ? Game.world().camera().getViewport() : null;
    for (Particle particle : this.particles) {
      if (((!particle.usesCustomRenderType() && renderType == RenderType.NONE) || (particle.usesCustomRenderType()
        && particle.getCustomRenderType() == renderType)) && viewport != null && viewport.intersects(particle.getBoundingBox(getOrigin()))) {
        particle.render(g, getOrigin());
      }
    }
  }

  /**
   * Listener interface for handling emitter completion events.
   *
   * <p>Implementations of this interface can be registered to an emitter to be notified
   * when the emitter finishes its lifecycle.
   */
  @FunctionalInterface public interface EmitterFinishedListener extends EventListener {

    /**
     * Called when the emitter has finished its lifecycle.
     *
     * @param emitter the emitter that has finished
     */
    void finished(Emitter emitter);
  }
}
