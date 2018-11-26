package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;

/**
 * This class is the engines entry point for accessing any kind of resources. A resource is any non-executable data that is deployed with your game.
 * The <code>Resources</code> class provides access to types of <code>ResourcesContainers</code> and is used by different (loading) mechanisms to make
 * resources available during runtime.
 * <p>
 * The LITIengine supports a variety of different resource types, including:
 * </p>
 * 
 * <ul>
 * <li>images</li>
 * <li>fonts</li>
 * <li>maps</li>
 * <li>(localizable) strings</li>
 * <li>spritesheets</li>
 * <li>sounds</li>
 * </ul>
 * 
 * @see ResourcesContainer
 */
public final class Resources {
  private static Fonts fonts;
  private static Sounds sounds;
  private static Maps maps;
  private static Tilesets tilesets;
  private static Strings strings;
  private static Images images;
  private static Spritesheets spritesheets;

  static {
    fonts = new Fonts();
    sounds = new Sounds();
    maps = new Maps();
    tilesets = new Tilesets();
    strings = new Strings();
    images = new Images();
    spritesheets = new Spritesheets();
  }

  private Resources() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the container that manages <code>Font</code> resources.
   * 
   * @return The Font resource container.
   * 
   * @see Font
   */
  public static Fonts fonts() {
    return fonts;
  }

  /**
   * Gets the container that manages <code>Sound</code> resources.
   * 
   * @return The Sound resource container.
   * 
   * @see Sound
   */
  public static Sounds sounds() {
    return sounds;
  }

  /**
   * Gets the container that manages <code>IMap</code> resources.
   * 
   * @return The IMap resource container.
   * 
   * @see IMap
   */
  public static Maps maps() {
    return maps;
  }

  /**
   * Gets the container that manages <code>Tileset</code> resources.<br>
   * This implementation uses raw {@code Tileset}s, to avoid problems with
   * {@code Tileset} methods that aren't in the {@code ITileset} interface.
   * 
   * @return The Tileset resource container.
   * 
   * @see Tileset
   */
  public static Tilesets tilesets() {
    return tilesets;
  }

  /**
   * Gets a container that manages <code>String</code> resources.<br>
   * This instance can be used to access localizable string from a ".properties" file.
   * 
   * @return The String resource container.
   */
  public static Strings strings() {
    return strings;
  }

  /**
   * Gets the container that manages <code>BufferedImage</code> resources.
   * 
   * @return The BufferedImage resource container.
   * 
   * @see BufferedImage
   */
  public static Images images() {
    return images;
  }

  /**
   * Gets the container that manages <code>Spritesheet</code> resources.
   * 
   * @return The Spritesheet resource container.
   * 
   * @see Spritesheet
   */
  public static Spritesheets spritesheets() {
    return spritesheets;
  }
}
