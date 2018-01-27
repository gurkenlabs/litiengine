package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IPolyline;

/**
 * The Class MapObject.
 */
@XmlRootElement(name = "object")
public class MapObject extends CustomPropertyProvider implements IMapObject {
  private static final long serialVersionUID = -6001981756772928868L;

  /** The id. */
  @XmlAttribute
  private int id;

  /** The name. */
  @XmlAttribute
  private String name;

  /** The type. */
  @XmlAttribute
  private String type;

  /** The x. */
  @XmlAttribute
  private int x;

  /** The y. */
  @XmlAttribute
  private int y;

  /** The width. */
  @XmlAttribute
  private int width;

  /** The height. */
  @XmlAttribute
  private int height;

  /** The gid. */
  @XmlAttribute
  private Integer gid;

  @XmlElement(name = "polyline")
  private Polyline polyline;

  public MapObject() {
  }

  public MapObject(MapObject mapObject) {
    this.gid = mapObject.gid;
    this.height = mapObject.height;
    this.width = mapObject.width;
    this.name = mapObject.name;
    this.polyline = mapObject.polyline;
    this.type = mapObject.type;
    this.x = mapObject.x;
    this.y = mapObject.y;
    this.setCustomProperties(mapObject.getAllCustomProperties());
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
  public Dimension getDimension() {
    return new Dimension(this.width, this.height);
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
    return new Rectangle2D.Double(this.x, this.y, this.width, this.height);
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public Point getLocation() {
    return new Point(this.x, this.y);
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
  public IPolyline getPolyline() {
    return this.polyline;
  }

  @Override
  @XmlTransient
  public void setGid(int gid) {
    this.gid = gid;
  }

  @Override
  @XmlTransient
  public void setHeight(int height) {
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
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  @XmlTransient
  public void setX(int x) {
    this.x = x;
  }

  @Override
  @XmlTransient
  public void setY(int y) {
    this.y = y;
  }

  @Override
  public int getX() {
    return this.x;
  }

  @Override
  public int getY() {
    return this.y;
  }

  @XmlTransient
  public void setPolyline(Polyline polyline) {
    this.polyline = polyline;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.gid != null && this.gid == 0) {
      this.gid = null;
    }
  }
}
