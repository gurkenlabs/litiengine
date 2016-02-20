package de.gurkenlabs.litiengine.physics.pathfinding;

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
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.util.geom.GeometricUtilities;

public class AStarPathFinder extends PathFinder {
  private static final int GRID_SIZE = 16;
  private final AStarGrid grid;

  public AStarPathFinder(final IPhysicsEngine physicsEngine, final IMap map) {
    this.grid = new AStarGrid(physicsEngine, map, GRID_SIZE);
  }

  @Override
  public Path findPath(IMovableEntity entity, Point2D target) {
    // if there is no collision between the start and the target return a direct
    // path
    Point2D startLocation = new Point2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY());

    AStarNode startNode = this.grid.getNodeFromMapLocation(startLocation);
    AStarNode targetNode = this.grid.getNodeFromMapLocation(target);

    if (startNode.equals(targetNode) || !targetNode.isWalkable()) {
      return null;
    }

    List<AStarNode> opened = new ArrayList<>();
    List<AStarNode> closed = new ArrayList<>();
    opened.add(startNode);

    while (opened.size() > 0) {
      AStarNode currentNode = opened.get(0);

      // find node with lowest cost
      for (int i = 1; i < opened.size(); i++) {
        if (opened.get(i).getfCost() < currentNode.getfCost() 
            || (opened.get(i).getfCost() == currentNode.getfCost() && opened.get(i).gethCost() < currentNode.gethCost())) {
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

        int newgCostOfNeighbour = currentNode.getgCost() + currentNode.getCosts(neighbour);
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

  private Path retracePath(AStarNode startNode, AStarNode targetNode) {
    List<AStarNode> path = new ArrayList<>();
    AStarNode currentNode = targetNode.getPredecessor();

    while (currentNode != startNode) {
      path.add(currentNode);
      currentNode = currentNode.getPredecessor();
    }
    Collections.reverse(path);
    // path = optimizePath(path);

    final Path2D path2D = new GeneralPath(Path2D.WIND_NON_ZERO);
    path2D.moveTo(startNode.getLocation().x, startNode.getLocation().y);

    for (int i = 0; i < path.size(); i++) {
      AStarNode current = path.get(i);
      path2D.lineTo(current.getLocation().x, current.getLocation().y);
    }

    path2D.lineTo(targetNode.getLocation().x, targetNode.getLocation().y);

    return new Path(startNode.getLocation(), targetNode.getLocation(), path2D);
  }

  private static List<AStarNode> optimizePath(final List<AStarNode> path) {
    List<AStarNode> optPath = new ArrayList<>();
    double oldAngle = 0;
    for (int i = 1; i < path.size(); i++) {
      double angle = GeometricUtilities.calcRotationAngleInDegrees(path.get(i - 1).getLocation(), path.get(i).getLocation());
      if (angle != oldAngle) {
        optPath.add(path.get(i));
      }

      oldAngle = angle;
    }
    return optPath;
  }

}
