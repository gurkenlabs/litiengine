package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resource;

public interface ITileset extends ICustomPropertyProvider, Resource {

  /**
   * Gets the first grid id.
   *
   * @return the first grid id
   */
  int getFirstGridId();

  /**
   * Gets the image.
   *
   * @return the image
   */
  IMapImage getImage();

  Spritesheet getSpritesheet();

  int getMargin();

  int getSpacing();

  /**
   * Gets the tile dimension.
   *
   * @return the tile dimension
   */
  Dimension getTileDimension();

  int getColumns();

  ITileOffset getTileOffset();

  int getTileHeight();

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  int getTileWidth();

  int getTileCount();

  ITilesetEntry getTile(int id);

  boolean containsTile(ITile tile);

  boolean containsTile(ITilesetEntry entry);

  boolean containsTile(int tileId);
}
