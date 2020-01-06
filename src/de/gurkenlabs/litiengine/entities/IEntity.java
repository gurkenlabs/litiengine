package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.gurkenlabs.litiengine.entities.ai.IBehaviorController;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;

public interface IEntity{
  public void addMessageListener(EntityMessageListener listener);

  public void addMessageListener(String message, EntityMessageListener listener);

  public void removeMessageListener(EntityMessageListener listener);

  public void addTransformListener(EntityTransformListener listener);

  public void removeTransformListener(EntityTransformListener listener);

  public void addListener(EntityListener listener);

  public void removeListener(EntityListener listener);

  /**
   * Adds the specified entity rendered listener to receive events when entities were rendered.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addEntityRenderedListener(final EntityRenderedListener listener);

  /**
   * Removes the specified entity rendered listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeEntityRenderedListener(final EntityRenderedListener listener);

  /**
   * Adds the specified entity render listener to receive events and callbacks about the rendering process of entities.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addEntityRenderListener(final EntityRenderListener listener);

  /**
   * Removes the specified entity render listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeEntityRenderListener(final EntityRenderListener listener);

  public double getAngle();

  /**
   * Sets the angle (in degrees) in which the entity is directed.
   *
   * @param angle
   *          the new angle in degrees
   */
  public void setAngle(double angle);

  /**
   * Gets the entities animation controller.
   * 
   * @return The entities animation controller or null if none was registered.
   * 
   * @see RenderEngine#renderEntity(java.awt.Graphics2D, IEntity)
   */
  public IEntityAnimationController<?> animations();

  public boolean isVisible();

  public void setVisible(boolean visible);

  public IBehaviorController behavior();

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
   * <p>
   * <i>Does nothing in case no action has been registered for the specified <code>actionName</code>.</i>
   * </p>
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

  public double getHeight();

  public Point2D getLocation();

  public int getMapId();

  /***
   * Gets the name of this entity.
   * 
   * @return The name of this entity.
   */
  public String getName();

  public RenderType getRenderType();

  /**
   * Determines whether this entity is being rendered with the layer it's originating from.
   * This ignores the specified <code>RenderType</code> and makes the entity dependent upon the visibility of it's layer.
   * <p>
   * This can only be used, of course, if the entity is related to a <code>MapObject</code>.
   * <br>
   * This defaults to <code>false</code> if not explicitly set on the <code>MapObject</code>.
   * </p>
   * 
   * @return True if the entity should be rendered with the layer of the corresponding map object; otherwise false.
   * 
   * @see ILayer#isVisible()
   * @see IMapObjectLayer#getMapObjects()
   * @see Environment#getEntitiesByLayer(int)
   * @see Environment#getEntitiesByLayer(String)
   */
  public boolean renderWithLayer();

  public double getWidth();

  public double getX();

  public double getY();

  public String sendMessage(Object sender, String message);

  public void setHeight(double height);

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

  public void setRenderWithLayer(boolean renderWithLayer);

  public void setSize(double width, double height);

  public void setWidth(double width);

  public void setX(double x);

  public void setY(double y);

  public ICustomPropertyProvider getProperties();

  /**
   * Gets the environment the entity was loaded to or null if it is not loaded.
   * 
   * @return The entity's environment.
   */
  public Environment getEnvironment();

  /**
   * This method provides the possibility to implement behavior whenever this entity was added to the environment.
   * 
   * @param environment
   *          The environment that the entity was added to
   * 
   * @see IEntity#addListener(EntityListener)
   */
  public void loaded(Environment environment);

  /**
   * This method provides the possibility to implement behavior whenever this entity was removed from the environment.
   * 
   * @param environment
   *          The environment that the entity was removed from
   * 
   * @see IEntity#addListener(EntityListener)
   */
  public void removed(Environment environment);

  /**
   * Indicates whether this entity is loaded on the currently active environment.
   * 
   * @return True if the entity is loaded on the game's currently active environment; otherwise false.
   * 
   * @see GameWorld#environment()
   * @see IEntity#loaded(Environment)
   * @see IEntity#removed(Environment)
   */
  public boolean isLoaded();
}
