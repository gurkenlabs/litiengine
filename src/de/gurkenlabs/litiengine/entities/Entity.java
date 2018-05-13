package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.annotation.Tag;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;

@EntityInfo
public abstract class Entity implements IEntity {
  public static final String ANY_MESSAGE = "";
  private final List<EntityTransformListener> transformListeners;
  private final Map<String, List<MessageListener>> messageListeners;
  private final List<String> tags;

  private float angle;

  private Rectangle2D boundingBox;

  private float height;

  private int mapId;

  private Point2D mapLocation;

  private String name;

  private RenderType renderType;

  private float width;

  /**
   * Instantiates a new entity.
   */
  protected Entity() {
    this.transformListeners = new CopyOnWriteArrayList<>();
    this.messageListeners = new ConcurrentHashMap<>();
    this.tags = new CopyOnWriteArrayList<>();

    this.mapLocation = new Point2D.Double(0, 0);
    final EntityInfo info = this.getClass().getAnnotation(EntityInfo.class);
    this.width = info.width();
    this.height = info.height();
    this.renderType = info.renderType();

    final Tag[] tagAnnotations = this.getClass().getAnnotationsByType(Tag.class);
    for (Tag t : tagAnnotations) {
      this.addTag(t.value());
    }
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
  public void addMessageListener(MessageListener listener) {
    this.addMessageListener(ANY_MESSAGE, listener);
  }

  @Override
  public void addMessageListener(String message, MessageListener listener) {
    if (!this.messageListeners.containsKey(message)) {
      this.messageListeners.put(message, new CopyOnWriteArrayList<>());
    }

    this.messageListeners.get(message).add(listener);
  }

  @Override
  public void removeMessageListener(MessageListener listener) {
    for (List<MessageListener> listeners : this.messageListeners.values()) {
      if (listeners == null || listeners.isEmpty()) {
        continue;
      }

      listeners.remove(listener);
    }
  }

  @Override
  public float getAngle() {
    return this.angle;
  }

  @Override
  public IAnimationController getAnimationController() {
    return Game.getEntityControllerManager().getAnimationController(this);
  }

  @Override
  public Rectangle2D getBoundingBox() {
    if (this.boundingBox != null) {
      return this.boundingBox;
    }

    this.boundingBox = new Rectangle2D.Double(this.getLocation().getX(), this.getLocation().getY(), this.getWidth(), this.getHeight());
    return this.boundingBox;
  }

  @Override
  public Point2D getCenter() {
    return new Point2D.Double(this.getLocation().getX() + this.getWidth() * 0.5, this.getLocation().getY() + this.getHeight() * 0.5);
  }

  @Override
  public float getHeight() {
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
  public float getWidth() {
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
  public String sendMessage(final Object sender, final String message) {
    MessageEvent event = this.fireMessageReceived(sender, ANY_MESSAGE, message, null);
    this.fireMessageReceived(sender, message, message, event);

    return null;
  }

  @Override
  public void setHeight(final float height) {
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
  public void setSize(final float width, final float height) {
    this.width = width;
    this.height = height;
    this.boundingBox = null;
    this.fireSizeChangedEvent();
  }

  @Override
  public void setWidth(final float width) {
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
    if (Game.getEnvironment() == null) {
      return;
    }
    if (Game.getEnvironment().getEntitiesByTag().containsKey(tag)) {
      Game.getEnvironment().getEntitiesByTag().get(tag).add(this);
      return;
    }
    Game.getEnvironment().getEntitiesByTag().put(tag, new CopyOnWriteArrayList<>());
    Game.getEnvironment().getEntitiesByTag().get(tag).add(this);
  }

  @Override
  public void removeTag(String tag) {
    this.getTags().remove(tag);
    if (Game.getEnvironment() == null) {
      return;
    }
    Game.getEnvironment().getEntitiesByTag().get(tag).remove(this);
    if (Game.getEnvironment().getEntitiesByTag().get(tag).isEmpty()) {
      Game.getEnvironment().getEntitiesByTag().remove(tag);
    }
  }

  public void setAngle(final float angle) {
    this.angle = angle;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (this.getName() != null && !this.getName().isEmpty()) {
      sb.append(this.getName());
    } else {
      sb.append(this.getClass().getSimpleName());
    }

    sb.append(" #");
    sb.append(this.getMapId());
    return sb.toString();
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

  private MessageEvent fireMessageReceived(Object sender, String listenerMessage, String message, MessageEvent event) {
    if (message == null) {
      return event;
    }

    if (this.messageListeners.containsKey(listenerMessage) && this.messageListeners.get(listenerMessage) != null) {
      MessageEvent receivedEvent = event;
      for (MessageListener listener : this.messageListeners.get(listenerMessage)) {
        if (receivedEvent == null) {
          receivedEvent = new MessageEvent(sender, this, message);
        }

        listener.messageReceived(receivedEvent);
      }
    }

    return event;
  }
}