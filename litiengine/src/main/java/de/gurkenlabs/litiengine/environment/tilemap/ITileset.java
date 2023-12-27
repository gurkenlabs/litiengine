package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.util.List;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resource;

public interface ITileset extends ICustomPropertyProvider, Resource {

  /**
   * Gets the first grid id.
   *
   * @return the first grid id
   */
  public int getFirstGridId();

  /**
   * Gets the image.
   *
   * @return the image
   */
  public IMapImage getImage();

  public Spritesheet getSpritesheet();

  public int getMargin();

  public int getSpacing();

  /**
   * Gets the tile dimension.
   *
   * @return the tile dimension
   */
  public Dimension getTileDimension();

  public int getColumns();

  public ITileOffset getTileOffset();

  public int getTileHeight();

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  public int getTileWidth();

  public int getTileCount();

  public ITilesetEntry getTile(int id);

  public boolean containsTile(ITile tile);

  public boolean containsTile(ITilesetEntry entry);

  public boolean containsTile(int tileId);
}
