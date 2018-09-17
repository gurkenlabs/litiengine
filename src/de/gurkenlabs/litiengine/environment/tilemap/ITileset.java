package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.util.List;

import de.gurkenlabs.litiengine.graphics.Spritesheet;

/**
 * The Interface ITileset.
 */
public interface ITileset extends ICustomPropertyProvider, Comparable<ITileset> {

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

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName();

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

  public ITile getTile(int id);

  public List<ITerrain> getTerrainTypes();

  public ITerrain[] getTerrain(int tileId);

  public ITileAnimation getAnimation(int tileId);

  public boolean containsTile(final ITile tile);

  public boolean containsTile(final int tileId);
}
