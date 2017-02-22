package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.physics.Path;
import de.gurkenlabs.util.geom.GeometricUtilities;

public abstract class PathFinder implements IPathFinder {
  public Path findDirectPath(final Point2D start, final Point2D target) {
    final Path2D path2D = new GeneralPath(Path2D.WIND_NON_ZERO);
    path2D.moveTo(start.getX(), start.getY());
    path2D.lineTo(target.getX(), target.getY());

    final List<Point2D> points = new ArrayList<>();
    points.add(start);
    points.add(target);
    return new Path(start, target, path2D, points);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.physics.IPathFinder#applyPathMargin(de.gurkenlabs.liti.
   * entities.Entity, java.awt.geom.Rectangle2D)
   */
  protected Rectangle2D applyPathMargin(final ICollisionEntity entity, final Rectangle2D rectangle) {
    final float Margin = 2.0f;
    // calculate offset in order to prevent collision
    final double newX = rectangle.getX() - (entity.getCollisionBox().getWidth() * 0.5 + Margin);
    final double newY = rectangle.getY() - (entity.getCollisionBox().getHeight() * 0.5 + Margin);
    final double newWidth = rectangle.getWidth() + entity.getCollisionBox().getWidth() + Margin * 2;
    final double newHeight = rectangle.getHeight() + entity.getCollisionBox().getHeight() + Margin * 2;
    return new Rectangle2D.Double(newX, newY, newWidth, newHeight);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.physics.IPhysicsEngine#getFirstIntersectedCollisionBox(
   * de.gurkenlabs.liti.entities.Entity, java.awt.geom.Point2D,
   * java.awt.geom.Point2D)
   */
  protected Rectangle2D getFirstIntersectedCollisionBox(final ICollisionEntity entity, final Point2D start, final Point2D target) {
    final List<Rectangle2D> allCollisionBoxes = Game.getPhysicsEngine().getAllCollisionBoxes();

    final Line2D line = new Line2D.Double(start, target);
    final HashMap<Rectangle2D, Point2D> intersectedShapes = new HashMap<>();
    for (final Rectangle2D collisionBox : allCollisionBoxes) {
      if (collisionBox.equals(entity.getCollisionBox())) {
        continue;
      }

      // apply a margin for the path calculation in order to take the entities
      // collision box into consideration
      final Rectangle2D rectangleWithMargin = this.applyPathMargin(entity, collisionBox);

      // if the start is in the margin, the margin is not considered when
      // checking for collision because this will always return true
      Point2D intersection = null;
      if (rectangleWithMargin.contains(start)) {
        intersection = GeometricUtilities.getIntersectionPoint(line, collisionBox);
        if (intersection != null) {
          intersectedShapes.put(rectangleWithMargin, intersection);
        }
      } else {
        intersection = GeometricUtilities.getIntersectionPoint(line, rectangleWithMargin);
      }

      if (intersection != null) {
        intersectedShapes.put(rectangleWithMargin, intersection);
      }
    }

    Rectangle2D min = null;
    double minDist = 0;
    for (final Entry<Rectangle2D, Point2D> entry : intersectedShapes.entrySet()) {
      final Rectangle2D shape = entry.getKey();
      final Point2D intersection = entry.getValue();
      final double dist = intersection.distance(target);
      if (min == null) {
        min = shape;
        minDist = dist;
        continue;
      }

      if (dist < minDist) {
        min = shape;
      }
    }

    return min;
  }
}
