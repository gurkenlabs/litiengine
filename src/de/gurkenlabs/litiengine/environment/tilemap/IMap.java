package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * The Interface IMap.
 */
public interface IMap extends ILayerList {

  /**
   * Gets the tilesets.
   *
   * @return the tilesets
   */
  public List<ITileset> getTilesets();

  /**
   * Gets the orientation.
   *
   * @return the orientation
   */
  public IMapOrientation getOrientation();

  public String getPath();

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

  public int getWidth();

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
}
