package de.gurkenlabs.litiengine.physics;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

/**
 * This class is used to hold all collision aware instances and static collision boxes.
 * It is responsible for resolving movement that respects the collision boxes in the game. This is achieved by the <b><code>move</code></b> method
 * group.
 * <br>
 * The <b><code>collides</code></b> method group can detect a collision at a certain location, for rectangles, or collision aware entities.
 * Also, there's an overload that takes a <code>Line2D</code> to perform a basic raycast check.
 */
public final class PhysicsEngine implements IUpdateable {
  private Rectangle2D environmentBounds;

  private final Map<Collision, List<ICollisionEntity>> collisionEntities;
  private final Map<Collision, List<Rectangle2D>> collisionBoxes;

  /**
   * Instantiates a new PhysicsEngine instance.
   * 
   * <p>
   * <b>You should never call this manually! Instead use the <code>Game.physics()</code> instance.</b>
   * </p>
   * 
   * @see Game#physics()
   */
  public PhysicsEngine() {
    this.collisionEntities = new ConcurrentHashMap<>();
    this.collisionEntities.put(Collision.DYNAMIC, new CopyOnWriteArrayList<>());
    this.collisionEntities.put(Collision.STATIC, new CopyOnWriteArrayList<>());
    this.collisionEntities.put(Collision.ANY, new CopyOnWriteArrayList<>());

    this.collisionBoxes = new ConcurrentHashMap<>();
    this.collisionBoxes.put(Collision.DYNAMIC, new CopyOnWriteArrayList<>());
    this.collisionBoxes.put(Collision.STATIC, new CopyOnWriteArrayList<>());
    this.collisionBoxes.put(Collision.ANY, new CopyOnWriteArrayList<>());
  }

  /**
   * Adds the specified collision aware entity to the physics engine which will make it respect the entity's collision box for upcoming calls.
   * 
   * <p>
   * <i>If you add a <code>ICollisionEntiy</code> to your Environment, it will automatically be added to the the PhysicsEngine. There is typically no
   * need to call this explicitly.</i>
   * </p>
   * 
   * @param entity
   *          The collision entity to be added.
   * 
   * @see ICollisionEntity#getCollisionBox()
   * @see PhysicsEngine#remove(ICollisionEntity)
   */
  public void add(final ICollisionEntity entity) {
    if (entity.getCollisionType() == null) {
      return;
    }

    switch (entity.getCollisionType()) {
    case DYNAMIC:
    case STATIC:
      this.collisionEntities.get(entity.getCollisionType()).add(entity);
      break;
    default:
      return;
    }

    this.collisionEntities.get(Collision.ANY).add(entity);
  }

  /**
   * Removes the specified entity from any collision processing. Typically this method is implicitly called when an entity is removed from the current
   * environment.
   * 
   * @param entity
   *          The entity that is about to be removed.
   */
  public void remove(final ICollisionEntity entity) {
    if (entity.getCollisionType() == null) {
      return;
    }

    switch (entity.getCollisionType()) {
    case DYNAMIC:
    case STATIC:
      this.collisionEntities.get(entity.getCollisionType()).remove(entity);
      break;
    default:
      return;
    }

    this.collisionEntities.get(Collision.ANY).remove(entity);
  }

  /**
   * Clears all previously registered participants in the collision process from this instance.
   * This includes all entities, static collision boxes and the map boundaries.
   */
  public void clear() {
    for (Collision type : Collision.values()) {
      if (type == Collision.NONE) {
        continue;
      }

      this.collisionEntities.get(type).clear();
      this.collisionBoxes.get(type).clear();
    }

    this.setBounds(null);
  }

  public Collection<Rectangle2D> getCollisionBoxes() {
    return this.getCollisionBoxes(Collision.ANY);
  }

  public Collection<Rectangle2D> getCollisionBoxes(Collision type) {
    if (type == Collision.NONE) {
      return new CopyOnWriteArrayList<>();
    }

    return this.collisionBoxes.get(type);
  }

  public Collection<ICollisionEntity> getCollisionEntities() {
    return this.getCollisionEntities(Collision.ANY);
  }

  public Collection<ICollisionEntity> getCollisionEntities(Collision type) {
    if (type == Collision.NONE) {
      return new CopyOnWriteArrayList<>();
    }

    return this.collisionEntities.get(type);
  }

  public Rectangle2D getBounds() {
    return this.environmentBounds;
  }

  public void setBounds(final Rectangle2D environmentBounds) {
    this.environmentBounds = environmentBounds;
  }

  public boolean collides(Line2D line) {
    return this.collides(line, Collision.ANY, null);
  }

  public boolean collides(Line2D line, Collision collision) {
    return this.collides(line, collision, null);
  }

  public boolean collides(Line2D line, ICollisionEntity entity) {
    return this.collides(line, Collision.ANY, entity);
  }

  public boolean collides(final Line2D line, Collision collision, ICollisionEntity entity) {
    return this.collides(entity, collision, otherEntity -> GeometricUtilities.getIntersectionPoint(line, otherEntity.getCollisionBox()) != null);
  }

  public boolean collides(final Rectangle2D rect) {
    return this.collides(rect, Collision.ANY);
  }

  /**
   * Checks whether the specified rectangle collides with anything.
   * 
   * @param rect
   *          The rectangle to check the collision for.
   * @param collisionEntity
   *          The entity on which this collision check is based on.
   * @return Returns true if the specified rectangle collides with any collision
   *         box of the specified type(s); otherwise false.
   */
  public boolean collides(Rectangle2D rect, ICollisionEntity collisionEntity) {
    return this.collides(rect, Collision.ANY, collisionEntity);
  }

  public boolean collides(Rectangle2D collisionBox, Collision type) {
    return collides(collisionBox, type, null);
  }

  public boolean collides(Rectangle2D rectangle, Collision type, ICollisionEntity entity) {
    if (this.environmentBounds != null && !this.environmentBounds.intersects(rectangle)) {
      return true;
    }

    return collides(entity, type, otherEntity -> GeometricUtilities.intersects(otherEntity.getCollisionBox(), rectangle));
  }

  public boolean collides(final Point2D location) {
    return this.collides(location, Collision.ANY);
  }

  public boolean collides(Point2D location, Collision type) {
    return collides(location, type, null);
  }

  public boolean collides(Point2D location, ICollisionEntity collisionEntity) {
    return this.collides(location, Collision.ANY, collisionEntity);
  }

  public boolean collides(Point2D location, Collision type, ICollisionEntity entity) {
    if (this.environmentBounds != null && !this.environmentBounds.contains(location)) {
      return true;
    }

    return collides(entity, type, otherEntity -> otherEntity.getCollisionBox().contains(location));
  }

  public boolean collides(final double x, final double y) {
    return this.collides(new Point2D.Double(x, y));
  }

  public boolean collides(double x, double y, Collision collisionType) {
    return this.collides(new Point2D.Double(x, y), collisionType);
  }

  public boolean collides(double x, double y, ICollisionEntity collisionEntity) {
    return collides(new Point2D.Double(x, y), collisionEntity);
  }

  public boolean collides(ICollisionEntity collisionEntity) {
    return this.collides(collisionEntity, Collision.ANY);
  }

  public boolean collides(ICollisionEntity collisionEntity, Collision collisionType) {
    return this.collides(collisionEntity.getCollisionBox(), collisionType, collisionEntity);
  }

  public RaycastHit raycast(Point2D point, double angle) {
    double diameter = GeometricUtilities.getDiagonal(this.environmentBounds);
    return raycast(point, GeometricUtilities.project(point, angle, diameter));
  }

  public RaycastHit raycast(Point2D start, Point2D target) {
    return raycast(start, target, Collision.ANY);
  }

  public RaycastHit raycast(Point2D start, Point2D target, Collision collisionType) {
    final Line2D line = new Line2D.Double(start.getX(), start.getY(), target.getX(), target.getY());
    return raycast(line, collisionType, null);
  }

  public RaycastHit raycast(Line2D line) {
    return raycast(line, Collision.ANY, null);
  }

  public RaycastHit raycast(Line2D line, Collision collisionType) {
    return raycast(line, collisionType, null);
  }

  public RaycastHit raycast(Line2D line, ICollisionEntity entity) {
    return raycast(line, Collision.ANY, entity);
  }

  public RaycastHit raycast(Line2D line, Collision collisionType, ICollisionEntity entity) {
    final Point2D rayCastSource = new Point2D.Double(line.getX1(), line.getY1());

    for (final ICollisionEntity collisionEntity : this.collisionEntities.get(collisionType)) {
      if (!canCollide(entity, collisionEntity)) {
        continue;
      }

      if (collisionEntity.getCollisionBox().intersectsLine(line)) {
        double closestDist = -1;
        Point2D closestPoint = null;
        for (final Point2D intersection : GeometricUtilities.getIntersectionPoints(line, collisionEntity.getCollisionBox())) {
          final double dist = intersection.distance(rayCastSource);
          if (closestPoint == null || dist < closestDist) {
            closestPoint = intersection;
            closestDist = dist;
          }
        }

        return new RaycastHit(closestPoint, collisionEntity, closestDist);
      }
    }

    return null;
  }

  /**
   * Moves the specified entity by the delta in the direction of the angle.
   *
   * @param entity
   *          the entity
   * @param angle
   *          the angle
   * @param delta
   *          the delta
   * @return true, if successful, false if the physics engine detected a
   *         collision.
   */
  public boolean move(final IMobileEntity entity, final double angle, final double delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, delta);
    return this.move(entity, newPosition);
  }

  public boolean move(IMobileEntity entity, Direction direction, double delta) {
    return this.move(entity, direction.toAngle(), delta);
  }

  public boolean move(final IMobileEntity entity, final double x, final double y, final float delta) {
    return this.move(entity, new Point2D.Double(x, y), delta);
  }

  public boolean move(final IMobileEntity entity, final float delta) {
    return this.move(entity, entity.getAngle(), delta);
  }

  public boolean move(final IMobileEntity entity, Point2D newLocation) {
    if (entity.turnOnMove()) {
      entity.setAngle((float) GeometricUtilities.calcRotationAngleInDegrees(entity.getLocation(), newLocation));
    }

    // don't set new location if it is outside the boundaries of the map
    if (!this.isInMap(entity.getCollisionBox(newLocation))) {
      newLocation = this.clamptoMap(entity, newLocation);
    }

    if (!entity.hasCollision()) {
      entity.setLocation(newLocation);
      return true;
    }

    // check if there is any collision to resolve on the new location
    if (this.resolveCollisionForNewPosition(entity, newLocation)) {
      return false;
    }

    // This method provides a simplified approach for a multi-sampling algorithm
    // to prevent glitching through collision boxes that are smaller than the
    // movement step size
    if (this.resolveCollisionForRaycastToNewPosition(entity, newLocation)) {
      return false;
    }

    // set new map location
    entity.setLocation(newLocation);
    return true;
  }

  public boolean move(final IMobileEntity entity, final Point2D target, final float delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), target, delta);
    return this.move(entity, newPosition);
  }

  @Override
  public void update() {
    // retrieve all collision box rectangles once per update
    for (Collision type : Collision.values()) {
      if (type == Collision.NONE) {
        continue;
      }

      this.collisionBoxes.get(type).clear();
      this.collisionBoxes.get(type).addAll(this.collisionEntities.get(type).stream().map(ICollisionEntity::getCollisionBox).collect(Collectors.toList()));
    }
  }

  private static boolean canCollide(ICollisionEntity entity, ICollisionEntity otherEntity) {
    if (otherEntity == null || !otherEntity.hasCollision()) {
      return false;
    }

    // the entity to check against is not provided
    if (entity == null) {
      return true;
    }

    // cannot collide with itself
    if (otherEntity.equals(entity)) {
      return false;
    }

    // an entity cannot collide with other entities that are excluded from collision by the canCollideWith method
    return entity.canCollideWith(otherEntity);
  }

  private Rectangle2D getIntersection(final ICollisionEntity entity, final Rectangle2D entityCollisionBox) {
    for (final ICollisionEntity collisionBox : this.getCollisionEntities()) {
      if (!canCollide(entity, collisionBox)) {
        continue;
      }

      if (collisionBox.getCollisionBox().contains(entityCollisionBox)) {
        return collisionBox.getCollisionBox();
      }

      if (GeometricUtilities.intersects(collisionBox.getCollisionBox(), entityCollisionBox)) {
        return collisionBox.getCollisionBox().createIntersection(entityCollisionBox);
      }
    }

    return null;
  }

  private boolean collides(final ICollisionEntity entity, Collision type, Predicate<ICollisionEntity> check) {
    for (final ICollisionEntity otherEntity : this.getCollisionEntities(type)) {
      if (!canCollide(entity, otherEntity)) {
        continue;
      }

      if (check.test(otherEntity)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if is in map.
   *
   * @param collisionBox
   *          the collision box
   * @return true, if is in map
   */
  private boolean isInMap(final Shape collisionBox) {
    if (this.environmentBounds == null) {
      return true;
    }

    return this.environmentBounds.contains(collisionBox.getBounds());
  }

  /**
   * With the current physics implementation is is possible to glitch through
   * other entities, if their collisionbox is smaller than the velocity of the
   * moving entity and they also move towards the currently moving entity.
   *
   * @param entity
   * @param targetPosition
   * @return
   */
  private Point2D resolveCollision(final ICollisionEntity entity, final Point2D targetPosition) {
    // first resolve x-axis movement
    Point2D resolvedPosition = new Point2D.Double(targetPosition.getX(), entity.getY());

    final Rectangle2D targetCollisionBoxX = entity.getCollisionBox(resolvedPosition);
    final Rectangle2D intersectionX = this.getIntersection(entity, targetCollisionBoxX);
    if (intersectionX != null) {
      if (entity.getCollisionBox().getX() < targetCollisionBoxX.getX()) {
        // entity was moved left -> right, so push out to the left
        resolvedPosition.setLocation(Math.max(entity.getX(), resolvedPosition.getX() - intersectionX.getWidth()), resolvedPosition.getY());
      } else {
        // push it out to the right
        resolvedPosition.setLocation(Math.min(entity.getX(), resolvedPosition.getX() + intersectionX.getWidth()), resolvedPosition.getY());
      }
    }

    // then resolve y-axis movement
    resolvedPosition.setLocation(resolvedPosition.getX(), targetPosition.getY());

    final Rectangle2D targetCollisionBoxY = entity.getCollisionBox(resolvedPosition);
    final Rectangle2D intersectionY = this.getIntersection(entity, targetCollisionBoxY);
    if (intersectionY != null) {
      if (entity.getCollisionBox().getY() < targetCollisionBoxY.getY()) {
        // entity was moved top -> bottom so push out towards the top
        resolvedPosition.setLocation(resolvedPosition.getX(), Math.max(entity.getY(), resolvedPosition.getY() - intersectionY.getHeight()));
      } else {
        resolvedPosition.setLocation(resolvedPosition.getX(), Math.min(entity.getY(), resolvedPosition.getY() + intersectionY.getHeight()));
      }
    }

    return resolvedPosition;
  }

  private Point2D clamptoMap(IMobileEntity entity, Point2D newLocation) {
    double collisionLocationX = entity.getCollisionBoxAlign().getLocation(entity.getWidth(), entity.getCollisionBoxWidth());
    double leftBoundX = this.getBounds().getMinX() - collisionLocationX;
    double deltaX = entity.getWidth() - entity.getCollisionBoxWidth() - collisionLocationX;
    double rightBoundX = this.getBounds().getMaxX() - entity.getWidth() + deltaX;

    double collisionLocationY = entity.getCollisionBoxValign().getLocation(entity.getHeight(), entity.getCollisionBoxHeight());
    double topBoundY = this.getBounds().getMinY() - collisionLocationY;
    double deltaY = entity.getHeight() - entity.getCollisionBoxHeight() - collisionLocationY;
    double buttomBoundY = this.getBounds().getMaxY() - entity.getHeight() + deltaY;

    // right and left border minus the collision box width
    double x = MathUtilities.clamp(newLocation.getX(), leftBoundX, rightBoundX);
    // bottom and top border minus the collision box height
    double y = MathUtilities.clamp(newLocation.getY(), topBoundY, buttomBoundY);
    return new Point2D.Double(x, y);
  }

  private boolean resolveCollisionForNewPosition(ICollisionEntity entity, Point2D location) {
    // resolve collision for new location
    if (this.collides(entity.getCollisionBox(location), entity)) {
      final Point2D resolvedPosition = this.resolveCollision(entity, location);
      entity.setLocation(resolvedPosition);
      return true;
    }

    return false;
  }

  private boolean resolveCollisionForRaycastToNewPosition(ICollisionEntity entity, Point2D newPosition) {
    // special case to prevent entities to glitch through collision boxes if
    // they have a large enough step size
    final Line2D line = new Line2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY(), entity.getCollisionBox(newPosition).getCenterX(), entity.getCollisionBox(newPosition).getCenterY());
    return this.collides(line, Collision.ANY, entity);
  }
}