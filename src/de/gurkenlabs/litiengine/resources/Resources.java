package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.TimeUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

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
   * Load <code>Spritesheets</code>, <code>Tilesets</code> and <code>Maps</code> from a game resource file created with the utiLITI editor.
   * After loading, these resources can be accessed via this API (e.g. <code>Resources.maps().get("mapname")</code>.
   * 
   * @param gameResourceFile
   *          The file name of the game resource file
   */
  public static void load(final String gameResourceFile) {
    final long loadStart = System.nanoTime();

    final ResourceBundle file = ResourceBundle.load(gameResourceFile);
    if (file == null) {
      return;
    }

    file.getMaps().parallelStream().forEach(m -> Resources.maps().add(m.getName(), m));

    log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] { file.getMaps().size(), gameResourceFile });

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
    InputStream stream = getResource(file);
    if (stream == null) {
      return null;
    }

    return stream.markSupported() ? stream : new BufferedInputStream(stream);
  }

  private static InputStream getResource(final String file) {
    // get resource from web
    if (file.startsWith("http://") || file.startsWith("https://")) {
      return getWebResource(file);
    }

    try {
      // get resource from class loader (required for jars)
      InputStream resourceStream = ClassLoader.getSystemResourceAsStream(file);
      if (resourceStream != null) {
        return resourceStream;
      }

      resourceStream = FileUtilities.class.getResourceAsStream(file);
      if (resourceStream != null) {
        return resourceStream;
      }

      // get resource from the local file system
      File f;
      try {
        URI uri = new URI(file);
        f = Paths.get(uri).toFile();
      } catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException e) {
        f = new File(file);
      }

      if (f.exists()) {
        resourceStream = new FileInputStream(f.getAbsolutePath());
        return resourceStream;
      } else {
        log.log(Level.INFO, "{0} could not be found.", file);
        return null;
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  private static InputStream getWebResource(String file) {

    try {
      URL url = new URL(file);
      final long downloadStart = System.nanoTime();
      try (InputStream in = url.openStream()) {
        File tmpFile = File.createTempFile(UUID.randomUUID().toString(), null);
        long downloaded = Files.copy(in, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        final double downloadTime = TimeUtilities.nanoToMs(System.nanoTime() - downloadStart);
        log.log(Level.INFO, "[Download: {0} bytes; {1} ms] from {2}", new Object[] { downloaded, downloadTime, url });
        return new FileInputStream(tmpFile);
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }
}
