package de.gurkenlabs.litiengine.physics;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
  private final List<ICollisionEntity> collisionEntities;

  private Rectangle2D environmentBounds;

  private final List<Rectangle2D> staticCollisionBoxes;

  private final List<CollisionBox> dynamicCollisionBoxes;
  private final List<CollisionBox> allCollisionBoxes;
  private final List<CollisionBox> staticBoxes;
  
  private final List<Rectangle2D> allCollisionBoxRectangles;
  private final List<Rectangle2D> dynamicCollisionBoxRectangles;

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
    this.collisionEntities = new CopyOnWriteArrayList<>();
    this.staticCollisionBoxes = new CopyOnWriteArrayList<>();

    // these collections are updated every tick so they don't use a CopyOnWriteArrayList due to performance
    this.dynamicCollisionBoxes = Collections.synchronizedList(new ArrayList<>());
    this.allCollisionBoxes = Collections.synchronizedList(new ArrayList<>());
    this.staticBoxes = Collections.synchronizedList(new ArrayList<>());
    this.allCollisionBoxRectangles = Collections.synchronizedList(new ArrayList<>());
    this.dynamicCollisionBoxRectangles = Collections.synchronizedList(new ArrayList<>());
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

    // special handling for making props be handled like static collision boxes.
    if (entity.getCollisionType() == CollisionType.STATIC) {

      this.add(entity.getCollisionBox());
      return;
    }

    if (!this.collisionEntities.contains(entity)) {
      this.collisionEntities.add(entity);
    }
  }

  /**
   * Removes the specified entity from any collision processing. Typically this method is implicitly called when an entity is removed from the current
   * environment.
   * 
   * @param entity
   *          The entity that is about to be removed.
   */
  public void remove(final ICollisionEntity entity) {
    if (entity.getCollisionType() == CollisionType.STATIC) {
      this.remove(entity.getCollisionBox());
      return;
    }

    this.collisionEntities.remove(entity);
  }

  /**
   * Clears all previously registered participants in the collision process from this instance.
   * This includes all entities, static collision boxes and the map boundaries.
   */
  public void clear() {
    this.collisionEntities.clear();
    this.staticCollisionBoxes.clear();
    this.allCollisionBoxes.clear();
    this.staticBoxes.clear();
    this.allCollisionBoxRectangles.clear();
    this.dynamicCollisionBoxRectangles.clear();
    this.setBounds(null);
  }

  public List<Rectangle2D> getAllCollisionBoxes() {
    return this.getAllCollisionBoxRectangles();
  }

  public List<ICollisionEntity> getCollisionEntities() {
    return this.collisionEntities;
  }

  public List<Rectangle2D> getStaticCollisionBoxes() {
    return this.staticCollisionBoxes;
  }

  public Rectangle2D getBounds() {
    return this.environmentBounds;
  }

  public void setBounds(final Rectangle2D environmentBounds) {
    this.environmentBounds = environmentBounds;
  }

  public boolean collides(final double x, final double y) {
    return this.collides(new Point2D.Double(x, y));
  }

  public boolean collides(double x, double y, CollisionType collisionType) {
    return this.collides(new Point2D.Double(x, y), collisionType);
  }

  public boolean collides(double x, double y, ICollisionEntity collisionEntity) {
    return collides(new Point2D.Double(x, y), collisionEntity);
  }

  public boolean collides(Point2D point, ICollisionEntity collisionEntity) {
    return this.collidesWithAnyEntity(collisionEntity, point) || this.collidesWithAnyStaticCollisionBox(point);
  }

  public boolean collides(Point2D point, CollisionType collisionType) {
    switch (collisionType) {
    case ALL:
      return this.collides(point);
    case DYNAMIC:
      return this.collidesWithAnyEntity(null, point);
    case STATIC:
      return this.collidesWithAnyStaticCollisionBox(point);
    default:
      return false;
    }
  }

  public Point2D collides(Line2D rayCast, CollisionType collisionType) {
    final Point2D rayCastSource = new Point2D.Double(rayCast.getX1(), rayCast.getY1());

    final List<Rectangle2D> collBoxes = this.getAllCollisionBoxRectangles(collisionType);
    for (final Rectangle2D collisionBox : collBoxes) {
      if (collisionBox.intersectsLine(rayCast)) {
        double closestDist = -1;
        Point2D closestPoint = null;
        for (final Point2D intersection : GeometricUtilities.getIntersectionPoints(rayCast, collisionBox)) {
          final double dist = intersection.distance(rayCastSource);
          if (closestPoint == null || dist < closestDist) {
            closestPoint = intersection;
            closestDist = dist;
          }
        }

        return closestPoint;
      }
    }

    return null;
  }

  public Point2D collides(final Line2D rayCast) {
    return this.collides(rayCast, CollisionType.ALL);
  }

  public boolean collides(final Point2D point) {
    if (this.environmentBounds != null && !this.environmentBounds.contains(point)) {
      return true;
    }

    for (final Rectangle2D collisionBox : this.getAllCollisionBoxes()) {
      if (collisionBox.contains(point)) {
        return true;
      }
    }

    return false;
  }

  public boolean collides(final Rectangle2D rect) {
    for (final Rectangle2D collisionBox : this.getAllCollisionBoxes()) {
      if (GeometricUtilities.intersects(rect, collisionBox)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether the specified rectangle collides with anything.
   * 
   * @param rect
   *          The rectangle to check the collision for.
   * @param collisionType
   *          use the following flags
   *          <ul>
   *          <li>COLLTYPE_ENTITY</li>
   *          <li>COLLTYPE_STATIC</li>
   *          <li>COLLTYPE_ALL</li>
   *          </ul>
   * @return Returns true if the specified rectangle collides with any collision
   *         box of the specified type(s); otherwise false.
   */
  public boolean collides(final Rectangle2D rect, final CollisionType collisionType) {
    return collides(rect, null, collisionType);
  }

  public boolean collides(Rectangle2D rect, ICollisionEntity collisionEntity) {
    return this.collides(rect, collisionEntity, CollisionType.ALL);
  }

  public boolean collides(Rectangle2D rect, ICollisionEntity collisionEntity, CollisionType collisionType) {
    switch (collisionType) {
    case ALL:
      return this.collides(rect);
    case DYNAMIC:
      return this.collidesWithAnyEntity(collisionEntity, rect) != null;
    case STATIC:
      return this.collidesWithAnyStaticCollisionBox(rect) != null;
    default:
      return false;
    }
  }

  public boolean collides(ICollisionEntity collisionEntity) {
    return this.collides(collisionEntity, CollisionType.ALL);
  }

  public boolean collides(ICollisionEntity collisionEntity, CollisionType collisionType) {
    return this.collides(collisionEntity.getCollisionBox(), collisionEntity, collisionType);
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
    this.updateAllCollisionBoxes();
  }

  private List<CollisionBox> getAllCollisionBoxesInternal() {
    if (this.allCollisionBoxes.isEmpty()) {
      this.updateAllCollisionBoxes();
    }

    return this.allCollisionBoxes;
  }

  private List<Rectangle2D> getAllCollisionBoxRectangles() {
    if (this.allCollisionBoxRectangles.isEmpty()) {
      this.updateAllCollisionBoxes();
    }

    return this.allCollisionBoxRectangles;
  }

  private List<Rectangle2D> getAllCollisionBoxRectangles(CollisionType collisionType) {
    switch (collisionType) {
    case DYNAMIC:
      return this.dynamicCollisionBoxRectangles;
    case STATIC:
      return this.staticCollisionBoxes;
    default:
      return getAllCollisionBoxRectangles();
    }
  }

  private void updateAllCollisionBoxes() {
    this.allCollisionBoxes.clear();
    this.dynamicCollisionBoxes.clear();
    this.staticBoxes.clear();

    this.dynamicCollisionBoxes.addAll(this.collisionEntities.stream().filter(ICollisionEntity::hasCollision).map(CollisionBox::new).collect(Collectors.toList()));
    this.staticBoxes.addAll(this.staticCollisionBoxes.stream().map(CollisionBox::new).collect(Collectors.toList()));

    this.allCollisionBoxes.addAll(dynamicCollisionBoxes);
    this.allCollisionBoxes.addAll(this.staticBoxes);

    this.allCollisionBoxRectangles.clear();
    this.dynamicCollisionBoxRectangles.clear();
    this.dynamicCollisionBoxRectangles.addAll(this.dynamicCollisionBoxes.stream().map(CollisionBox::getCollisionBox).collect(Collectors.toList()));

    this.allCollisionBoxRectangles.addAll(this.allCollisionBoxes.stream().map(CollisionBox::getCollisionBox).collect(Collectors.toList()));
  }

  /**
   * Collides with any entity.
   *
   * @param entity
   *          the entity
   * @param newPosition
   *          the new position
   * @return true, if successful
   */
  private Rectangle2D collidesWithAnyEntity(final ICollisionEntity entity, final Rectangle2D collisionBox) {
    for (final ICollisionEntity otherEntity : this.collisionEntities) {
      if (otherEntity == null || !otherEntity.hasCollision() || entity != null && otherEntity.equals(entity) || entity != null && !entity.canCollideWith(otherEntity)) {
        continue;
      }

      if (GeometricUtilities.intersects(otherEntity.getCollisionBox(), collisionBox)) {
        return otherEntity.getCollisionBox().createIntersection(collisionBox);
      }
    }

    return null;
  }

  private boolean collidesWithAnyEntity(final ICollisionEntity entity, final Point2D location) {
    for (final ICollisionEntity otherEntity : this.collisionEntities) {
      if (otherEntity == null || !otherEntity.hasCollision() || entity != null && otherEntity.equals(entity) || entity != null && !entity.canCollideWith(otherEntity)) {
        continue;
      }

      if (otherEntity.getCollisionBox().contains(location)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Collides with any map object.
   *
   * @param entity
   *          the entity
   * @param newPosition
   *          the new position
   * @return true, if successful
   */
  private Rectangle2D collidesWithAnyStaticCollisionBox(final Rectangle2D entityCollisionBox) {
    for (final Rectangle2D collisionBox : this.staticCollisionBoxes) {
      if (GeometricUtilities.intersects(collisionBox, entityCollisionBox)) {
        return collisionBox.createIntersection(entityCollisionBox);
      }
    }

    return null;
  }

  private boolean collidesWithAnyStaticCollisionBox(final Point2D location) {
    for (final Rectangle2D collisionBox : this.staticCollisionBoxes) {
      if (collisionBox.contains(location)) {
        return true;
      }
    }

    return false;
  }

  private Rectangle2D collidesWithAnything(final ICollisionEntity entity, final Rectangle2D entityCollisionBox) {
    for (final CollisionBox collisionBox : this.allCollisionBoxes) {

      // an entity cannot collide with itself or other entities that are
      // excluded from collision by the canCollideWith method
      if (collisionBox.getEntity() != null && (collisionBox.getEntity().equals(entity) || !entity.canCollideWith(collisionBox.getEntity()))) {
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
  private Point2D resolveCollision(final IMobileEntity entity, final Point2D targetPosition) {
    // first resolve x-axis movement
    Point2D resolvedPosition = new Point2D.Double(targetPosition.getX(), entity.getY());

    final Rectangle2D targetCollisionBoxX = entity.getCollisionBox(resolvedPosition);
    final Rectangle2D intersectionX = this.collidesWithAnything(entity, targetCollisionBoxX);
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
    final Rectangle2D intersectionY = this.collidesWithAnything(entity, targetCollisionBoxY);
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

  private boolean resolveCollisionForNewPosition(IMobileEntity entity, Point2D location) {
    // resolve collision for new location
    if (this.collidesWithAnything(entity, entity.getCollisionBox(location)) != null) {
      final Point2D resolvedPosition = this.resolveCollision(entity, location);
      entity.setLocation(resolvedPosition);
      return true;
    }

    return false;
  }

  private boolean resolveCollisionForRaycastToNewPosition(IMobileEntity entity, Point2D newPosition) {
    // special case to prevent entities to glitch through collision boxes if
    // they have a large enough step size
    final Line2D line = new Line2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY(), entity.getCollisionBox(newPosition).getCenterX(), entity.getCollisionBox(newPosition).getCenterY());
    for (final CollisionBox collisionBox : this.getAllCollisionBoxesInternal()) {
      if (collisionBox.getEntity() != null && (collisionBox.getEntity().equals(entity) || !entity.canCollideWith(collisionBox.getEntity()))) {
        continue;
      }

      // there was a collision in between
      final Point2D intersection = GeometricUtilities.getIntersectionPoint(line, collisionBox.getCollisionBox());
      if (intersection != null) {
        return true;
      }
    }

    return false;
  }
  
  /**
   * Adds the specified static collision box to the physics engine.
   * 
   * @param staticCollisionBox
   *          The static collision box to be added.
   */
  private void add(final Rectangle2D staticCollisionBox) {
    if (!this.staticCollisionBoxes.contains(staticCollisionBox)) {
      this.staticCollisionBoxes.add(staticCollisionBox);
    }
  }

  /**
   * Removes the specified static collision box.
   * 
   * @param staticCollisionBox
   *          The static collision box that is about to be removed.
   */
  private void remove(final Rectangle2D staticCollisionBox) {
    this.staticCollisionBoxes.remove(staticCollisionBox);
  }


  private class CollisionBox {
    private final Rectangle2D box;

    private final ICollisionEntity entity;

    private CollisionBox(Rectangle2D box) {
      this.box = box;
      this.entity = null;
    }

    private CollisionBox(ICollisionEntity entity) {
      this.box = entity.getCollisionBox();
      this.entity = entity;
    }

    public Rectangle2D getCollisionBox() {
      return this.box;
    }

    public ICollisionEntity getEntity() {
      return this.entity;
    }
  }
}