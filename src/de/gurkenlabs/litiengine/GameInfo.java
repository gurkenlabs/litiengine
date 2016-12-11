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
  

  @XmlElement
  private String name;

  
  @XmlElement
  private String subTitle;

  @XmlElement
  private float version;
  
  private float renderScale;  
  private String spritesDirectory;  
  private String[] developers;
  private String icon;
  private String logo;

  public GameInfo() {
    this.cooperation = "gurkenlabs";
    this.name = "LitiEngine Game";
    this.subTitle = "The ultimate java engine.";
    this.description = "A game, created with the allmighty LITI ENGINE.";
    this.developers = new String[] { "Steffen Wilke", "Matthias Wilke" };
    this.icon = "";
    this.logo = "";
    this.version = 1.0f;

    this.renderScale = 3.0f;
    this.spritesDirectory = "sprites/";
  }

  @XmlTransient
  public String getCooperation() {
    return this.cooperation;
  }

  public void setCooperation(String cooperation) {
    this.cooperation = cooperation;
  }

  @XmlTransient
  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @XmlTransient
  public String[] getDevelopers() {
    return this.developers;
  }

  public void setDevelopers(String[] developers) {
    this.developers = developers;
  }

  @XmlTransient
  public String getIcon() {
    return this.icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  @XmlTransient
  public String getLogo() {
    return this.logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  @XmlTransient
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlTransient
  public String getSpritesDirectory() {
    return this.spritesDirectory;
  }

  public void setSpritesDirectory(String spritesDirectory) {
    this.spritesDirectory = spritesDirectory;
  }

  @XmlTransient
  public String getSubTitle() {
    return this.subTitle;
  }

  public void setSubTitle(String subTitle) {
    this.subTitle = subTitle;
  }

  @XmlTransient
  public float getVersion() {
    return this.version;
  }

  public void setVersion(float version) {
    this.version = version;
  }

  @XmlTransient
  public float getRenderScale() {
    return this.renderScale;
  }

  public void setRenderScale(float renderScale) {
    this.renderScale = renderScale;
  }
  
  public String toString(){
    return !this.getSubTitle().isEmpty() ? this.getName() + " - " + this.getSubTitle() + " " + this.getVersion() : this.getName() + " - " + this.getVersion();
  }
}
