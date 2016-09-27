package de.gurkenlabs.litiengine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  @XmlAttribute(name = "name")
  private String name;

  public SpriteSheetInfo() {
  }

  public SpriteSheetInfo(String basepath, String path, int width, int height) {
    super();
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
    System.out.println(this.getName());
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
