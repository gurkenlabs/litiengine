package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.List;

import de.gurkenlabs.litiengine.util.AlphanumComparator;

/**
 * The Interface IMap.
 */
public interface IMap extends ILayerList, Comparable<IMap> {

  /**
   * Gets the tilesets.
   *
   * @return the tilesets
   */
  public List<ITileset> getTilesets();

  public ITilesetEntry getTilesetEntry(int gid);

  /**
   * Gets the orientation.
   *
   * @return the orientation
   */
  public IMapOrientation getOrientation();

  public URL getPath();

  /**
   * Gets the renderorder.
   *
   * @return the renderorder
   */
  public RenderOrder getRenderOrder();

  /**
   * Gets the size in pixels.
   *
   * @return the size in pixels
   */
  public Dimension getSizeInPixels();

  /**
   * Gets the map width in tiles.
   *
   * @return the width in tiles
   */
  public int getWidth();

  /**
   * Gets the map height in tiles.
   *
   * @return the height in tiles
   */
  public int getHeight();

  /**
   * Gets the sizein tiles.
   *
   * @return the sizein tiles
   */
  public Dimension getSizeInTiles();

  public Rectangle2D getBounds();

  /**
   * Gets the tile size.
   *
   * @return the tile size
   */
  public Dimension getTileSize();

  /**
   * Gets the horizontal tile size.
   *
   * @return the horizontal tile size
   */
  public int getTileWidth();

  /**
   * Gets the vertical tile size.
   *
   * @return the vertical tile size
   */
  public int getTileHeight();

  /**
   * Gets the straight edges' length for hexagonal maps.
   *
   * @return the hex side length
   */
  public int getHexSideLength();

  /**
   * Gets the staggering axis
   *
   * @return the tile size
   */
  public StaggerAxis getStaggerAxis();

  /**
   * Gets the tile size.
   *
   * @return the tile size
   */
  public StaggerIndex getStaggerIndex();

  /**
   * Gets the version.
   *
   * @return the version
   */
  public double getVersion();

  public String getTiledVersion();

  /**
   * Sets the name.
   *
   * @param name
   *          the new name
   */
  public void setName(String name);

  public String getName();

  public int getNextObjectId();

  public int getNextLayerId();

  public Color getBackgroundColor();

  public boolean isInfinite();

  @Override
  public default int compareTo(IMap map) {
    return AlphanumComparator.compareTo(this.getName(), map.getName());
  }
}
