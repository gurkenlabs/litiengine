package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.io.Codec;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a resource for managing spritesheets.
 * <p>
 * This class extends the {@code NamedResource} class and implements {@code Serializable} to provide specific functionality for handling spritesheet
 * resources.
 * </p>
 */
@XmlRootElement(name = "sprite")
public class SpritesheetResource extends NamedResource implements Serializable {
  public static final String PLAIN_TEXT_FILE_EXTENSION = "info";
  private static final long serialVersionUID = 3864637034834813554L;
  @XmlAttribute(name = "width")
  private int width;

  @XmlAttribute(name = "height")
  private int height;

  @XmlAttribute(name = "imageformat")
  private ImageFormat imageformat;

  @XmlElement(required = false)
  private String image;

  @XmlElement(required = false)
  private String keyframes;

  /**
   * Default constructor for SpritesheetResource.
   * <p>
   * This constructor is kept for serialization purposes.
   * </p>
   */
  public SpritesheetResource() {
    // keep empty constructor for serialization
  }

  /**
   * Constructs a new SpritesheetResource from a Spritesheet object.
   * <p>
   * This constructor initializes the SpritesheetResource with the specified sprite's width, height, name, image, image format, and keyframes.
   * </p>
   *
   * @param sprite The Spritesheet object to initialize the resource from.
   */
  public SpritesheetResource(final Spritesheet sprite) {
    this(sprite.getSpriteWidth(), sprite.getSpriteHeight(), sprite.getName());
    this.setImage(Codec.encode(sprite.getImage(), sprite.getImageFormat()));
    this.setImageFormat(sprite.getImageFormat());
    this.setKeyframes(Resources.spritesheets().getCustomKeyFrameDurations(sprite));
  }

  /**
   * Constructs a new SpritesheetResource from a BufferedImage.
   * <p>
   * This constructor initializes the SpritesheetResource with the specified image, name, width, and height.
   * </p>
   *
   * @param image  The BufferedImage to initialize the resource from.
   * @param name   The name of the spritesheet.
   * @param width  The width of the spritesheet.
   * @param height The height of the spritesheet.
   */
  public SpritesheetResource(final BufferedImage image, String name, final int width, final int height) {
    this(width, height, name);
    this.setImage(Codec.encode(image));
    this.setImageFormat(ImageFormat.PNG);
  }

  /**
   * Constructs a new SpritesheetResource with the specified width, height, and name.
   * <p>
   * This private constructor is used internally to initialize the resource with the specified dimensions and name.
   * </p>
   *
   * @param width  The width of the spritesheet.
   * @param height The height of the spritesheet.
   * @param name   The name of the spritesheet.
   */
  private SpritesheetResource(int width, int height, String name) {
    this.setWidth(width);
    this.setHeight(height);
    this.setName(name);
  }

  /**
   * Gets the height of the spritesheet.
   *
   * @return The height of the spritesheet.
   */
  @XmlTransient
  public int getHeight() {
    return this.height;
  }

  /**
   * Gets the image data of the spritesheet.
   *
   * @return The image data of the spritesheet.
   */
  @XmlTransient
  public String getImage() {
    return this.image;
  }

  /**
   * Gets the width of the spritesheet.
   *
   * @return The width of the spritesheet.
   */
  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the image format of the spritesheet.
   *
   * @return The image format of the spritesheet.
   */
  @XmlTransient
  public ImageFormat getImageFormat() {
    return this.imageformat;
  }

  /**
   * Gets the keyframes of the spritesheet.
   *
   * @return An array of keyframes of the spritesheet.
   */
  @XmlTransient
  public int[] getKeyframes() {
    if (this.keyframes == null || this.keyframes.isEmpty()) {
      return new int[0];
    }

    return Arrays.stream(this.keyframes.split(",")).mapToInt(Integer::parseInt).toArray();
  }

  /**
   * Sets the height of the spritesheet.
   *
   * @param h The height to set.
   */
  public void setHeight(final int h) {
    this.height = h;
  }

  /**
   * Sets the image data of the spritesheet.
   *
   * @param image The image data to set.
   */
  public void setImage(final String image) {
    this.image = image;
  }

  /**
   * Sets the width of the spritesheet.
   *
   * @param w The width to set.
   */
  public void setWidth(final int w) {
    this.width = w;
  }

  /**
   * Sets the image format of the spritesheet.
   *
   * @param f The image format to set.
   */
  public void setImageFormat(final ImageFormat f) {
    this.imageformat = f;
  }

  /**
   * Sets the keyframes of the spritesheet.
   *
   * @param keyframes An array of keyframes to set.
   */
  public void setKeyframes(int[] keyframes) {
    this.keyframes = Arrays.stream(keyframes).mapToObj(String::valueOf).collect(Collectors.joining(","));
  }

  /**
   * Prepares the object for marshalling.
   * <p>
   * This method is called before the object is marshalled to XML. It ensures that the keyframes field is set to null if it is empty.
   * </p>
   *
   * @param m The marshaller.
   */
  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
    if (this.keyframes != null && this.keyframes.isEmpty()) {
      this.keyframes = null;
    }
  }
}
