package de.gurkenlabs.litiengine;

import java.io.Serializable;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ArrayUtilities;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

@XmlRootElement(name = "sprite")
public class SpriteSheetInfo implements Serializable {
  public static final String FILE_EXTENSION = "info";
  private static final long serialVersionUID = 3864637034834813554L;
  @XmlAttribute(name = "width")
  private int width;

  @XmlAttribute(name = "height")
  private int height;

  @XmlAttribute(name = "name")
  private String name;

  @XmlAttribute(name = "path", required = false)
  private String path;

  @XmlElement(required = false)
  private String image;

  @XmlElement(required = false)
  private String keyframes;

  public SpriteSheetInfo() {
  }

  public SpriteSheetInfo(final Spritesheet sprite) {
    this.setWidth(sprite.getSpriteWidth());
    this.setHeight(sprite.getSpriteHeight());
    this.setImage(ImageProcessing.encodeToString(sprite.getImage(), sprite.getImageFormat()));
    this.setName(sprite.getName());
    this.setKeyframes(Spritesheet.getCustomKeyFrameDurations(sprite));
  }

  public SpriteSheetInfo(final String basepath, final String path, final int width, final int height) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(FileUtilities.getFileName(path));
    this.setImage(ImageProcessing.encodeToString(Resources.getImage(basepath + path), ImageFormat.get(FileUtilities.getExtension(path))));
  }

  public SpriteSheetInfo(final String path, final int width, final int height) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(FileUtilities.getFileName(path));
    this.setPath(path);
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
  public String getPath() {
    return this.path;
  }

  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  @XmlTransient
  public int[] getKeyframes() {
    if (this.keyframes == null || this.keyframes.isEmpty()) {
      return new int[0];
    }

    return ArrayUtilities.getIntegerArray(this.keyframes);
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

  public void setPath(final String path) {
    this.path = path;
  }

  public void setKeyframes(int[] keyframes) {
    this.keyframes = ArrayUtilities.getCommaSeparatedString(keyframes);
  }

  void beforeMarshal(Marshaller m) {
    if (this.keyframes != null && this.keyframes.isEmpty()) {
      this.keyframes = null;
    }
  }
}