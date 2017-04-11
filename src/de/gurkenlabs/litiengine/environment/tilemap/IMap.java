/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.util.List;

// TODO: Auto-generated Javadoc
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
   * Gets the name of the map.
   *
   * @return the name
   */
  public String getFileName();

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

  /**
   * Gets the shape layers.
   *
   * @return the shape layers
   */
  public List<IMapObjectLayer> getMapObjectLayers();
  
  public void addMapObjectLayer(IMapObjectLayer layer);
  public void removeMapObjectLayer(IMapObjectLayer layer);
  public void removeMapObjectLayer(int index);

  public List<IMapObject> getMapObjects(String...type);
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
  public Dimension getSizeinTiles();

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

  /**
   * Sets the name.
   *
   * @param name
   *          the new name
   */
  public void setFileName(String name);
}
