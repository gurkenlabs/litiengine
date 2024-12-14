package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a texture atlas, which is a collection of images packed into a single image. This class provides methods to read the texture atlas from
 * an XML file and access its properties.
 */
@XmlRootElement(name = "TextureAtlas")
public class TextureAtlas {
  private static final Logger log = Logger.getLogger(TextureAtlas.class.getName());

  @XmlAttribute(name = "imagePath")
  private String rawImagePath;

  @XmlAttribute
  private int width;

  @XmlAttribute
  private int height;

  @XmlElement(name = "sprite")
  private List<Sprite> sprites;

  private String absoluteImagePath;

  /**
   * Default constructor for the TextureAtlas class. This constructor is kept for serialization purposes.
   */
  TextureAtlas() {
    // keep for serialization
  }

  /**
   * Reads a TextureAtlas from the specified XML file.
   *
   * @param textureAtlasFile The path to the XML file containing the texture atlas data.
   * @return The TextureAtlas object read from the file, or null if the file could not be read.
   */
  public static TextureAtlas read(String textureAtlasFile) {
    try {
      TextureAtlas atlas = XmlUtilities.read(TextureAtlas.class, Resources.getLocation(textureAtlasFile));
      if (atlas == null) {
        return null;
      }

      String directory = FileUtilities.getParentDirPath(textureAtlasFile);
      atlas.absoluteImagePath = FileUtilities.combine(directory, atlas.rawImagePath);
      return atlas;
    } catch (JAXBException e) {
      log.log(Level.SEVERE, String.format("TextureAtlas %s could not be read.", textureAtlasFile), e);
      return null;
    }
  }

  /**
   * Gets the absolute image path of the texture atlas.
   *
   * @return The absolute image path.
   */
  @XmlTransient
  public String getAbsoluteImagePath() {
    return this.absoluteImagePath;
  }

  /**
   * Gets the width of the texture atlas.
   *
   * @return The width of the texture atlas.
   */
  @XmlTransient
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the height of the texture atlas.
   *
   * @return The height of the texture atlas.
   */
  @XmlTransient
  public int getHeight() {
    return this.height;
  }

  /**
   * Gets the list of sprites in the texture atlas. If the list is null, it initializes a new empty list.
   *
   * @return The list of sprites.
   */
  @XmlTransient
  public List<Sprite> getSprites() {
    if (this.sprites == null) {
      this.sprites = new ArrayList<>();
    }

    return this.sprites;
  }

  /**
   * Retrieves a sprite by its name from the texture atlas.
   *
   * @param name The name of the sprite to retrieve.
   * @return The sprite with the specified name, or null if no such sprite exists.
   */
  public Sprite getSprite(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    return this.getSprites().stream().filter(x -> x.getName().equals(name)).findFirst().orElse(null);
  }

  /**
   * Sets the image path for the texture atlas.
   *
   * @param imagePath The new image path to set.
   */
  public void setImagePath(String imagePath) {
    this.rawImagePath = imagePath;
  }

  /**
   * Sets the width of the texture atlas.
   *
   * @param width The new width to set.
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Sets the height of the texture atlas.
   *
   * @param height The new height to set.
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Sets the list of sprites in the texture atlas.
   *
   * @param sprites The new list of sprites to set.
   */
  public void setSprites(List<Sprite> sprites) {
    this.sprites = sprites;
  }

  /**
   * Represents a sprite in the texture atlas. This class contains properties such as name, position, dimensions, and rotation status of the sprite.
   */
  @XmlRootElement(name = "sprite")
  public static class Sprite {
    @XmlAttribute(name = "n")
    private String name;

    @XmlAttribute()
    private int x;

    @XmlAttribute()
    private int y;

    @XmlAttribute(name = "w")
    private int width;

    @XmlAttribute(name = "h")
    private int height;

    @XmlAttribute(name = "oX")
    private int offsetX;

    @XmlAttribute(name = "oY")
    private int offsetY;

    @XmlAttribute(name = "r")
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean rotated;

    /**
     * Default constructor for the Sprite class. This constructor is kept for serialization purposes.
     */
    Sprite() {
      // keep for serialization
    }

    /**
     * Gets the name of the sprite.
     *
     * @return The name of the sprite.
     */
    @XmlTransient
    public String getName() {
      return this.name;
    }

    /**
     * Gets the x-coordinate of the sprite.
     *
     * @return The x-coordinate of the sprite.
     */
    @XmlTransient
    public int getX() {
      return this.x;
    }

    /**
     * Gets the y-coordinate of the sprite.
     *
     * @return The y-coordinate of the sprite.
     */
    @XmlTransient
    public int getY() {
      return this.y;
    }

    /**
     * Gets the width of the sprite.
     *
     * @return The width of the sprite.
     */
    @XmlTransient
    public int getWidth() {
      return this.width;
    }

    /**
     * Gets the height of the sprite.
     *
     * @return The height of the sprite.
     */
    @XmlTransient
    public int getHeight() {
      return this.height;
    }

    /**
     * Gets the x-offset of the sprite.
     *
     * @return The x-offset of the sprite.
     */
    @XmlTransient
    public int getOffsetX() {
      return this.offsetX;
    }

    /**
     * Gets the y-offset of the sprite.
     *
     * @return The y-offset of the sprite.
     */
    @XmlTransient
    public int getOffsetY() {
      return this.offsetY;
    }

    /**
     * Checks if the sprite is rotated.
     *
     * @return True if the sprite is rotated, false otherwise.
     */
    @XmlTransient
    public boolean isRotated() {
      return this.rotated != null && this.rotated;
    }

    /**
     * Sets the name of the sprite.
     *
     * @param name The new name to set.
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Sets the x-coordinate of the sprite.
     *
     * @param x The new x-coordinate to set.
     */
    public void setX(int x) {
      this.x = x;
    }

    /**
     * Sets the y-coordinate of the sprite.
     *
     * @param y The new y-coordinate to set.
     */
    public void setY(int y) {
      this.y = y;
    }

    /**
     * Sets the width of the sprite.
     *
     * @param width The new width to set.
     */
    public void setWidth(int width) {
      this.width = width;
    }

    /**
     * Sets the height of the sprite.
     *
     * @param height The new height to set.
     */
    public void setHeight(int height) {
      this.height = height;
    }

    /**
     * Sets the x-offset of the sprite.
     *
     * @param offsetX The new x-offset to set.
     */
    public void setOffsetX(int offsetX) {
      this.offsetX = offsetX;
    }

    /**
     * Sets the y-offset of the sprite.
     *
     * @param offsetY The new y-offset to set.
     */
    public void setOffsetY(int offsetY) {
      this.offsetY = offsetY;
    }

    /**
     * Sets the rotation status of the sprite.
     *
     * @param rotated The new rotation status to set.
     */
    public void setRotated(boolean rotated) {
      this.rotated = rotated;
    }
  }

  /**
   * Custom adapter to convert between String and Boolean values for XML serialization. This adapter interprets "y", "yes", "1", and "true"
   * (case-insensitive) as true, and any other value as false.
   */
  public static class CustomBooleanAdapter extends XmlAdapter<String, Boolean> {
    /**
     * Converts a String value to a Boolean.
     *
     * @param v The String value to convert.
     * @return The Boolean representation of the String value.
     * @throws Exception If an error occurs during conversion.
     */
    @Override
    public Boolean unmarshal(String v) throws Exception {
      if (v == null || v.isEmpty()) {
        return false;
      }

      return v.equalsIgnoreCase("y") || v.equalsIgnoreCase("yes") || v.equals("1") || v.equalsIgnoreCase("true");
    }

    /**
     * Converts a Boolean value to a String.
     *
     * @param v The Boolean value to convert.
     * @return The String representation of the Boolean value.
     * @throws Exception If an error occurs during conversion.
     */
    @Override
    public String marshal(Boolean v) throws Exception {
      return v ? "y" : "n";
    }
  }
}
