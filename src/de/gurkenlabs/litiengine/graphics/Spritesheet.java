/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.tilemap.ITileset;
import de.gurkenlabs.util.image.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

public class Spritesheet {
  public static final Map<String, Spritesheet> spritesheets = new ConcurrentHashMap<>();

  private static final Logger log = Logger.getLogger(Spritesheet.class.getName());
  private final String name;

  /** The rows. */
  private int rows;

  /** The sprite height. */
  private int spriteHeight;

  /** The sprites per row. */
  private int columns;

  /** The sprite width. */
  private int spriteWidth;

  private final int hashCode;

  private BufferedImage image;

  /**
   * The sprite info file must be located under the
   * {@link de.gurkenlags.litiengine.GameInfo#getSpritesDirectory()}. directory.
   * 
   * @param spriteInfoFile
   * @return
   */
  public static List<Spritesheet> load(final String spriteInfoFile) {
    final String COMMENT_CHAR = "#";

    ArrayList<Spritesheet> sprites = new ArrayList<>();
    InputStream fileStream = FileUtilities.getGameResource(spriteInfoFile);
    if (fileStream == null) {
      return sprites;
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
      String line;
      while ((line = br.readLine()) != null) {

        if (line.isEmpty() || line.startsWith(COMMENT_CHAR)) {
          continue;
        }

        List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
        if (items.size() < 3) {
          continue;
        }

        try {
          String dir = FileUtilities.getParentDirPath(spriteInfoFile);
          String name = Paths.get(dir, items.get(0)).toString();

          int width = Integer.parseInt(items.get(1));
          int height = Integer.parseInt(items.get(2));

          sprites.add(load(name, width, height));
        } catch (NumberFormatException nfe) {
          nfe.printStackTrace();
          continue;
        }
      }

      System.out.println(sprites.size() + " spritesheets loaded from '" + spriteInfoFile + "'");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return sprites;
  }

  public static Spritesheet load(final ITileset tileset) {
    if (tileset == null || tileset.getImage() == null) {
      return null;
    }

    if (tileset.getImage().getAbsoluteSourcePath() == null) {
      return null;
    }

    Spritesheet sprite = new Spritesheet(tileset);
    return sprite;
  }

  public static Spritesheet load(final String path, final int spriteWidth, final int spriteHeight) {
    Spritesheet sprite = new Spritesheet(path, spriteWidth, spriteHeight);

    return sprite;
  }

  public static Spritesheet load(final SpriteSheetInfo info) {
    return Spritesheet.load(ImageProcessing.decodeToImage(info.getImage()), info.getName(), info.getWidth(), info.getHeight());
  }

  public static Spritesheet load(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    Spritesheet sprite = new Spritesheet(image, path, spriteWidth, spriteHeight);
    return sprite;
  }

  public static Spritesheet find(final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    String name = FileUtilities.getFileName(path);

    return spritesheets.get(name.toLowerCase());
  }

  public static void remove(final String path) {
    spritesheets.values().removeIf(x -> x.getName().equals(path));
  }

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

  public BufferedImage getImage() {
    return this.image;
  }

  /**
   * Gets the sprites per row.
   *
   * @return the sprites per row
   */
  public int getColumns() {
    return this.columns;
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
    } catch (RasterFormatException rfe) {
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

  public void setSpriteHeight(int spriteHeight) {
    this.spriteHeight = spriteHeight;
    this.updateRowsAndCols();
  }

  public void setSpriteWidth(int spriteWidth) {
    this.spriteWidth = spriteWidth;
    this.updateRowsAndCols();
  }

  private Point getLocation(final int index) {
    final int row = index / this.getColumns();
    final int column = index % this.getColumns();

    return new Point(column * this.getSpriteWidth(), row * this.getSpriteHeight());
  }

  private void updateRowsAndCols() {
    BufferedImage sprite = this.getImage();
    if (sprite != null && sprite.getWidth() != 0 && sprite.getHeight() != 0 && this.spriteWidth != 0 && this.spriteHeight != 0) {
      this.columns = sprite.getWidth() / this.spriteWidth;
      this.rows = sprite.getHeight() / this.spriteHeight;
    } else {
      this.columns = 0;
      this.rows = 0;
    }
  }
}