package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.image.ImageProcessing;

@XmlRootElement(name = "sprite")
public class SpriteSheetInfo {

  @XmlElement
  private String image;

  @XmlAttribute(name = "path")
  private String path;

  @XmlAttribute(name = "width")
  private int width;

  @XmlAttribute(name = "height")
  private int height;

  @XmlAttribute(name = "name")
  private String name;

  public SpriteSheetInfo() {
  }

  public SpriteSheetInfo(Spritesheet sprite){
    this.setPath(sprite.getPath());
    this.setWidth(sprite.getSpriteWidth());
    this.setHeight(sprite.getSpriteHeight());
    this.setImage(ImageProcessing.encodeToString(sprite.getImage()));
    this.setName(sprite.getName());
  }
  
  public SpriteSheetInfo(String basepath, String path, int width, int height) {
    this.setPath(path);
    this.setWidth(width);
    this.setHeight(height);
    this.setImage(ImageProcessing.encodeToString(RenderEngine.getImage(basepath + this.getPath())));
    this.initializeName();
  }

  private void initializeName() {
    String name = this.getPath();

    String[] parts = name.split("\\\\");
    this.setName(parts[parts.length - 1].replaceAll("\\.(jpg|png|gif|bmp)", ""));
  }

  @XmlTransient
  public String getPath() {
    return this.path;
  }

  public void setPath(String p) {
    this.path = p;
  }

  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  public void setWidth(int w) {
    this.width = w;
  }

  @XmlTransient
  public int getHeight() {
    return this.height;
  }

  public void setHeight(int h) {
    this.height = h;
  }

  @XmlTransient
  public String getImage() {
    return this.image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  @XmlTransient
  public String getName() {
    return this.name;
  }

  public void setName(String n) {
    this.name = n;
  }

}
