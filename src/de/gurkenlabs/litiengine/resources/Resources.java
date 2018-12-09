package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.GameData;
import de.gurkenlabs.litiengine.SpritesheetInfo;
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
  private static final Logger log = Logger.getLogger(Resources.class.getName());
  private static Fonts fonts = new Fonts();
  private static Sounds sounds = new Sounds();
  private static Maps maps = new Maps();
  private static Tilesets tilesets = new Tilesets();
  private static Strings strings = new Strings();
  private static Images images = new Images();
  private static Spritesheets spritesheets = new Spritesheets();

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
  
  /**
   * Load Spritesheets, Tilesets and Maps from a game resource file created with the utiLITI editor.
   * 
   * @param gameResourceFile
   *          the file name of the game resource file
   */
  public static void load(final String gameResourceFile) {
    final GameData file = GameData.load(gameResourceFile);
    if (file == null) {
      return;
    }

    int mapCnt = 0;
    for (final IMap m : file.getMaps()) {
      Resources.maps().add(m.getName(), m);
      mapCnt++;
    }

    log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] { mapCnt, gameResourceFile });

    int tileCnt = 0;
    for (final Tileset tileset : file.getTilesets()) {
      if (Resources.tilesets().contains(tileset.getName())) {
        continue;
      }

      Resources.tilesets().add(tileset.getName(), tileset);
      tileCnt++;
    }

    log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] { tileCnt, gameResourceFile });

    final List<Spritesheet> loadedSprites = new ArrayList<>();
    for (final SpritesheetInfo tileset : file.getSpriteSheets()) {
      final Spritesheet sprite = Resources.spritesheets().load(tileset);
      loadedSprites.add(sprite);
    }

    log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] { loadedSprites.size(), gameResourceFile });

    int spriteload = 0;
    for (final Spritesheet s : loadedSprites) {
      for (int i = 0; i < s.getRows() * s.getColumns(); i++) {
        BufferedImage sprite = s.getSprite(i);
        if (sprite != null) {
          spriteload++;
        }
      }
    }

    log.log(Level.INFO, "{0} sprites loaded to memory", new Object[] { spriteload });
  }
}
