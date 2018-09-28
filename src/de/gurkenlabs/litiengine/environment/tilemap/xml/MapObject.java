package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;

/**
 * The Class MapObject.
 */
@XmlRootElement(name = "object")
public class MapObject extends CustomPropertyProvider implements IMapObject {
  private static final long serialVersionUID = -6001981756772928868L;

  @XmlAttribute
  private int id;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private String type;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float x;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float y;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float width;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float height;

  @XmlAttribute
  private Integer gid;

  @XmlElement(name = "polyline")
  private Polyline polyline;

  private transient MapObjectLayer layer;

  public MapObject() {
    super();
    this.setX(0f);
    this.setY(0f);
    this.setWidth(0f);
    this.setHeight(0f);
  }

  /**
   * Copy Constructor for copying instances of MapObjects.
   * This variant of the constructor will assign an entirely new ID to the newly created MapObject.
   *
   * @param mapObjectToBeCopied
   *          the MapObject we want to copy
   */
  public MapObject(MapObject mapObjectToBeCopied) {
    super(mapObjectToBeCopied);
    this.setName(mapObjectToBeCopied.getName());
    this.setId(Game.getEnvironment().getNextMapId());
    this.setPolyline(new Polyline(mapObjectToBeCopied.getPolyline()));
    this.setType(mapObjectToBeCopied.getType());
    this.setX(mapObjectToBeCopied.getX());
    this.setY(mapObjectToBeCopied.getY());
    this.setWidth(mapObjectToBeCopied.getWidth());
    this.setHeight(mapObjectToBeCopied.getHeight());
  }

  /**
   * Copy Constructor for copying instances of MapObjects.
   * This variant of the constructor lets you decide if the copy instance will get the same ID as the old MapObject or get a new ID.
   * 
   * @param mapObjectToBeCopied
   *          the MapObject we want to copy
   * @param keepID
   *          decide if the new instance will adopt the old MapObject's ID or get a new, unique one.
   */
  public MapObject(MapObject mapObjectToBeCopied, boolean keepID) {
    this(mapObjectToBeCopied);
    if (keepID) {
      this.setId(mapObjectToBeCopied.getId());
    }
  }

  public static Rectangle2D getBounds2D(IMapObject... objects) {
    return getBounds(objects);
  }

  public static Rectangle2D getBounds2D(Iterable<IMapObject> objects) {
    return getBounds(objects);
  }

  public static Rectangle2D getBounds(IMapObject... objects) {
    return getBounds(Arrays.asList(objects));
  }

  public static Rectangle2D getBounds(Iterable<IMapObject> objects) {
    float minX = -1;
    float minY = -1;
    float maxX = -1;
    float maxY = -1;
    for (IMapObject item : objects) {
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

    return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
  }

  @Override
  public int compareTo(IMapObject obj) {
    if (obj == null) {
      return 1;
    }

    if (this.getName() == null) {
      if (obj.getName() == null) {
        return 0;
      }

      return -1;
    }

    return this.getName().compareTo(obj.getName());
  }

  @Override
  public int getGridId() {
    if (this.gid == null) {
      return 0;
    }

    return this.gid;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(this.getX(), this.getY(), this.width, this.height);
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public Point2D getLocation() {
    return new Point2D.Double(this.getX(), this.getY());
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public Polyline getPolyline() {
    return this.polyline;
  }

  @Override
  @XmlTransient
  public void setGridId(int gid) {
    this.gid = gid;
  }

  @Override
  @XmlTransient
  public void setHeight(float height) {
    this.height = height;
  }

  @Override
  @XmlTransient
  public void setId(int id) {
    this.id = id;
  }

  @Override
  @XmlTransient
  public void setName(String name) {
    this.name = name;
  }

  @Override
  @XmlTransient
  public void setType(String type) {
    this.type = type;
  }

  @Override
  @XmlTransient
  public void setWidth(float width) {
    this.width = width;
  }

  @Override
  @XmlTransient
  public void setX(float x) {
    if (this.isInfiniteMap()) {
      Map map = (Map) this.getLayer().getMap();
      this.x = x + map.getChunkOffsetX() * map.getTileWidth();
      return;
    }

    this.x = x;
  }

  @Override
  @XmlTransient
  public void setY(float y) {
    if (this.isInfiniteMap()) {
      Map map = (Map) this.getLayer().getMap();
      this.y = y + map.getChunkOffsetY() * map.getTileHeight();
      return;
    }

    this.y = y;
  }

  @Override
  public float getX() {
    if (this.isInfiniteMap()) {
      Map map = (Map) this.getLayer().getMap();
      return this.x - map.getChunkOffsetX() * map.getTileWidth();
    }

    return this.x;
  }

  @Override
  public float getY() {
    if (this.isInfiniteMap()) {
      Map map = (Map) this.getLayer().getMap();
      return this.y - map.getChunkOffsetY() * map.getTileHeight();
    }

    return this.y;
  }

  @XmlTransient
  public void setPolyline(Polyline polyline) {
    this.polyline = polyline;
  }

  @Override
  public float getWidth() {
    return this.width;
  }

  @Override
  public float getHeight() {
    return this.height;
  }

  void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.gid != null && this.gid == 0) {
      this.gid = null;
    }
  }

  @Override
  public IMapObjectLayer getLayer() {
    return this.layer;
  }

  protected void setLayer(MapObjectLayer layer) {
    this.layer = layer;
  }

  private boolean isInfiniteMap() {
    return this.getLayer() != null && this.getLayer().getMap() != null && this.getLayer().getMap().isInfinite() && this.getLayer().getMap() instanceof Map;
  }
}
