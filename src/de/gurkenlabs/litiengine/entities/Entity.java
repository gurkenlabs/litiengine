package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;

/**
 * The Class Entity.
 */
@EntityInfo
public abstract class Entity implements IEntity {
  private final List<String> tags;

  /** The direction. */
  private float angle;

  private Rectangle2D boundingBox;

  private float height;

  private int mapId;

  /** The map location. */
  private Point2D mapLocation;

  private String name;

  private RenderType renderType;

  private float width;

  /**
   * Instantiates a new entity.
   */
  protected Entity() {
    this.tags = new CopyOnWriteArrayList<>();
    this.mapLocation = new Point2D.Double(0, 0);
    final EntityInfo info = this.getClass().getAnnotation(EntityInfo.class);
    this.width = info.width();
    this.height = info.height();
    this.renderType = info.renderType();
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().getEntities().add(this);
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

  /**
   * Gets the map dimension center.
   *
   * @return the map dimension center
   */
  @Override
  public Point2D getDimensionCenter() {
    return new Point2D.Double(this.getLocation().getX() + this.getWidth() * 0.5, this.getLocation().getY() + this.getHeight() * 0.5);
  }

  @Override
  public float getHeight() {
    return this.height;
  }

  /**
   * Gets the map location.
   *
   * @return the map location
   */
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
  public String sendMessage(final Object sender, final String message) {
    return null;
  }

  @Override
  public void setHeight(final float height) {
    this.height = height;
    this.boundingBox = null;
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
  }

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   *
   * @param mapId
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
    this.setWidth(width);
    this.setHeight(height);
  }

  @Override
  public void setWidth(final float width) {
    this.width = width;
    this.boundingBox = null;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public boolean hasTag(String tag) {
    return this.tags.contains(tag);
  }

  public void addTag(String tag) {
    if (!this.tags.contains(tag)) {
      this.tags.add(tag);
    }
  }

  public void removeTag(String tag) {
    if (!this.tags.contains(tag)) {
      this.tags.remove(tag);
    }
  }

  protected void setAngle(final float angle) {
    this.angle = angle;
  }
}