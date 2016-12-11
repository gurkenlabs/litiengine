package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.image.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

@XmlRootElement(name = "sprite")
public class SpriteSheetInfo {

  @XmlElement
  private String image;

  @XmlAttribute(name = "width")
  private int width;

  @XmlAttribute(name = "height")
  private int height;

  @XmlAttribute(name = "name")
  private String name;

  public SpriteSheetInfo() {
  }

  public SpriteSheetInfo(Spritesheet sprite){
    this.setWidth(sprite.getSpriteWidth());
    this.setHeight(sprite.getSpriteHeight());
    this.setImage(ImageProcessing.encodeToString(sprite.getImage()));
    this.setName(sprite.getName());
  }
  
  public SpriteSheetInfo(String basepath, String path, int width, int height) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(FileUtilities.getFileName(path));
    this.setImage(ImageProcessing.encodeToString(RenderEngine.getImage(basepath + path)));
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
