package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

/**
 * The Interface IMap.
 */
public interface IMap extends ICustomPropertyProvider {

  /**
   * Gets the tilesets.
   *
   * @return the tilesets
   */
  public List<ITileset> getTilesets();

  /**
   * Gets the image layers.
   *
   * @return the image layers
   */
  public List<IImageLayer> getImageLayers();

  /**
   * Gets the orientation.
   *
   * @return the orientation
   */
  public MapOrientation getOrientation();

  public String getPath();

  /**
   * Gets the renderorder.
   *
   * @return the renderorder
   */
  public String getRenderOrder();

  public List<ILayer> getRenderLayers();

  /**
   * Gets the shape layers.
   *
   * @return the shape layers
   */
  public List<IMapObjectLayer> getMapObjectLayers();

  public void addMapObjectLayer(IMapObjectLayer layer);

  public void addMapObjectLayer(int index, IMapObjectLayer layer);

  public void removeMapObjectLayer(IMapObjectLayer layer);

  public IMapObjectLayer getMapObjectLayer(IMapObject mapObject);

  public void removeMapObjectLayer(int index);

  public Collection<IMapObject> getMapObjects();

  public Collection<IMapObject> getMapObjects(String... type);

  public IMapObject getMapObject(int mapId);

  public void removeMapObject(int mapId);

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
   * Gets the tile layers.
   *
   * @return the tile layers
   */
  public List<ITileLayer> getTileLayers();

  /**
   * Gets the shape of the tile [X|Y] at its absolute location.
   *
   * @return the tile shape
   */
  public Shape getTileShape(int tileX, int tileY);

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

  public Color getBackgroundColor();

}
