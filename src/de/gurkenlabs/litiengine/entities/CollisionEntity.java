package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.physics.CollisionEvent;

@CollisionInfo(collision = true)
public abstract class CollisionEntity extends Entity implements ICollisionEntity {
  private static final Logger log = Logger.getLogger(CollisionEntity.class.getName());

  private static final double HEIGHT_FACTOR = 0.4;

  private static final double WIDTH_FACTOR = 0.4;

  private final Collection<CollisionListener> collisionListener = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.COLLISION_ALIGN)
  private Align align;

  @TmxProperty(name = MapObjectProperty.COLLISION)
  private boolean collision;

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_HEIGHT)
  private double collisionBoxHeight;

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_WIDTH)
  private double collisionBoxWidth;

  @TmxProperty(name = MapObjectProperty.COLLISION_VALIGN)
  private Valign valign;

  @TmxProperty(name = MapObjectProperty.COLLISION_TYPE)
  private Collision collisionType;

  private Rectangle2D collisionBox;

  protected CollisionEntity() {
    super();
    final CollisionInfo info = this.getClass().getAnnotation(CollisionInfo.class);
    this.collisionBoxWidth = info.collisionBoxWidth();
    this.collisionBoxHeight = info.collisionBoxHeight();
    this.collision = info.collision();
    this.valign = info.valign();
    this.align = info.align();
    this.collisionType = info.collisionType();
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  public static Rectangle2D getCollisionBox(final Point2D location, final double entityWidth, final double entityHeight, final double collisionBoxWidth, final double collisionBoxHeight, final Align align, final Valign valign) {
    double x = location.getX() + align.getLocation(entityWidth, collisionBoxWidth);
    double y = location.getY() + valign.getLocation(entityHeight, collisionBoxHeight);
    return new Rectangle2D.Double(x, y, collisionBoxWidth, collisionBoxHeight);
  }

  @Override
  public boolean canCollideWith(final ICollisionEntity otherEntity) {
    return true;
  }

  @Override
  public Align getCollisionBoxAlign() {
    return this.align;
  }

  /**
   * Gets the collision box.
   *
   * @return the collision box
   */
  @Override
  public Rectangle2D getCollisionBox() {
    return this.collisionBox;
  }

  /**
   * Gets the collision box.
   *
   * @param location
   *          the location
   * @return the collision box
   */
  @Override
  public Rectangle2D getCollisionBox(final Point2D location) {
    final double newCollisionBoxWidth = this.getCollisionBoxWidth() != -1 ? this.getCollisionBoxWidth() : this.getWidth() * WIDTH_FACTOR;
    final double newCollisionBoxHeight = this.getCollisionBoxHeight() != -1 ? this.getCollisionBoxHeight() : this.getHeight() * HEIGHT_FACTOR;

    return getCollisionBox(location, this.getWidth(), this.getHeight(), newCollisionBoxWidth, newCollisionBoxHeight, this.getCollisionBoxAlign(), this.getCollisionBoxValign());
  }

  @Override
  public double getCollisionBoxHeight() {
    return this.collisionBoxHeight;
  }

  @Override
  public double getCollisionBoxWidth() {
    return this.collisionBoxWidth;
  }

  @Override
  public Point2D getCollisionBoxCenter() {
    return new Point2D.Double(this.getCollisionBox().getCenterX(), this.getCollisionBox().getCenterY());
  }

  @Override
  public Valign getCollisionBoxValign() {
    return this.valign;
  }

  @Override
  public Collision getCollisionType() {
    return this.collisionType;
  }

  /**
   * Checks for collision.
   *
   * @return true, if successful
   */
  @Override
  public boolean hasCollision() {
    return this.collision && this.getCollisionBoxWidth() > 0 && this.getCollisionBoxHeight() > 0;
  }

  /**
   * Sets the collision.
   *
   * @param collision
   *          the new collision
   */
  @Override
  public void setCollision(final boolean collision) {
    this.collision = collision;
  }

  @Override
  public void setCollisionBoxAlign(final Align align) {
    this.align = align;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setCollisionBoxHeight(final double collisionBoxHeight) {
    this.collisionBoxHeight = collisionBoxHeight;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setCollisionBoxValign(final Valign valign) {
    this.valign = valign;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setCollisionBoxWidth(final double collisionBoxWidth) {
    this.collisionBoxWidth = collisionBoxWidth;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setSize(final double width, final double height) {
    super.setSize(width, height);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setHeight(final double height) {
    super.setHeight(height);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setWidth(final double width) {
    super.setWidth(width);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setCollisionType(Collision type) {
    if (type == Collision.ANY) {
      log.log(Level.WARNING, "CollistionType.ALL is not allowed to be assigned to an entity. It may only be used for filtering in the PhysicsEngine.");
      return;
    }

    if (this.getEnvironment() != null && this.getEnvironment().isLoaded()) {
      // re-add the entity to the physics engine so it will be treated with the updated collision type
      Game.physics().remove(this);
      this.collisionType = type;
      Game.physics().add(this);
    } else {
      this.collisionType = type;
    }
  }

  @Override
  public void onCollision(CollisionListener listener) {
    this.collisionListener.add(listener);
  }

  @Override
  public void removeCollisionListener(CollisionListener listener) {
    this.collisionListener.remove(listener);
  }

  @Override
  public void fireCollisionEvent(CollisionEvent event) {
    for (CollisionListener listener : this.collisionListener) {
      listener.collisionResolved(event);
    }
  }
}
