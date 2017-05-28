package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "info")
public class GameInfo {
  @XmlElement
  private String cooperation;

  @XmlElement
  private String description;

  private String[] developers;

  private String emitterDirectory;

  private String icon;

  private String logo;
  @XmlElement
  private String name;
  private float renderScale;
  private String spritesDirectory;
  @XmlElement
  private String subTitle;
  @XmlElement
  private String version;

  public GameInfo() {
    this.cooperation = "gurkenlabs";
    this.name = "LITIengine Game";
    this.subTitle = "The pure 2D java game engine";
    this.description = "A game, created with the allmighty LITIengine.";
    this.developers = new String[] { "Steffen Wilke", "Matthias Wilke" };
    this.icon = "";
    this.logo = "";
    this.version = "v1.0";

    this.renderScale = 3.0f;
    this.spritesDirectory = "sprites/";
    this.emitterDirectory = "emitters/";
  }

  @XmlTransient
  public String getCooperation() {
    return this.cooperation;
  }

  @XmlTransient
  public String getDescription() {
    return this.description;
  }

  @XmlTransient
  public String[] getDevelopers() {
    return this.developers;
  }

  public String getEmitterDirectory() {
    return this.emitterDirectory;
  }

  @XmlTransient
  public String getIcon() {
    return this.icon;
  }

  @XmlTransient
  public String getLogo() {
    return this.logo;
  }

  @XmlTransient
  public String getName() {
    return this.name;
  }

  @XmlTransient
  public float getRenderScale() {
    return this.renderScale;
  }

  @XmlTransient
  public String getSpritesDirectory() {
    return this.spritesDirectory;
  }

  @XmlTransient
  public String getSubTitle() {
    return this.subTitle;
  }

  @XmlTransient
  public String getVersion() {
    return this.version;
  }

  public void setCooperation(final String cooperation) {
    this.cooperation = cooperation;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setDevelopers(final String[] developers) {
    this.developers = developers;
  }

  public void setEmitterDirectory(final String emitterDirectory) {
    this.emitterDirectory = emitterDirectory;
  }

  public void setIcon(final String icon) {
    this.icon = icon;
  }

  public void setLogo(final String logo) {
    this.logo = logo;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setRenderScale(final float renderScale) {
    this.renderScale = renderScale;
  }

  public void setSpritesDirectory(final String spritesDirectory) {
    this.spritesDirectory = spritesDirectory;
  }

  public void setSubTitle(final String subTitle) {
    this.subTitle = subTitle;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return this.getSubTitle() != null && !this.getSubTitle().isEmpty() ? this.getName() + " " + this.getVersion() + " - " + this.getSubTitle() : this.getName() + " " + this.getVersion();
  }
}
