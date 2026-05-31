package de.gurkenlabs.litiengine.graphics.emitters.xml;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resource;
import de.gurkenlabs.litiengine.util.ColorHelper;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the data structure for a particle emitter.
 * <p>
 * This class is used to serialize and configure emitter properties such as color, particle movement, rendering options, and particle life cycle. It
 * supports XML binding for persistent configurations.
 * </p>
 */
@XmlRootElement(name = "emitter")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmitterAttributes implements Serializable, Resource {
  /**
   * Default base color used for emitted particles when no colors are configured.
   */
  public static final Color DEFAULT_COLOR = new Color(0, 165, 188, 204);
  /**
   * Default spritesheet name; empty means no spritesheet is associated.
   */
  public static final String DEFAULT_SPRITESHEET = "";
  /** Default display name assigned to a newly created emitter. */
  public static final String DEFAULT_NAME = "Custom Emitter";
  /** Default text content used by text-rendering particles. */
  public static final String DEFAULT_TEXT = "LITI";
  /** Default value controlling whether sprite-based particles are animated. */
  public static final boolean DEFAULT_ANIMATE_SPRITE = true;
  /** Default value controlling whether sprite-based particle animations are looped. */
  public static final boolean DEFAULT_LOOP_SPRITE = true;
  /** Default value controlling whether particles fade out over their lifetime. */
  public static final boolean DEFAULT_FADE = true;
  /** Default value controlling whether particles fade upon collision. */
  public static final boolean DEFAULT_FADE_ON_COLLISION = false;
  /** Default value controlling whether shape-based particles are rendered as outlines only. */
  public static final boolean DEFAULT_OUTLINE_ONLY = false;
  /** Default value controlling whether particles are rendered with anti-aliasing. */
  public static final boolean DEFAULT_ANTIALIASING = false;
  /** Default collision behavior applied to emitted particles. */
  public static final Collision DEFAULT_COLLISION = Collision.NONE;
  /** Default particle shape/type used when none is specified. */
  public static final ParticleType DEFAULT_PARTICLE_TYPE = ParticleType.RECTANGLE;
  /** Default minimum graphics quality required for the emitter to be rendered. */
  public static final Quality DEFAULT_REQUIRED_QUALITY = Quality.VERYLOW;
  /** Default horizontal alignment of the emitter origin. */
  public static final Align DEFAULT_ORIGIN_ALIGN = Align.CENTER;
  /** Default vertical alignment of the emitter origin. */
  public static final Valign DEFAULT_ORIGIN_VALIGN = Valign.MIDDLE;
  /** Default emitter width in pixels. */
  public static final float DEFAULT_WIDTH = 16f;
  /** Default emitter height in pixels. */
  public static final float DEFAULT_HEIGHT = 16f;
  /** Default color variance (0..1) applied per particle. */
  public static final float DEFAULT_COLOR_VARIANCE = 0f;
  /** Default alpha variance (0..1) applied per particle. */
  public static final float DEFAULT_ALPHA_VARIANCE = 0f;
  /** Default update rate of the emitter in milliseconds. */
  public static final int DEFAULT_UPDATERATE = 40;
  /** Default number of particles spawned per spawn tick. */
  public static final int DEFAULT_SPAWNAMOUNT = 20;
  /** Default spawn rate in milliseconds between spawn ticks. */
  public static final int DEFAULT_SPAWNRATE = 100;
  /** Default maximum number of concurrently alive particles. */
  public static final int DEFAULT_MAXPARTICLES = 400;
  /** Default emitter duration in milliseconds; {@code 0} means the emitter runs indefinitely. */
  public static final int DEFAULT_DURATION = 0;
  /** Default minimum time-to-live for a particle in milliseconds. */
  public static final int DEFAULT_MIN_PARTICLE_TTL = 400;
  /** Default maximum time-to-live for a particle in milliseconds. */
  public static final int DEFAULT_MAX_PARTICLE_TTL = 1500;
  /** Default minimum horizontal spawn offset relative to the emitter origin. */
  public static final float DEFAULT_MIN_OFFSET_X = -4f;
  /** Default maximum horizontal spawn offset relative to the emitter origin. */
  public static final float DEFAULT_MAX_OFFSET_X = 4f;
  /** Default minimum vertical spawn offset relative to the emitter origin. */
  public static final float DEFAULT_MIN_OFFSET_Y = -4f;
  /** Default maximum vertical spawn offset relative to the emitter origin. */
  public static final float DEFAULT_MAX_OFFSET_Y = 4f;
  /** Default minimum width delta applied per update to a particle. */
  public static final float DEFAULT_MIN_DELTA_WIDTH = -.1f;
  /** Default maximum width delta applied per update to a particle. */
  public static final float DEFAULT_MAX_DELTA_WIDTH = .1f;
  /** Default minimum height delta applied per update to a particle. */
  public static final float DEFAULT_MIN_DELTA_HEIGHT = -.1f;
  /** Default maximum height delta applied per update to a particle. */
  public static final float DEFAULT_MAX_DELTA_HEIGHT = .1f;
  /** Default minimum horizontal acceleration applied to a particle. */
  public static final float DEFAULT_MIN_ACCELERATION_X = -.01f;
  /** Default maximum horizontal acceleration applied to a particle. */
  public static final float DEFAULT_MAX_ACCELERATION_X = .01f;
  /** Default minimum vertical acceleration applied to a particle. */
  public static final float DEFAULT_MIN_ACCELERATION_Y = -.01f;
  /** Default maximum vertical acceleration applied to a particle. */
  public static final float DEFAULT_MAX_ACCELERATION_Y = .01f;
  /** Default minimum starting angle in degrees of a spawned particle. */
  public static final float DEFAULT_MIN_ANGLE = 0;
  /** Default maximum starting angle in degrees of a spawned particle. */
  public static final float DEFAULT_MAX_ROTATION = 360;
  /** Default minimum angle delta applied per update to a particle. */
  public static final float DEFAULT_MIN_DELTA_ANGLE = -1;
  /** Default maximum angle delta applied per update to a particle. */
  public static final float DEFAULT_MAX_DELTA_ANGLE = 1;
  /** Default minimum horizontal velocity of a spawned particle. */
  public static final float DEFAULT_MIN_VELOCITY_X = -.1f;
  /** Default maximum horizontal velocity of a spawned particle. */
  public static final float DEFAULT_MAX_VELOCITY_X = .1f;
  /** Default minimum vertical velocity of a spawned particle. */
  public static final float DEFAULT_MIN_VELOCITY_Y = -.1f;
  /** Default maximum vertical velocity of a spawned particle. */
  public static final float DEFAULT_MAX_VELOCITY_Y = .1f;
  /** Default minimum outline thickness for outline-rendered particles. */
  public static final float DEFAULT_MIN_OUTLINETHICKNESS = 1f;
  /** Default maximum outline thickness for outline-rendered particles. */
  public static final float DEFAULT_MAX_OUTLINETHICKNESS = 1f;
  /** Default minimum width of a spawned particle. */
  public static final float DEFAULT_MIN_WIDTH = 2f;
  /** Default maximum width of a spawned particle. */
  public static final float DEFAULT_MAX_WIDTH = 6f;
  /** Default minimum height of a spawned particle. */
  public static final float DEFAULT_MIN_HEIGHT = 2f;
  /** Default maximum height of a spawned particle. */
  public static final float DEFAULT_MAX_HEIGHT = 6f;
  @Serial
  private static final long serialVersionUID = 50238884097993529L;
  /** Per-particle alpha variance (0..1). */
  @XmlElement
  private float alphaVariance;

  /** Whether sprite-based particles are animated. */
  @XmlElement
  private boolean animateSprite;

  /** Whether sprite-based particle animations are looped. */
  @XmlElement
  private boolean loopSprite;

  /** Collision behavior applied to emitted particles. */
  @XmlElement
  private Collision collision;

  /** Minimum graphics quality required for this emitter to be rendered. */
  @XmlElement
  private Quality requiredQuality;

  /** Per-particle color variance (0..1). */
  @XmlElement
  private float colorVariance;

  /** Configured list of encoded color strings used when spawning particles. */
  @XmlElementWrapper
  @XmlElement(name = "color")
  private List<String> colors;

  /** Cached list of decoded colors derived from {@link #colors}. */
  @XmlTransient
  private List<Color> decodedColors;

  /** Per-particle height delta parameter. */
  @XmlElement
  private RangeAttribute<Float> deltaHeight;

  /** Per-particle width delta parameter. */
  @XmlElement
  private RangeAttribute<Float> deltaWidth;

  /** Horizontal velocity parameter of spawned particles. */
  @XmlElement
  private RangeAttribute<Float> velocityX;

  /** Vertical velocity parameter of spawned particles. */
  @XmlElement
  private RangeAttribute<Float> velocityY;

  /** Emitter duration in milliseconds; {@code 0} means the emitter runs indefinitely. */
  @XmlAttribute
  private int emitterDuration;

  /** Whether particles fade out over their lifetime. */
  @XmlElement
  private boolean fade;

  /** Whether particles fade upon collision. */
  @XmlElement
  private boolean fadeOnCollision;

  /** Whether shape-based particles are rendered as outlines only. */
  @XmlElement
  private boolean outlineOnly;

  /** Whether particles are rendered with anti-aliasing. */
  @XmlElement
  private boolean antiAliasing;

  /** Horizontal acceleration parameter of spawned particles. */
  @XmlElement
  private RangeAttribute<Float> accelerationX;

  /** Vertical acceleration parameter of spawned particles. */
  @XmlElement
  private RangeAttribute<Float> accelerationY;

  /** Starting angle parameter for spawned particles, in degrees. */
  @XmlElement
  private RangeAttribute<Float> angle;

  /** Per-particle angle delta parameter, in degrees per update. */
  @XmlElement
  private RangeAttribute<Float> deltaAngle;

  /** Outline thickness parameter applied to outline-rendered particles. */
  @XmlElement
  private RangeAttribute<Float> outlineThickness;

  /** Height of the emitter in pixels. */
  @XmlAttribute
  private float height;

  /** Maximum number of concurrently alive particles allowed. */
  @XmlAttribute
  private int maxParticles;

  /** Display name of the emitter. */
  @XmlAttribute
  private String name;

  /** Horizontal alignment of the emitter origin. */
  @XmlElement
  private Align originAlign;

  /** Vertical alignment of the emitter origin. */
  @XmlElement
  private Valign originValign;

  /** Per-particle height parameter. */
  @XmlElement
  private RangeAttribute<Float> particleHeight;

  /** Particle time-to-live (TTL) parameter in milliseconds. */
  @XmlElement
  private RangeAttribute<Long> particleTTL;

  /** List of texts used by text-rendering particles. */
  @XmlElementWrapper
  @XmlElement(name = "text")
  private List<String> texts;

  /** Particle shape/type used when spawning particles. */
  @XmlAttribute
  private ParticleType particleType;

  /** Per-particle width parameter. */
  @XmlElement
  private RangeAttribute<Float> particleWidth;

  /** Number of particles spawned per spawn tick. */
  @XmlAttribute
  private int spawnAmount;

  /** Spawn rate in milliseconds between spawn ticks. */
  @XmlAttribute
  private int spawnRate;

  /** Name of the spritesheet used by sprite-based particles. */
  @XmlElement
  private String spritesheet;

  /** Update rate of the emitter in milliseconds. */
  @XmlAttribute
  private int updateRate;

  /** Width of the emitter in pixels. */
  @XmlAttribute
  private float width;

  /** Horizontal spawn offset parameter relative to the emitter origin. */
  @XmlElement
  private RangeAttribute<Float> offsetX;

  /** Vertical spawn offset parameter relative to the emitter origin. */
  @XmlElement
  private RangeAttribute<Float> offsetY;

  /**
   * Creates a new {@code EmitterAttributes} instance and initializes all {@link RangeAttribute} fields and other defaults required for the emitter to be
   * rendered and updated correctly.
   */
  public EmitterAttributes() {
    // initialize fields required for rendering and updating properly.
    this.requiredQuality = DEFAULT_REQUIRED_QUALITY;
    this.offsetX = new RangeAttribute<>();
    this.offsetY = new RangeAttribute<>();
    this.deltaWidth = new RangeAttribute<>();
    this.deltaHeight = new RangeAttribute<>();
    this.angle = new RangeAttribute<>();
    this.deltaAngle = new RangeAttribute<>();
    this.velocityX = new RangeAttribute<>();
    this.velocityY = new RangeAttribute<>();
    this.accelerationX = new RangeAttribute<>();
    this.accelerationY = new RangeAttribute<>();
    this.outlineThickness = new RangeAttribute<>();
    this.particleWidth = new RangeAttribute<>();
    this.particleHeight = new RangeAttribute<>();
    this.particleTTL = new RangeAttribute<>();
    this.collision = DEFAULT_COLLISION;
    this.particleType = DEFAULT_PARTICLE_TYPE;
    this.originValign = DEFAULT_ORIGIN_VALIGN;
    this.originAlign = DEFAULT_ORIGIN_ALIGN;
    this.setColor(DEFAULT_COLOR);
  }

  /**
   * Gets the per-particle alpha variance.
   *
   * @return the alpha variance in the range {@code [0, 1]}
   */
  @XmlTransient
  public float getAlphaVariance() {
    return this.alphaVariance;
  }

  /**
   * Sets the per-particle alpha variance. The value is clamped to the range {@code [0, 1]}.
   *
   * @param alphaVariance the alpha variance to set
   */
  public void setAlphaVariance(final float alphaVariance) {
    this.alphaVariance = Math.clamp(alphaVariance, 0, 1);
  }

  /**
   * Gets the collision behavior of emitted particles.
   *
   * @return the configured {@link Collision} behavior
   */
  @XmlTransient
  public Collision getCollision() {
    return this.collision;
  }

  /**
   * Sets the collision behavior of emitted particles.
   *
   * @param collision the {@link Collision} behavior to apply
   */
  public void setCollision(final Collision collision) {
    this.collision = collision;
  }

  /**
   * Gets the minimum graphics {@link Quality} required for this emitter to be rendered.
   *
   * @return the required quality
   */
  @XmlTransient
  public Quality getRequiredQuality() {
    return this.requiredQuality;
  }

  /**
   * Sets the minimum graphics {@link Quality} required for this emitter to be rendered.
   *
   * @param minQuality the minimum required quality
   */
  public void setRequiredQuality(final Quality minQuality) {
    this.requiredQuality = minQuality;
  }

  /**
   * Gets the per-particle color variance.
   *
   * @return the color variance in the range {@code [0, 1]}
   */
  @XmlTransient
  public float getColorVariance() {
    return this.colorVariance;
  }

  /**
   * Sets the per-particle color variance. The value is clamped to the range {@code [0, 1]}.
   *
   * @param colorVariance the color variance to set
   */
  public void setColorVariance(final float colorVariance) {
    this.colorVariance = Math.clamp(colorVariance, 0, 1);
  }

  /**
   * Gets the configured list of color strings used when spawning particles.
   *
   * @return the list of encoded color strings
   */
  @XmlTransient
  public List<String> getColors() {
    return this.colors;
  }

  /**
   * Gets the decoded {@link Color} list derived from the configured color strings. The result is cached after the first call.
   *
   * @return the decoded colors, falling back to {@link #DEFAULT_COLOR} for entries that cannot be decoded
   */
  public List<Color> getDecodedColors() {
    if (this.decodedColors != null) {
      return this.decodedColors;
    }

    List<Color> newColors = new ArrayList<>();
    for (var color : this.getColors()) {
      Color decoded = ColorHelper.decode(color);
      newColors.add(decoded != null ? decoded : DEFAULT_COLOR);
    }

    this.decodedColors = newColors;
    return this.decodedColors;
  }

  /**
   * Sets the list of color strings used when spawning particles and invalidates the decoded color cache.
   *
   * @param colors the list of encoded color strings
   */
  public void setColors(final List<String> colors) {
    this.colors = colors;
    this.decodedColors = null;
  }

  /**
   * Sets the colors used when spawning particles by encoding the given AWT colors.
   *
   * @param colors the colors to apply
   */
  public void setColors(final Color... colors) {
    this.colors = Arrays.stream(colors).map(ColorHelper::encode).toList();
  }

  /**
   * Gets the per-particle height delta parameter.
   *
   * @return the height delta {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getDeltaHeight() {
    return this.deltaHeight;
  }

  /**
   * Sets the per-particle height delta parameter.
   *
   * @param deltaHeight the height delta parameter to set
   */
  public void setDeltaHeight(final RangeAttribute<Float> deltaHeight) {
    this.deltaHeight = deltaHeight;
  }

  /**
   * Gets the per-particle width delta parameter.
   *
   * @return the width delta {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getDeltaWidth() {
    return this.deltaWidth;
  }

  /**
   * Sets the per-particle width delta parameter.
   *
   * @param deltaWidth the width delta parameter to set
   */
  public void setDeltaWidth(final RangeAttribute<Float> deltaWidth) {
    this.deltaWidth = deltaWidth;
  }

  /**
   * Gets the starting angle parameter for spawned particles.
   *
   * @return the angle {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getAngle() {
    return this.angle;
  }

  /**
   * Sets the starting angle parameter for spawned particles.
   *
   * @param angle the angle parameter to set
   */
  public void setAngle(final RangeAttribute<Float> angle) {
    this.angle = angle;
  }

  /**
   * Gets the per-particle angle delta parameter.
   *
   * @return the angle delta {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getDeltaAngle() {
    return this.deltaAngle;
  }

  /**
   * Gets the horizontal velocity parameter of spawned particles.
   *
   * @return the horizontal velocity {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getVelocityX() {
    return this.velocityX;
  }

  /**
   * Sets the horizontal velocity parameter of spawned particles.
   *
   * @param velocityX the horizontal velocity parameter to set
   */
  public void setVelocityX(final RangeAttribute<Float> velocityX) {
    this.velocityX = velocityX;
  }

  /**
   * Gets the vertical velocity parameter of spawned particles.
   *
   * @return the vertical velocity {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getVelocityY() {
    return this.velocityY;
  }

  /**
   * Sets the vertical velocity parameter of spawned particles.
   *
   * @param velocityY the vertical velocity parameter to set
   */
  public void setVelocityY(final RangeAttribute<Float> velocityY) {
    this.velocityY = velocityY;
  }

  /**
   * Gets the emitter duration in milliseconds. A value of {@code 0} means the emitter runs indefinitely.
   *
   * @return the emitter duration in milliseconds
   */
  @XmlTransient
  public int getEmitterDuration() {
    return this.emitterDuration;
  }

  /**
   * Sets the emitter duration in milliseconds. A value of {@code 0} means the emitter runs indefinitely.
   *
   * @param emitterDuration the emitter duration in milliseconds
   */
  public void setEmitterDuration(final int emitterDuration) {
    this.emitterDuration = emitterDuration;
  }

  /**
   * Gets the horizontal acceleration parameter of spawned particles.
   *
   * @return the horizontal acceleration {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getAccelerationX() {
    return this.accelerationX;
  }

  /**
   * Sets the horizontal acceleration parameter of spawned particles.
   *
   * @param accelerationX the horizontal acceleration parameter to set
   */
  public void setAccelerationX(final RangeAttribute<Float> accelerationX) {
    this.accelerationX = accelerationX;
  }

  /**
   * Gets the vertical acceleration parameter of spawned particles.
   *
   * @return the vertical acceleration {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getAccelerationY() {
    return this.accelerationY;
  }

  /**
   * Sets the vertical acceleration parameter of spawned particles.
   *
   * @param accelerationY the vertical acceleration parameter to set
   */
  public void setAccelerationY(final RangeAttribute<Float> accelerationY) {
    this.accelerationY = accelerationY;
  }

  /**
   * Gets the outline thickness parameter applied to outline-rendered particles.
   *
   * @return the outline thickness {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getOutlineThickness() {
    return this.outlineThickness;
  }

  /**
   * Sets the outline thickness parameter applied to outline-rendered particles.
   *
   * @param outlineThickness the outline thickness parameter to set
   */
  public void setOutlineThickness(final RangeAttribute<Float> outlineThickness) {
    this.outlineThickness = outlineThickness;
  }

  /**
   * Gets the height of the emitter in pixels.
   *
   * @return the emitter height
   */
  @XmlTransient
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the height of the emitter in pixels.
   *
   * @param height the emitter height to set
   */
  public void setHeight(final float height) {
    this.height = height;
  }

  /**
   * Gets the maximum number of concurrently alive particles allowed for this emitter.
   *
   * @return the maximum number of particles
   */
  @XmlTransient
  public int getMaxParticles() {
    return this.maxParticles;
  }

  /**
   * Sets the maximum number of concurrently alive particles allowed for this emitter.
   *
   * @param maxParticles the maximum number of particles
   */
  public void setMaxParticles(final int maxParticles) {
    this.maxParticles = maxParticles;
  }

  @XmlTransient
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets the horizontal alignment of the emitter origin.
   *
   * @return the horizontal origin alignment
   */
  @XmlTransient
  public Align getOriginAlign() {
    return this.originAlign;
  }

  /**
   * Sets the horizontal alignment of the emitter origin.
   *
   * @param align the horizontal origin alignment to set
   */
  public void setOriginAlign(final Align align) {
    this.originAlign = align;
  }

  /**
   * Gets the vertical alignment of the emitter origin.
   *
   * @return the vertical origin alignment
   */
  @XmlTransient
  public Valign getOriginValign() {
    return this.originValign;
  }

  /**
   * Sets the vertical alignment of the emitter origin.
   *
   * @param valign the vertical origin alignment to set
   */
  public void setOriginValign(final Valign valign) {
    this.originValign = valign;
  }

  /**
   * Gets the per-particle height parameter.
   *
   * @return the particle height {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getParticleHeight() {
    return this.particleHeight;
  }

  /**
   * Sets the per-particle height parameter.
   *
   * @param particleHeight the particle height parameter to set
   */
  public void setParticleHeight(final RangeAttribute<Float> particleHeight) {
    this.particleHeight = particleHeight;
  }

  /**
   * Gets the particle time-to-live (TTL) parameter, in milliseconds.
   *
   * @return the particle TTL {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Long> getParticleTTL() {
    return this.particleTTL;
  }

  /**
   * Sets the particle time-to-live (TTL) parameter, in milliseconds.
   *
   * @param particleTTL the particle TTL parameter to set
   */
  public void setParticleTTL(final RangeAttribute<Long> particleTTL) {
    this.particleTTL = particleTTL;
  }

  /**
   * Gets the list of text strings used by text-rendering particles.
   *
   * @return the list of texts
   */
  @XmlTransient
  public List<String> getTexts() {
    return this.texts;
  }

  /**
   * Sets the list of text strings used by text-rendering particles.
   *
   * @param texts the list of texts
   */
  public void setTexts(final List<String> texts) {
    this.texts = texts;
  }

  /**
   * Gets the particle type (shape) used when spawning particles.
   *
   * @return the particle type
   */
  @XmlTransient
  public ParticleType getParticleType() {
    return this.particleType;
  }

  /**
   * Sets the particle type (shape) used when spawning particles.
   *
   * @param particleType the particle type to set
   */
  public void setParticleType(final ParticleType particleType) {
    this.particleType = particleType;
  }

  /**
   * Gets the per-particle width parameter.
   *
   * @return the particle width {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getParticleWidth() {
    return this.particleWidth;
  }

  /**
   * Sets the per-particle width parameter.
   *
   * @param particleWidth the particle width parameter to set
   */
  public void setParticleWidth(final RangeAttribute<Float> particleWidth) {
    this.particleWidth = particleWidth;
  }

  /**
   * Gets the horizontal spawn offset parameter relative to the emitter origin.
   *
   * @return the horizontal offset {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getParticleOffsetX() {
    return this.offsetX;
  }

  /**
   * Sets the horizontal spawn offset parameter relative to the emitter origin.
   *
   * @param x the horizontal offset parameter to set
   */
  public void setParticleOffsetX(final RangeAttribute<Float> x) {
    this.offsetX = x;
  }

  /**
   * Gets the vertical spawn offset parameter relative to the emitter origin.
   *
   * @return the vertical offset {@link RangeAttribute}
   */
  @XmlTransient
  public RangeAttribute<Float> getParticleOffsetY() {
    return this.offsetY;
  }

  /**
   * Sets the vertical spawn offset parameter relative to the emitter origin.
   *
   * @param y the vertical offset parameter to set
   */
  public void setParticleOffsetY(final RangeAttribute<Float> y) {
    this.offsetY = y;
  }

  /**
   * Gets the number of particles that are spawned per spawn tick.
   *
   * @return the spawn amount
   */
  @XmlTransient
  public int getSpawnAmount() {
    return this.spawnAmount;
  }

  /**
   * Sets the number of particles that are spawned per spawn tick.
   *
   * @param spawnAmount the spawn amount to set
   */
  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  /**
   * Gets the spawn rate in milliseconds between spawn ticks.
   *
   * @return the spawn rate
   */
  @XmlTransient
  public int getSpawnRate() {
    return this.spawnRate;
  }

  /**
   * Sets the spawn rate in milliseconds between spawn ticks.
   *
   * @param spawnRate the spawn rate to set
   */
  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
  }

  /**
   * Gets the name of the spritesheet used by sprite-based particles.
   *
   * @return the spritesheet name
   */
  @XmlTransient
  public String getSpritesheet() {
    return this.spritesheet;
  }

  /**
   * Sets the name of the spritesheet used by sprite-based particles.
   *
   * @param spritesheetName the spritesheet name
   */
  public void setSpritesheet(final String spritesheetName) {
    this.spritesheet = spritesheetName;
  }

  /**
   * Sets the spritesheet used by sprite-based particles using a {@link Spritesheet} reference.
   *
   * @param spritesheet the spritesheet whose name will be stored
   */
  public void setSpritesheet(final Spritesheet spritesheet) {
    this.spritesheet = spritesheet.getName();
  }

  /**
   * Gets the update rate of the emitter in milliseconds.
   *
   * @return the update rate
   */
  @XmlTransient
  public int getUpdateRate() {
    return this.updateRate;
  }

  /**
   * Sets the update rate of the emitter in milliseconds. A value of {@code 0} is ignored.
   *
   * @param updateRate the update rate to set
   */
  public void setUpdateRate(final int updateRate) {
    if (updateRate == 0) {
      return;
    }

    this.updateRate = updateRate;
  }

  /**
   * Gets the width of the emitter in pixels.
   *
   * @return the emitter width
   */
  @XmlTransient
  public float getWidth() {
    return this.width;
  }

  /**
   * Sets the width of the emitter in pixels.
   *
   * @param width the emitter width to set
   */
  public void setWidth(final float width) {
    this.width = width;
  }

  /**
   * Returns whether sprite-based particles are animated.
   *
   * @return {@code true} if sprite animation is enabled
   */
  public boolean isAnimatingSprite() {
    return this.animateSprite;
  }

  /**
   * Returns whether sprite-based particle animations are looped.
   *
   * @return {@code true} if sprite looping is enabled
   */
  public boolean isLoopingSprite() {
    return this.loopSprite;
  }

  /**
   * Returns whether particles fade out over their lifetime.
   *
   * @return {@code true} if particles fade
   */
  public boolean isFading() {
    return this.fade;
  }

  /**
   * Returns whether particles fade upon collision.
   *
   * @return {@code true} if particles fade on collision
   */
  public boolean isFadingOnCollision() {
    return this.fadeOnCollision;
  }

  /**
   * Returns whether shape-based particles are rendered as outlines only.
   *
   * @return {@code true} if outline-only rendering is enabled
   */
  public boolean isOutlineOnly() {
    return this.outlineOnly;
  }

  /**
   * Sets whether shape-based particles are rendered as outlines only.
   *
   * @param outlineOnly {@code true} to enable outline-only rendering
   */
  public void setOutlineOnly(final boolean outlineOnly) {
    this.outlineOnly = outlineOnly;
  }

  /**
   * Returns whether particles are rendered with anti-aliasing.
   *
   * @return {@code true} if anti-aliasing is enabled
   */
  public boolean isAntiAliased() {
    return this.antiAliasing;
  }

  /**
   * Sets whether sprite-based particles are animated.
   *
   * @param animateSprite {@code true} to enable sprite animation
   */
  public void setAnimateSprite(final boolean animateSprite) {
    this.animateSprite = animateSprite;
  }

  /**
   * Sets whether sprite-based particle animations are looped.
   *
   * @param loopSprite {@code true} to enable sprite looping
   */
  public void setLoopSprite(final boolean loopSprite) {
    this.loopSprite = loopSprite;
  }

  /**
   * Sets a single color used for spawning particles, replacing any previously configured colors.
   *
   * @param color the color to set
   */
  public void setColor(final Color color) {
    final List<String> tmpList = new ArrayList<>();
    tmpList.add(ColorHelper.encode(color));
    this.colors = tmpList;
  }

  /**
   * Initializes all configurable emitter and particle parameters to their {@code DEFAULT_*} values.
   */
  public void initDefaults() {
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.originAlign = DEFAULT_ORIGIN_ALIGN;
    this.originValign = DEFAULT_ORIGIN_VALIGN;
    this.offsetX.setMin(DEFAULT_MIN_OFFSET_X);
    this.offsetX.setMax(DEFAULT_MAX_OFFSET_X);
    this.offsetY.setMin(DEFAULT_MIN_OFFSET_Y);
    this.offsetY.setMax(DEFAULT_MAX_OFFSET_Y);
    this.deltaWidth.setMin(DEFAULT_MIN_DELTA_WIDTH);
    this.deltaWidth.setMax(DEFAULT_MAX_DELTA_WIDTH);
    this.deltaHeight.setMin(DEFAULT_MIN_DELTA_HEIGHT);
    this.deltaHeight.setMax(DEFAULT_MAX_DELTA_HEIGHT);
    this.angle.setMin(DEFAULT_MIN_ANGLE);
    this.angle.setMax(DEFAULT_MAX_ROTATION);
    this.deltaAngle.setMin(DEFAULT_MIN_DELTA_ANGLE);
    this.deltaAngle.setMax(DEFAULT_MAX_DELTA_ANGLE);
    this.velocityX.setMin(DEFAULT_MIN_VELOCITY_X);
    this.velocityX.setMax(DEFAULT_MAX_VELOCITY_X);
    this.velocityY.setMin(DEFAULT_MIN_VELOCITY_Y);
    this.velocityY.setMax(DEFAULT_MAX_VELOCITY_Y);
    this.outlineThickness.setMin(DEFAULT_MIN_OUTLINETHICKNESS);
    this.outlineThickness.setMax(DEFAULT_MAX_OUTLINETHICKNESS);
    this.accelerationX.setMin(DEFAULT_MIN_ACCELERATION_X);
    this.accelerationX.setMax(DEFAULT_MAX_ACCELERATION_X);
    this.accelerationY.setMin(DEFAULT_MIN_ACCELERATION_Y);
    this.accelerationY.setMax(DEFAULT_MAX_ACCELERATION_Y);
    this.particleWidth.setMin(DEFAULT_MIN_WIDTH);
    this.particleWidth.setMax(DEFAULT_MAX_WIDTH);
    this.particleHeight.setMin(DEFAULT_MIN_HEIGHT);
    this.particleHeight.setMax(DEFAULT_MAX_HEIGHT);
    this.particleTTL.setMin((long) DEFAULT_MIN_PARTICLE_TTL);
    this.particleTTL.setMax((long) DEFAULT_MAX_PARTICLE_TTL);

    this.setColor(DEFAULT_COLOR);
    this.emitterDuration = DEFAULT_DURATION;
    this.colorVariance = DEFAULT_COLOR_VARIANCE;
    this.alphaVariance = DEFAULT_ALPHA_VARIANCE;
    this.updateRate = DEFAULT_UPDATERATE;
    this.maxParticles = DEFAULT_MAXPARTICLES;
    this.name = DEFAULT_NAME;
    this.setText(DEFAULT_TEXT);
    this.spawnAmount = DEFAULT_SPAWNAMOUNT;
    this.spawnRate = DEFAULT_SPAWNRATE;
    this.animateSprite = DEFAULT_ANIMATE_SPRITE;
    this.loopSprite = DEFAULT_LOOP_SPRITE;
    this.spritesheet = DEFAULT_SPRITESHEET;
    this.fade = DEFAULT_FADE;
    this.fadeOnCollision = DEFAULT_FADE_ON_COLLISION;
    this.outlineOnly = DEFAULT_OUTLINE_ONLY;
    this.antiAliasing = DEFAULT_ANTIALIASING;
  }

  /**
   * Sets the per-particle angle delta parameter.
   *
   * @param deltaRotation the angle delta parameter to set
   */
  public void setDeltaRotation(final RangeAttribute<Float> deltaRotation) {
    this.deltaAngle = deltaRotation;
  }

  /**
   * Sets whether particles fade out over their lifetime.
   *
   * @param fade {@code true} to enable fading
   */
  public void setFade(final boolean fade) {
    this.fade = fade;
  }

  /**
   * Sets whether particles fade upon collision.
   *
   * @param fadeOnCollision {@code true} to enable fade-on-collision
   */
  public void setFadeOnCollision(final boolean fadeOnCollision) {
    this.fadeOnCollision = fadeOnCollision;
  }

  /**
   * Sets whether particles are rendered with anti-aliasing.
   *
   * @param antiAliasing {@code true} to enable anti-aliasing
   */
  public void setAntiAliasing(final boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
  }

  /**
   * Sets a single text used by text-rendering particles, replacing any previously configured texts.
   *
   * @param text the text to set
   */
  public void setText(final String text) {
    final List<String> tmpList = new ArrayList<>();
    tmpList.add(text);
    this.texts = tmpList;
  }
}
