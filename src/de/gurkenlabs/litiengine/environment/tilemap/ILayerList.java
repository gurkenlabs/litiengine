package de.gurkenlabs.litiengine.environment.tilemap;

import java.util.Collection;
import java.util.List;

/**
 * The Interface ILayerList.
 */
public interface ILayerList extends ICustomPropertyProvider {

  /**
   * Gets all render layers in the Layer list.
   *
   * @return a List of ILayers
   */
  public List<ILayer> getRenderLayers();

  /**
   * Gets all MapObjectLayers in the Layer list.
   *
   * @return a List of IMapObjectLayers
   */
  public List<IMapObjectLayer> getMapObjectLayers();

  /**
   * Adds an <code>ILayer</code> to the Layer list.
   *
   * @param layer
   *          the layer to be added
   */
  public void addLayer(ILayer layer);

  /**
   * Adds an <code>ILayer</code> to the Layer list at the given index.
   *
   * @param index
   *          the index
   * @param layer
   *          the layer to be added
   */
  public void addLayer(int index, ILayer layer);

  /**
   * Removes an <code>ILayer</code> from the Layer list.
   *
   * @param layer
   *          the layer to be removed
   */
  public void removeLayer(ILayer layer);

  /**
   * Gets the <code>IMapObjectLayer</code> containing a given <code>IMapObject</code>.
   *
   * @param mapObject
   *          the map object being searched
   * @return the map object layer containing the map object
   */
  public IMapObjectLayer getMapObjectLayer(IMapObject mapObject);

  /**
   * Removes a layer from the Layer list.
   *
   * @param index
   *          the index of the layer to be removed
   */
  public void removeLayer(int index);

  /**
   * Gets all map objects in the layer list.
   *
   * @return a Collection of all IMapObjects in the layer list
   */
  public Collection<IMapObject> getMapObjects();

  /**
   * Gets all map objects in the layer list that belong to the types passed as a parameter.
   *
   * @param types
   *          an array of types for which the layer list is searched
   * @return a Collection of IMapObjects matching the given MapObjectTypes
   */
  public Collection<IMapObject> getMapObjects(String... types);

  /**
   * Gets all map objects in the layer list using the map IDs passed as a parameter. Please note that map IDs are intended to be unique identifiers
   * for <code>IMapObject</code>s (and their corresponding <code>Entity</code>). This method is just a way of checking for non-unique IDs and re-assigning
   * them before adding entities.
   * 
   * @see <code>Environment.add(final IEntity entity)</code>
   * @param mapIDs
   *          an array of mapIDs for which the layer list is searched
   * @return a Collection of IMapObjects matching the given MapObject IDs
   */
  public Collection<IMapObject> getMapObjects(int... mapIDs);

  /**
   * Gets the first <code>IMapObject</code> with the given ID from a layer list.
   *
   * @param mapId
   *          the map id of the desired <code>IMapObject</code>
   * @return the <code>IMapObject</code> with the given ID
   */
  public IMapObject getMapObject(int mapId);

  /**
   * Removes the first <code>IMapObject</code> with the given ID.
   *
   * @param mapId
   *          the map id of the <code>IMapObject</code> we want to remove
   */
  public void removeMapObject(int mapId);

  /**
   * Gets the <code>ITileLayer</code>s contained in a Layer list.
   *
   * @return a <code>List</code> of all <code>ITileLayer</code>s
   */
  public List<ITileLayer> getTileLayers();

  /**
   * Gets the <code>IImageLayer</code>s contained in a Layer list.
   *
   * @return a <code>List</code> of all <code>IImageLayer</code>s
   */
  public List<IImageLayer> getImageLayers();

  /**
   * Gets the <code>IGroupLayer</code>s contained in a Layer list.
   *
   * @return a <code>List</code> of all <code>IGroupLayer</code>s
   */
  public List<IGroupLayer> getGroupLayers();

}
