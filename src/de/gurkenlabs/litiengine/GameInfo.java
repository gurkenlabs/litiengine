package de.gurkenlabs.litiengine;

public class GameInfo {
  private String cooperation;
  private String description;
  private String[] developers;
  private String icon;
  private String logo;
  private String name;
  private String spritesDirectory;
  private String subTitle;

  private float version;
  private float renderScale;

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

  public String getCooperation() {
    return this.cooperation;
  }

  public void setCooperation(String cooperation) {
    this.cooperation = cooperation;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getDevelopers() {
    return this.developers;
  }

  public void setDevelopers(String[] developers) {
    this.developers = developers;
  }

  public String getIcon() {
    return this.icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getLogo() {
    return this.logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSpritesDirectory() {
    return this.spritesDirectory;
  }

  public void setSpritesDirectory(String spritesDirectory) {
    this.spritesDirectory = spritesDirectory;
  }

  public String getSubTitle() {
    return this.subTitle;
  }

  public void setSubTitle(String subTitle) {
    this.subTitle = subTitle;
  }

  public float getVersion() {
    return this.version;
  }

  public void setVersion(float version) {
    this.version = version;
  }

  public float getRenderScale() {
    return this.renderScale;
  }

  public void setRenderScale(float renderScale) {
    this.renderScale = renderScale;
  }
}
