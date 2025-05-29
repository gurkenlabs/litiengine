package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the loading, storing, and retrieval of spritesheets.
 * <p>
 * This class provides methods to load spritesheets from various sources, manage custom keyframe durations, and handle listeners for resource
 * container clearing events.
 * </p>
 */
public final class Spritesheets {
  private final Map<String, int[]> customKeyFrameDurations = new ConcurrentHashMap<>();
  private final Map<String, Spritesheet> loadedSpritesheets = new ConcurrentHashMap<>();
  private final Collection<ResourcesContainerClearedListener> listeners = ConcurrentHashMap.newKeySet();
  private static final Logger log = Logger.getLogger(Spritesheets.class.getName());
  private static final String SPRITE_INFO_COMMENT_CHAR = "#";

  Spritesheets() {
  }

  /**
   * Adds a spritesheet to the collection of loaded spritesheets.
   *
   * @param name        The name of the spritesheet.
   * @param spritesheet The spritesheet to add.
   */
  public void add(String name, Spritesheet spritesheet) {
    this.loadedSpritesheets.put(name, spritesheet);
  }

  /**
   * Adds a listener for resource container clearing events.
   *
   * @param listener The listener to add.
   */
  public void addClearedListener(ResourcesContainerClearedListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes a listener for resource container clearing events.
   *
   * @param listener The listener to remove.
   */
  public void removeClearedListener(ResourcesContainerClearedListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Clears all loaded spritesheets and notifies listeners of the clearing event.
   */
  public void clear() {
    this.loadedSpritesheets.clear();
    listeners.forEach(ResourcesContainerClearedListener::cleared);
  }

  /**
   * Checks if a spritesheet with the specified name is loaded.
   *
   * @param name The name of the spritesheet.
   * @return True if the spritesheet is loaded, false otherwise.
   */
  public boolean contains(String name) {
    return this.loadedSpritesheets.containsKey(name);
  }

  /**
   * Finds Spritesheets that were previously loaded by any load method or by the sprites.info file.
   *
   * @param path The path of the spritesheet.
   * @return The {@link Spritesheet} associated with the path or null if not loaded yet
   */
  public Spritesheet get(final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    final String name = FileUtilities.getFileName(path);

    return this.loadedSpritesheets.get(name); // this already returns null if absent
  }

  /**
   * Gets a collection of spritesheets that match the specified predicate.
   *
   * @param pred The predicate to filter the spritesheets.
   * @return A collection of spritesheets that match the predicate, or an empty collection if the predicate is null.
   */
  public Collection<Spritesheet> get(Predicate<? super Spritesheet> pred) {
    if (pred == null) {
      return new ArrayList<>();
    }

    return this.loadedSpritesheets.values().stream().filter(pred).toList();
  }

  /**
   * Gets a collection of all loaded spritesheets.
   *
   * @return A collection of all loaded spritesheets.
   */
  public Collection<Spritesheet> getAll() {
    return this.loadedSpritesheets.values();
  }

  /**
   * Gets the custom keyframe durations for the specified spritesheet name.
   *
   * @param name The name of the spritesheet.
   * @return An array of custom keyframe durations, or an empty array if none are found.
   */
  public int[] getCustomKeyFrameDurations(final String name) {
    return this.customKeyFrameDurations.getOrDefault(FileUtilities.getFileName(name), new int[0]);
  }

  /**
   * Gets the custom keyframe durations for the specified spritesheet.
   *
   * @param sprite The spritesheet to get the custom keyframe durations for.
   * @return An array of custom keyframe durations, or an empty array if none are found.
   */
  public int[] getCustomKeyFrameDurations(final Spritesheet sprite) {
    return getCustomKeyFrameDurations(sprite.getName());
  }

  /**
   * Loads a spritesheet from the specified image, path, width, and height.
   *
   * @param image        The image of the spritesheet.
   * @param path         The path of the spritesheet.
   * @param spriteWidth  The width of each sprite in the spritesheet.
   * @param spriteHeight The height of each sprite in the spritesheet.
   * @return The loaded spritesheet.
   */
  public Spritesheet load(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(image, path, spriteWidth, spriteHeight);
  }

  /**
   * Loads a spritesheet from the specified tileset.
   *
   * @param tileset The tileset to load the spritesheet from.
   * @return The loaded spritesheet, or null if the tileset or its image is null, or if the image's absolute source path is null.
   */
  public Spritesheet load(final ITileset tileset) {
    if (tileset == null || tileset.getImage() == null) {
      return null;
    }

    if (tileset.getImage().getAbsoluteSourcePath() == null) {
      return null;
    }

    return new Spritesheet(Resources.images().get(tileset.getImage().getAbsoluteSourcePath(), true), tileset.getImage().getSource(),
      tileset.getTileDimension().width, tileset.getTileDimension().height);
  }

  /**
   * Loads a spritesheet from the specified spritesheet resource.
   *
   * @param info The spritesheet resource containing the information to load the spritesheet.
   * @return The loaded spritesheet, or null if the image is null or empty.
   */
  public Spritesheet load(final SpritesheetResource info) {
    Spritesheet sprite;
    if (info.getImage() == null || info.getImage().isEmpty()) {
      log.log(Level.SEVERE, "Sprite {0} could not be loaded because no image is defined.", new Object[] {info.getName()});
      return null;
    } else {
      String fileExtension = info.getImageFormat() == null ? "" : info.getImageFormat().toFileExtension();
      sprite = load(Codec.decodeImage(info.getImage()), info.getName() + fileExtension, info.getWidth(), info.getHeight());
    }

    if (info.getKeyframes() != null && info.getKeyframes().length > 0) {
      customKeyFrameDurations.put(sprite.getName(), info.getKeyframes());
    }

    return sprite;
  }

  /**
   * The sprite info file must be located under the GameInfo#getSpritesDirectory() directory.
   *
   * @param spriteInfoFile The path to the sprite info file.
   * @return A list of spritesheets that were loaded from the info file.
   */
  public List<Spritesheet> loadFrom(final String spriteInfoFile) {

    final ArrayList<Spritesheet> sprites = new ArrayList<>();
    final InputStream fileStream = Resources.get(spriteInfoFile);
    if (fileStream == null) {
      return sprites;
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
      String line;
      while ((line = br.readLine()) != null) {

        if (line.isEmpty() || line.startsWith(SPRITE_INFO_COMMENT_CHAR)) {
          continue;
        }

        final String[] parts = line.split(";");
        if (parts.length == 0) {
          continue;
        }

        final List<String> items = Arrays.asList(parts[0].split("\\s*,\\s*"));
        if (items.size() < 3) {
          continue;
        }

        getSpriteSheetFromSpriteInfoLine(FileUtilities.getParentDirPath(spriteInfoFile), sprites, items, parts);
      }

      log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] {sprites.size(), spriteInfoFile});
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return sprites;
  }

  /**
   * Saves the spritesheet information to the specified file.
   *
   * @param spriteInfoFile The path to the file where the spritesheet information will be saved.
   * @param metadataOnly   If true, only the metadata will be saved; if false, both the sprites and metadata will be saved.
   * @return True if the spritesheet information was successfully saved, false otherwise.
   */
  public boolean saveTo(final String spriteInfoFile, boolean metadataOnly) {
    // get all spritesheets
    List<Spritesheet> allSpriteSheets = new ArrayList<>(getAll());
    Collections.sort(allSpriteSheets);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(spriteInfoFile))) {
      // print the spritesheet information to the info file
      for (Spritesheet spritesheet : allSpriteSheets) {
        // check for keyframes
        int[] keyFrames = Resources.spritesheets().getCustomKeyFrameDurations(spritesheet);

        String fileExtension = spritesheet.getImageFormat() == ImageFormat.UNSUPPORTED ? "" : spritesheet.getImageFormat().toFileExtension();

        writer.write(String.format(
          "%s%s,%d,%d",
          spritesheet.getName(),
          fileExtension,
          spritesheet.getSpriteWidth(),
          spritesheet.getSpriteHeight()));

        // print keyframes (if they exist)
        if (keyFrames.length > 0) {
          writer.write(";");
          writer.write(ArrayUtilities.join(keyFrames));
        }

        writer.write("\n");

        if (!metadataOnly) {
          Path spriteInfoPath = Path.of(spriteInfoFile);
          ImageSerializer.saveImage(
            spriteInfoPath.resolveSibling(spritesheet.getName() + spritesheet.getImageFormat().toFileExtension()).toString(),
            spritesheet.getImage(),
            spritesheet.getImageFormat());
        }
      }

      var msg = metadataOnly ? "Exported Spritesheet metadata to {0}" : "Exported sprites and metadata to {0}";
      log.log(Level.INFO, msg, new Object[] {spriteInfoFile});

      return true;
    } catch (IOException e2) {
      log.log(Level.SEVERE, e2.getLocalizedMessage(), e2);
      return false;
    }
  }

  /**
   * Loads a spritesheet from the specified path, width, and height.
   *
   * @param path         The path of the spritesheet.
   * @param spriteWidth  The width of each sprite in the spritesheet.
   * @param spriteHeight The height of each sprite in the spritesheet.
   * @return The loaded spritesheet.
   */
  public Spritesheet load(final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(Resources.images().get(path, true), path, spriteWidth, spriteHeight);
  }

  /**
   * Removes a spritesheet from the collection of loaded spritesheets.
   *
   * @param path The path of the spritesheet to remove.
   * @return The removed spritesheet, or null if no spritesheet was found for the specified path.
   */
  public Spritesheet remove(final String path) {
    Spritesheet spriteToRemove = this.loadedSpritesheets.remove(path);
    customKeyFrameDurations.remove(path);
    return spriteToRemove;
  }

  /**
   * Updates a spritesheet with the specified resource information.
   *
   * @param info The resource information for the spritesheet to update.
   * @return The updated spritesheet, or null if the resource information is invalid or the spritesheet could not be updated.
   */
  public Spritesheet update(final SpritesheetResource info) {
    if (info == null || info.getName() == null) {
      return null;
    }

    Spritesheet spriteToRemove = remove(info.getName());

    if (spriteToRemove != null) {
      customKeyFrameDurations.remove(info.getName());
      if (info.getHeight() == 0 && info.getWidth() == 0) {
        return null;
      }
      return load(info);
    }
    return null;
  }

  /**
   * Parses a line from the sprite info file and loads the corresponding spritesheet.
   *
   * @param baseDirectory The base directory of the sprite info file.
   * @param sprites       The list to which the loaded spritesheet will be added.
   * @param items         The list of items parsed from the line.
   * @param parts         The array of parts parsed from the line.
   */
  private void getSpriteSheetFromSpriteInfoLine(String baseDirectory, ArrayList<Spritesheet> sprites, List<String> items, String[] parts) {
    try {
      final String name = baseDirectory + items.get(0);

      final int width = Integer.parseInt(items.get(1));
      final int height = Integer.parseInt(items.get(2));

      final Spritesheet sprite = load(name, width, height);
      sprites.add(sprite);
      if (parts.length >= 2) {
        final List<String> keyFrameStrings = Arrays.asList(parts[1].split("\\s*,\\s*"));
        customKeyFrameDurations.put(sprite.getName(), keyFrameStrings.stream().mapToInt(Integer::parseInt).toArray());
      }
    } catch (final NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
