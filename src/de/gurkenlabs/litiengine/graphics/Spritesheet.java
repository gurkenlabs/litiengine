/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Image;
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

  public static final List<Spritesheet> spritesheets = new CopyOnWriteArrayList<>();

  public static Spritesheet findByName(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    final Optional<Spritesheet> sheet = spritesheets.stream().filter(x -> x.getPath().endsWith(name)).findFirst();
    if (!sheet.isPresent()) {
      return null;
    }

    return sheet.get();
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

  /** The path. */
  private final String path;

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

  public Spritesheet(final ITileset tileset) {
    this.path = tileset.getImage().getAbsoluteSourcePath();
    this.spriteWidth = tileset.getTileDimension().width;
    this.spriteHeight = tileset.getTileDimension().height;
    this.columns = tileset.getImage().getDimension().width / tileset.getTileDimension().width;
    this.rows = tileset.getImage().getDimension().height / tileset.getTileDimension().height;
    spritesheets.add(this);
    this.hashCode = this.getPath().hashCode();
  }

  public Spritesheet(final String path, final int spriteWidth, final int spriteHeight) {
    this.path = path;
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;

    spritesheets.add(this);
    this.hashCode = this.getPath().hashCode();
    this.updateRowsAndCols();
  }

  public Spritesheet(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    this.image = image;
    
    this.path = path;
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;
    spritesheets.add(this);
    this.hashCode = this.getPath().hashCode();
    this.updateRowsAndCols();
  }

  private void updateRowsAndCols() {
    BufferedImage sprite =  this.getImage();
    if (sprite != null && sprite.getWidth() != 0 && sprite.getHeight() != 0 && this.spriteWidth != 0 && this.spriteHeight != 0) {
      this.columns = sprite.getWidth() / this.spriteWidth;
      this.rows = sprite.getHeight() / this.spriteHeight;
    } else {
      this.columns = 0;
      this.rows = 0;
    }
  }
  public BufferedImage getImage(){
    return this.image != null ? this.image : RenderEngine.getImage(this.getPath());
  }
  /**
   * Gets the sprites per row.
   *
   * @return the sprites per row
   */
  public int getColumns() {
    return this.columns;
  }

  private Point getLocation(final int index) {
    final int row = index / this.getColumns();
    final int column = index % this.getColumns();

    return new Point(column * this.getSpriteWidth(), row * this.getSpriteHeight());
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public String getPath() {
    return this.path != null ? this.path : "";
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

    final BufferedImage bigImg = this.getImage();
    if (bigImg == null) {
      return null;
    }

    final Point position = this.getLocation(index);
    final BufferedImage smallImage = bigImg.getSubimage(position.x, position.y, this.spriteWidth, this.spriteHeight);
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
}