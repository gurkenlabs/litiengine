package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;

public class GroupLayer extends Layer implements IGroupLayer {

  @XmlElements({
      @XmlElement(name = "imagelayer", type = ImageLayer.class),
      @XmlElement(name = "layer", type = TileLayer.class),
      @XmlElement(name = "objectgroup", type = MapObjectLayer.class),
      @XmlElement(name = "group", type = GroupLayer.class)
  })
  private List<ILayer> layers;

  private transient List<ITileLayer> rawTileLayers = new ArrayList<>();
  private transient List<IMapObjectLayer> rawMapObjectLayers = new ArrayList<>();
  private transient List<IImageLayer> rawImageLayers = new ArrayList<>();
  private transient List<IGroupLayer> rawGroupLayers = new ArrayList<>();

  private transient List<ITileLayer> tileLayers = Collections.unmodifiableList(this.rawTileLayers);
  private transient List<IMapObjectLayer> mapObjectLayers = Collections.unmodifiableList(this.rawMapObjectLayers);
  private transient List<IImageLayer> imageLayers = Collections.unmodifiableList(this.rawImageLayers);
  private transient List<IGroupLayer> groupLayers = Collections.unmodifiableList(this.rawGroupLayers);

  @Override
  public List<ILayer> getRenderLayers() {
    return this.layers;
  }

  @Override
  public List<IMapObjectLayer> getMapObjectLayers() {
    return this.mapObjectLayers;
  }

  @Override
  public void addLayer(ILayer layer) {
    this.layers.add(layer);
    this.layerAdded(layer);
    if (layer instanceof Layer) {
      ((Layer) layer).setMap((Map) this.getMap());
    }
  }

  @Override
  public void addLayer(int index, ILayer layer) {
    this.layers.add(index, layer);
    this.layerAdded(layer);
    if (layer instanceof Layer) {
      ((Layer) layer).setMap((Map) this.getMap());
    }
  }

  @Override
  public void removeLayer(ILayer layer) {
    this.layers.remove(layer);
    this.layerRemoved(layer);
    if (layer instanceof Layer) {
      ((Layer) layer).setMap(null);
    }
  }

  @Override
  public void removeLayer(int index) {
    ILayer removed = this.layers.remove(index);
    this.layerRemoved(removed);
    if (removed != null && removed instanceof Layer) {
      ((Layer) removed).setMap(null);
    }
  }

  private void layerRemoved(ILayer layer) {
    if (layer instanceof ITileLayer) {
      this.rawTileLayers.remove(layer);
    }
    if (layer instanceof IMapObjectLayer) {
      this.rawMapObjectLayers.remove(layer);
    }
    if (layer instanceof IImageLayer) {
      this.rawImageLayers.remove(layer);
    }
    if (layer instanceof IGroupLayer) {
      this.rawGroupLayers.remove(layer);
    }
  }

  private void layerAdded(ILayer layer) {
    if (layer instanceof ITileLayer) {
      this.rawTileLayers.add((ITileLayer) layer);
    }
    if (layer instanceof IMapObjectLayer) {
      this.rawMapObjectLayers.add((IMapObjectLayer) layer);
    }
    if (layer instanceof IImageLayer) {
      this.rawImageLayers.add((IImageLayer) layer);
    }
    if (layer instanceof IGroupLayer) {
      this.rawGroupLayers.add((IGroupLayer) layer);
    }
  }

  @Override
  public IMapObjectLayer getMapObjectLayer(IMapObject mapObject) {
    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      Optional<IMapObject> found = layer.getMapObjects().stream().filter(x -> x.getId() == mapObject.getId()).findFirst();
      if (found.isPresent()) {
        return layer;
      }
    }

    return null;
  }

  @Override
  public Collection<IMapObject> getMapObjects() {
    List<IMapObject> mapObjects = new ArrayList<>();
    if (this.getMapObjectLayers() == null) {
      return mapObjects;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject != null) {
          mapObjects.add(mapObject);
        }
      }
    }

    return mapObjects;
  }

  @Override
  public Collection<IMapObject> getMapObjects(int... mapIDs) {
    List<IMapObject> mapObjects = new ArrayList<>();
    if (this.getMapObjectLayers() == null || this.getMapObjectLayers().isEmpty()) {
      return mapObjects;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      mapObjects.addAll(layer.getMapObjects(mapIDs));
    }

    return mapObjects;
  }

  @Override
  public Collection<IMapObject> getMapObjects(String... types) {
    List<IMapObject> mapObjects = new ArrayList<>();
    if (this.getMapObjectLayers() == null || this.getMapObjectLayers().isEmpty() || types.length == 0) {
      return mapObjects;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      mapObjects.addAll(layer.getMapObjects(types));
    }

    return mapObjects;
  }

  @Override
  public IMapObject getMapObject(int mapId) {
    if (this.getMapObjectLayers() == null) {
      return null;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject != null && mapObject.getId() == mapId) {
          return mapObject;
        }
      }
    }

    return null;
  }

  @Override
  public void removeMapObject(int mapId) {
    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      IMapObject remove = null;
      for (IMapObject obj : layer.getMapObjects()) {
        if (obj.getId() == mapId) {
          remove = obj;
          break;
        }
      }

      if (remove != null) {
        layer.removeMapObject(remove);
        break;
      }
    }
  }

  @Override
  public List<ITileLayer> getTileLayers() {
    return this.tileLayers;
  }

  @Override
  public List<IImageLayer> getImageLayers() {
    return this.imageLayers;
  }

  @Override
  public List<IGroupLayer> getGroupLayers() {
    return this.groupLayers;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    for (ILayer layer : this.layers) {
      if (layer instanceof Layer) {
        ((Layer) layer).finish(location);
      }
    }
  }
}
