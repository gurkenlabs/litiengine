package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

@XmlRootElement(name = "blueprint")
public class Blueprint extends MapObject {
  private static final long serialVersionUID = -7235380251249427834L;

  @XmlElementWrapper(name = "items")
  @XmlElement(name = "object")
  private List<MapObject> items = new ArrayList<>();

  @XmlTransient
  private boolean keepIds;

  public Blueprint() {
    super();
  }

  public Blueprint(String name, MapObject... items) {
    this(name, false, items);
  }

  public Blueprint(String name, boolean keepIds, MapObject... items) {
    this.keepIds = keepIds;
    this.setType(MapObjectType.AREA.toString());
    this.setName(name);
    int minX = -1;
    int minY = -1;
    int maxX = -1;
    int maxY = -1;
    for (MapObject item : items) {
      if (minX == -1 || item.getX() < minX) {
        minX = item.getX();
      }

      if (minY == -1 || item.getY() < minY) {
        minY = item.getY();
      }

      if (maxX == -1 || item.getBoundingBox().getMaxX() > maxX) {
        maxX = (int) item.getBoundingBox().getMaxX();
      }

      if (maxY == -1 || item.getBoundingBox().getMaxY() > maxY) {
        maxY = (int) item.getBoundingBox().getMaxY();
      }
    }

    this.setWidth(maxX - minX);
    this.setHeight(maxY - minY);

    for (MapObject item : items) {
      MapObject newItem = new MapObject(item);
      newItem.setX(item.getX() - minX);
      newItem.setY(item.getY() - minY);
      if (keepIds) {
        newItem.setId(item.getId());
      }

      this.items.add(newItem);
    }
  }

  @XmlTransient
  public Iterable<MapObject> getItems() {
    return this.items;
  }

  /**
   * Gets a value that indicates whether the IDs if this blueprint's map-objects
   * should be kept. This is currently used when objects are cut and pasted
   * afterwards.
   * 
   * @return
   */
  public boolean keepIds() {
    return this.keepIds;
  }

  public List<MapObject> build(int x, int y) {
    List<MapObject> builtObjects = new ArrayList<>();

    for (MapObject item : this.getItems()) {
      MapObject newObject = new MapObject(item);
      newObject.setX(newObject.getX() + x);
      newObject.setY(newObject.getY() + y);
      newObject.setId(Game.getEnvironment().getNextMapId());
      if (this.keepIds) {
        newObject.setId(item.getId());
      }

      builtObjects.add(newObject);
    }

    return builtObjects;
  }
}
