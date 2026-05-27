package de.gurkenlabs.litiengine.graphics.emitters.particles;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.physics.Collision;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Base class for particles spawned by a {@link Emitter}. A particle has a position, velocity, acceleration, lifetime, color and rendering options.
 * <p>
 * Subclasses are responsible for implementing {@link #render(Graphics2D, Point2D)} which draws the particle relative to the emitter origin.
 * </p>
 */
public abstract class Particle implements ITimeToLive {

  private static final Color DEFAULT_COLOR = Color.BLACK;
  private long aliveTick;
  private long aliveTime;
  private float angle;
  private float deltaAngle;

  private Collision collisionType;
  private Color color = DEFAULT_COLOR;
  private float deltaHeight;
  private float deltaWidth;
  /**
   * The horizontal velocity (horizontal movement per update) for this particle.
   */
  private float velocityX;
  /**
   * The vertical velocity (vertical movement per update) for this particle.
   */
  private float velocityY;
  private float outlineThickness;
  private boolean outlineOnly;
  private boolean antiAliasing;

  /**
   * The horizontal acceleration (increase / decrease in velocity over time) for this particle.
   */
  private float accelerationX;

  /**
   * The vertical acceleration (increase / decrease in velocity over time) for this particle.
   */
  private float accelerationY;
  private float height;
  private int timeToLive;
  private float width;

  /**
   * The current location of the particle on the X-axis.
   */
  private float x;

  /**
   * The current location of the particle on the Y-axis.
   */
  private float y;

  private RenderType customRenderType = RenderType.NONE;
  private boolean useCustomRenderType;

  private boolean fade;

  private boolean fadeOnCollision;

  private boolean colliding;

  private boolean continuousCollision;

  private boolean stopOnCollision;

  /**
   * Constructs a new particle.
   *
   * @param width  the particle width in pixels
   * @param height the particle height in pixels
   */
  protected Particle(final float width, final float height) {
    this.setWidth(width);
    this.setHeight(height);
    this.collisionType = Collision.NONE;
    this.fade = true;
    this.setStopOnCollision(true);
    this.setContinuousCollision(false);
  }

  @Override
  public long getAliveTime() {
    return aliveTime;
  }

  /**
   * Gets the current bounding box of the particle, depending on its spawn location.
   *
   * @param origin the spawn location of this particle
   * @return The Rectangular particle bounding box.
   */
  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + this.getX(), origin.getY() + this.getY(),
      this.getWidth(), this.getHeight());
  }

  /**
   * Gets the collision behavior of this particle.
   *
   * @return the collision behavior
   */
  public Collision getCollisionType() {
    return collisionType;
  }

  /**
   * Gets the current color of the particle.
   *
   * @return the color
   */
  public Color getColor() {
    return color;
  }

  /**
   * Gets the per-update height delta applied to the particle.
   *
   * @return the height delta
   */
  public float getDeltaHeight() {
    return deltaHeight;
  }

  /**
   * Gets the per-update width delta applied to the particle.
   *
   * @return the width delta
   */
  public float getDeltaWidth() {
    return deltaWidth;
  }

  /**
   * Gets the horizontal velocity of the particle.
   *
   * @return the horizontal velocity
   */
  public float getVelocityX() {
    return velocityX;
  }

  /**
   * Gets the vertical velocity of the particle.
   *
   * @return the vertical velocity
   */
  public float getVelocityY() {
    return velocityY;
  }

  /**
   * Gets the horizontal acceleration of the particle.
   *
   * @return the horizontal acceleration
   */
  public float getAccelerationX() {
    return accelerationX;
  }

  /**
   * Gets the vertical acceleration of the particle.
   *
   * @return the vertical acceleration
   */
  public float getAccelerationY() {
    return accelerationY;
  }

  /**
   * Gets the current rotation angle of the particle, in degrees.
   *
   * @return the current angle
   */
  public float getAngle() {
    return angle;
  }

  /**
   * Gets the per-update angle delta applied to the particle, in degrees.
   *
   * @return the angle delta
   */
  public float getDeltaAngle() {
    return deltaAngle;
  }

  /**
   * Gets the current height of the particle, in pixels.
   *
   * @return the particle height
   */
  public float getHeight() {
    return height;
  }

  /**
   * Gets the outline thickness used when rendering the particle as an outline.
   *
   * @return the outline thickness
   */
  public float getOutlineThickness() {
    return outlineThickness;
  }

  /**
   * Returns whether the particle is rendered as an outline only.
   *
   * @return {@code true} if outline-only rendering is enabled
   */
  public boolean isOutlineOnly() {
    return outlineOnly;
  }

  /**
   * Returns whether the particle is rendered with anti-aliasing.
   *
   * @return {@code true} if anti-aliasing is enabled
   */
  public boolean isAntiAliased() {
    return antiAliasing;
  }

  /**
   * Computes the current opacity of the particle. If fading is enabled and the particle has a finite time-to-live, the opacity decreases linearly
   * from the color's initial alpha towards zero as the particle ages.
   *
   * @return the opacity in the range {@code [0, 1]}
   */
  public float getOpacity() {
    if (isFading() && getTimeToLive() > 0) {
      float maxAlpha = getColor().getAlpha() / 255f;
      float progress = (float) getAliveTime() / getTimeToLive();
      return Math.clamp(maxAlpha - progress * maxAlpha, 0, 1);
    }
    return 1;
  }

  /**
   * Gets the location relative to the specified effect location.
   *
   * @param effectLocation the effect position
   * @return the location
   */
  public Point2D getRenderLocation(Point2D effectLocation) {
    // if we have a camera, we need to render the particle relative to the
    // viewport
    Point2D newEffectLocation =
      Game.screens() != null ? Game.world().camera().getViewportLocation(effectLocation)
        : effectLocation;
    return getAbsoluteLocation(newEffectLocation);
  }

  /**
   * Gets the custom render type that overrides the emitter's render type, if {@link #usesCustomRenderType()} is {@code true}.
   *
   * @return the custom render type
   */
  public RenderType getCustomRenderType() {
    return customRenderType;
  }

  @Override
  public int getTimeToLive() {
    return timeToLive;
  }

  /**
   * Gets the current width of the particle, in pixels.
   *
   * @return the particle width
   */
  public float getWidth() {
    return width;
  }

  /**
   * Gets the X position of the particle relative to its emitter origin.
   *
   * @return the X position
   */
  public float getX() {
    return x;
  }

  /**
   * Gets the Y position of the particle relative to its emitter origin.
   *
   * @return the Y position
   */
  public float getY() {
    return y;
  }

  /**
   * Returns whether the particle fades out over its lifetime.
   *
   * @return {@code true} if fading is enabled
   */
  public boolean isFading() {
    return fade;
  }

  /**
   * Returns whether the particle fades upon collision.
   *
   * @return {@code true} if fade-on-collision is enabled
   */
  public boolean isFadingOnCollision() {
    return fadeOnCollision;
  }

  /**
   * Returns whether continuous (ray-cast) collision detection is enabled for this particle.
   *
   * @return {@code true} if continuous collision detection is enabled
   */
  public boolean isContinuousCollisionEnabled() {
    return continuousCollision;
  }

  /**
   * Returns whether the particle stops moving once a collision occurs.
   *
   * @return {@code true} if the particle stops on collision
   */
  public boolean isStoppingOnCollision() {
    return stopOnCollision;
  }

  /**
   * Renders this particle to the given graphics context.
   *
   * @param g             the graphics context to draw to
   * @param emitterOrigin the world location of the owning emitter
   */
  public abstract void render(final Graphics2D g, final Point2D emitterOrigin);

  /**
   * Sets the collision behavior of this particle.
   *
   * @param collisionType the new collision behavior
   * @return this particle instance for chaining
   */
  public Particle setCollisionType(final Collision collisionType) {
    this.collisionType = collisionType;
    return this;
  }

  /**
   * Enabling this check can be very performance hungry and should be used with caution and only for a small amount of particles.
   *
   * @param ccd If set to true, the collision will be checked continuously by a ray-cast approximation.
   * @return This particle instance.
   */
  public Particle setContinuousCollision(boolean ccd) {
    this.continuousCollision = ccd;
    return this;
  }

  /**
   * Sets whether the particle stops moving when it collides.
   *
   * @param stopOnCollision {@code true} to stop the particle on collision
   * @return this particle instance for chaining
   */
  public Particle setStopOnCollision(boolean stopOnCollision) {
    this.stopOnCollision = stopOnCollision;
    return this;
  }

  /**
   * Sets the color of the particle. {@code null} values are ignored.
   *
   * @param color the new color
   * @return this particle instance for chaining
   */
  public Particle setColor(final Color color) {
    if (color != null) {
      this.color = color;
    }
    return this;
  }

  /**
   * Sets the per-update height delta applied to the particle.
   *
   * @param deltaHeight the height delta
   * @return this particle instance for chaining
   */
  public Particle setDeltaHeight(final float deltaHeight) {
    this.deltaHeight = deltaHeight;
    return this;
  }

  /**
   * Sets the horizontal acceleration of the particle.
   *
   * @param accelerationX the horizontal acceleration
   * @return this particle instance for chaining
   */
  public Particle setAccelerationX(final float accelerationX) {
    this.accelerationX = accelerationX;
    return this;
  }

  /**
   * Sets the vertical acceleration of the particle.
   *
   * @param accelerationY the vertical acceleration
   * @return this particle instance for chaining
   */
  public Particle setAccelerationY(final float accelerationY) {
    this.accelerationY = accelerationY;
    return this;
  }

  /**
   * Sets the current rotation angle of the particle, in degrees.
   *
   * @param angle the angle to set
   * @return this particle instance for chaining
   */
  public Particle setAngle(final float angle) {
    this.angle = angle;
    return this;
  }

  /**
   * Sets the per-update angle delta applied to the particle, in degrees.
   *
   * @param deltaAngle the angle delta
   * @return this particle instance for chaining
   */
  public Particle setDeltaAngle(final float deltaAngle) {
    this.deltaAngle = deltaAngle;
    return this;
  }

  /**
   * Sets the per-update width delta applied to the particle.
   *
   * @param deltaWidth the width delta
   * @return this particle instance for chaining
   */
  public Particle setDeltaWidth(final float deltaWidth) {
    this.deltaWidth = deltaWidth;
    return this;
  }

  /**
   * Sets the horizontal velocity of the particle.
   *
   * @param velocityX the horizontal velocity
   * @return this particle instance for chaining
   */
  public Particle setVelocityX(final float velocityX) {
    this.velocityX = velocityX;
    return this;
  }

  /**
   * Sets the vertical velocity of the particle.
   *
   * @param velocityY the vertical velocity
   * @return this particle instance for chaining
   */
  public Particle setVelocityY(final float velocityY) {
    this.velocityY = velocityY;
    return this;
  }

  /**
   * Sets whether the particle fades out over its lifetime.
   *
   * @param fade {@code true} to enable fading
   * @return this particle instance for chaining
   */
  public Particle setFade(boolean fade) {
    this.fade = fade;
    return this;
  }

  /**
   * Sets whether the particle fades upon collision.
   *
   * @param fadeOnCollision {@code true} to enable fade-on-collision
   * @return this particle instance for chaining
   */
  public Particle setFadeOnCollision(boolean fadeOnCollision) {
    this.fadeOnCollision = fadeOnCollision;
    return this;
  }

  /**
   * Sets the height of the particle, in pixels.
   *
   * @param height the height to set
   * @return this particle instance for chaining
   */
  public Particle setHeight(final float height) {
    this.height = height;
    return this;
  }

  /**
   * Sets the outline thickness used when rendering the particle as an outline.
   *
   * @param outlineThickness the outline thickness
   * @return this particle instance for chaining
   */
  public Particle setOutlineThickness(final float outlineThickness) {
    this.outlineThickness = outlineThickness;
    return this;
  }

  /**
   * Sets whether the particle is rendered as an outline only.
   *
   * @param outlineOnly {@code true} to enable outline-only rendering
   * @return this particle instance for chaining
   */
  public Particle setOutlineOnly(final boolean outlineOnly) {
    this.outlineOnly = outlineOnly;
    return this;
  }

  /**
   * Sets whether the particle is rendered with anti-aliasing.
   *
   * @param antiAliasing {@code true} to enable anti-aliasing
   * @return this particle instance for chaining
   */
  public Particle setAntiAliasing(final boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
    return this;
  }

  /**
   * Sets a custom render type that overrides the emitter's render type for this particle.
   *
   * @param renderType the render type to use
   * @return this particle instance for chaining
   */
  public Particle setCustomRenderType(RenderType renderType) {
    this.customRenderType = renderType;
    this.useCustomRenderType = true;
    return this;
  }

  /**
   * Sets the width of the particle, in pixels.
   *
   * @param width the width to set
   * @return this particle instance for chaining
   */
  public Particle setWidth(final float width) {
    this.width = width;
    return this;
  }

  /**
   * Sets the X position of the particle relative to its emitter origin.
   *
   * @param x the X position
   * @return this particle instance for chaining
   */
  public Particle setX(final float x) {
    this.x = x;
    return this;
  }

  /**
   * Sets the Y position of the particle relative to its emitter origin.
   *
   * @param y the Y position
   * @return this particle instance for chaining
   */
  public Particle setY(final float y) {
    this.y = y;
    return this;
  }

  /**
   * Sets the time-to-live (TTL) of the particle, in milliseconds.
   *
   * @param ttl the TTL to set
   * @return this particle instance for chaining
   */
  public Particle setTimeToLive(final int ttl) {
    this.timeToLive = ttl;
    return this;
  }

  /**
   * Initializes this particle's mutable state from the supplied {@link EmitterData}.
   *
   * @param data the emitter data providing initial parameter values
   * @return this particle instance for chaining
   */
  public Particle init(final EmitterData data) {
    this.setX((float) data.getParticleOffsetX().get());
    this.setY((float) data.getParticleOffsetY().get());

    this.setAccelerationX((float) data.getAccelerationX().get());
    this.setAccelerationY((float) data.getAccelerationY().get());

    this.setVelocityX((float) data.getVelocityX().get());
    this.setVelocityY((float) data.getVelocityY().get());

    this.setDeltaWidth((float) data.getDeltaWidth().get());
    this.setDeltaHeight((float) data.getDeltaHeight().get());

    this.setAngle((float) data.getAngle().get());
    this.setDeltaAngle((float) data.getDeltaAngle().get());

    this.setTimeToLive((int) data.getParticleTTL().get());
    this.setColor(Game.random().choose(data.getDecodedColors()));

    this.setOutlineThickness((float) data.getOutlineThickness().get());

    this.setCollisionType(data.getCollision());
    this.setOutlineOnly(data.isOutlineOnly());
    this.setAntiAliasing(data.isAntiAliased());
    this.setFade(data.isFading());

    this.setFadeOnCollision(data.isFadingOnCollision());
    return this;
  }

  @Override
  public boolean timeToLiveReached() {
    return getTimeToLive() > 0 && this.getAliveTime() >= this.getTimeToLive();
  }

  /**
   * Updates the effect's position, change in xCurrent, change in yCurrent, remaining lifetime, and color.
   *
   * @param emitterOrigin The current {@link Emitter} origin
   * @param updateRatio   The update ratio for this particle.
   */
  public void update(final Point2D emitterOrigin, final float updateRatio) {
    if (this.aliveTick == 0) {
      this.aliveTick = Game.time().now();
    }

    this.aliveTime = Game.time().since(this.aliveTick);
    if (this.timeToLiveReached()) {
      return;
    }

    if (this.colliding) {
      return;
    }

    if (this.getDeltaWidth() != 0) {
      this.width += this.getDeltaWidth() * updateRatio;
    }

    if (this.getDeltaHeight() != 0) {
      this.height += this.getDeltaHeight() * updateRatio;
    }

    if (this.getDeltaAngle() != 0) {
      this.angle += this.getDeltaAngle() * updateRatio;
    }

    if (hasRayCastCollision(emitterOrigin, updateRatio)) {
      return;
    }

    if (this.getAccelerationX() != 0) {
      this.velocityX += this.getAccelerationX() * updateRatio;
    }

    if (this.getAccelerationY() != 0) {
      this.velocityY += this.getAccelerationY() * updateRatio;
    }
  }


  /**
   * Test for ray cast collisions
   *
   * @param emitterOrigin The current {@link Emitter} origin
   * @param updateRatio   The update ratio for this particle.
   * @return True if ray cast collision occurs
   */
  protected boolean hasRayCastCollision(final Point2D emitterOrigin, final float updateRatio) {
    final float targetX = this.x + this.getVelocityX() * updateRatio;
    final float targetY = this.y + this.getVelocityY() * updateRatio;

    if (targetX == this.x && targetY == this.y) {
      return true;
    }

    if (this.checkForCollision(emitterOrigin, targetX, targetY)) {
      return true;
    }

    if (this.getVelocityX() != 0) {
      this.x = targetX;
    }

    if (this.getVelocityY() != 0) {
      this.y = targetY;
    }

    return false;
  }

  private boolean checkForCollision(final Point2D emitterOrigin, float targetX, float targetY) {
    if (this.isStoppingOnCollision() && this.colliding) {
      return true;
    }

    if (this.isContinuousCollisionEnabled()) {
      Point2D start = this.getAbsoluteLocation(emitterOrigin);
      double endX = emitterOrigin.getX() + targetX;
      double endY = emitterOrigin.getY() + targetY;
      Line2D ray = new Line2D.Double(start.getX(), start.getY(), endX, endY);
      if (this.getCollisionType() != Collision.NONE && Game.physics() != null && Game.physics()
        .collides(ray, this.getCollisionType())) {
        collide();
        return true;
      }
    } else if (this.getCollisionType() != Collision.NONE && Game.physics() != null && Game.physics()
      .collides(this.getBoundingBox(emitterOrigin).getBounds2D(), this.getCollisionType())) {
      collide();
      return true;
    }

    return false;
  }

  private void collide() {
    if (!this.colliding) {
      this.colliding = true;
      if (this.isFadingOnCollision()) {
        this.setFade(true);
      }
    }
  }

  /**
   * Computes the absolute world location of this particle given an emitter origin.
   *
   * @param effectLocation the emitter origin
   * @return the absolute world location of the particle
   */
  public Point2D getAbsoluteLocation(final Point2D effectLocation) {
    return new Point2D.Float(getAbsoluteX(effectLocation), getAbsoluteY(effectLocation));
  }

  /**
   * Computes the absolute world X coordinate of this particle, accounting for the particle's width.
   *
   * @param emitterOrigin the emitter origin
   * @return the absolute X coordinate
   */
  protected float getAbsoluteX(Point2D emitterOrigin) {
    return (float) (emitterOrigin.getX() + this.getX() - this.getWidth() / 2.0);
  }

  /**
   * Computes the absolute world Y coordinate of this particle, accounting for the particle's height.
   *
   * @param emitterOrigin the emitter origin
   * @return the absolute Y coordinate
   */
  protected float getAbsoluteY(Point2D emitterOrigin) {
    return (float) (emitterOrigin.getY() + this.getY() - this.getHeight() / 2.0);
  }

  /**
   * Returns whether this particle has a custom render type that overrides the emitter's render type.
   *
   * @return {@code true} if a custom render type is set
   */
  public boolean usesCustomRenderType() {
    return useCustomRenderType;
  }
}
