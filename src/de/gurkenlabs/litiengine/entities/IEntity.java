package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.gurkenlabs.litiengine.entities.ai.IBehaviorController;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;

public interface IEntity {
  public void addMessageListener(MessageListener listener);

  public void addMessageListener(String message, MessageListener listener);

  public void removeMessageListener(MessageListener listener);

  public void addTransformListener(EntityTransformListener listener);

  public void removeTransformListener(EntityTransformListener listener);

  public void addListener(EntityListener listener);

  public void removeListener(EntityListener listener);

  public float getAngle();
  
  /**
   * Sets the angle (in degrees) in which the entity is directed.
   *
   * @param angle
   *          the new angle in degrees
   */
  public void setAngle(float angle);

  public IEntityAnimationController getAnimationController();

  public IBehaviorController getBehaviorController();

  public void addController(IEntityController controller);

  public <T extends IEntityController> void setController(Class<T> clss, T controller);

  public <T extends IEntityController> T getController(Class<T> clss);

  /**
   * All registered actions of this entity.
   * 
   * @return The EntityActionMap that holds all registered EntityActions for this instance.
   * 
   * @see EntityActionMap
   * @see IEntity#register(String, Runnable)
   */
  public EntityActionMap actions();

  /**
   * Performs an <code>EntityAction</code> that was previously registered for this entity.
   * <p><i>Does nothing in case no action has been registered for the specified <code>actionName</code>.</i></p>
   * 
   * @param actionName
   *          The name of the action to be performed.
   * 
   * @see IEntity#actions()
   * @see IEntity#register(String, Runnable)
   */
  public void perform(String actionName);

  /**
   * Registers an <code>EntityAction</code> with the specified name.
   * It's later possible to execute these actions on the entity by using the <code>Entity.perform(String actionName)</code> method.
   * 
   * @param name
   *          The name of the action to be registered.
   * @param action
   *          The action to be performed by the entity.
   * 
   * @return The created EntityAction instance; or null if the name or action parameter were invalid.
   * 
   * @see IEntity#perform(String)
   * @see IEntity#actions()
   */
  public EntityAction register(String name, Runnable action);

  public void detachControllers();

  public void attachControllers();

  public Rectangle2D getBoundingBox();

  public Point2D getCenter();

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

  public void setX(double x);

  public void setY(double y);

  public ICustomPropertyProvider getProperties();

  /**
   * Gets the environment the entity was loaded to or null if it is not loaded.
   * 
   * @return The entity's environment.
   */
  public IEnvironment getEnvironment();

  /**
   * This method provides the possibility to implement behavior whenever this entity was added to the environment.
   * 
   * @param environment
   *          The environment that the entity was added to
   * 
   * @see IEntity#addListener(EntityListener)
   */
  public void loaded(IEnvironment environment);

  /**
   * This method provides the possibility to implement behavior whenever this entity was removed from the environment.
   * 
   * @param environment
   *          The environment that the entity was removed from
   * 
   * @see IEntity#addListener(EntityListener)
   */
  public void removed(IEnvironment environment);
}
