package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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

    final Rectangle2D bounds = MapObject.getBounds(items);
    this.setWidth((float) bounds.getWidth());
    this.setHeight((float) bounds.getHeight());

    for (MapObject item : items) {
      MapObject newItem = new MapObject(item, this.keepIds());
      newItem.setX((float) (item.getX() - bounds.getX()));
      newItem.setY((float) (item.getY() - bounds.getY()));
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
   * @return True if the ids for all {@link MapObject}s of this {@link Blueprint} should be re-applied after building new instances.
   */
  public boolean keepIds() {
    return this.keepIds;
  }

  public List<MapObject> build(Point2D location) {
    return this.build(Math.round((float) location.getX()), Math.round((float) location.getY()));
  }

  public List<MapObject> build(float x, float y) {
    List<MapObject> builtObjects = new ArrayList<>();

    for (MapObject item : this.getItems()) {
      MapObject newObject = new MapObject(item, this.keepIds());
      newObject.setX(newObject.getX() + x);
      newObject.setY(newObject.getY() + y);
      builtObjects.add(newObject);
    }

    return builtObjects;
  }
}
