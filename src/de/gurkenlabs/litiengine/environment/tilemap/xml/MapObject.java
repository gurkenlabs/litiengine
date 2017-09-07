/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IShape#getDimension()
   */
  @Override
  public Dimension getDimension() {
    return new Dimension((int) this.width, (int) this.height);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IShape#getGridId()
   */
  @Override
  public int getGridId() {
    if (this.gid == null) {
      return 0;
    }

    return this.gid;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IShape#getHitBox()
   */
  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(this.x, this.y, this.width, this.height);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IShape#getId()
   */
  @Override
  public int getId() {
    return this.id;
  }

  /*
   * (non-Javadoc)
   *
   * @see liti.map.IShape#getPosition()
   */
  @Override
  public Point getLocation() {
    return new Point((int) this.x, (int) this.y);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IShape#getName()
   */
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

  @XmlTransient
  public void setGid(int gid) {
    this.gid = gid;
  }

  @XmlTransient
  public void setHeight(int height) {
    this.height = height;
  }

  @XmlTransient
  public void setId(int id) {
    this.id = id;
  }

  @XmlTransient
  public void setName(String name) {
    this.name = name;
  }

  @XmlTransient
  public void setType(String type) {
    this.type = type;
  }

  @XmlTransient
  public void setWidth(int width) {
    this.width = width;
  }

  @XmlTransient
  public void setX(int x) {
    this.x = x;
  }

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

  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.gid != null && this.gid == 0) {
      this.gid = null;
    }
  }
}
