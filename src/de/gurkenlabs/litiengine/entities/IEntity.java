package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;

public interface IEntity {
  public float getAngle();

  public IAnimationController getAnimationController();

  public Rectangle2D getBoundingBox();

  public Point2D getDimensionCenter();

  public float getHeight();

  public Point2D getLocation();

  public int getMapId();

  /***
   * Gets the name of this entity.
   * 
   * @return The name of this entity.
   */
  public String getName();

  public RenderType getRenderType();

  public float getWidth();
  
  public double getX();
  
  public double getY();

  public String sendMessage(Object sender, String message);

  public void registerMessageAction(String message, Consumer<MessageArgs> action);

  public void setHeight(float height);

  public void setLocation(double x, double y);

  public boolean hasTag(String tag);

  public List<String> getTags();

  public void addTag(String tag);

  public void removeTag(String tag);

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  public void setLocation(Point2D location);

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   *
   * @param mapId
   *          The unique map ID for this {@link IEntity}
   */
  public void setMapId(int mapId);

  public void setName(String name);

  public void setRenderType(RenderType renderType);

  public void setSize(float width, float height);

  public void setWidth(float width);
}
