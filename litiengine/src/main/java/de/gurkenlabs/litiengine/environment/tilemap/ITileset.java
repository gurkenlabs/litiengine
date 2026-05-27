package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resource;
import java.awt.Dimension;
import java.util.List;

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

  /**
   * Gets the spritesheet that contains the rendered tile images.
   *
   * @return the spritesheet
   */
  Spritesheet getSpritesheet();

  /**
   * Gets the margin (in pixels) between the tileset image edge and the first tile.
   *
   * @return the margin in pixels
   */
  int getMargin();

  /**
   * Gets the spacing (in pixels) between adjacent tiles in the tileset image.
   *
   * @return the spacing in pixels
   */
  int getSpacing();

  /**
   * Gets the tile dimension.
   *
   * @return the tile dimension
   */
  Dimension getTileDimension();

  /**
   * Gets the number of tile columns in the tileset.
   *
   * @return the number of columns
   */
  int getColumns();

  /**
   * Gets the per-tileset rendering offset.
   *
   * @return the tile offset
   */
  ITileOffset getTileOffset();

  /**
   * Gets the tile height in pixels.
   *
   * @return the tile height
   */
  int getTileHeight();

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  int getTileWidth();

  /**
   * Gets the total number of tiles defined by this tileset.
   *
   * @return the tile count
   */
  int getTileCount();

  /**
   * Gets the tileset entry with the given local id.
   *
   * @param id the local tile id
   * @return the tileset entry, or {@code null} if no such entry exists
   */
  ITilesetEntry getTile(int id);

  /**
   * Returns whether this tileset contains the supplied tile.
   *
   * @param tile the tile to test
   * @return {@code true} if this tileset contains the tile
   */
  boolean containsTile(ITile tile);

  /**
   * Returns whether this tileset contains the supplied tileset entry.
   *
   * @param entry the entry to test
   * @return {@code true} if this tileset contains the entry
   */
  boolean containsTile(ITilesetEntry entry);

  /**
   * Returns whether this tileset contains a tile with the supplied global id.
   *
   * @param tileId the global tile id
   * @return {@code true} if the global id belongs to this tileset
   */
  boolean containsTile(int tileId);

  /**
   * Gets the terrain sets defined by this tile set.
   *
   * @return The terrain sets of this instance.
   */
  List<ITerrainSet> getTerrainSets();
}
