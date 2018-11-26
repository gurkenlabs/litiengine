package de.gurkenlabs.litiengine;

import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ImageProcessing;

@XmlRootElement(name = "sprite")
public class SpritesheetInfo implements Serializable, Comparable<SpritesheetInfo> {
  public static final String PLAIN_TEXT_FILE_EXTENSION = "info";
  private static final long serialVersionUID = 3864637034834813554L;
  @XmlAttribute(name = "width")
  private int width;

  @XmlAttribute(name = "height")
  private int height;

  @XmlAttribute(name = "name")
  private String name;

  @XmlElement(required = false)
  private String image;

  @XmlElement(required = false)
  private String keyframes;

  public SpritesheetInfo() {
    // keep empty constructor for serialization
  }

  public SpritesheetInfo(final Spritesheet sprite) {
    this(sprite.getSpriteWidth(), sprite.getSpriteHeight(), sprite.getName());
    this.setImage(ImageProcessing.encodeToString(sprite.getImage(), sprite.getImageFormat()));
    this.setKeyframes(Resources.spritesheets().getCustomKeyFrameDurations(sprite));
  }

  public SpritesheetInfo(final BufferedImage image, String name, final int width, final int height) {
    this(width, height, name);
    this.setImage(ImageProcessing.encodeToString(image));
  }

  private SpritesheetInfo(int width, int height, String name) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(name);
  }

  @Override
  public int compareTo(SpritesheetInfo obj) {
    if (obj == null) {
      return 1;
    }

    if (this.getName() == null) {
      if (obj.getName() == null) {
        return 0;
      }

      return -1;
    }

    return this.getName().compareTo(obj.getName());
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

  public void setKeyframes(int[] keyframes) {
    this.keyframes = ArrayUtilities.join(keyframes);
  }

  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
    if (this.keyframes != null && this.keyframes.isEmpty()) {
      this.keyframes = null;
    }
  }
}