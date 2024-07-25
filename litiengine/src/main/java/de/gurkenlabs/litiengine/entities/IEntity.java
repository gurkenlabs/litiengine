package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.entities.behavior.IBehaviorController;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface IEntity {
  /**
   * Adds a {@link EntityMessageListener} to receive messages sent to this entity.
   *
   * @param listener The {@link EntityMessageListener} to add.
   */
  void onMessage(EntityMessageListener listener);

  /**
   * Adds a {@link EntityMessageListener} to receive messages with a specific content sent to this entity.
   *
   * @param message  The message content to listen for.
   * @param listener The {@link EntityMessageListener} to add.
   */
  void onMessage(String message, EntityMessageListener listener);

  /**
   * Adds a {@link EntityTransformListener} to receive transform events for this entity.
   *
   * @param listener The {@link EntityTransformListener} to add.
   */
  void addTransformListener(EntityTransformListener listener);

  /**
   * Adds a {@link EntityListener} to this entity that fires whenever the entity is added to or removed from the environment.
   *
   * @param listener The {@link EntityListener} to add.
   */
  void addListener(EntityListener listener);

  /**
   * Removes a {@link EntityMessageListener} from this entity.
   *
   * @param listener The {@link EntityMessageListener} to remove.
   */
  void removeListener(EntityMessageListener listener);

  /**
   * Removes a {@link EntityTransformListener} from this entity.
   *
   * @param listener The {@link EntityTransformListener} to remove.
   */
  void removeListener(EntityTransformListener listener);

  /**
   * Removes a {@link EntityListener} from this entity.
   *
   * @param listener The {@link EntityListener} to remove.
   */
  void removeListener(EntityListener listener);

  /**
   * Adds the specified {@link EntityRenderedListener} to receive events when entities were rendered.
   *
   * @param listener The {@link EntityRenderedListener} to add.
   */
  void onRendered(final EntityRenderedListener listener);

  /**
   * Removes the specified {@link EntityRenderedListener}.
   *
   * @param listener The {@link EntityRenderedListener} to remove.
   */
  void removeListener(final EntityRenderedListener listener);

  /**
   * Adds the specified {@link EntityRenderListener} to receive events and callbacks about the rendering process of entities.
   *
   * @param listener The {@link EntityRenderListener} to add.
   */
  void addEntityRenderListener(final EntityRenderListener listener);

  /**
   * Removes the specified {@link EntityRenderListener}.
   *
   * @param listener The {@link EntityRenderListener} to remove.
   */
  void removeListener(final EntityRenderListener listener);

  double getAngle();

  /**
   * Sets the angle (in degrees) in which the entity is directed.
   *
   * @param angle the new angle in degrees
   */
  void setAngle(double angle);

  /**
   * Gets the entities animation controller.
   *
   * @return The entities animation controller or null if none was registered.
   * @see RenderEngine#renderEntity(java.awt.Graphics2D, IEntity)
   */
  IEntityAnimationController<?> animations();

  /**
   * Checks if the entity is visible.
   *
   * @return True if the entity is visible; otherwise false.
   */
  boolean isVisible();

  /**
   * Sets the visibility of the entity.
   *
   * @param visible True to make the entity visible; false to make it invisible.
   */
  void setVisible(boolean visible);

  /**
   * Gets the behavior controller of the entity.
   *
   * @return The behavior controller of the entity.
   */
  IBehaviorController behavior();

  /**
   * Adds a controller to the entity.
   *
   * @param controller The controller to add.
   */
  void addController(IEntityController controller);

  /**
   * Sets a specific controller for the entity.
   *
   * @param <T>        The type of the controller.
   * @param clss       The class of the controller.
   * @param controller The controller to set.
   */
  <T extends IEntityController> void setController(Class<T> clss, T controller);

  /**
   * Gets a specific controller of the entity.
   *
   * @param <T>  The type of the controller.
   * @param clss The class of the controller.
   * @return The controller of the specified type.
   */
  <T extends IEntityController> T getController(Class<T> clss);

  /**
   * All registered actions of this entity.
   *
   * @return The EntityActionMap that holds all registered EntityActions for this instance.
   * @see EntityActionMap
   * @see IEntity#register(String, Runnable)
   */
  EntityActionMap actions();

  /**
   * Performs an {@code EntityAction} that was previously registered for this entity.
   *
   * <p>
   * <i>Does nothing in case no action has been registered for the specified {@code
   * actionName}.</i>
   *
   * @param actionName The name of the action to be performed.
   * @see IEntity#actions()
   * @see IEntity#register(String, Runnable)
   */
  void perform(String actionName);

  /**
   * Registers an {@code EntityAction} with the specified name. It's later possible to execute these actions on the entity by using the
   * {@code Entity.perform(String actionName)} method.
   *
   * @param name   The name of the action to be registered.
   * @param action The action to be performed by the entity.
   * @return The created EntityAction instance; or null if the name or action parameter were invalid.
   * @see IEntity#perform(String)
   * @see IEntity#actions()
   */
  EntityAction register(String name, Runnable action);

  /**
   * Detaches all controllers from this entity.
   */
  void detachControllers();

  /**
   * Attaches all controllers to this entity.
   */
  void attachControllers();

  /**
   * Gets the bounding box of the entity.
   *
   * @return The bounding box of the entity.
   */
  Rectangle2D getBoundingBox();

  /**
   * Gets the center point of the entity.
   *
   * @return The center point of the entity.
   */
  Point2D getCenter();

  /**
   * Gets the height of the entity.
   *
   * @return The height of the entity.
   */
  double getHeight();

  /**
   * Gets the location of the entity.
   *
   * @return The location of the entity.
   */
  Point2D getLocation();

  /**
   * Gets the map ID of the entity.
   *
   * @return The map ID of the entity.
   */
  int getMapId();

  /***
   * Gets the name of this entity.
   *
   * @return The name of this entity.
   */
  String getName();

  /**
   * Gets the render type of the entity.
   *
   * @return The render type of the entity.
   */
  RenderType getRenderType();

  /**
   * Determines whether this entity is being rendered with the layer it's originating from. This ignores the specified {@code RenderType} and makes
   * the entity dependent upon the visibility of its layer.
   *
   * <p>
   * This can only be used, of course, if the entity is related to a {@code MapObject}. <br> This defaults to {@code false} if not explicitly set on
   * the {@code MapObject}.
   *
   * @return True if the entity should be rendered with the layer of the corresponding map object; otherwise false.
   * @see ILayer#isVisible()
   * @see IMapObjectLayer#getMapObjects()
   * @see Environment#getEntitiesByLayer(int)
   * @see Environment#getEntitiesByLayer(String)
   */
  boolean renderWithLayer();

  /**
   * Gets the width of the entity.
   *
   * @return The width of the entity.
   */
  double getWidth();

  /**
   * Gets the X coordinate of the entity.
   *
   * @return The X coordinate of the entity.
   */
  double getX();

  /**
   * Gets the Y coordinate of the entity.
   *
   * @return The Y coordinate of the entity.
   */
  double getY();

  /**
   * Sends a message from the specified sender to this entity.
   *
   * @param sender  The sender of the message.
   * @param message The message to send.
   * @return The response to the message.
   */
  String sendMessage(Object sender, String message);

  /**
   * Sets the height of the entity.
   *
   * @param height The new height of the entity.
   */
  void setHeight(double height);

  /**
   * Sets the location of the entity.
   *
   * @param x The X coordinate of the new location.
   * @param y The Y coordinate of the new location.
   */
  void setLocation(double x, double y);

  /**
   * Checks if the entity has the specified tag.
   *
   * @param tag The tag to check for.
   * @return True if the entity has the tag; otherwise false.
   */
  boolean hasTag(String tag);

  /**
   * Gets the list of tags associated with the entity.
   *
   * @return A list of tags associated with the entity.
   */
  List<String> getTags();

  /**
   * Adds a tag to the entity.
   *
   * @param tag The tag to add.
   */
  void addTag(String tag);

  /**
   * Removes a tag from the entity.
   *
   * @param tag The tag to remove.
   */
  void removeTag(String tag);

  /**
   * Sets the map location.
   *
   * @param location the new map location
   */
  void setLocation(Point2D location);

  /**
   * Sets an id which should only be filled when an entity gets added due to map information.
   *
   * @param mapId The unique map ID for this {@link IEntity}
   */
  void setMapId(int mapId);

  /**
   * Sets the name of the entity.
   *
   * @param name The new name of the entity.
   */
  void setName(String name);

  /**
   * Sets the render type of the entity.
   *
   * @param renderType The new render type of the entity.
   */
  void setRenderType(RenderType renderType);

  /**
   * Sets whether the entity should be rendered with its layer.
   *
   * @param renderWithLayer True to render with the layer; otherwise false.
   */
  void setRenderWithLayer(boolean renderWithLayer);

  /**
   * Sets the size of the entity.
   *
   * @param width  The new width of the entity.
   * @param height The new height of the entity.
   */
  void setSize(double width, double height);

  /**
   * Sets the width of the entity.
   *
   * @param width The new width of the entity.
   */
  void setWidth(double width);

  /**
   * Sets the X coordinate of the entity.
   *
   * @param x The new X coordinate of the entity.
   */
  void setX(double x);

  /**
   * Sets the Y coordinate of the entity.
   *
   * @param y The new Y coordinate of the entity.
   */
  void setY(double y);

  /**
   * Gets the custom properties of the entity.
   *
   * @return The custom property provider of the entity.
   */
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
   * @param environment The environment that the entity was added to
   * @see IEntity#addListener(EntityListener)
   */
  void loaded(Environment environment);

  /**
   * This method provides the possibility to implement behavior whenever this entity was removed from the environment.
   *
   * @param environment The environment that the entity was removed from
   * @see IEntity#addListener(EntityListener)
   */
  void removed(Environment environment);

  /**
   * Indicates whether this entity is loaded on the currently active environment.
   *
   * @return True if the entity is loaded on the game's currently active environment; otherwise false.
   * @see GameWorld#environment()
   * @see IEntity#loaded(Environment)
   * @see IEntity#removed(Environment)
   */
  boolean isLoaded();
}
