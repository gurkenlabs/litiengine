package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.physics.CollisionEvent;
import de.gurkenlabs.litiengine.tweening.TweenType;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract collision entity. This class provides the base implementation for entities that can collide with other entities. It extends
 * the Entity class and implements the ICollisionEntity interface.
 */
@CollisionInfo(collision = true) public abstract class CollisionEntity extends Entity implements ICollisionEntity {
  private static final Logger log = Logger.getLogger(CollisionEntity.class.getName());

  private static final double HEIGHT_FACTOR = 0.4;

  private static final double WIDTH_FACTOR = 0.4;

  private final Collection<CollisionListener> collisionListener = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.COLLISION_ALIGN) private Align align;

  @TmxProperty(name = MapObjectProperty.COLLISION) private boolean collision;

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_HEIGHT) private double collisionBoxHeight;

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_WIDTH) private double collisionBoxWidth;

  @TmxProperty(name = MapObjectProperty.COLLISION_VALIGN) private Valign valign;

  @TmxProperty(name = MapObjectProperty.COLLISION_TYPE) private Collision collisionType;

  private Rectangle2D collisionBox;

  /**
   * Constructs a new CollisionEntity. Initializes the collision box dimensions, alignment, and type based on the CollisionInfo annotation. Refreshes
   * the collision box to reflect the initial state.
   */
  protected CollisionEntity() {
    super();
    final CollisionInfo info = getClass().getAnnotation(CollisionInfo.class);
    this.collisionBoxWidth = info.collisionBoxWidth();
    this.collisionBoxHeight = info.collisionBoxHeight();
    this.collision = info.collision();
    this.valign = info.valign();
    this.align = info.align();
    this.collisionType = info.collisionType();
    this.refreshCollisionBox();
  }

  /**
   * Calculates the collision box for an entity based on its location, dimensions, and alignment.
   *
   * @param location           the location of the entity
   * @param entityWidth        the width of the entity
   * @param entityHeight       the height of the entity
   * @param collisionBoxWidth  the width of the collision box
   * @param collisionBoxHeight the height of the collision box
   * @param align              the horizontal alignment of the collision box
   * @param valign             the vertical alignment of the collision box
   * @return the calculated collision box as a Rectangle2D object
   */
  public static Rectangle2D getCollisionBox(final Point2D location, final double entityWidth, final double entityHeight,
    final double collisionBoxWidth, final double collisionBoxHeight, final Align align, final Valign valign) {
    double x = location.getX() + align.getLocation(entityWidth, collisionBoxWidth);
    double y = location.getY() + valign.getLocation(entityHeight, collisionBoxHeight);
    return new Rectangle2D.Double(x, y, collisionBoxWidth, collisionBoxHeight);
  }

  @Override public boolean canCollideWith(final ICollisionEntity otherEntity) {
    return true;
  }

  @Override public Align getCollisionBoxAlign() {
    return this.align;
  }

  /**
   * Gets the collision box.
   *
   * @return the collision box
   */
  @Override public Rectangle2D getCollisionBox() {
    return this.collisionBox;
  }

  /**
   * Gets the collision box.
   *
   * @param location the location
   * @return the collision box
   */
  @Override public Rectangle2D getCollisionBox(final Point2D location) {
    final double newCollisionBoxWidth = getCollisionBoxWidth() != -1 ? getCollisionBoxWidth() : Math.round(getWidth() * WIDTH_FACTOR);
    final double newCollisionBoxHeight = getCollisionBoxHeight() != -1 ? getCollisionBoxHeight() : Math.round(getHeight() * HEIGHT_FACTOR);

    return getCollisionBox(location, getWidth(), getHeight(), newCollisionBoxWidth, newCollisionBoxHeight, getCollisionBoxAlign(),
      getCollisionBoxValign());
  }

  @Override public double getCollisionBoxHeight() {
    return this.collisionBoxHeight;
  }

  @Override public double getCollisionBoxWidth() {
    return this.collisionBoxWidth;
  }

  @Override public Point2D getCollisionBoxCenter() {
    return new Point2D.Double(getCollisionBox().getCenterX(), getCollisionBox().getCenterY());
  }

  @Override public Valign getCollisionBoxValign() {
    return this.valign;
  }

  @Override public Collision getCollisionType() {
    return this.collisionType;
  }

  @Override public float[] getTweenValues(TweenType tweenType) {
    switch (tweenType) {
      case COLLISION_WIDTH:
        return new float[] {(float) getCollisionBoxWidth()};
      case COLLISION_HEIGHT:
        return new float[] {(float) getCollisionBoxHeight()};
      case COLLISION_BOTH:
        return new float[] {(float) getCollisionBoxWidth(), (float) getCollisionBoxHeight()};
      default:
        return super.getTweenValues(tweenType);
    }
  }

  @Override public void setTweenValues(TweenType tweenType, float[] newValues) {
    switch (tweenType) {
      case COLLISION_WIDTH:
        this.setCollisionBoxWidth(newValues[0]);
        break;
      case COLLISION_HEIGHT:
        this.setCollisionBoxHeight(newValues[0]);
        break;
      case COLLISION_BOTH:
        this.setCollisionBoxWidth(newValues[0]);
        this.setCollisionBoxHeight(newValues[1]);
        break;
      default:
        super.setTweenValues(tweenType, newValues);
    }
  }

  /**
   * Checks for collision.
   *
   * @return true, if successful
   */
  @Override public boolean hasCollision() {
    return this.collision && getCollisionBoxWidth() > 0 && getCollisionBoxHeight() > 0;
  }

  /**
   * Sets the collision.
   *
   * @param collision the new collision
   */
  @Override public void setCollision(final boolean collision) {
    this.collision = collision;
  }

  @Override public void setCollisionBoxAlign(final Align align) {
    this.align = align;
    this.refreshCollisionBox();
  }

  @Override public void setCollisionBoxHeight(final double collisionBoxHeight) {
    this.collisionBoxHeight = collisionBoxHeight;
    this.refreshCollisionBox();
  }

  @Override public void setCollisionBoxValign(final Valign valign) {
    this.valign = valign;
    this.refreshCollisionBox();
  }

  @Override public void setCollisionBoxWidth(final double collisionBoxWidth) {
    this.collisionBoxWidth = collisionBoxWidth;
    this.refreshCollisionBox();
  }

  @Override public void setLocation(final Point2D location) {
    super.setLocation(location);
    this.refreshCollisionBox();
  }

  @Override public void setSize(final double width, final double height) {
    super.setSize(width, height);
    this.refreshCollisionBox();
  }

  @Override public void setHeight(final double height) {
    super.setHeight(height);
    this.refreshCollisionBox();
  }

  @Override public void setWidth(final double width) {
    super.setWidth(width);
    this.refreshCollisionBox();
  }

  @Override public void setCollisionType(Collision type) {
    if (type == Collision.ANY) {
      log.log(Level.WARNING, "Collision.ANY is not allowed to be assigned to an entity. It may only be used for filtering in the PhysicsEngine.");
      return;
    }

    if (getEnvironment() != null && getEnvironment().isLoaded()) {
      // re-add the entity to the physics engine so it will be treated with the updated collision
      // type
      Game.physics().remove(this);
      this.collisionType = type;
      Game.physics().add(this);
    } else {
      this.collisionType = type;
    }
  }

  @Override public void onCollision(CollisionListener listener) {
    this.collisionListener.add(listener);
  }

  @Override public void removeCollisionListener(CollisionListener listener) {
    this.collisionListener.remove(listener);
  }

  @Override public void fireCollisionEvent(CollisionEvent event) {
    for (CollisionListener listener : this.collisionListener) {
      listener.collisionResolved(event);
    }
  }

  /**
   * Refreshes the collision box to reflect the current state of the entity. This method recalculates the collision box based on the entity's
   * location, dimensions, and alignment.
   */
  protected void refreshCollisionBox() {
    this.collisionBox = getCollisionBox(getLocation());
  }

  @SuppressWarnings("unused") private void afterTmxUnmarshal(IMapObject mapObject) {
    this.refreshCollisionBox();
  }
}
