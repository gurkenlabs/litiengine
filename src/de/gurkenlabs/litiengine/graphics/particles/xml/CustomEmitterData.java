package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlTransient
  public int getParticleMinTTL() {
    return this.particleMinTTL;
  }

  public void setParticleMinTTL(int particleMinTTL) {
    this.particleMinTTL = particleMinTTL;
  }

  @XmlTransient
  public int getParticleMaxTTL() {
    return this.particleMaxTTL;
  }

  public void setParticleMaxTTL(int particleMaxTTL) {
    this.particleMaxTTL = particleMaxTTL;
  }

  @XmlTransient
  public int getSpawnAmount() {
    return this.spawnAmount;
  }

  public void setSpawnAmount(int spawnAmount) {
    this.spawnAmount = spawnAmount;
  }

  @XmlTransient
  public int getSpawnRate() {
    return this.spawnRate;
  }

  public void setSpawnRate(int spawnRate) {
    this.spawnRate = spawnRate;
  }

  @XmlTransient
  public int getMaxParticles() {
    return this.maxParticles;
  }

  public void setMaxParticles(int maxParticles) {
    this.maxParticles = maxParticles;
  }

  @XmlTransient
  public int getEmitterTTL() {
    return this.emitterTTL;
  }

  public void setEmitterTTL(int emitterTTL) {
    this.emitterTTL = emitterTTL;
  }

  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  @XmlTransient
  public int getHeight() {
    return this.height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @XmlTransient
  public ParticleParameter getX() {
    return this.x;
  }

  public void setX(ParticleParameter x) {
    this.x = x;
  }

  @XmlTransient
  public ParticleParameter getY() {
    return this.y;
  }

  public void setY(ParticleParameter y) {
    this.y = y;
  }

  @XmlTransient
  public ParticleParameter getDeltaX() {
    return this.deltaX;
  }

  public void setDeltaX(ParticleParameter deltaX) {
    this.deltaX = deltaX;
  }

  @XmlTransient
  public ParticleParameter getDeltaY() {
    return this.deltaY;
  }

  public void setDeltaY(ParticleParameter deltaY) {
    this.deltaY = deltaY;
  }

  @XmlTransient
  public ParticleParameter getGravityX() {
    return this.gravityX;
  }

  public void setGravityX(ParticleParameter gravityX) {
    this.gravityX = gravityX;
  }

  @XmlTransient
  public ParticleParameter getGravityY() {
    return this.gravityY;
  }

  public void setGravityY(ParticleParameter gravityY) {
    this.gravityY = gravityY;
  }

  @XmlTransient
  public ParticleParameter getParticleWidth() {
    return this.particleWidth;
  }

  public void setParticleWidth(ParticleParameter particleWidth) {
    this.particleWidth = particleWidth;
  }

  @XmlTransient
  public ParticleParameter getParticleHeight() {
    return this.particleHeight;
  }

  public void setParticleHeight(ParticleParameter particleHeight) {
    this.particleHeight = particleHeight;
  }

  @XmlTransient
  public ParticleParameter getDeltaWidth() {
    return this.deltaWidth;
  }

  public void setDeltaWidth(ParticleParameter deltaWidth) {
    this.deltaWidth = deltaWidth;
  }

  @XmlTransient
  public ParticleParameter getDeltaHeight() {
    return this.deltaHeight;
  }

  public void setDeltaHeight(ParticleParameter deltaHeight) {
    this.deltaHeight = deltaHeight;
  }

  @XmlTransient
  public List<ParticleColor> getColors() {
    return this.colors;
  }

  public void setColors(List<ParticleColor> colors) {
    this.colors = colors;
  }

  @XmlTransient
  public int getUpdateRate() {
    return this.updateRate;
  }

  public void setUpdateRate(int updateRate) {
    this.updateRate = updateRate;
  }

  @XmlTransient
  public ParticleType getParticleType() {
    return this.particleType;
  }

  public void setParticleType(ParticleType particleType) {
    this.particleType = particleType;
  }

  @XmlTransient
  public String getParticleText() {
    return this.particleText;
  }

  public void setParticleText(String particleText) {
    this.particleText = particleText;
  }

  @XmlTransient
  public boolean isApplyingStaticPhysics() {
    return this.applyStaticPhysics;
  }

  public void setApplyStaticPhysics(boolean applyStaticPhysics) {
    this.applyStaticPhysics = applyStaticPhysics;
  }
  
  

}