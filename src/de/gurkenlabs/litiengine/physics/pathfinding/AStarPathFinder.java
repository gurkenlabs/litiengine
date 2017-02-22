package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.litiengine.physics.Path;
import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.util.geom.GeometricUtilities;

public class AStarPathFinder extends PathFinder {

  /**
   * TODO: Improve this optimization.
   *
   * @param path
   * @return
   */
  private static List<AStarNode> optimizePath(final List<AStarNode> path) {
    final List<AStarNode> optPath = new ArrayList<>();
    double oldAngle = 0;
    for (int i = 1; i < path.size(); i++) {

      final double angle = GeometricUtilities.calcRotationAngleInDegrees(path.get(i - 1).getLocation(), path.get(i).getLocation());
      if (angle != oldAngle) {
        optPath.add(path.get(i));
      }

      oldAngle = angle;
    }
    return optPath;
  }

  private final AStarGrid grid;

  public AStarPathFinder(final IPhysicsEngine physicsEngine, final IMap map) {
    this.grid = new AStarGrid(physicsEngine, map, map.getTileSize().width);
  }

  public AStarPathFinder(final IPhysicsEngine physicsEngine, final IMap map, final int gridNodeSize) {
    this.grid = new AStarGrid(physicsEngine, map, gridNodeSize);
  }

  @Override
  public Path findPath(final IMovableEntity entity, final Point2D target) {
    // if there is no collision between the start and the target return a direct
    // path
    final Point2D startLocation = new Point2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY());
    final Rectangle2D collisionBox = this.getFirstIntersectedCollisionBox(entity, startLocation, target);
    if (collisionBox == null) {
      return this.findDirectPath(startLocation, target);
    }

    final AStarNode startNode = this.getGrid().getNodeFromMapLocation(startLocation);
    final AStarNode targetNode = this.getGrid().getNodeFromMapLocation(target);
    if (startNode.equals(targetNode)) {
      return null;
    }

    // simple fallback if the target tile is not walkable.
    if (!targetNode.isWalkable()) {
      return this.findDirectPath(startLocation, target);
    }

    final List<AStarNode> opened = new ArrayList<>();
    final List<AStarNode> closed = new ArrayList<>();
    opened.add(startNode);

    while (opened.size() > 0) {
      AStarNode currentNode = opened.get(0);

      // find node with lowest cost
      for (int i = 1; i < opened.size(); i++) {
        if (opened.get(i).getfCost() < currentNode.getfCost() || opened.get(i).getfCost() == currentNode.getfCost() && opened.get(i).gethCost() < currentNode.gethCost()) {
          currentNode = opened.get(i);
        }
      }

      // add node to closed list after checking it
      opened.remove(currentNode);
      closed.add(currentNode);

      if (currentNode.equals(targetNode)) {
        return this.retracePath(startNode, targetNode);
      }

      // check all neighbors for the potential next one
      for (final AStarNode neighbour : this.grid.getNeighbours(currentNode)) {
        if (!neighbour.equals(targetNode) && !neighbour.isWalkable()) {
          continue;
        }

        if (closed.contains(neighbour)) {
          continue;
        }

        final int newgCostOfNeighbour = currentNode.getgCost() + currentNode.getCosts(neighbour);
        if (newgCostOfNeighbour < neighbour.getgCost() || !opened.contains(neighbour)) {
          neighbour.setgCost(newgCostOfNeighbour);
          neighbour.sethCost(neighbour.getCosts(targetNode));
          neighbour.setPredecessor(currentNode);

          if (!opened.contains(neighbour)) {
            opened.add(neighbour);
          }
        }
      }

    }
    return null;
  }

  public AStarGrid getGrid() {
    return this.grid;
  }

  private Path retracePath(final AStarNode startNode, final AStarNode targetNode) {
    final List<AStarNode> path = new ArrayList<>();
    AStarNode currentNode = targetNode.getPredecessor();

    while (currentNode != startNode) {
      path.add(currentNode);
      currentNode = currentNode.getPredecessor();
    }
    Collections.reverse(path);
    // path = optimizePath(path);

    final Path2D path2D = new GeneralPath(Path2D.WIND_NON_ZERO);
    path2D.moveTo(startNode.getLocation().x, startNode.getLocation().y);

    final List<Point2D> pointsOfPath = new ArrayList<>();
    for (int i = 0; i < path.size(); i++) {
      final AStarNode current = path.get(i);
      final Point currentPoint = new Point(current.getLocation().x, current.getLocation().y);
      pointsOfPath.add(currentPoint);
      path2D.lineTo(currentPoint.x, currentPoint.y);
    }

    path2D.lineTo(targetNode.getLocation().x, targetNode.getLocation().y);

    return new Path(startNode.getLocation(), targetNode.getLocation(), path2D, pointsOfPath);
  }

}
