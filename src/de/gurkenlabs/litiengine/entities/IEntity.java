package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.gurkenlabs.litiengine.entities.behavior.IBehaviorController;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;

public interface IEntity{
  void onMessage(EntityMessageListener listener);

  void onMessage(String message, EntityMessageListener listener);

  void addTransformListener(EntityTransformListener listener);

  void addListener(EntityListener listener);

  void removeListener(EntityMessageListener listener);

  void removeListener(EntityTransformListener listener);

  void removeListener(EntityListener listener);

  /**
   * Adds the specified entity rendered listener to receive events when entities were rendered.
   * 
   * @param listener
   *          The listener to add.
   */
  void onRendered(final EntityRenderedListener listener);

  /**
   * Removes the specified entity rendered listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  void removeListener(final EntityRenderedListener listener);

  /**
   * Adds the specified entity render listener to receive events and callbacks about the rendering process of entities.
   * 
   * @param listener
   *          The listener to add.
   */
  void addEntityRenderListener(final EntityRenderListener listener);

  /**
   * Removes the specified entity render listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  void removeListener(final EntityRenderListener listener);

  double getAngle();

  /**
   * Sets the angle (in degrees) in which the entity is directed.
   *
   * @param angle
   *          the new angle in degrees
   */
  void setAngle(double angle);

  /**
   * Gets the entities animation controller.
   * 
   * @return The entities animation controller or null if none was registered.
   * 
   * @see RenderEngine#renderEntity(java.awt.Graphics2D, IEntity)
   */
  IEntityAnimationController<?> animations();

  boolean isVisible();

  void setVisible(boolean visible);

  IBehaviorController behavior();

  void addController(IEntityController controller);

  <T extends IEntityController> void setController(Class<T> clss, T controller);

  <T extends IEntityController> T getController(Class<T> clss);

  /**
   * All registered actions of this entity.
   * 
   * @return The EntityActionMap that holds all registered EntityActions for this instance.
   * 
   * @see EntityActionMap
   * @see IEntity#register(String, Runnable)
   */
  EntityActionMap actions();

  /**
   * Performs an {@code EntityAction} that was previously registered for this entity.
   * <p>
   * <i>Does nothing in case no action has been registered for the specified {@code actionName}.</i>
   * </p>
   * 
   * @param actionName
   *          The name of the action to be performed.
   * 
   * @see IEntity#actions()
   * @see IEntity#register(String, Runnable)
   */
  void perform(String actionName);

  /**
   * Registers an {@code EntityAction} with the specified name.
   * It's later possible to execute these actions on the entity by using the {@code Entity.perform(String actionName)} method.
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
  EntityAction register(String name, Runnable action);

  void detachControllers();

  void attachControllers();

  Rectangle2D getBoundingBox();

  Point2D getCenter();

  double getHeight();

  Point2D getLocation();

  int getMapId();

  /***
   * Gets the name of this entity.
   * 
   * @return The name of this entity.
   */
  String getName();

  RenderType getRenderType();

  /**
   * Determines whether this entity is being rendered with the layer it's originating from.
   * This ignores the specified {@code RenderType} and makes the entity dependent upon the visibility of it's layer.
   * <p>
   * This can only be used, of course, if the entity is related to a {@code MapObject}.
   * <br>
   * This defaults to {@code false} if not explicitly set on the {@code MapObject}.
   * </p>
   * 
   * @return True if the entity should be rendered with the layer of the corresponding map object; otherwise false.
   * 
   * @see ILayer#isVisible()
   * @see IMapObjectLayer#getMapObjects()
   * @see Environment#getEntitiesByLayer(int)
   * @see Environment#getEntitiesByLayer(String)
   */
  boolean renderWithLayer();

  double getWidth();

  double getX();

  double getY();

  String sendMessage(Object sender, String message);

  void setHeight(double height);

  void setLocation(double x, double y);

  boolean hasTag(String tag);

  List<String> getTags();

  void addTag(String tag);

  void removeTag(String tag);

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  void setLocation(Point2D location);

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   *
   * @param mapId
   *          The unique map ID for this {@link IEntity}
   */
  void setMapId(int mapId);

  void setName(String name);

  void setRenderType(RenderType renderType);

  void setRenderWithLayer(boolean renderWithLayer);

  void setSize(double width, double height);

  void setWidth(double width);

  void setX(double x);

  void setY(double y);

  ICustomPropertyProvider getProperties();

  /**
   * Gets the environment the entity was loaded to or null if it is not loaded.
   * 
   * @return The entity's environment.
   */
  Environment getEnvironment();

  /**
   * This method provides the possibility to implement behavior whenever this entity was added to the environment.
   * 
   * @param environment
   *          The environment that the entity was added to
   * 
   * @see IEntity#addListener(EntityListener)
   */
  void loaded(Environment environment);

  /**
   * This method provides the possibility to implement behavior whenever this entity was removed from the environment.
   * 
   * @param environment
   *          The environment that the entity was removed from
   * 
   * @see IEntity#addListener(EntityListener)
   */
  void removed(Environment environment);

  /**
   * Indicates whether this entity is loaded on the currently active environment.
   * 
   * @return True if the entity is loaded on the game's currently active environment; otherwise false.
   * 
   * @see GameWorld#environment()
   * @see IEntity#loaded(Environment)
   * @see IEntity#removed(Environment)
   */
  boolean isLoaded();
}
