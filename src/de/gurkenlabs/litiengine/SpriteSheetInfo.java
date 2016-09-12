package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.RenderEngine;
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

  public SpriteSheetInfo(){
  }
  
  public SpriteSheetInfo(String basepath, String path, int width, int height) {
    super();
    this.path = path;
    this.width = width;
    this.height = height;
    this.setImage(ImageProcessing.encodeToString(RenderEngine.getImage(basepath + this.getPath())));
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
    return height;
  }

  public void setHeight(int h) {
    this.height = h;
  }

  @XmlTransient
  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }
  
  
}
