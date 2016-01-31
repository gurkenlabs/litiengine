/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class EntityNavigator.
 */
public class EntityNavigator implements IEntityNavigator {

  /** The Constant ACCEPTABLE_ERROR. */
  private static final float ACCEPTABLE_ERROR = 0.1f;

  private final IMovableEntity entity;
  private final IPathFinder pathFinder;

  /** The current segments. */
  private int currentSegment;

  /** The navigations. */
  private Path path;

  /**
   * Instantiates a new entity navigator.
   */
  public EntityNavigator(final IMovableEntity entity, final IPathFinder pathFinder) {
    this.entity = entity;
    this.pathFinder = pathFinder;
    Game.registerForUpdate(this);
  }

  @Override
  public IPathFinder getPathFinder() {
    return this.pathFinder;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update() {
    if (this.path == null) {
      return;
    }

    this.navigateAlongPath();
  }

  /**
   * Navigate along path.
   *
   * @param entity
   *          the entity
   */
  private void navigateAlongPath() {
    int currentSegment = this.currentSegment;

    final PathIterator pi = this.path.getPath().getPathIterator(null);
    final double[] startCoordinates = new double[6];
    final double[] coordinates = new double[6];
    for (int i = 0; i <= currentSegment; i++) {
      pi.currentSegment(startCoordinates);
      pi.next();
    }

    if (pi.isDone()) {
      this.currentSegment = 0;
      this.path = null;
      return;
    }

    pi.currentSegment(coordinates);

    final double distance = GeometricUtilities.calcDistance(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY(), coordinates[0], coordinates[1]);
    if (distance < ACCEPTABLE_ERROR) {
      ++this.currentSegment;
      return;
    }

    final double angle = GeometricUtilities.calcRotationAngleInDegrees(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY(), coordinates[0], coordinates[1]);
    final float pixelsPerTick = entity.getVelocityInPixelsPerSecond() / Game.getConfiguration().CLIENT.getUpdaterate();
    Game.getPhysicsEngine().move(entity, angle, (float) (distance < pixelsPerTick ? distance : pixelsPerTick));
  }

  @Override
  public void navigate(Point2D target) {
    this.path = this.getPathFinder().findPath(this.entity, target);
  }

  @Override
  public Path getPath() {
    return this.path;
  }
}