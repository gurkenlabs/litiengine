/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.Path;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.geom.PointDistanceComparator;

// TODO: Auto-generated Javadoc
/**
 * The Class PathFinder.
 */
public class GeometricPathFinder extends PathFinder {

  /**
   * Checks if is in rectangle.
   *
   * @param point
   *          the point
   * @param rect
   *          the rect
   * @return true, if is in rectangle
   */
  private static boolean isInRectangle(final Point2D point, final Rectangle2D rect) {
    return point.getX() >= rect.getMinX() && point.getX() <= rect.getMaxX() && point.getY() >= rect.getMinY() && point.getY() <= rect.getMaxY();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.physics.IPathFinder#getPath(de.gurkenlabs.liti.entities.
   * Entity, java.awt.geom.Point2D)
   */
  @Override
  public Path findPath(final IMovableEntity entity, final Point2D target) {
    // if there is no collision between the start and the target return a direct
    // path
    final Point2D startLocation = new Point2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY());
    final Rectangle2D collisionBox = this.getFirstIntersectedCollisionBox(entity, startLocation, target);
    if (collisionBox == null) {
      return this.findDirectPath(startLocation, target);
    }

    final List<Point2D> pointsOfPath = new ArrayList<>();
    final Path2D path = new GeneralPath(Path2D.WIND_NON_ZERO);

    // start at the center location of the entities collision box
    final Point2D start = new Point2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY());
    Point2D currentPoint = start;
    path.moveTo(currentPoint.getX(), currentPoint.getY());
    pointsOfPath.add(currentPoint);
    boolean pathFinished = false;

    while (!pathFinished) {

      final Point2D nextPoint = this.getNextPoint(entity, currentPoint, target);
      if (nextPoint == null) {
        pathFinished = true;
        continue;
      }

      if (pointsOfPath.contains(nextPoint)) {
        pathFinished = true;
        continue;
      }

      path.lineTo(nextPoint.getX(), nextPoint.getY());
      currentPoint = nextPoint;
      pointsOfPath.add(currentPoint);
    }

    if (!pointsOfPath.contains(target)) {
      path.lineTo(target.getX(), target.getY());
      pointsOfPath.add(target);
    }

    return new Path(start, target, path, pointsOfPath);
  }

  private Point2D getNextPoint(final IMovableEntity entity, final Point2D currentPoint, final Point2D target) {
    // 1. get first intersected collision box
    final Rectangle2D currentCollisionBox = this.getFirstIntersectedCollisionBox(entity, currentPoint, target);
    if (currentCollisionBox == null || currentCollisionBox.contains(target)) {
      // if no collision box is found, we can directly go to the target
      return target;
    }

    // 2. if the current point is within the collisionbox navigate to the
    // closest
    // corner first
    if (isInRectangle(currentPoint, currentCollisionBox)) {
      final ArrayList<Point2D> collisionBoxPoints = GeometricUtilities.getPoints(currentCollisionBox);
      collisionBoxPoints.sort(new PointDistanceComparator(currentPoint));
      return collisionBoxPoints.get(0);
    }

    // 3. get all visible corners of the collision box
    final Point2D[] possiblePoints = GeometricUtilities.rayCastPoints(currentPoint, currentCollisionBox);
    if (possiblePoints == null || possiblePoints.length == 0) {
      return target;
    }

    // greedy path creation
    Arrays.sort(possiblePoints, new PointDistanceComparator(target));
    for (final Point2D possiblePoint : possiblePoints) {
      // if the direct path to a point intersects another collision box, skip
      // this point and take another
      final Rectangle2D collisionBox = this.getFirstIntersectedCollisionBox(entity, currentPoint, possiblePoint);
      if (collisionBox != null && !collisionBox.equals(currentCollisionBox)) {
        continue;
      }

      return possiblePoint;
    }

    return null;
  }
}
