package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.TimeUtilities;

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
 * <li>videos</li>
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
  private static Blueprints blueprints = new Blueprints();
  private static Videos videos = new Videos();

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
   * Gets the container that manages <code>Blueprint</code> resources.
   * 
   * @return The Blueprint resource container.
   * 
   * @see Blueprint
   */
  public static Blueprints blueprints() {
    return blueprints;
  }
  
  /**
   * Gets the container that manages <code>VideoManager</code> resources.
   * 
   * @return The VideoManager resource container.
   * 
   * @see VideoManager
   */
  public static Videos videos() {
    return videos;
  }

  /**
   * Load <code>Spritesheets</code>, <code>Tilesets</code> and <code>Maps</code> from a game resource file created with the utiLITI editor.
   * After loading, these resources can be accessed via this API (e.g. <code>Resources.maps().get("mapname")</code>.
   * 
   * @param gameResourceFile
   *          The file name of the game resource file
   */
  public static void load(final String gameResourceFile) {
    load(getLocation(gameResourceFile));
  }

  /**
   * Load <code>Spritesheets</code>, <code>Tilesets</code> and <code>Maps</code> from a game resource file created with the utiLITI editor.
   * After loading, these resources can be accessed via this API (e.g. <code>Resources.maps().get("mapname")</code>.
   * 
   * @param gameResourceFile
   *          The URL to the game resource file
   */
  public static void load(final URL gameResourceFile) {
    final long loadStart = System.nanoTime();

    final ResourceBundle file = ResourceBundle.load(gameResourceFile);
    if (file == null) {
      return;
    }

    file.getMaps().parallelStream().forEach(m -> Resources.maps().add(m.getName(), m));

    log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] { file.getMaps().size(), gameResourceFile });

    file.getBluePrints().parallelStream().forEach(m -> Resources.blueprints().add(m.getName(), m));

    log.log(Level.INFO, "{0} blueprints loaded from {1}", new Object[] { file.getBluePrints().size(), gameResourceFile });

    int tileCnt = 0;
    for (final Tileset tileset : file.getTilesets()) {
      if (Resources.tilesets().contains(tileset.getName())) {
        continue;
      }

      Resources.tilesets().add(tileset.getName(), tileset);
      tileCnt++;
    }

    log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] { tileCnt, gameResourceFile });

    final List<Spritesheet> loadedSprites = Collections.synchronizedList(new ArrayList<>());
    file.getSpriteSheets().parallelStream().forEach(spriteSheetInfo -> {
      final Spritesheet sprite = Resources.spritesheets().load(spriteSheetInfo);
      loadedSprites.add(sprite);
    });

    log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] { loadedSprites.size(), gameResourceFile });

    final List<Sound> loadedSounds = Collections.synchronizedList(new ArrayList<>());
    file.getSounds().parallelStream().forEach(soundResource -> {
      final Sound sound = Resources.sounds().load(soundResource);
      loadedSounds.add(sound);
    });

    log.log(Level.INFO, "{0} sounds loaded from {1}", new Object[] { loadedSounds.size(), gameResourceFile });

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

    for (final EmitterData emitter : file.getEmitters()) {
      CustomEmitter.load(emitter);
    }

    final double loadTime = TimeUtilities.nanoToMs(System.nanoTime() - loadStart);

    log.log(Level.INFO, "loading game resources from {0} took {1} ms", new Object[] { gameResourceFile, loadTime });
  }

  /**
   * Gets the specified file as InputStream from either a resource folder or the file system.
   * 
   * @param file
   *          The path to the file.
   * @return The contents of the specified file as {@code InputStream}.
   * @see Resources
   */
  public static InputStream get(String file) {
    return get(getLocation(file));
  }

  /**
   * Gets the specified file as InputStream from either a resource folder or the file system.
   * 
   * @param file
   *          The path to the file.
   * @return The contents of the specified file as {@code InputStream}.
   * @see Resources
   */
  public static InputStream get(URL file) {
    InputStream stream = getResource(file);
    if (stream == null) {
      return null;
    }

    return stream.markSupported() ? stream : new BufferedInputStream(stream);
  }

  /**
   * Reads the specified file as String from either a resource folder or the file system.<br>
   * Since no {@code Charset} is specified with this overload, the implementation uses UTF-8 by default.
   * 
   * @param file
   *          The path to the file.
   * @return The contents of the specified file as {@code String}
   */
  public static String read(String file) {
    return read(file, StandardCharsets.UTF_8);
  }

  /**
   * Reads the specified file as String from either a resource folder or the file system.<br>
   * 
   * @param file
   *          The path to the file.
   * @param charset
   *          The charset that is used to read the String from the file.
   * @return The contents of the specified file as {@code String}
   */
  public static String read(String file, Charset charset) {
    final URL location = getLocation(file);
    if (location == null) {
      return null;
    }

    return read(location, charset);
  }

  /**
   * Reads the specified file as String from either a resource folder or the file system.<br>
   * Since no {@code Charset} is specified with this overload, the implementation uses UTF-8 by default.
   * 
   * @param file
   *          The path to the file.
   * @return The contents of the specified file as {@code String}
   */
  public static String read(URL file) {
    return read(file, StandardCharsets.UTF_8);
  }

  /**
   * Reads the specified file as String from either a resource folder or the file system.<br>
   * 
   * @param file
   *          The path to the file.
   * @param charset
   *          The charset that is used to read the String from the file.
   * @return The contents of the specified file as {@code String}
   */
  public static String read(URL file, Charset charset) {
    try (Scanner scanner = new Scanner(file.openStream(), charset.toString())) {
      scanner.useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : null;
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  /**
   * Clears the all resource containers by removing previously loaded resources.
   */
  public static void clearAll() {
    fonts().clear();
    sounds().clear();
    maps().clear();
    tilesets().clear();
    images().clear();
    spritesheets().clear();
    videos().clear();
  }

  public static URL getLocation(String name) {
    URL fromClass = ClassLoader.getSystemResource(name);
    if (fromClass != null) {
      return fromClass;
    }
    try {
      return new URL(name);
    } catch (MalformedURLException e) {
      try {
        return (new File(name)).toURI().toURL();
      } catch (MalformedURLException e1) {
        return null;
      }
    }
  }

  private static InputStream getResource(final URL file) {
    try {
      return file.openStream();
    } catch (IOException e) {
      return null;
    }
  }
}
