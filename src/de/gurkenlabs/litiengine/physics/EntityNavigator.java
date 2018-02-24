package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.pathfinding.IPathFinder;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class EntityNavigator.
 */
public class EntityNavigator implements IEntityNavigator {

  /** The Constant ACCEPTABLE_ERROR. */
  private static final float ACCEPTABLE_ERROR = 0.3f;

  private final List<Predicate<IMobileEntity>> cancelNavigationConditions;
  /** The current segments. */
  private int currentSegment;
  private final IMobileEntity entity;

  /** The navigations. */
  private Path path;

  private final IPathFinder pathFinder;

  /**
   * Instantiates a new entity navigator.
   * 
   * @param entity
   *          The entity that will be navigated by this instance
   * @param pathFinder
   *          The pathfinder that is used to navigate the entity
   */
  public EntityNavigator(final IMobileEntity entity, final IPathFinder pathFinder) {
    this.cancelNavigationConditions = new CopyOnWriteArrayList<>();
    this.entity = entity;
    this.pathFinder = pathFinder;
    Game.getLoop().attach(this);
  }

  @Override
  public void cancelNavigation(final Predicate<IMobileEntity> predicate) {
    if (!this.cancelNavigationConditions.contains(predicate)) {
      this.cancelNavigationConditions.add(predicate);
    }
  }

  @Override
  public IMobileEntity getEntity() {
    return this.entity;
  }

  @Override
  public Path getPath() {
    return this.path;
  }

  @Override
  public IPathFinder getPathFinder() {
    return this.pathFinder;
  }

  @Override
  public boolean isNavigating() {
    return this.path != null;
  }

  @Override
  public void navigate(final Path2D path) {
    this.path = new Path(path);
  }

  @Override
  public void navigate(final Point2D target) {
    if (this.getPathFinder() != null) {
      this.path = this.getPathFinder().findPath(this.entity, target);
    }
  }

  @Override
  public void rotateTowards(final Point2D target) {
    final double angle = GeometricUtilities.calcRotationAngleInDegrees(this.entity.getCollisionBox().getCenterX(), this.entity.getCollisionBox().getCenterY(), target.getX(), target.getY());
    this.entity.setAngle((float) angle);
  }

  @Override
  public void stop() {
    this.currentSegment = 0;
    this.path = null;
  }

  @Override
  public void update() {
    if (!this.isNavigating()) {
      return;
    }

    if (this.path == null) {
      return;
    }

    for (final Predicate<IMobileEntity> pred : this.cancelNavigationConditions) {
      if (pred.test(this.getEntity())) {
        this.stop();
        return;
      }
    }

    final PathIterator pi = this.path.getPath().getPathIterator(null);
    if (pi.isDone()) {
      this.stop();
      return;
    }

    // although at max 6 elements are returned, sometimes the path
    // implementation tries to access index 20 ... don't know why, but this
    // prevents it
    final double[] startCoordinates = new double[22];
    final double[] coordinates = new double[22];
    for (int i = 0; i <= this.currentSegment; i++) {
      if (pi.isDone()) {
        this.stop();
        return;
      }

      pi.currentSegment(startCoordinates);
      pi.next();
    }

    if (pi.isDone()) {
      this.stop();
      return;
    }

    pi.currentSegment(coordinates);

    final double distance = GeometricUtilities.distance(this.entity.getCollisionBox().getCenterX(), this.entity.getCollisionBox().getCenterY(), coordinates[0], coordinates[1]);
    if (distance < ACCEPTABLE_ERROR) {
      ++this.currentSegment;
      return;
    }

    final double angle = GeometricUtilities.calcRotationAngleInDegrees(this.entity.getCollisionBox().getCenterX(), this.entity.getCollisionBox().getCenterY(), coordinates[0], coordinates[1]);
    final float pixelsPerTick = Game.getLoop().getDeltaTime() * 0.001f * this.entity.getVelocity() * Game.getLoop().getTimeScale();
    Game.getPhysicsEngine().move(this.entity, (float) angle, (float) (distance < pixelsPerTick ? distance : pixelsPerTick));
  }
}