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
import de.gurkenlabs.litiengine.util.MathUtilities;
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

@XmlRootElement(name = "emitter")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmitterData implements Serializable, Resource {

  public static final Color DEFAULT_COLOR = ColorHelper.decode("#CC00a5bc");
  public static final String DEFAULT_SPRITESHEET = "";
  public static final String DEFAULT_NAME = "Custom Emitter";
  public static final String DEFAULT_TEXT = "LITI";
  public static final boolean DEFAULT_ANIMATE_SPRITE = true;
  public static final boolean DEFAULT_LOOP_SPRITE = true;
  public static final boolean DEFAULT_FADE = true;
  public static final boolean DEFAULT_FADE_ON_COLLISION = false;
  public static final boolean DEFAULT_OUTLINE_ONLY = false;
  public static final boolean DEFAULT_ANTIALIASING = false;
  public static final Collision DEFAULT_COLLISION = Collision.NONE;
  public static final ParticleType DEFAULT_PARTICLE_TYPE = ParticleType.RECTANGLE;
  public static final Quality DEFAULT_REQUIRED_QUALITY = Quality.VERYLOW;
  public static final Align DEFAULT_ORIGIN_ALIGN = Align.CENTER;
  public static final Valign DEFAULT_ORIGIN_VALIGN = Valign.MIDDLE;
  public static final float DEFAULT_WIDTH = 16f;
  public static final float DEFAULT_HEIGHT = 16f;
  public static final float DEFAULT_COLOR_VARIANCE = 0f;
  public static final float DEFAULT_ALPHA_VARIANCE = 0f;
  public static final int DEFAULT_UPDATERATE = 40;
  public static final int DEFAULT_SPAWNAMOUNT = 20;
  public static final int DEFAULT_SPAWNRATE = 100;
  public static final int DEFAULT_MAXPARTICLES = 400;
  public static final int DEFAULT_DURATION = 0;
  public static final long DEFAULT_MIN_PARTICLE_TTL = 400;
  public static final long DEFAULT_MAX_PARTICLE_TTL = 1500;
  public static final float DEFAULT_MIN_OFFSET_X = -4f;
  public static final float DEFAULT_MAX_OFFSET_X = 4f;
  public static final float DEFAULT_MIN_OFFSET_Y = -4f;
  public static final float DEFAULT_MAX_OFFSET_Y = 4f;
  public static final float DEFAULT_MIN_DELTA_WIDTH = -.1f;
  public static final float DEFAULT_MAX_DELTA_WIDTH = .1f;
  public static final float DEFAULT_MIN_DELTA_HEIGHT = -.1f;
  public static final float DEFAULT_MAX_DELTA_HEIGHT = .1f;
  public static final float DEFAULT_MIN_ACCELERATION_X = -.01f;
  public static final float DEFAULT_MAX_ACCELERATION_X = .01f;
  public static final float DEFAULT_MIN_ACCELERATION_Y = -.01f;
  public static final float DEFAULT_MAX_ACCELERATION_Y = .01f;
  public static final float DEFAULT_MIN_ANGLE = 0f;
  public static final float DEFAULT_MAX_ANGLE = 360f;
  public static final float DEFAULT_MIN_DELTA_ANGLE = -1;
  public static final float DEFAULT_MAX_DELTA_ANGLE = 1;
  public static final float DEFAULT_MIN_VELOCITY_X = -.1f;
  public static final float DEFAULT_MAX_VELOCITY_X = .1f;
  public static final float DEFAULT_MIN_VELOCITY_Y = -.1f;
  public static final float DEFAULT_MAX_VELOCITY_Y = .1f;
  public static final float DEFAULT_MIN_WIDTH = 2f;
  public static final float DEFAULT_MAX_WIDTH = 6f;
  public static final float DEFAULT_MIN_HEIGHT = 2f;
  public static final float DEFAULT_MAX_HEIGHT = 6f;
  @Serial
  private static final long serialVersionUID = 50238884097993529L;
  @XmlElement
  private float alphaVariance;

  @XmlElement
  private boolean animateSprite;

  @XmlElement
  private boolean loopSprite;

  @XmlElement
  private Collision collision;

  @XmlElement
  private Quality requiredQuality;

  @XmlElement
  private float colorVariance;

  @XmlElementWrapper
  @XmlElement(name = "color")
  private List<String> colors;

  @XmlTransient
  private List<Color> decodedColors;

  @XmlElement
  private RangeAttribute<Float> deltaHeight;

  @XmlElement
  private RangeAttribute<Float> deltaWidth;

  @XmlElement
  private RangeAttribute<Float> velocityX;

  @XmlElement
  private RangeAttribute<Float> velocityY;

  @XmlAttribute
  private int emitterDuration;

  @XmlElement
  private boolean fade;

  @XmlElement
  private boolean fadeOnCollision;

  @XmlElement
  private boolean outlineOnly;

  @XmlElement
  private boolean antiAliasing;

  @XmlElement
  private RangeAttribute<Float> accelerationX;

  @XmlElement
  private RangeAttribute<Float> accelerationY;

  @XmlElement
  private RangeAttribute<Float> angle;

  @XmlElement
  private RangeAttribute<Float> deltaAngle;

  @XmlAttribute
  private float height;

  @XmlAttribute
  private int maxParticles;

  @XmlAttribute
  private String name;

  @XmlElement
  private Align originAlign;

  @XmlElement
  private Valign originValign;

  @XmlElement
  private RangeAttribute<Float> particleHeight;

  @XmlElement
  private RangeAttribute<Long> particleTTL;

  @XmlElementWrapper
  @XmlElement(name = "text")
  private List<String> texts;

  @XmlAttribute
  private ParticleType particleType;

  @XmlElement
  private RangeAttribute<Float> particleWidth;

  @XmlAttribute
  private int spawnAmount;

  @XmlAttribute
  private int spawnRate;

  @XmlElement
  private String spritesheet;

  @XmlAttribute
  private int updateRate;

  @XmlAttribute
  private float width;

  @XmlElement
  private RangeAttribute<Float> offsetX;

  @XmlElement
  private RangeAttribute<Float> offsetY;

  public EmitterData() {
    // initialize fields required for rendering and updating properly.
    setRequiredQuality(DEFAULT_REQUIRED_QUALITY);
    setParticleOffsetX(new RangeAttribute<>(DEFAULT_MAX_OFFSET_X, DEFAULT_MIN_OFFSET_X,
      DEFAULT_MIN_OFFSET_X));
    setParticleOffsetY(new RangeAttribute<>(DEFAULT_MAX_OFFSET_Y, DEFAULT_MIN_OFFSET_Y,
      DEFAULT_MIN_OFFSET_Y));
    setDeltaWidth(new RangeAttribute<>(DEFAULT_MAX_WIDTH, DEFAULT_MIN_WIDTH, DEFAULT_MIN_WIDTH));
    setDeltaHeight(new RangeAttribute<>(DEFAULT_MAX_HEIGHT, DEFAULT_MIN_HEIGHT,
      DEFAULT_MIN_HEIGHT));
    setAngle(new RangeAttribute<>(DEFAULT_MAX_ANGLE, DEFAULT_MIN_ANGLE, DEFAULT_MIN_ANGLE));
    setDeltaAngle(new RangeAttribute<>(DEFAULT_MAX_DELTA_ANGLE, DEFAULT_MIN_DELTA_ANGLE,
      DEFAULT_MIN_DELTA_ANGLE));
    setVelocityX(new RangeAttribute<>(DEFAULT_MAX_VELOCITY_X, DEFAULT_MIN_VELOCITY_X,
      DEFAULT_MIN_VELOCITY_X));
    setVelocityY(new RangeAttribute<>(DEFAULT_MAX_VELOCITY_Y, DEFAULT_MIN_VELOCITY_Y,
      DEFAULT_MIN_VELOCITY_Y));
    setAccelerationX(new RangeAttribute<>(DEFAULT_MAX_ACCELERATION_X,
      DEFAULT_MIN_ACCELERATION_X, DEFAULT_MIN_ACCELERATION_X));
    setAccelerationY(new RangeAttribute<>(DEFAULT_MAX_ACCELERATION_Y,
      DEFAULT_MIN_ACCELERATION_Y, DEFAULT_MIN_ACCELERATION_Y));
    setParticleWidth(new RangeAttribute<>(DEFAULT_MIN_WIDTH, DEFAULT_MIN_WIDTH,
      DEFAULT_MIN_WIDTH));
    setParticleHeight(new RangeAttribute<>(DEFAULT_MAX_HEIGHT, DEFAULT_MIN_HEIGHT,
      DEFAULT_MIN_HEIGHT));
    setParticleTTL(new RangeAttribute<>(DEFAULT_MAX_PARTICLE_TTL, DEFAULT_MIN_PARTICLE_TTL,
      DEFAULT_MIN_PARTICLE_TTL));
    setCollision(DEFAULT_COLLISION);
    setParticleType(DEFAULT_PARTICLE_TYPE);
    setOriginValign(DEFAULT_ORIGIN_VALIGN);
    setOriginAlign(DEFAULT_ORIGIN_ALIGN);
    setColor(DEFAULT_COLOR);
  }

  @XmlTransient
  public float getAlphaVariance() {
    return this.alphaVariance;
  }

  public void setAlphaVariance(final float alphaVariance) {
    this.alphaVariance = MathUtilities.clamp(alphaVariance, 0, 1);
  }

  @XmlTransient
  public Collision getCollision() {
    return this.collision;
  }

  public void setCollision(final Collision collision) {
    this.collision = collision;
  }

  @XmlTransient
  public Quality getRequiredQuality() {
    return this.requiredQuality;
  }

  public void setRequiredQuality(final Quality minQuality) {
    this.requiredQuality = minQuality;
  }

  @XmlTransient
  public float getColorVariance() {
    return this.colorVariance;
  }

  public void setColorVariance(final float colorVariance) {
    this.colorVariance = MathUtilities.clamp(colorVariance, 0, 1);
  }

  @XmlTransient
  public List<String> getColors() {
    return this.colors;
  }

  public List<Color> getDecodedColors() {
    if (this.decodedColors != null) {
      return this.decodedColors;
    }

    List<Color> cols = new ArrayList<>();
    for (var color : getColors()) {
      Color decoded = ColorHelper.decode(color);
      cols.add(decoded != null ? decoded : DEFAULT_COLOR);
    }

    this.decodedColors = cols;
    return decodedColors;
  }

  public void setColors(final List<String> colors) {
    this.colors = colors;
    this.decodedColors = null;
  }

  public void setColors(final Color... colors) {
    this.colors = Arrays.stream(colors).map(ColorHelper::encode).toList();
  }

  @XmlTransient
  public RangeAttribute<Float> getDeltaHeight() {
    return this.deltaHeight;
  }

  public void setDeltaHeight(final RangeAttribute<Float> deltaHeight) {
    this.deltaHeight = deltaHeight;
  }

  @XmlTransient
  public RangeAttribute<Float> getDeltaWidth() {
    return this.deltaWidth;
  }

  public void setDeltaWidth(final RangeAttribute<Float> deltaWidth) {
    this.deltaWidth = deltaWidth;
  }

  @XmlTransient
  public RangeAttribute<Float> getAngle() {
    return this.angle;
  }

  public void setAngle(final RangeAttribute<Float> angle) {
    this.angle = angle;
  }

  @XmlTransient
  public RangeAttribute<Float> getDeltaAngle() {
    return this.deltaAngle;
  }

  @XmlTransient
  public RangeAttribute<Float> getVelocityX() {
    return this.velocityX;
  }

  public void setVelocityX(final RangeAttribute<Float> velocityX) {
    this.velocityX = velocityX;
  }

  @XmlTransient
  public RangeAttribute<Float> getVelocityY() {
    return this.velocityY;
  }

  public void setVelocityY(final RangeAttribute<Float> velocityY) {
    this.velocityY = velocityY;
  }

  @XmlTransient
  public int getEmitterDuration() {
    return this.emitterDuration;
  }

  public void setEmitterDuration(final int emitterDuration) {
    this.emitterDuration = emitterDuration;
  }

  @XmlTransient
  public RangeAttribute<Float> getAccelerationX() {
    return this.accelerationX;
  }

  public void setAccelerationX(final RangeAttribute<Float> accelerationX) {
    this.accelerationX = accelerationX;
  }

  @XmlTransient
  public RangeAttribute<Float> getAccelerationY() {
    return this.accelerationY;
  }

  public void setAccelerationY(final RangeAttribute<Float> accelerationY) {
    this.accelerationY = accelerationY;
  }

  @XmlTransient
  public float getHeight() {
    return this.height;
  }

  public void setHeight(final float height) {
    this.height = height;
  }

  @XmlTransient
  public int getMaxParticles() {
    return this.maxParticles;
  }

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

  @XmlTransient
  public Align getOriginAlign() {
    return this.originAlign;
  }

  public void setOriginAlign(final Align align) {
    this.originAlign = align;
  }

  @XmlTransient
  public Valign getOriginValign() {
    return this.originValign;
  }

  public void setOriginValign(final Valign valign) {
    this.originValign = valign;
  }

  @XmlTransient
  public RangeAttribute<Float> getParticleHeight() {
    return this.particleHeight;
  }

  public void setParticleHeight(final RangeAttribute<Float> particleHeight) {
    this.particleHeight = particleHeight;
  }

  @XmlTransient
  public RangeAttribute<Long> getParticleTTL() {
    return this.particleTTL;
  }

  public void setParticleTTL(final RangeAttribute<Long> particleTTL) {
    this.particleTTL = particleTTL;
  }

  @XmlTransient
  public List<String> getTexts() {
    return this.texts;
  }

  public void setTexts(final List<String> texts) {
    this.texts = texts;
  }

  @XmlTransient
  public ParticleType getParticleType() {
    return this.particleType;
  }

  public void setParticleType(final ParticleType particleType) {
    this.particleType = particleType;
  }

  @XmlTransient
  public RangeAttribute<Float> getParticleWidth() {
    return this.particleWidth;
  }

  public void setParticleWidth(final RangeAttribute<Float> particleWidth) {
    this.particleWidth = particleWidth;
  }

  @XmlTransient
  public RangeAttribute<Float> getParticleOffsetX() {
    return this.offsetX;
  }

  public void setParticleOffsetX(final RangeAttribute<Float> x) {
    this.offsetX = x;
  }

  @XmlTransient
  public RangeAttribute<Float> getParticleOffsetY() {
    return this.offsetY;
  }

  public void setParticleOffsetY(final RangeAttribute<Float> y) {
    this.offsetY = y;
  }

  @XmlTransient
  public int getSpawnAmount() {
    return this.spawnAmount;
  }

  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  @XmlTransient
  public int getSpawnRate() {
    return this.spawnRate;
  }

  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
  }

  @XmlTransient
  public String getSpritesheet() {
    return this.spritesheet;
  }

  public void setSpritesheet(final String spritesheetName) {
    this.spritesheet = spritesheetName;
  }

  public void setSpritesheet(final Spritesheet spritesheet) {
    this.spritesheet = spritesheet.getName();
  }

  @XmlTransient
  public int getUpdateRate() {
    return this.updateRate;
  }

  public void setUpdateRate(final int updateRate) {
    if (updateRate == 0) {
      return;
    }

    this.updateRate = updateRate;
  }

  @XmlTransient
  public float getWidth() {
    return this.width;
  }

  public void setWidth(final float width) {
    this.width = width;
  }

  public boolean isAnimatingSprite() {
    return this.animateSprite;
  }

  public boolean isLoopingSprite() {
    return this.loopSprite;
  }

  public boolean isFading() {
    return this.fade;
  }

  public boolean isFadingOnCollision() {
    return this.fadeOnCollision;
  }

  public boolean isOutlineOnly() {
    return this.outlineOnly;
  }

  public void setOutlineOnly(final boolean outlineOnly) {
    this.outlineOnly = outlineOnly;
  }

  public boolean isAntiAliased() {
    return this.antiAliasing;
  }

  public void setAnimateSprite(final boolean animateSprite) {
    this.animateSprite = animateSprite;
  }

  public void setLoopSprite(final boolean loopSprite) {
    this.loopSprite = loopSprite;
  }

  public void setColor(final Color color) {
    final List<String> tmpList = new ArrayList<>();
    tmpList.add(ColorHelper.encode(color));
    this.colors = tmpList;
  }

  public void initDefaults() {
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.originAlign = DEFAULT_ORIGIN_ALIGN;
    this.originValign = DEFAULT_ORIGIN_VALIGN;
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

  public void setDeltaAngle(final RangeAttribute<Float> deltaRotation) {
    this.deltaAngle = deltaRotation;
  }

  public void setFade(final boolean fade) {
    this.fade = fade;
  }

  public void setFadeOnCollision(final boolean fadeOnCollision) {
    this.fadeOnCollision = fadeOnCollision;
  }

  public void setAntiAliasing(final boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
  }

  public void setText(final String text) {
    final List<String> tmpList = new ArrayList<>();
    tmpList.add(text);
    this.texts = tmpList;
  }
}
