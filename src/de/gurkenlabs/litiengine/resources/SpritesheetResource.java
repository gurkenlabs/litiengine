package de.gurkenlabs.litiengine.resources;

import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.io.Codec;

@XmlRootElement(name = "sprite")
public class SpritesheetResource extends NamedResource implements Serializable {
  public static final String PLAIN_TEXT_FILE_EXTENSION = "info";
  private static final long serialVersionUID = 3864637034834813554L;
  @XmlAttribute(name = "width")
  private int width;

  @XmlAttribute(name = "height")
  private int height;

  @XmlElement(required = false)
  private String image;

  @XmlElement(required = false)
  private String keyframes;

  public SpritesheetResource() {
    // keep empty constructor for serialization
  }

  public SpritesheetResource(final Spritesheet sprite) {
    this(sprite.getSpriteWidth(), sprite.getSpriteHeight(), sprite.getName());
    this.setImage(Codec.encode(sprite.getImage(), sprite.getImageFormat()));
    this.setKeyframes(Resources.spritesheets().getCustomKeyFrameDurations(sprite));
  }

  public SpritesheetResource(final BufferedImage image, String name, final int width, final int height) {
    this(width, height, name);
    this.setImage(Codec.encode(image));
  }

  private SpritesheetResource(int width, int height, String name) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(name);
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