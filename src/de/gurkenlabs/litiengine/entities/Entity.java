package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ai.IBehaviorController;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomPropertyProvider;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.util.ReflectionUtilities;

@EntityInfo
public abstract class Entity implements IEntity, EntityRenderListener {
  private static final Logger log = Logger.getLogger(Entity.class.getName());
  public static final String ANY_MESSAGE = "";
  private final Collection<EntityTransformListener> transformListeners = ConcurrentHashMap.newKeySet();
  private final Collection<EntityListener> listeners = ConcurrentHashMap.newKeySet();
  private final Collection<EntityRenderListener> renderListeners = ConcurrentHashMap.newKeySet();
  private final Collection<EntityRenderedListener> renderedListeners = ConcurrentHashMap.newKeySet();
  private final Map<String, Collection<EntityMessageListener>> messageListeners = new ConcurrentHashMap<>();

  private final EntityControllers controllers = new EntityControllers();
  private final EntityActionMap actions = new EntityActionMap();
  private final ICustomPropertyProvider properties = new CustomPropertyProvider();

  private boolean renderWithLayer;
  private Environment environment;
  private boolean loaded;

  @TmxProperty(name = MapObjectProperty.TAGS)
  private final List<String> tags = new CopyOnWriteArrayList<>();

  private double angle;

  private Rectangle2D boundingBox;

  private double height;

  private int mapId;

  private Point2D mapLocation;

  private String name;

  private RenderType renderType;

  private double width;

  /**
   * Instantiates a new entity.
   */
  protected Entity() {
    this.mapLocation = new Point2D.Double(0, 0);
    final EntityInfo info = this.getClass().getAnnotation(EntityInfo.class);
    this.width = info.width();
    this.height = info.height();
    this.renderType = info.renderType();
    this.renderWithLayer = info.renderWithLayer();

    final Tag[] tagAnnotations = this.getClass().getAnnotationsByType(Tag.class);
    for (Tag t : tagAnnotations) {
      this.addTag(t.value());
    }

    this.registerActions();
  }

  protected Entity(boolean renderWithLayer) {
    this();
    this.renderWithLayer = renderWithLayer;
  }

  protected Entity(int mapId) {
    this();
    this.mapId = mapId;
  }

  protected Entity(String name) {
    this();
    this.name = name;
  }

  protected Entity(int mapId, String name) {
    this(mapId);
    this.name = name;
  }

  @Override
  public void addTransformListener(EntityTransformListener listener) {
    this.transformListeners.add(listener);
  }

  @Override
  public void removeTransformListener(EntityTransformListener listener) {
    this.transformListeners.remove(listener);
  }

  @Override
  public void addListener(EntityListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeListener(EntityListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public void onRendered(final EntityRenderedListener listener) {
    this.renderedListeners.add(listener);
  }

  @Override
  public void removeEntityRenderedListener(final EntityRenderedListener listener) {
    this.renderedListeners.remove(listener);
  }

  @Override
  public void addEntityRenderListener(final EntityRenderListener listener) {
    this.renderListeners.add(listener);
  }

  @Override
  public void removeEntityRenderListener(final EntityRenderListener listener) {
    this.renderListeners.remove(listener);
  }

  @Override
  public void onMessage(EntityMessageListener listener) {
    this.onMessage(ANY_MESSAGE, listener);
  }

  @Override
  public void onMessage(String message, EntityMessageListener listener) {
    if (!this.messageListeners.containsKey(message)) {
      this.messageListeners.put(message, ConcurrentHashMap.newKeySet());
    }

    this.messageListeners.get(message).add(listener);
  }

  @Override
  public void removeMessageListener(EntityMessageListener listener) {
    for (Collection<EntityMessageListener> listenerType : this.messageListeners.values()) {
      if (listenerType == null || listenerType.isEmpty()) {
        continue;
      }

      listenerType.remove(listener);
    }
  }

  @Override
  public void attachControllers() {
    this.controllers.attachAll();
  }

  @Override
  public void detachControllers() {
    this.controllers.detachAll();
  }

  @Override
  public ICustomPropertyProvider getProperties() {
    return this.properties;
  }

  @Override
  public double getAngle() {
    return this.angle;
  }

  @Override
  public IEntityAnimationController<?> animations() {
    return this.getController(IEntityAnimationController.class);
  }

  @Override
  public IBehaviorController behavior() {
    return this.getController(IBehaviorController.class);
  }

  @Override
  public void addController(IEntityController controller) {
    this.controllers.addController(controller);
  }

  @Override
  public <T extends IEntityController> void setController(Class<T> clss, T controller) {
    this.controllers.setController(clss, controller);
  }

  @Override
  public <T extends IEntityController> T getController(Class<T> clss) {
    return this.controllers.getController(clss);
  }

  @Override
  public Rectangle2D getBoundingBox() {
    if (this.boundingBox != null) {
      return this.boundingBox;
    }

    this.boundingBox = new Rectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    return this.boundingBox;
  }

  @Override
  public Point2D getCenter() {
    return new Point2D.Double(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5);
  }

  @Override
  public double getHeight() {
    return this.height;
  }

  @Override
  public Point2D getLocation() {
    return this.mapLocation;
  }

  @Override
  public int getMapId() {
    return this.mapId;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public RenderType getRenderType() {
    return this.renderType;
  }

  @Override
  public double getWidth() {
    return this.width;
  }

  @Override
  public double getX() {
    return this.getLocation().getX();
  }

  @Override
  public double getY() {
    return this.getLocation().getY();
  }

  @Override
  public boolean isVisible() {
    return this.animations() != null && this.animations().isEnabled();
  }

  @Override
  public void setVisible(boolean visible) {
    if (this.animations() == null) {
      return;
    }

    this.animations().setEnabled(visible);
  }

  @Override
  public EntityActionMap actions() {
    return this.actions;
  }

  @Override
  public void perform(String actionName) {
    if (actionName == null || actionName.isEmpty()) {
      return;
    }

    if (!this.actions.exists(actionName)) {
      log.log(Level.INFO, "Entity \"{0}\" could not perform the action \"{1}\". \nMaybe you need to register the action or provide an appropriate Action annotation on the method you want to call.", new Object[] { this, actionName });
      return;
    }

    this.actions.get(actionName).perform();
  }

  @Override
  public EntityAction register(String name, Runnable action) {
    return this.actions.register(name, action);
  }

  @Override
  public String sendMessage(final Object sender, final String message) {
    EntityMessageEvent event = this.fireMessageReceived(sender, ANY_MESSAGE, message, null);
    this.fireMessageReceived(sender, message, message, event);

    return null;
  }

  @Override
  public void setHeight(final double height) {
    this.height = height;
    this.boundingBox = null;
    this.fireSizeChangedEvent();
  }

  @Override
  public void setLocation(final double x, final double y) {
    this.setLocation(new Point2D.Double(x, y));
  }

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  @Override
  public void setLocation(final Point2D location) {
    this.mapLocation = location;
    this.boundingBox = null;
    this.fireLocationChangedEvent();
  }

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   */
  @Override
  public void setMapId(final int mapId) {
    this.mapId = mapId;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public void setRenderType(final RenderType renderType) {
    this.renderType = renderType;
  }

  @Override
  public void setSize(final double width, final double height) {
    this.width = width;
    this.height = height;
    this.boundingBox = null;
    this.fireSizeChangedEvent();
  }

  @Override
  public void setWidth(final double width) {
    this.width = width;
    this.boundingBox = null;
    this.fireSizeChangedEvent();
  }

  @Override
  public void setX(double x) {
    this.setLocation(x, this.getY());
  }

  @Override
  public void setY(double y) {
    this.setLocation(this.getX(), y);
  }

  @Override
  public List<String> getTags() {
    return this.tags;
  }

  @Override
  public boolean hasTag(String tag) {
    return this.tags.contains(tag);
  }

  @Override
  public void addTag(String tag) {
    if (!this.getTags().contains(tag)) {
      this.getTags().add(tag);
    }
    if (this.getEnvironment() != null) {
      this.getEnvironment().getEntitiesByTag().computeIfAbsent(tag, t -> new CopyOnWriteArrayList<>()).add(this);
    }
  }

  @Override
  public void removeTag(String tag) {
    this.getTags().remove(tag);
    if (Game.world().environment() == null) {
      return;
    }
    this.getEnvironment().getEntitiesByTag().get(tag).remove(this);
    if (this.getEnvironment().getEntitiesByTag().get(tag).isEmpty()) {
      this.getEnvironment().getEntitiesByTag().remove(tag);
    }
  }

  @Override
  public void setAngle(final double angle) {
    this.angle = angle;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("#" + this.getMapId() + ": ");
    if (this.getName() != null && !this.getName().isEmpty()) {
      sb.append(this.getName());
    } else {
      sb.append(this.getClass().getSimpleName());
    }
    return sb.toString();
  }

  @Override
  public Environment getEnvironment() {
    return this.environment;
  }

  @Override
  public void loaded(Environment environment) {
    this.environment = environment;

    this.loaded = true;

    for (EntityListener listener : this.listeners) {
      listener.loaded(this, this.getEnvironment());
    }
  }

  @Override
  public void removed(Environment environment) {
    this.loaded = false;

    for (EntityListener listener : this.listeners) {
      listener.removed(this, this.getEnvironment());
    }

    // set to null after informing the listeners so they can still access the environment instance
    this.environment = null;
  }

  @Override
  public boolean isLoaded() {
    return this.loaded;
  }

  @Override
  public boolean renderWithLayer() {
    return this.renderWithLayer;
  }

  @Override
  public void setRenderWithLayer(boolean renderWithLayer) {
    this.renderWithLayer = renderWithLayer;
  }

  @Override
  public void rendering(EntityRenderEvent event) {
    if (event.getEntity() == null || !event.getEntity().equals(Entity.this)) {
      return;
    }

    for (EntityRenderListener listener : Entity.this.renderListeners) {
      listener.rendering(event);
    }
  }

  @Override
  public void rendered(EntityRenderEvent event) {
    if (event.getEntity() == null || !event.getEntity().equals(Entity.this)) {
      return;
    }

    for (EntityRenderListener listener : Entity.this.renderListeners) {
      listener.rendered(event);
    }

    for (EntityRenderedListener listener : Entity.this.renderedListeners) {
      listener.rendered(event);
    }
  }

  @Override
  public boolean canRender(IEntity entity) {
    if (entity == null || !entity.equals(Entity.this)) {
      return true;
    }

    for (EntityRenderListener listener : Entity.this.renderListeners) {
      if (!listener.canRender(entity)) {
        return false;
      }
    }

    return true;
  }

  protected EntityControllers getControllers() {
    return this.controllers;
  }

  private void fireSizeChangedEvent() {
    for (EntityTransformListener listener : this.transformListeners) {
      listener.sizeChanged(this);
    }
  }

  private void fireLocationChangedEvent() {
    for (EntityTransformListener listener : this.transformListeners) {
      listener.locationChanged(this);
    }
  }

  private EntityMessageEvent fireMessageReceived(Object sender, String listenerMessage, String message, EntityMessageEvent event) {
    if (message == null) {
      return event;
    }

    if (this.messageListeners.containsKey(listenerMessage) && this.messageListeners.get(listenerMessage) != null) {
      EntityMessageEvent receivedEvent = event;
      for (EntityMessageListener listener : this.messageListeners.get(listenerMessage)) {
        if (receivedEvent == null) {
          receivedEvent = new EntityMessageEvent(sender, this, message);
        }

        listener.messageReceived(receivedEvent);
      }
    }

    return event;
  }

  /**
   * Registers all default actions that are annotated with a <code>EntityAction</code> annotation.
   */
  private void registerActions() {
    List<Method> methods = ReflectionUtilities.getMethodsAnnotatedWith(this.getClass(), Action.class);

    // iterate over all methods that have the EntityActionInfo annotation and register them
    for (Method method : methods) {
      if (!Modifier.isPublic(method.getModifiers()) || method.getParameterCount() > 0) {
        log.log(Level.INFO, "\"{0}\" is not a valid entity action. Either make it public and parameterless or remove the Action annotation.", new Object[] { method });
        continue;
      }

      Action info = method.getAnnotation(Action.class);
      if (info == null) {
        continue;
      }

      final String actionName = info.name() == null || info.name().isEmpty() ? method.getName() : info.name();
      EntityAction action = this.register(actionName, () -> {
        try {
          method.invoke(this);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          log.log(Level.SEVERE, "Could not perform the entity action " + actionName, e);
        }
      });

      if (action != null) {
        action.setDescription(info.description());
      }
    }
  }
}
