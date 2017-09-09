package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

public final class Spritesheet {
  private static final Map<String, int[]> customKeyFrameDurations = new ConcurrentHashMap<>();
  private static final Map<String, Spritesheet> spritesheets = new ConcurrentHashMap<>();
  private static final Logger log = Logger.getLogger(Spritesheet.class.getName());
  private static final String SPRITE_INFO_COMMENT_CHAR = "#";

  private int columns;

  private final int hashCode;

  private final BufferedImage image;

  private final String name;

  /** The rows. */
  private int rows;

  /** The sprite height. */
  private int spriteHeight;

  /** The sprite width. */
  private int spriteWidth;

  private Spritesheet(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    this.image = image;

    this.name = FileUtilities.getFileName(path);
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;

    this.hashCode = this.getName().hashCode();
    this.updateRowsAndCols();

    spritesheets.put(this.name.toLowerCase(), this);
  }

  private Spritesheet(final ITileset tileset) {
    this(RenderEngine.getImage(tileset.getImage().getAbsoluteSourcePath(), true), tileset.getImage().getSource(), tileset.getTileDimension().width, tileset.getTileDimension().height);
  }

  private Spritesheet(final String path, final int spriteWidth, final int spriteHeight) {
    this(RenderEngine.getImage(path, true), path, spriteWidth, spriteHeight);
  }

  public static Map<String, Spritesheet> getSpritesheets() {
    return spritesheets;
  }

  /**
   * Finds Spritesheets that were previously loaded by any load method or by the
   * sprites.info file.
   * 
   * @param path
   * @return The Spritesheet assotiated with the path or null if not loaded yet
   */
  public static Spritesheet find(final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    final String name = FileUtilities.getFileName(path).toLowerCase();

    if (!spritesheets.containsKey(name)) {
      return null;
    }

    return spritesheets.get(name);
  }

  public static int[] getCustomKeyFrameDurations(final String name) {
    if (customKeyFrameDurations.containsKey(FileUtilities.getFileName(name).toLowerCase())) {
      return customKeyFrameDurations.get(FileUtilities.getFileName(name).toLowerCase());
    }

    return new int[0];
  }

  public static Spritesheet load(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(image, path, spriteWidth, spriteHeight);
  }

  public static Spritesheet load(final ITileset tileset) {
    if (tileset == null || tileset.getImage() == null) {
      return null;
    }

    if (tileset.getImage().getAbsoluteSourcePath() == null) {
      return null;
    }

    return new Spritesheet(tileset);
  }

  public static Spritesheet load(final SpriteSheetInfo info) {
    return Spritesheet.load(ImageProcessing.decodeToImage(info.getImage()), info.getName(), info.getWidth(), info.getHeight());
  }

  public static List<Spritesheet> load(final String spriteInfoFile) {
    return load(spriteInfoFile, "");
  }

  /**
   * The sprite info file must be located under the
   * {@link de.gurkenlags.litiengine.GameInfo#getSpritesDirectory()}. directory.
   *
   * @param spriteInfoFile
   * @return
   */
  public static List<Spritesheet> load(final String spriteInfoFile, final String gameDirectory) {

    final ArrayList<Spritesheet> sprites = new ArrayList<>();
    final InputStream fileStream = FileUtilities.getGameResource(spriteInfoFile);
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

        try {
          final String name = gameDirectory + Game.getInfo().getSpritesDirectory() + items.get(0);

          final int width = Integer.parseInt(items.get(1));
          final int height = Integer.parseInt(items.get(2));

          final Spritesheet sprite = load(name, width, height);
          sprites.add(sprite);
          if (parts.length >= 2) {
            final List<String> keyFrameStrings = Arrays.asList(parts[1].split("\\s*,\\s*"));
            if (!keyFrameStrings.isEmpty()) {
              final int[] keyFrames = new int[keyFrameStrings.size()];
              for (int i = 0; i < keyFrameStrings.size(); i++) {
                final int keyFrame = Integer.parseInt(keyFrameStrings.get(i));
                keyFrames[i] = keyFrame;
              }

              customKeyFrameDurations.put(sprite.getName().toLowerCase(), keyFrames);
            }
          }
        } catch (final NumberFormatException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
          continue;
        }
      }

      log.log(Level.INFO, "{0} spritesheets loaded from \'{1}\'", new Object[] { sprites.size(), spriteInfoFile });
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return sprites;
  }

  public static Spritesheet load(final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(path, spriteWidth, spriteHeight);
  }

  public static void remove(final String path) {
    spritesheets.values().removeIf(x -> x.getName().equals(path));
  }

  /**
   * Gets the sprites per row.
   *
   * @return the sprites per row
   */
  public int getColumns() {
    return this.columns;
  }

  public BufferedImage getImage() {
    return this.image;
  }

  /**
   * The unique name of this spritesheet. A spritesheet can always be identified
   * by this name within a game project.
   *
   * @return The name of the spritesheet.
   */
  public String getName() {
    return this.name;
  }

  public int getRows() {
    return this.rows;
  }

  public BufferedImage getSprite(final int index) {
    final String imageCacheKey = MessageFormat.format("{0}_{1}", this.hashCode, index);
    if (ImageCache.SPRITES.containsKey(imageCacheKey)) {
      return ImageCache.SPRITES.get(imageCacheKey);
    }

    if (this.getImage() == null) {
      log.warning("no image defined for sprite '" + this.getName() + "'");
      return null;
    }

    final Point position = this.getLocation(index);
    try {
      final BufferedImage smallImage = this.getImage().getSubimage(position.x, position.y, this.spriteWidth, this.spriteHeight);
      ImageCache.SPRITES.put(imageCacheKey, smallImage);
      return smallImage;
    } catch (final RasterFormatException rfe) {
      log.warning("could not read sprite of size [" + this.spriteWidth + "x" + this.spriteHeight + " at position [" + position.x + "," + position.y + "] from sprite'" + this.getName() + "'");
      return null;
    }
  }

  /**
   * Gets the sprite height.
   *
   * @return the sprite height
   */
  public int getSpriteHeight() {
    return this.spriteHeight;
  }

  /**
   * Gets the sprite width.
   *
   * @return the sprite width
   */
  public int getSpriteWidth() {
    return this.spriteWidth;
  }

  /**
   * Gets the total sprites.
   *
   * @return the total sprites
   */
  public int getTotalNumberOfSprites() {
    return this.getRows() * this.getColumns();
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Spritesheet) {
      return object.hashCode() == this.hashCode();
    }

    return super.equals(object);
  }

  public void setSpriteHeight(final int spriteHeight) {
    this.spriteHeight = spriteHeight;
    this.updateRowsAndCols();
  }

  public void setSpriteWidth(final int spriteWidth) {
    this.spriteWidth = spriteWidth;
    this.updateRowsAndCols();
  }

  private Point getLocation(final int index) {
    final int row = index / this.getColumns();
    final int column = index % this.getColumns();

    return new Point(column * this.getSpriteWidth(), row * this.getSpriteHeight());
  }

  private void updateRowsAndCols() {
    final BufferedImage sprite = this.getImage();
    if (sprite != null && sprite.getWidth() != 0 && sprite.getHeight() != 0 && this.spriteWidth != 0 && this.spriteHeight != 0) {
      this.columns = sprite.getWidth() / this.spriteWidth;
      this.rows = sprite.getHeight() / this.spriteHeight;
    } else {
      this.columns = 0;
      this.rows = 0;
    }
  }
}