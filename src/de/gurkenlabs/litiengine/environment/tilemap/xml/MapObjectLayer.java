package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.util.ColorHelper;

/**
 * The Class ShapeLayer.
 */
@XmlRootElement(name = "objectgroup")
public class MapObjectLayer extends Layer implements IMapObjectLayer {
  private static final long serialVersionUID = -6130660578937427531L;

  private static final Logger log = Logger.getLogger(MapObjectLayer.class.getName());

  /** The objects. */
  @XmlElement(name = "object")
  private ArrayList<MapObject> objects = new ArrayList<>();

  @XmlAttribute
  private String color;

  private transient Color decodedColor;

  private transient List<IMapObject> mapObjects = new CopyOnWriteArrayList<>();

  private transient boolean added;

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
    return mapObjects;
  }

  @Override
  public void removeMapObject(IMapObject mapObject) {
    this.mapObjects.remove(mapObject);
    this.objects.remove(mapObject);
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
      this.objects.add((MapObject) mapObject);
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
  public void setColor(String color) {
    this.color = color;
    this.decodedColor = null;
  }

  @Override
  public Collection<IMapObject> getMapObjects(String... types) {
    List<IMapObject> objs = new ArrayList<>();
    for (IMapObject mapObject : this.getMapObjects()) {
      if (mapObject == null || mapObject.getType() == null || mapObject.getType().isEmpty()) {
        continue;
      }

      for (String type : types) {
        if (mapObject.getType().equals(type)) {
          objs.add(mapObject);
        }
      }
    }

    return objs;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.objects == null) {
      this.objects = new ArrayList<>();
    }

    Method m;
    try {
      m = getClass().getSuperclass().getDeclaredMethod("afterUnmarshal", Unmarshaller.class, Object.class);
      m.setAccessible(true);
      m.invoke(this, u, parent);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
