package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.util.List;

/**
 * The Interface ITileset.
 */
public interface ITileset extends ICustomPropertyProvider {

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

  public int getTileHeight();

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  public int getTileWidth();

  public int getTilecount();

  public List<ITerrain> getTerrainTypes();

  public ITerrain[] getTerrain(int tileId);

  public ITileAnimation getAnimation(int tileId);
}
