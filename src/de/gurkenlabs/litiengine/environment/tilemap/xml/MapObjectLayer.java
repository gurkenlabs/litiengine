package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.util.ColorHelper;

/**
 * The Class ShapeLayer.
 */
public class MapObjectLayer extends Layer implements IMapObjectLayer {

  /** The objects. */
  @XmlElement(name = "object")
  private ArrayList<MapObject> objects = new ArrayList<>();

  @XmlAttribute
  private String color;

  private transient Color decodedColor;

  private transient List<IMapObject> mapObjects = new CopyOnWriteArrayList<>();

  private transient boolean added;

  public MapObjectLayer() {
    super();
  }

  /**
   * Copy Constructor for copying instances of MapObjectLayers.
   *
   * @param layerToBeCopied
   *          the layer we want to copy
   */
  public MapObjectLayer(MapObjectLayer layerToBeCopied) {
    super(layerToBeCopied);
    for (IMapObject obj : layerToBeCopied.getMapObjects()) {
      this.addMapObject(new MapObject((MapObject) obj));
    }
    if (layerToBeCopied.getColor() != null) {
      this.setColor(layerToBeCopied.getColorHexString());
    }
  }

  private void loadMapObjects() {
    if (!this.added) {
      if (this.objects != null) {
        this.mapObjects.addAll(this.objects);
      }

      this.added = true;
    }
  }

  @Override
  public List<IMapObject> getMapObjects() {
    loadMapObjects();
    return this.mapObjects;
  }

  @Override
  public void removeMapObject(IMapObject mapObject) {
    this.mapObjects.remove(mapObject);
    this.objects.remove(mapObject);

    if (mapObject instanceof MapObject) {
      ((MapObject) mapObject).setLayer(null);
    }
  }

  @Override
  public Dimension getSizeInTiles() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public void addMapObject(IMapObject mapObject) {
    loadMapObjects();
    this.mapObjects.add(mapObject);
    if (mapObject instanceof MapObject) {
      MapObject obj = (MapObject) mapObject;
      if (obj.getLayer() != null) {
        obj.getLayer().removeMapObject(obj);
      }
      
      this.objects.add(obj);
      obj.setLayer(this);
    }
  }

  @Override
  public Color getColor() {
    if (this.color == null || this.color.isEmpty()) {
      return null;
    }

    if (this.decodedColor != null) {
      return this.decodedColor;
    }

    this.decodedColor = ColorHelper.decode(this.color);
    return this.decodedColor;
  }

  @Override
  public String getColorHexString() {
    return this.color;
  }

  @Override
  public void setColor(String color) {
    this.color = color;
    this.decodedColor = null;
  }

  @Override
  public Collection<IMapObject> getMapObjects(String... types) {
    List<IMapObject> objs = new ArrayList<>();
    for (IMapObject mapObject : this.getMapObjects()) {
      if (mapObject != null && Arrays.stream(types).anyMatch(type -> type.equals(mapObject.getType()))) {
        objs.add(mapObject);
      }
    }
    return objs;
  }

  @Override
  public Collection<IMapObject> getMapObjects(int... mapIDs) {
    List<IMapObject> objs = new ArrayList<>();
    for (IMapObject mapObject : this.getMapObjects()) {
      if (mapObject != null && Arrays.stream(mapIDs).anyMatch(id -> id == mapObject.getId())) {
        objs.add(mapObject);
      }
    }
    return objs;
  }

  @Override
  protected void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.objects == null) {
      this.objects = new ArrayList<>();
    }

    super.afterUnmarshal(u, parent);
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    for (MapObject object : this.objects) {
      object.finish(location);
    }
  }
}
