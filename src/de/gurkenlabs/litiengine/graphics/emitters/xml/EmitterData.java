package de.gurkenlabs.litiengine.graphics.emitters.xml;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resource;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.MathUtilities;

@XmlRootElement(name = "emitter")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmitterData implements Serializable, Resource {
  private static final long serialVersionUID = 50238884097993529L;

  public static final Color DEFAULT_COLOR = ColorHelper.decode("#CC00a5bc");

  public static final String DEFAULT_COLOR_PROBABILITIES = "";
  public static final String DEFAULT_SPRITESHEET = "";
  public static final String DEFAULT_NAME = "Custom Emitter";
  public static final String DEFAULT_TEXT = "LITI";

  public static final boolean DEFAULT_ANIMATE_SPRITE = true;
  public static final boolean DEFAULT_FADE = true;

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
  public static final int DEFAULT_TTL = 0;
  public static final int DEFAULT_MAX_PARTICLE_TTL = 1500;
  public static final int DEFAULT_MIN_PARTICLE_TTL = 400;

  public static final float DEFAULT_MIN_OFFSET_X = -4f;
  public static final float DEFAULT_MAX_OFFSET_X = 4f;
  public static final float DEFAULT_MIN_OFFSET_Y = -4f;
  public static final float DEFAULT_MAX_OFFSET_Y = 4f;

  public static final float DEFAULT_MIN_DELTA_WIDTH = -.1f;
  public static final float DEFAULT_MAX_DELTA_WIDTH = .1f;
  public static final float DEFAULT_MIN_DELTA_HEIGHT = -.1f;
  public static final float DEFAULT_MAX_DELTA_HEIGHT = .1f;

  public static final float DEFAULT_MIN_GRAVITY_X = -.01f;
  public static final float DEFAULT_MAX_GRAVITY_X = .01f;
  public static final float DEFAULT_MIN_GRAVITY_Y = -.01f;
  public static final float DEFAULT_MAX_GRAVITY_Y = .01f;

  public static final float DEFAULT_MIN_DELTA_X = -.1f;
  public static final float DEFAULT_MAX_DELTA_X = .1f;
  public static final float DEFAULT_MIN_DELTA_Y = -.1f;
  public static final float DEFAULT_MAX_DELTA_Y = .1f;

  public static final float DEFAULT_MIN_WIDTH = 2f;
  public static final float DEFAULT_MAX_WIDTH = 6f;
  public static final float DEFAULT_MIN_HEIGHT = 2f;
  public static final float DEFAULT_MAX_HEIGHT = 6f;

  @XmlElement
  private float alphaVariance;

  @XmlElement
  private boolean animateSprite;

  @XmlElement
  private Collision collisionType;

  @XmlElement
  private float colorVariance;

  @XmlElement
  private String colorProbabilities;

  @XmlElementWrapper
  @XmlElement(name="color")
  private List<String> colors;

  @XmlElement
  private ParticleParameter deltaHeight;

  @XmlElement
  private ParticleParameter deltaWidth;

  @XmlElement
  private ParticleParameter deltaX;

  @XmlElement
  private ParticleParameter deltaY;

  @XmlAttribute
  private int emitterTTL;

  @XmlElement
  private boolean fade;

  @XmlElement
  private ParticleParameter gravityX;

  @XmlElement
  private ParticleParameter gravityY;

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
  private ParticleParameter particleHeight;

  @XmlAttribute
  private int particleMaxTTL;

  @XmlAttribute
  private int particleMinTTL;

  @XmlElement
  private String particleText;

  @XmlAttribute
  private ParticleType particleType;

  @XmlElement
  private ParticleParameter particleWidth;

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
  private ParticleParameter offsetX;

  @XmlElement
  private ParticleParameter offsetY;

  public EmitterData() {
    this.offsetX = new ParticleParameter(DEFAULT_MIN_OFFSET_X, DEFAULT_MAX_OFFSET_X);
    this.offsetY = new ParticleParameter(DEFAULT_MIN_OFFSET_Y, DEFAULT_MAX_OFFSET_Y);
    this.deltaWidth = new ParticleParameter(DEFAULT_MIN_DELTA_WIDTH, DEFAULT_MAX_DELTA_WIDTH);
    this.deltaHeight = new ParticleParameter(DEFAULT_MIN_DELTA_HEIGHT, DEFAULT_MAX_DELTA_HEIGHT);
    this.deltaX = new ParticleParameter(DEFAULT_MIN_DELTA_X, DEFAULT_MAX_DELTA_X);
    this.deltaY = new ParticleParameter(DEFAULT_MIN_DELTA_Y, DEFAULT_MAX_DELTA_Y);
    this.gravityX = new ParticleParameter(DEFAULT_MIN_GRAVITY_X, DEFAULT_MAX_GRAVITY_X);
    this.gravityY = new ParticleParameter(DEFAULT_MIN_GRAVITY_Y, DEFAULT_MAX_GRAVITY_Y);
    this.particleWidth = new ParticleParameter(DEFAULT_MIN_WIDTH, DEFAULT_MAX_WIDTH);
    this.particleHeight = new ParticleParameter(DEFAULT_MIN_HEIGHT, DEFAULT_MAX_HEIGHT);
    this.setColor(DEFAULT_COLOR);
    this.colorProbabilities = DEFAULT_COLOR_PROBABILITIES;
    this.emitterTTL = DEFAULT_TTL;
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.colorVariance = DEFAULT_COLOR_VARIANCE;
    this.alphaVariance = DEFAULT_ALPHA_VARIANCE;
    this.updateRate = DEFAULT_UPDATERATE;
    this.collisionType = DEFAULT_COLLISION;
    this.maxParticles = DEFAULT_MAXPARTICLES;
    this.name = DEFAULT_NAME;
    this.particleMinTTL = DEFAULT_MIN_PARTICLE_TTL;
    this.particleMaxTTL = DEFAULT_MAX_PARTICLE_TTL;
    this.particleText = DEFAULT_TEXT;
    this.particleType = DEFAULT_PARTICLE_TYPE;
    this.spawnAmount = DEFAULT_SPAWNAMOUNT;
    this.spawnRate = DEFAULT_SPAWNRATE;
    this.animateSprite = DEFAULT_ANIMATE_SPRITE;
    this.spritesheet = DEFAULT_SPRITESHEET;
    this.originValign = DEFAULT_ORIGIN_VALIGN;
    this.originAlign = DEFAULT_ORIGIN_ALIGN;
    this.fade = DEFAULT_FADE;
  }

  @XmlTransient
  public float getAlphaVariance() {
    return this.alphaVariance;
  }

  @XmlTransient
  public Collision getCollisionType() {
    return this.collisionType;
  }

  @XmlTransient
  public float getColorVariance() {
    return this.colorVariance;
  }

  @XmlTransient
  public double[] getColorProbabilities() {
    return ArrayUtilities.splitDouble(this.colorProbabilities);
  }

  @XmlTransient
  public List<String> getColors() {
    return this.colors;
  }

  @XmlTransient
  public ParticleParameter getDeltaHeight() {
    return this.deltaHeight;
  }

  @XmlTransient
  public ParticleParameter getDeltaWidth() {
    return this.deltaWidth;
  }

  @XmlTransient
  public ParticleParameter getDeltaX() {
    return this.deltaX;
  }

  @XmlTransient
  public ParticleParameter getDeltaY() {
    return this.deltaY;
  }

  @XmlTransient
  public int getEmitterTTL() {
    return this.emitterTTL;
  }

  @XmlTransient
  public ParticleParameter getGravityX() {
    return this.gravityX;
  }

  @XmlTransient
  public ParticleParameter getGravityY() {
    return this.gravityY;
  }

  @XmlTransient
  public float getHeight() {
    return this.height;
  }

  @XmlTransient
  public int getMaxParticles() {
    return this.maxParticles;
  }

  @XmlTransient
  @Override
  public String getName() {
    return this.name;
  }

  @XmlTransient
  public Align getOriginAlign() {
    return this.originAlign;
  }

  public Valign getOriginValign() {
    return this.originValign;
  }

  @XmlTransient
  public ParticleParameter getParticleHeight() {
    return this.particleHeight;
  }

  @XmlTransient
  public int getParticleMaxTTL() {
    return this.particleMaxTTL;
  }

  @XmlTransient
  public int getParticleMinTTL() {
    return this.particleMinTTL;
  }

  @XmlTransient
  public String getParticleText() {
    return this.particleText;
  }

  @XmlTransient
  public ParticleType getParticleType() {
    return this.particleType;
  }

  @XmlTransient
  public ParticleParameter getParticleWidth() {
    return this.particleWidth;
  }

  @XmlTransient
  public ParticleParameter getParticleOffsetX() {
    return this.offsetX;
  }

  @XmlTransient
  public ParticleParameter getParticleOffsetY() {
    return this.offsetY;
  }

  @XmlTransient
  public int getSpawnAmount() {
    return this.spawnAmount;
  }

  @XmlTransient
  public int getSpawnRate() {
    return this.spawnRate;
  }

  @XmlTransient
  public String getSpritesheet() {
    return this.spritesheet;
  }

  @XmlTransient
  public int getUpdateRate() {
    return this.updateRate;
  }

  @XmlTransient
  public float getWidth() {
    return this.width;
  }

  public boolean isAnimateSprite() {
    return this.animateSprite;
  }

  public boolean isFading() {
    return this.fade;
  }

  public void setAlphaVariance(final float alphaVariance) {
    this.alphaVariance = MathUtilities.clamp(alphaVariance, 0, 1);
  }

  public void setAnimateSprite(final boolean animateSprite) {
    this.animateSprite = animateSprite;
  }

  public void setCollisionType(final Collision physics) {
    this.collisionType = physics;
  }

  public void setColor(final Color color) {
    final List<String> tmpList = new ArrayList<>();
    tmpList.add(ColorHelper.encode(color));
    this.colors = tmpList;
  }

  public void setColorVariance(final float colorVariance) {
    this.colorVariance = MathUtilities.clamp(colorVariance, 0, 1);
  }

  public void setColorProbabilities(final double[] colorProbabilities) {
    this.colorProbabilities = ArrayUtilities.join(colorProbabilities);
  }

  public void setColorProbabilities(final String colorProbabilities) {
    this.colorProbabilities = colorProbabilities;
  }

  public void setColors(final List<String> colors) {
    this.colors = colors;
  }

  public void setDeltaHeight(final ParticleParameter deltaHeight) {
    this.deltaHeight = deltaHeight;
  }

  public void setDeltaWidth(final ParticleParameter deltaWidth) {
    this.deltaWidth = deltaWidth;
  }

  public void setDeltaX(final ParticleParameter deltaX) {
    this.deltaX = deltaX;
  }

  public void setDeltaY(final ParticleParameter deltaY) {
    this.deltaY = deltaY;
  }

  public void setEmitterTTL(final int emitterTTL) {
    this.emitterTTL = emitterTTL;
  }

  public void setFade(final boolean fade) {
    this.fade = fade;
  }

  public void setGravityX(final ParticleParameter gravityX) {
    this.gravityX = gravityX;
  }

  public void setGravityY(final ParticleParameter gravityY) {
    this.gravityY = gravityY;
  }

  public void setHeight(final float height) {
    this.height = height;
  }

  public void setMaxParticles(final int maxParticles) {
    this.maxParticles = maxParticles;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  public void setOriginAlign(final Align align) {
    this.originAlign = align;
  }

  public void setOriginValign(final Valign valign) {
    this.originValign = valign;
  }

  public void setParticleHeight(final ParticleParameter particleHeight) {
    this.particleHeight = particleHeight;
  }

  public void setParticleMaxTTL(final int particleMaxTTL) {
    this.particleMaxTTL = particleMaxTTL;
  }

  public void setParticleMinTTL(final int particleMinTTL) {
    this.particleMinTTL = particleMinTTL;
  }

  public void setParticleText(final String particleText) {
    this.particleText = particleText;
  }

  public void setParticleType(final ParticleType particleType) {
    this.particleType = particleType;
  }

  public void setParticleWidth(final ParticleParameter particleWidth) {
    this.particleWidth = particleWidth;
  }

  public void setParticleX(final ParticleParameter x) {
    this.offsetX = x;
  }

  public void setParticleY(final ParticleParameter y) {
    this.offsetY = y;
  }

  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
  }

  public void setSpritesheet(final String spritesheet) {
    this.spritesheet = spritesheet;
  }

  public void setUpdateRate(final int updateRate) {
    if (updateRate == 0) {
      return;
    }

    this.updateRate = updateRate;
  }

  public void setWidth(final float width) {
    this.width = width;
  }
}