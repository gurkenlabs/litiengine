package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "emitter")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomEmitterData {
  @XmlAttribute
  private String name;

  @XmlAttribute
  private int particleMinTTL;

  @XmlAttribute
  private int particleMaxTTL;

  @XmlAttribute
  private int spawnAmount;

  @XmlAttribute
  private int spawnRate;

  @XmlAttribute
  private int maxParticles;

  @XmlAttribute
  private int emitterTTL;

  @XmlAttribute
  private int width;

  @XmlAttribute
  private int height;

  @XmlAttribute
  private int updateRate;

  @XmlAttribute
  private ParticleType particleType;

  @XmlElement
  private ParticleParameter x;

  @XmlElement
  private ParticleParameter y;

  @XmlElement
  private ParticleParameter deltaX;

  @XmlElement
  private ParticleParameter deltaY;

  @XmlElement
  private ParticleParameter gravityX;

  @XmlElement
  private ParticleParameter gravityY;

  @XmlElement
  private ParticleParameter particleWidth;

  @XmlElement
  private ParticleParameter particleHeight;

  @XmlElement
  private ParticleParameter deltaWidth;

  @XmlElement
  private ParticleParameter deltaHeight;

  @XmlElement
  private String particleText;

  @XmlElement
  private boolean applyStaticPhysics;

  @XmlElementWrapper(name = "colors")
  @XmlElement(name = "color")
  private List<ParticleColor> colors;

  public CustomEmitterData() {
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
    this.updateRate = 30;
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
  public int getHeight() {
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
  public int getSpawnAmount() {
    return this.spawnAmount;
  }

  @XmlTransient
  public int getSpawnRate() {
    return this.spawnRate;
  }

  @XmlTransient
  public int getUpdateRate() {
    return this.updateRate;
  }

  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  @XmlTransient
  public ParticleParameter getX() {
    return this.x;
  }

  @XmlTransient
  public ParticleParameter getY() {
    return this.y;
  }

  @XmlTransient
  public boolean isApplyingStaticPhysics() {
    return this.applyStaticPhysics;
  }

  public void setApplyStaticPhysics(final boolean applyStaticPhysics) {
    this.applyStaticPhysics = applyStaticPhysics;
  }

  public void setColors(final List<ParticleColor> colors) {
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

  public void setGravityX(final ParticleParameter gravityX) {
    this.gravityX = gravityX;
  }

  public void setGravityY(final ParticleParameter gravityY) {
    this.gravityY = gravityY;
  }

  public void setHeight(final int height) {
    this.height = height;
  }

  public void setMaxParticles(final int maxParticles) {
    this.maxParticles = maxParticles;
  }

  public void setName(final String name) {
    this.name = name;
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

  public void setSpawnAmount(final int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  public void setSpawnRate(final int spawnRate) {
    this.spawnRate = spawnRate;
  }

  public void setUpdateRate(final int updateRate) {
    this.updateRate = updateRate;
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public void setX(final ParticleParameter x) {
    this.x = x;
  }

  public void setY(final ParticleParameter y) {
    this.y = y;
  }

}