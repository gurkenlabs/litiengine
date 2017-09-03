package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

@XmlRootElement(name = "sprite")
public class SpriteSheetInfo {

  @XmlAttribute(name = "height")
  private int height;

  @XmlElement
  private String image;

  @XmlAttribute(name = "name")
  private String name;

  @XmlAttribute(name = "width")
  private int width;

  public SpriteSheetInfo() {
  }

  public SpriteSheetInfo(final Spritesheet sprite) {
    this.setWidth(sprite.getSpriteWidth());
    this.setHeight(sprite.getSpriteHeight());
    this.setImage(ImageProcessing.encodeToString(sprite.getImage()));
    this.setName(sprite.getName());
  }

  public SpriteSheetInfo(final String basepath, final String path, final int width, final int height) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(FileUtilities.getFileName(path));
    this.setImage(ImageProcessing.encodeToString(RenderEngine.getImage(basepath + path)));
  }

  @XmlTransient
  public int getHeight() {
    return this.height;
  }

  @XmlTransient
  public String getImage() {
    return this.image;
  }

  @XmlTransient
  public String getName() {
    return this.name;
  }

  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  public void setHeight(final int h) {
    this.height = h;
  }

  public void setImage(final String image) {
    this.image = image;
  }

  public void setName(final String n) {
    this.name = n;
  }

  public void setWidth(final int w) {
    this.width = w;
  }

}
