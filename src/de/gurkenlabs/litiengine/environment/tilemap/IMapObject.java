package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.environment.IPolygon;

/**
 * This interface represents an instance on a map that can define various things
 * for an engine. e.g. it can be used to define static collision boxes or other
 * special regions on the map.
 */
public interface IMapObject extends ICustomPropertyProvider, Comparable<IMapObject> {

  /**
   * Gets the grid id.
   *
   * @return the grid id
   */
  public int getGridId();

  /**
   * Gets the hit box.
   *
   * @return the hit box
   */
  public Rectangle2D getBoundingBox();

  /**
   * Gets the id.
   *
   * @return the id
   */
  public int getId();

  /**
   * Gets the location.
   *
   * @return the location
   */
  public Point2D getLocation();

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName();

  public String getType();

  public IPolyline getPolyline();
  
  public IPolygon getPolygon();
  
  public Ellipse2D getEllipse();

  public IMapObjectLayer getLayer();

  public void setGridId(int gid);

  public void setId(int id);

  public void setName(String name);

  public void setType(String type);

  public void setX(float x);

  public void setY(float y);

  public void setLocation(Point2D location);

  public void setLocation(float x, float y);

  public void setWidth(float width);

  public void setHeight(float height);

  public float getX();

  public float getY();

  public float getWidth();

  public float getHeight();
  
  public boolean isPolyline();
  
  public boolean isPolygon();

  public boolean isPoint();

  public boolean isEllipse();
}
