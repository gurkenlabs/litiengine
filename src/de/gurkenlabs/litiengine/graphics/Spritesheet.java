package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpritesheetInfo;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Spritesheet {
  private static final Map<String, int[]> customKeyFrameDurations = new ConcurrentHashMap<>();
  private static final Map<String, Spritesheet> spritesheets = new ConcurrentHashMap<>();
  private static final Logger log = Logger.getLogger(Spritesheet.class.getName());
  private static final String SPRITE_INFO_COMMENT_CHAR = "#";

  private final List<Integer> emptySprites = new CopyOnWriteArrayList<>();

  private final int hashCode;
  private final BufferedImage image;
  private final String name;
  private final ImageFormat imageFormat;

  private BufferedImage[] sprites;
  private int columns;
  private int rows;
  private int spriteHeight;
  private int spriteWidth;

  private boolean loaded;

  private Spritesheet(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    checkImage(image, path);
    checkDimension(spriteHeight, "height");
    checkDimension(spriteWidth, "width");

    this.image = image;

    this.name = FileUtilities.getFileName(path);
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;
    this.imageFormat = ImageFormat.get(FileUtilities.getExtension(path));

    this.hashCode = this.getName().hashCode();
    this.updateRowsAndCols();
    this.sprites = new BufferedImage[this.getTotalNumberOfSprites()];

    spritesheets.put(this.name.toLowerCase(), this);
    this.loaded = true;
    ImageCache.SPRITES.onCleared(cache -> {
      this.emptySprites.clear();
      this.sprites = new BufferedImage[this.getTotalNumberOfSprites()];
    });
  }

  private Spritesheet(final ITileset tileset) {
    this(Resources.getImage(tileset.getImage().getAbsoluteSourcePath(), true), tileset.getImage().getSource(), tileset.getTileDimension().width, tileset.getTileDimension().height);
  }

  private Spritesheet(final String path, final int spriteWidth, final int spriteHeight) {
    this(Resources.getImage(path, true), path, spriteWidth, spriteHeight);
  }

  public static Collection<Spritesheet> getSpritesheets() {
    return spritesheets.values();
  }

  /**
   * Finds Spritesheets that were previously loaded by any load method or by the
   * sprites.info file.
   * 
   * @param path
   *          The path of the spritesheet.
   * @return The {@link Spritesheet} assotiated with the path or null if not
   *         loaded yet
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

  public static Collection<Spritesheet> find(Predicate<Spritesheet> pred) {
    if (pred == null) {
      return new ArrayList<>();
    }

    return spritesheets.values().stream().filter(pred).collect(Collectors.toList());
  }

  public static int[] getCustomKeyFrameDurations(final String name) {
    if (customKeyFrameDurations.containsKey(FileUtilities.getFileName(name).toLowerCase())) {
      return customKeyFrameDurations.get(FileUtilities.getFileName(name).toLowerCase());
    }

    return new int[0];
  }

  public static int[] getCustomKeyFrameDurations(final Spritesheet sprite) {
    return getCustomKeyFrameDurations(sprite.getName());
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

  public static Spritesheet load(final SpritesheetInfo info) {
    Spritesheet sprite = null;
    if (info.getImage() == null || info.getImage().isEmpty()) {
      log.log(Level.SEVERE, "Sprite {0} could not be loaded because no image is defined.", new Object[] { info.getName() });
      return null;
    } else {
      sprite = Spritesheet.load(ImageProcessing.decodeToImage(info.getImage()), info.getName(), info.getWidth(), info.getHeight());
    }

    if (info.getKeyframes() != null && info.getKeyframes().length > 0) {
      customKeyFrameDurations.put(sprite.getName().toLowerCase(), info.getKeyframes());
    }

    return sprite;
  }

  /**
   * The sprite info file must be located under the
   * GameInfo#getSpritesDirectory() directory.
   *
   * @param spriteInfoFile
   *          The path to the sprite info file.
   * @return A list of spritesheets that were loaded from the info file.
   */
  public static List<Spritesheet> load(final String spriteInfoFile) {

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

        getSpriteSheetFromSpriteInfoLine(FileUtilities.getParentDirPath(spriteInfoFile), sprites, items, parts);
      }

      log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] { sprites.size(), spriteInfoFile });
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return sprites;
  }

  public static Spritesheet load(final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(path, spriteWidth, spriteHeight);
  }

  public static Spritesheet remove(final String path) {
    if (!spritesheets.containsKey(path.toLowerCase())) {
      return null;
    }

    Spritesheet spriteToRemove = spritesheets.get(path.toLowerCase());
    spritesheets.remove(path.toLowerCase());
    spriteToRemove.loaded = false;

    customKeyFrameDurations.remove(path);
    return spriteToRemove;
  }

  public static void update(final SpritesheetInfo info) {
    if (info == null || info.getName() == null) {
      return;
    }

    final String spriteName = info.getName().toLowerCase();
    if (!spritesheets.containsKey(spriteName)) {
      return;
    }

    Spritesheet spriteToRemove = spritesheets.get(spriteName);
    spritesheets.remove(spriteName);
    customKeyFrameDurations.remove(spriteName);

    if (info.getHeight() == 0 && info.getWidth() == 0) {
      spriteToRemove.loaded = false;
      return;
    }

    load(info);
    spriteToRemove.loaded = false;
  }

  /**
   * Gets the sprites per row.
   *
   * @return the sprites per row
   */
  public int getColumns() {
    return this.columns;
  }

  public BufferedImage getPreview(int dimension) {
    final BufferedImage img = this.getSprite(0);
    BufferedImage scaled = null;
    String cacheKey = "iconx" + dimension + this.getName();
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      scaled = ImageCache.SPRITES.get(cacheKey);
    } else {
      if (img != null) {
        scaled = ImageProcessing.scaleImage(img, dimension, dimension, true);
      } else {
        scaled = ImageProcessing.getCompatibleImage(dimension, dimension);
      }

      ImageCache.SPRITES.put(cacheKey, scaled);
    }

    return scaled;
  }

  public BufferedImage getImage() {
    return this.image;
  }

  public ImageFormat getImageFormat() {
    return this.imageFormat;
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
    return this.getSprite(index, 0, 0);
  }

  public BufferedImage getSprite(final int index, final int margin, final int spacing) {
    if (this.emptySprites.contains(index)) {
      return null;
    }

    if (this.sprites[index] != null) {
      return this.sprites[index];
    }

    if (this.getImage() == null) {
      log.warning("no image defined for sprite '" + this.getName() + "'");
      return null;
    }

    final Point position = this.getLocation(index, margin, spacing);
    try {
      final BufferedImage sprite = this.getImage().getSubimage(position.x, position.y, this.spriteWidth, this.spriteHeight);
      if (ImageProcessing.isEmpty(sprite)) {
        emptySprites.add(index);
        return null;
      }

      this.sprites[index] = sprite;
      return sprite;
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

  public boolean isLoaded() {
    return this.loaded;
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
    checkDimension(spriteHeight, "height");

    this.spriteHeight = spriteHeight;
    this.updateRowsAndCols();
  }

  public void setSpriteWidth(final int spriteWidth) {
    checkDimension(spriteWidth, "width");

    this.spriteWidth = spriteWidth;
    this.updateRowsAndCols();
  }

  private static void getSpriteSheetFromSpriteInfoLine(String baseDirectory, ArrayList<Spritesheet> sprites, List<String> items, String[] parts) {
    try {
      final String name = baseDirectory + items.get(0);

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
    }
  }

  private static void checkDimension(int spriteHeight, String dimension) {
    if (spriteHeight <= 0) {
      throw new IllegalArgumentException("Invalid sprite dimensions! Sprite " + dimension + " must to be greater than 0.");
    }
  }

  private static void checkImage(BufferedImage image, String name) {
    if (image == null) {
      throw new IllegalArgumentException("The image for the spritesheet '" + name + "' is null!");
    }

    if (image.getWidth() <= 0 || image.getHeight() <= 0) {
      String error = String.format("Invalid image dimensions for spritesheet %s! Width and height must be greater than 0 (actual dimensions: %dx%d).", name, image.getWidth(), image.getHeight());
      throw new IllegalArgumentException(error);
    }
  }

  private Point getLocation(final int index, final int margin, final int spacing) {
    final int row = index / this.getColumns();
    final int column = index % this.getColumns();

    return new Point(margin + column * (this.getSpriteWidth() + spacing), margin + row * (this.getSpriteHeight() + spacing));
  }

  private void updateRowsAndCols() {
    final BufferedImage sprite = this.getImage();
    this.columns = sprite.getWidth() / this.spriteWidth;
    this.rows = sprite.getHeight() / this.spriteHeight;
  }
}