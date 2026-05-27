package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.resources.Resource;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * This interface represents an instance on a map that can define various things for an engine. e.g. it can be used to
 * define static collision boxes or other special regions on the map.
 */
public interface IMapObject extends ICustomPropertyProvider, Resource {

  /**
   * Gets the grid id.
   *
   * @return the grid id
   */
  public int getGridId();

  /**
   * Gets the tileset entry referenced by this map object's {@linkplain #getGridId() grid id}, if any.
   *
   * @return the referenced tileset entry, or {@code null} if this object does not reference a tile
   */
  public ITilesetEntry getTile();

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
   * Gets the user-defined type string of this map object.
   *
   * @return the type string
   */
  public String getType();

  /**
   * Gets the polyline shape of this map object, if any.
   *
   * @return the polyline, or {@code null} if this object is not a polyline
   */
  public IPolyShape getPolyline();

  /**
   * Gets the polygon shape of this map object, if any.
   *
   * @return the polygon, or {@code null} if this object is not a polygon
   */
  public IPolyShape getPolygon();

  /**
   * Gets the ellipse shape of this map object, if any.
   *
   * @return the ellipse, or {@code null} if this object is not an ellipse
   */
  public Ellipse2D getEllipse();

  /**
   * Gets the text content of this map object, if it represents a text object.
   *
   * @return the text content, or {@code null} if this is not a text object
   */
  public IMapObjectText getText();

  /**
   * Gets the layer that contains this map object.
   *
   * @return the owning layer
   */
  public IMapObjectLayer getLayer();

  /**
   * Sets the grid id, i.e. the global tile id this object references.
   *
   * @param gid the grid id to set
   */
  public void setGridId(int gid);

  /**
   * Sets the unique id of this map object.
   *
   * @param id the id to set
   */
  public void setId(int id);

  /**
   * Sets the user-defined type string of this map object.
   *
   * @param type the type to set
   */
  public void setType(String type);

  /**
   * Sets the x coordinate of this map object.
   *
   * @param x the x coordinate
   */
  public void setX(float x);

  /**
   * Sets the y coordinate of this map object.
   *
   * @param y the y coordinate
   */
  public void setY(float y);

  /**
   * Sets the location of this map object.
   *
   * @param location the new location
   */
  public void setLocation(Point2D location);

  /**
   * Sets the location of this map object.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public void setLocation(float x, float y);

  /**
   * Sets the width of this map object.
   *
   * @param width the width to set
   */
  public void setWidth(float width);

  /**
   * Sets the height of this map object.
   *
   * @param height the height to set
   */
  public void setHeight(float height);

  /**
   * Gets the x coordinate of this map object.
   *
   * @return the x coordinate
   */
  public float getX();

  /**
   * Gets the y coordinate of this map object.
   *
   * @return the y coordinate
   */
  public float getY();

  /**
   * Gets the width of this map object.
   *
   * @return the width
   */
  public float getWidth();

  /**
   * Gets the height of this map object.
   *
   * @return the height
   */
  public float getHeight();

  /**
   * Returns whether this map object is a polyline.
   *
   * @return {@code true} if this object is a polyline
   */
  public boolean isPolyline();

  /**
   * Returns whether this map object is a polygon.
   *
   * @return {@code true} if this object is a polygon
   */
  public boolean isPolygon();

  /**
   * Returns whether this map object is a single point.
   *
   * @return {@code true} if this object is a point
   */
  public boolean isPoint();

  /**
   * Returns whether this map object is an ellipse.
   *
   * @return {@code true} if this object is an ellipse
   */
  public boolean isEllipse();

  /**
   * Sets the polyline shape of this map object.
   *
   * @param polyline the polyline shape to set
   */
  public void setPolyline(IPolyShape polyline);

  /**
   * Sets the polygon shape of this map object.
   *
   * @param polygon the polygon shape to set
   */
  public void setPolygon(IPolyShape polygon);
}
