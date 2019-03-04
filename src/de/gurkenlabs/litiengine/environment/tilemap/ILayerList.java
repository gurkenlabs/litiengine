package de.gurkenlabs.litiengine.environment.tilemap;

import java.util.Collection;
import java.util.List;

public interface ILayerList extends ICustomPropertyProvider {

  public List<ILayer> getRenderLayers();

  /**
   * Gets the shape layers.
   *
   * @return the shape layers
   */
  public List<IMapObjectLayer> getMapObjectLayers();

  public void addLayer(ILayer layer);

  public void addLayer(int index, ILayer layer);

  public void removeLayer(ILayer layer);

  public IMapObjectLayer getMapObjectLayer(IMapObject mapObject);

  public void removeLayer(int index);

  public Collection<IMapObject> getMapObjects();

  public Collection<IMapObject> getMapObjects(String... types);

  public IMapObject getMapObject(int mapId);

  public void removeMapObject(int mapId);

  /**
   * Gets the tile layers.
   *
   * @return the tile layers
   */
  public List<ITileLayer> getTileLayers();

  /**
   * Gets the image layers.
   *
   * @return the image layers
   */
  public List<IImageLayer> getImageLayers();

  /**
   * Gets the group layers.
   *
   * @return the group layers
   */
  public List<IGroupLayer> getGroupLayers();
}
