package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Dimension;
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
  public String getRenderorder();

  public List<ILayer> getRenderLayers();

  /**
   * Gets the shape layers.
   *
   * @return the shape layers
   */
  public List<IMapObjectLayer> getMapObjectLayers();

  public void addMapObjectLayer(IMapObjectLayer layer);

  public void removeMapObjectLayer(IMapObjectLayer layer);

  public IMapObjectLayer getMapObjectLayer(IMapObject mapObject);

  public void removeMapObjectLayer(int index);

  public Collection<IMapObject> getMapObjects();

  public Collection<IMapObject> getMapObjects(String... type);

  public IMapObject getMapObject(int mapId);

  public void removeMapObject(int mapId);

  /**
   * Gets the size in pixles.
   *
   * @return the size in pixles
   */
  public Dimension getSizeInPixels();

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
   * Gets the tile size.
   *
   * @return the tile size
   */
  public Dimension getTileSize();

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
  public void setFileName(String name);

  public String getName();

  public int getNextObjectId();

  public Color getBackgroundColor();
}
