package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Spritesheet {
  private static final Logger log = Logger.getLogger(Spritesheet.class.getName());

  private final List<Integer> emptySprites = new CopyOnWriteArrayList<>();

  private final BufferedImage image;
  private final String name;
  private final ImageFormat imageFormat;

  private BufferedImage[] sprites;
  private int columns;
  private int rows;
  private int spriteHeight;
  private int spriteWidth;

  public Spritesheet(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    checkImage(image, path);
    checkDimension(spriteHeight, "height");
    checkDimension(spriteWidth, "width");

    this.image = image;

    this.name = FileUtilities.getFileName(path);
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;
    this.imageFormat = ImageFormat.get(FileUtilities.getExtension(path));

    this.updateRowsAndCols();
    this.sprites = new BufferedImage[this.getTotalNumberOfSprites()];

    Resources.spritesheets().add(this.name, this);
    Resources.images().addClearedListener(() -> {
      this.emptySprites.clear();
      this.sprites = new BufferedImage[this.getTotalNumberOfSprites()];
    });
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

    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      scaled = opt.get();
    } else {
      if (img != null) {
        scaled = ImageProcessing.scaleImage(img, dimension, dimension, true);
      } else {
        scaled = ImageProcessing.getCompatibleImage(dimension, dimension);
      }

      Resources.images().add(cacheKey, scaled);
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
    if (this.emptySprites.contains(index) || this.sprites.length == 0) {
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
    return Resources.spritesheets().contains(this);
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

  private static void checkDimension(int value, String dimension) {
    if (value <= 0) {
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