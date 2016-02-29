/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.tiled.tmx.ITileset;

/**
 * The spritesheet class represents a series of sprites, with the specified
 * dimensions and attributes.
 */
public class Spritesheet {

  private static final List<Spritesheet> spritesheets = new CopyOnWriteArrayList<>();

  /** The path. */
  private final String path;

  /** The rows. */
  private final int rows;

  /** The sprite height. */
  private final int spriteHeight;

  /** The sprites per row. */
  private final int columns;

  /** The sprite width. */
  private final int spriteWidth;

  public Spritesheet(final ITileset tileset) {
    this.path = tileset.getImage().getAbsoluteSourcePath();
    this.spriteWidth = tileset.getTileDimension().width;
    this.spriteHeight = tileset.getTileDimension().height;
    this.columns = tileset.getImage().getDimension().width / tileset.getTileDimension().width;
    this.rows = tileset.getImage().getDimension().height / tileset.getTileDimension().height;
    spritesheets.add(this);
  }

  /**
   * Instantiates a new spritesheet.
   *
   * @param path
   *          the path
   * @param spriteWidth
   *          the sprite width
   * @param spriteHeight
   *          the sprite height
   * @param columns
   *          the sprites per row
   * @param rows
   *          the rows
   */
  public Spritesheet(final String path, final int spriteWidth, final int spriteHeight, final int columns, final int rows) {
    this.path = path;
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;
    this.columns = columns;
    this.rows = rows;
    spritesheets.add(this);
  }

  public static Spritesheet find(final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    final Optional<Spritesheet> sheet = spritesheets.stream().filter(x -> x.getPath().equalsIgnoreCase(path)).findFirst();
    if (!sheet.isPresent()) {
      return null;
    }

    return sheet.get();
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
   * Gets the path.
   *
   * @return the path
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Gets the rows.
   *
   * @return the rows
   */
  public int getRows() {
    return this.rows;
  }

  public BufferedImage getSprite(final int index) {
    final String imageCacheKey = MessageFormat.format("{0}_{1}", this.getPath().hashCode(), index);
    if (ImageCache.SPRITES.containsKey(imageCacheKey)) {
      return ImageCache.SPRITES.get(imageCacheKey);
    }

    
    final BufferedImage bigImg = RenderEngine.getImage(this.getPath());
    if (bigImg == null) {
      return null;
    }

    final Point position = this.getLocation(index);
    final BufferedImage smallImage = bigImg.getSubimage((int) position.getX(), (int) position.getY(), this.spriteWidth, this.spriteHeight);
    ImageCache.SPRITES.putPersistent(imageCacheKey, smallImage);
    return smallImage;
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

  private Point getLocation(final int index) {
    final int row = index / this.columns;
    final int column = index % this.columns;

    return new Point(column * this.getSpriteWidth(), row * this.getSpriteHeight());
  }
}