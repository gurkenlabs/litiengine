package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.graphics.RenderType;

public interface IEntity {
  public float getAngle();

  public RenderType getRenderType();

  public Rectangle2D getBoundingBox();

  public Point2D getDimensionCenter();

  public float getHeight();

  public Point2D getLocation();

  public int getMapId();

  public String getName();

  public float getWidth();

  public String sendMessage(Object sender, String message);

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  public void setLocation(Point2D location);

  public void setLocation(double x, double y);

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   *
   * @param mapId
   */
  public void setMapId(int mapId);

  public void setSize(float width, float height);

  public void setRenderType(RenderType renderType);

  public void setHeight(float height);

  public void setWidth(float width);

  public void setName(String name);
}
