package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupLayer extends Layer implements IGroupLayer {

  @XmlElements({
    @XmlElement(name = "imagelayer", type = ImageLayer.class),
    @XmlElement(name = "layer", type = TileLayer.class),
    @XmlElement(name = "objectgroup", type = MapObjectLayer.class),
    @XmlElement(name = "group", type = GroupLayer.class)
  })
  private List<ILayer> layers;

  private final transient List<ITileLayer> rawTileLayers = new ArrayList<>();
  private final transient List<IMapObjectLayer> rawMapObjectLayers = new ArrayList<>();
  private final transient List<IImageLayer> rawImageLayers = new ArrayList<>();
  private final transient List<IGroupLayer> rawGroupLayers = new ArrayList<>();

  private final transient List<ITileLayer> tileLayers = Collections.unmodifiableList(this.rawTileLayers);
  private final transient List<IMapObjectLayer> mapObjectLayers = Collections.unmodifiableList(this.rawMapObjectLayers);
  private final transient List<IImageLayer> imageLayers = Collections.unmodifiableList(this.rawImageLayers);
  private final transient List<IGroupLayer> groupLayers = Collections.unmodifiableList(this.rawGroupLayers);

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
    if (layer instanceof Layer l) {
      l.setMap((TmxMap) this.getMap());
    }
  }

  @Override
  public void addLayer(int index, ILayer layer) {
    this.layers.add(index, layer);
    this.layerAdded(layer);
    if (layer instanceof Layer l) {
      l.setMap((TmxMap) this.getMap());
    }
  }

  @Override
  public void removeLayer(ILayer layer) {
    this.layers.remove(layer);
    this.layerRemoved(layer);
    if (layer instanceof Layer l) {
      l.setMap(null);
    }
  }

  @Override
  public void removeLayer(int index) {
    ILayer removed = this.layers.remove(index);
    this.layerRemoved(removed);
    if (removed instanceof Layer l) {
      l.setMap(null);
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
    if (layer instanceof ITileLayer itl) {
      this.rawTileLayers.add(itl);
    }
    if (layer instanceof IMapObjectLayer imol) {
      this.rawMapObjectLayers.add(imol);
    }
    if (layer instanceof IImageLayer iil) {
      this.rawImageLayers.add(iil);
    }
    if (layer instanceof IGroupLayer igl) {
      this.rawGroupLayers.add(igl);
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
  protected void afterUnmarshal(Unmarshaller u, Object parent) {
    super.afterUnmarshal(u, parent);
    if (getMap() != null) {
      for (ILayer layer : layers) {
        ((Layer) layer).setMap((TmxMap) getMap());
      }
    }
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    for (ILayer layer : this.layers) {
      if (layer instanceof Layer l) {
        l.finish(location);
      }
    }
  }
}
