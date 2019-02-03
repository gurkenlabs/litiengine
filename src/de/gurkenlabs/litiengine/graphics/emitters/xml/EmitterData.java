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
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.resources.Resource;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.MathUtilities;

@XmlRootElement(name = "emitter")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmitterData implements Serializable, Resource {
  private static final long serialVersionUID = 50238884097993529L;

  @XmlElementWrapper(name = "colors")
  @XmlElement(name = "color")
  private List<ParticleColor> colors;

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

  @XmlAttribute
  private int updateRate;

  @XmlAttribute
  private float width;

  @XmlElement
  private float colorDeviation;

  @XmlElement
  private float alphaDeviation;

  @XmlElement
  private ParticleParameter x;

  @XmlElement
  private ParticleParameter y;

  @XmlElement
  private boolean animateSprite;

  @XmlElement
  private String spritesheet;

  @XmlElement
  private String colorProbabilities;

  @XmlElement
  private Align originAlign;

  @XmlElement
  private Valign originValign;

  @XmlElement
  private CollisionType collisionType;

  @XmlElement
  private boolean fade;

  public EmitterData() {
    this.colors = new ArrayList<>();
    this.x = new ParticleParameter();
    this.y = new ParticleParameter();
    this.deltaX = new ParticleParameter();
    this.deltaY = new ParticleParameter();
    this.gravityX = new ParticleParameter();
    this.gravityY = new ParticleParameter();
    this.particleWidth = new ParticleParameter();
    this.particleHeight = new ParticleParameter();
    this.deltaWidth = new ParticleParameter();
    this.deltaHeight = new ParticleParameter();
    this.colorDeviation = 0;
    this.alphaDeviation = 0;
    this.collisionType = CollisionType.NONE;
    this.updateRate = Emitter.DEFAULT_UPDATERATE;
    this.originValign = Valign.TOP;
    this.originAlign = Align.LEFT;
    this.fade = true;
  }

  public EmitterData(EmitterData data) {
    this.colors = data.colors;
    this.colorProbabilities = data.colorProbabilities;
    this.x = data.x;
    this.y = data.y;
    this.deltaHeight = data.deltaHeight;
    this.deltaWidth = data.deltaWidth;
    this.deltaX = data.deltaX;
    this.deltaY = data.deltaY;
    this.emitterTTL = data.emitterTTL;
    this.gravityX = data.gravityX;
    this.gravityY = data.gravityY;
    this.width = data.width;
    this.height = data.height;
    this.particleWidth = data.particleWidth;
    this.particleHeight = data.particleHeight;
    this.colorDeviation = data.colorDeviation;
    this.alphaDeviation = data.alphaDeviation;
    this.updateRate = data.updateRate;
    this.collisionType = data.collisionType;
    this.maxParticles = data.maxParticles;
    this.name = data.name;
    this.particleMinTTL = data.particleMinTTL;
    this.particleMaxTTL = data.particleMaxTTL;
    this.particleText = data.particleText;
    this.particleType = data.particleType;
    this.spawnAmount = data.spawnAmount;
    this.spawnRate = data.spawnRate;
    this.animateSprite = data.animateSprite;
    this.spritesheet = data.spritesheet;
    this.originValign = data.getOriginValign();
    this.originAlign = data.getOriginAlign();
    this.fade = data.fade;
  }

  @XmlTransient
  public float getColorDeviation() {
    return colorDeviation;
  }

  @XmlTransient
  public float getAlphaDeviation() {
    return alphaDeviation;
  }

  @XmlTransient
  public List<ParticleColor> getColors() {
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
  public ParticleParameter getParticleX() {
    return this.x;
  }

  @XmlTransient
  public ParticleParameter getParticleY() {
    return this.y;
  }

  @XmlTransient
  public int getSpawnAmount() {
    return this.spawnAmount;
  }

  @XmlTransient
  public CollisionType getCollisionType() {
    return this.collisionType;
  }

  @XmlTransient
  public int getSpawnRate() {
    return this.spawnRate;
  }

  public boolean isAnimateSprite() {
    return animateSprite;
  }

  public boolean isFading() {
    return this.fade;
  }

  public void setAnimateSprite(boolean animateSprite) {
    this.animateSprite = animateSprite;
  }

  @XmlTransient
  public String getSpritesheet() {
    return spritesheet;
  }

  public void setSpritesheet(String spritesheet) {
    this.spritesheet = spritesheet;
  }

  @XmlTransient
  public int getUpdateRate() {
    return this.updateRate;
  }

  @XmlTransient
  public float getWidth() {
    return this.width;
  }

  @XmlTransient
  public double[] getColorProbabilities() {
    return ArrayUtilities.getDoubleArray(this.colorProbabilities);
  }

  public void setColorProbabilities(double[] colorProbabilities) {
    this.colorProbabilities = ArrayUtilities.join(colorProbabilities);
  }

  public void setColorProbabilities(String colorProbabilities) {
    this.colorProbabilities = colorProbabilities;
  }

  public void setColors(final List<ParticleColor> colors) {
    this.colors = colors;
  }

  public void setColor(Color color) {
    List<ParticleColor> tmpList = new ArrayList<>();
    tmpList.add(new ParticleColor(color));
    this.colors = tmpList;
  }

  public void setAlphaDeviation(float alphaDeviation) {
    this.alphaDeviation = MathUtilities.clamp(alphaDeviation, 0, 1);
  }

  public void setColorDeviation(float colorDeviation) {
    this.colorDeviation = MathUtilities.clamp(colorDeviation, 0, 1);
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

  public void setName(final String name) {
    this.name = name;
  }

  public void setOriginAlign(Align align) {
    this.originAlign = align;
  }

  public void setOriginValign(Valign valign) {
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

  public void setCollisionType(CollisionType physics) {
    this.collisionType = physics;
  }

  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
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

  public void setParticleX(final ParticleParameter x) {
    this.x = x;
  }

  public void setParticleY(final ParticleParameter y) {
    this.y = y;
  }
}