package de.gurkenlabs.litiengine.pathfinding;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public abstract class PathFinder {
  private static final float PATH_MARGIN = 2.0f;

  /**
   * Gets the path.
   *
   * @param start
   *          the start
   * @param target
   *          the goal
   * @return the path
   */
  public abstract Path findPath(IMobileEntity start, Point2D target);

  protected Path findDirectPath(final Point2D start, final Point2D target) {
    final Path2D path2D = new GeneralPath(Path2D.WIND_NON_ZERO);
    path2D.moveTo(start.getX(), start.getY());
    path2D.lineTo(target.getX(), target.getY());

    final List<Point2D> points = new ArrayList<>();
    points.add(start);
    points.add(target);
    return new Path(start, target, path2D, points);
  }

  protected Rectangle2D applyPathMargin(final ICollisionEntity entity, final Rectangle2D rectangle) {
    // calculate offset in order to prevent collision
    final double newX = rectangle.getX() - (entity.getCollisionBox().getWidth() * 0.5 + PATH_MARGIN);
    final double newY = rectangle.getY() - (entity.getCollisionBox().getHeight() * 0.5 + PATH_MARGIN);
    final double newWidth = rectangle.getWidth() + entity.getCollisionBox().getWidth() + PATH_MARGIN * 2;
    final double newHeight = rectangle.getHeight() + entity.getCollisionBox().getHeight() + PATH_MARGIN * 2;
    return new Rectangle2D.Double(newX, newY, newWidth, newHeight);
  }

  protected boolean intersectsWithAnyCollisionBox(final ICollisionEntity entity, final Point2D start, final Point2D target) {
    final Collection<Rectangle2D> allCollisionBoxes = Game.physics().getCollisionBoxes();

    final Line2D line = new Line2D.Double(start, target);
    for (final Rectangle2D collisionBox : allCollisionBoxes) {
      if (collisionBox.equals(entity.getCollisionBox())) {
        continue;
      }

      // apply a margin for the path calculation in order to take the entities
      // collision box into consideration
      final Rectangle2D rectangleWithMargin = this.applyPathMargin(entity, collisionBox);

      // if the start is in the margin, the margin is not considered when
      // checking for collision because this will always return true
      Point2D intersection = GeometricUtilities.getIntersectionPoint(line, rectangleWithMargin.contains(start) ? collisionBox : rectangleWithMargin);
      if (intersection != null) {
        return true;
      }
    }

    return false;
  }
}
