package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

@XmlRootElement(name = "template")
public class Blueprint extends MapObject {
  /**
   * Templates in this format typically come from the Tiled editor and only support a single MapObject.
   */
  public static final String TEMPLATE_FILE_EXTENSION = "tx";

  /**
   * Blueprint in this format support multiple map objects as children (extended template XML).
   */
  public static final String BLUEPRINT_FILE_EXTENSION = "xtx";

  @XmlElement(name = "object")
  private List<MapObject> items = new ArrayList<>();

  @XmlTransient
  private boolean keepIds;

  /**
   * Initializes a new instance of the <code>Blueprint</code> map object.
   */
  public Blueprint() {
    super();
  }

  /**
   * Initializes a new instance of the <code>Blueprint</code> map object.
   *
   * @param name
   *          The name of the blueprint.
   * 
   * @param mapObjects
   *          The map objects to build the blueprint from.
   */
  public Blueprint(String name, MapObject... mapObjects) {
    this(name, false, mapObjects);
  }

  /**
   * Initializes a new instance of the <code>Blueprint</code> map object.
   *
   * @param name
   *          The name of the blueprint.
   * 
   * @param keepIds
   *          A flag indicating whether the IDs of the specified map objects should be kept.
   * 
   * @param mapObjects
   *          The map objects to build the blueprint from.
   */
  public Blueprint(String name, boolean keepIds, MapObject... mapObjects) {
    this.keepIds = keepIds;
    this.setType(MapObjectType.AREA.toString());
    if (name != null && !name.isEmpty()) {
      this.setName(name);
    }

    final Rectangle2D bounds = MapObject.getBounds(mapObjects);
    this.setWidth((float) bounds.getWidth());
    this.setHeight((float) bounds.getHeight());

    for (MapObject item : mapObjects) {
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
   * @return True if the ids for all {@link IMapObject}s of this {@link Blueprint} should be re-applied after building new instances.
   */
  public boolean keepIds() {
    return this.keepIds;
  }

  public List<IMapObject> build(Point2D location) {
    return this.build(Math.round((float) location.getX()), Math.round((float) location.getY()));
  }

  public List<IMapObject> build(float x, float y) {
    List<IMapObject> builtObjects = new ArrayList<>();

    int baseId = Game.world().environment().getNextMapId();
    for (MapObject item : this.getItems()) {
      MapObject newObject = new MapObject(item, this.keepIds());
      if (!this.keepIds()) {
        newObject.setId(baseId);
        baseId++;
      }
      newObject.setX(newObject.getX() + x);
      newObject.setY(newObject.getY() + y);
      builtObjects.add(newObject);
    }

    return builtObjects;
  }
}
