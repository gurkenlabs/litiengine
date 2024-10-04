package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * The EntityNavigator class is responsible for navigating an entity along a path. It implements the IUpdateable and IRenderable interfaces.
 */
public class EntityNavigator implements IUpdateable, IRenderable {

  private static final float DEFAULT_ACCEPTABLE_ERROR = 0.3f;

  private final List<Predicate<IMobileEntity>> cancelNavigationConditions;
  private final List<NavigationListener> listeners;

  private final IMobileEntity entity;
  private final PathFinder pathFinder;

  private int currentSegment;
  private Path path;
  private float acceptableError;

  /**
   * Constructs an EntityNavigator with a specified entity and path finder.
   *
   * @param entity     the entity that will be navigated by this instance
   * @param pathFinder the path finder used to find paths for navigation
   */
  public EntityNavigator(final IMobileEntity entity, final PathFinder pathFinder) {
    this.cancelNavigationConditions = new CopyOnWriteArrayList<>();
    this.listeners = new CopyOnWriteArrayList<>();
    this.entity = entity;
    this.pathFinder = pathFinder;
    this.setAcceptableError(DEFAULT_ACCEPTABLE_ERROR);
    Game.loop().attach(this);
  }

  /**
   * Instantiates a new entity navigator without a pre-initialized PathFinder.
   *
   * @param entity The entity that will be navigated by this instance
   */
  public EntityNavigator(final IMobileEntity entity) {
    this(entity, null);
  }

  /**
   * Adds a navigation listener to this EntityNavigator.
   *
   * @param listener the NavigationListener to be added
   */
  public void addNavigationListener(NavigationListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes a navigation listener from this EntityNavigator.
   *
   * @param listener the NavigationListener to be removed
   */
  public void removeNavigationListener(NavigationListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Adds a condition to cancel the navigation if the specified predicate evaluates to true.
   *
   * @param predicate the condition to evaluate for canceling the navigation
   */
  public void cancelNavigation(final Predicate<IMobileEntity> predicate) {
    if (!this.cancelNavigationConditions.contains(predicate)) {
      this.cancelNavigationConditions.add(predicate);
    }
  }

  /**
   * Gets the entity being navigated.
   *
   * @return the entity being navigated
   */
  public IMobileEntity getEntity() {
    return entity;
  }

  /**
   * Gets the current path for navigation.
   *
   * @return the current path, or null if no path is set
   */
  public Path getPath() {
    return this.path;
  }

  /**
   * Gets the path finder used for navigation.
   *
   * @return the path finder, or null if no path finder is set
   */
  public PathFinder getPathFinder() {
    return this.pathFinder;
  }

  /**
   * Gets the acceptable error for navigation.
   *
   * @return the acceptable error
   */
  public float getAcceptableError() {
    return this.acceptableError;
  }

  /**
   * Checks if the entity is currently navigating.
   *
   * @return true if the entity is navigating, false otherwise
   */
  public boolean isNavigating() {
    return getPath() != null;
  }

  /**
   * Sets the current path for navigation.
   *
   * @param path the path to be navigated
   * @return true if the path is set successfully, false otherwise
   */
  public boolean navigate(final Path2D path) {
    this.path = new Path(path);
    return getPath() != null;
  }

  /**
   * Finds and sets a path to the specified target point.
   *
   * @param target the target point to navigate to
   * @return true if the path is found and set successfully, false otherwise
   */
  public boolean navigate(final Point2D target) {
    if (this.getPathFinder() != null) {
      this.path = getPathFinder().findPath(getEntity(), target);
    }

    return getPath() != null;
  }

  @Override
  public void render(Graphics2D g) {
    if (this.getPath() == null) {
      return;
    }

    g.setColor(Color.MAGENTA);
    Game.graphics().renderOutline(g, getPath().getPath());
  }

  /**
   * Rotates the entity towards the specified target point.
   *
   * @param target the target point to rotate towards
   */
  public void rotateTowards(final Point2D target) {
    final double angle =
      GeometricUtilities.calcRotationAngleInDegrees(
        getEntity().getCollisionBox().getCenterX(),
        getEntity().getCollisionBox().getCenterY(),
        target.getX(),
        target.getY());
    getEntity().setAngle((float) angle);
  }

  /**
   * Sets the acceptable error for navigation.
   *
   * @param acceptableError the acceptable error to set
   */
  public void setAcceptableError(float acceptableError) {
    this.acceptableError = acceptableError;
  }

  /**
   * Stops the navigation and resets the current segment and path.
   */
  public void stop() {
    this.currentSegment = 0;
    this.path = null;

    for (NavigationListener listener : this.listeners) {
      listener.stopped();
    }
  }

  @Override
  public void update() {
    if (!this.isNavigating()) {
      return;
    }

    if (getPath() == null) {
      return;
    }

    for (final Predicate<IMobileEntity> pred : this.cancelNavigationConditions) {
      if (pred.test(getEntity())) {
        stop();
        return;
      }
    }

    final PathIterator pi = getPath().getPath().getPathIterator(null);
    if (pi.isDone()) {
      stop();
      return;
    }

    // although at max 6 elements are returned, sometimes the path
    // implementation tries to access index 20 ... don't know why, but this
    // prevents it
    final double[] startCoordinates = new double[22];
    final double[] coordinates = new double[22];
    for (int i = 0; i <= this.currentSegment; i++) {
      if (pi.isDone()) {
        stop();
        return;
      }

      pi.currentSegment(startCoordinates);
      pi.next();
    }

    if (pi.isDone()) {
      stop();
      return;
    }

    pi.currentSegment(coordinates);

    final double distance =
      GeometricUtilities.distance(
        getEntity().getCollisionBox().getCenterX(),
        getEntity().getCollisionBox().getCenterY(),
        coordinates[0],
        coordinates[1]);
    if (distance < getAcceptableError()) {
      ++this.currentSegment;
      return;
    }

    final double angle =
      GeometricUtilities.calcRotationAngleInDegrees(
        getEntity().getCollisionBox().getCenterX(),
        getEntity().getCollisionBox().getCenterY(),
        coordinates[0],
        coordinates[1]);
    final float pixelsPerTick = getEntity().getTickVelocity();
    Game.physics()
      .move(
        getEntity(),
        (float) angle,
        (float) (distance < pixelsPerTick ? distance : pixelsPerTick));
  }
}
