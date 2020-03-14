package de.gurkenlabs.litiengine.entities.behavior;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;

public class AStarPathFinder extends PathFinder {

  private final AStarGrid grid;

  public AStarPathFinder(AStarGrid grid) {
    this.grid = grid;
  }

  public AStarPathFinder(Dimension size, int gridNodeSize) {
    this.grid = new AStarGrid(size, gridNodeSize);
  }

  public AStarPathFinder(final IMap map, final int gridNodeSize) {
    this(map.getSizeInPixels(), gridNodeSize);
  }

  public AStarPathFinder(final IMap map) {
    this(map.getSizeInPixels(), map.getTileSize().width);
  }

  @Override
  public Path findPath(final IMobileEntity entity, final Point2D target) {
    // if there is no collision between the start and the target return a direct
    // path
    final Point2D startLocation = entity.getCollisionBoxCenter();
    if (!this.intersectsWithAnyCollisionBox(entity, startLocation, target)) {
      return this.findDirectPath(startLocation, target);
    }

    final AStarNode startNode = this.getGrid().getNode(startLocation);
    AStarNode targetNode = this.getGrid().getNode(target);
    if (startNode.equals(targetNode)) {
      return null;
    }

    // simple fallback if the target tile is not walkable.
    boolean gotoNeighbor = false;
    if (!targetNode.isWalkable()) {
      for (AStarNode neighbor : this.getGrid().getNeighbors(targetNode)) {
        if (neighbor.isWalkable()) {
          targetNode = neighbor;
          gotoNeighbor = true;
          break;
        }
      }

      if (!gotoNeighbor) {
        return this.findDirectPath(startLocation, target);
      }
    }

    if (gotoNeighbor && startNode.equals(targetNode)) {
      return null;
    }

    return this.findAStarPath(startNode, targetNode);
  }

  public AStarGrid getGrid() {
    return this.grid;
  }

  private Path findAStarPath(AStarNode startNode, AStarNode targetNode) {
    final List<AStarNode> opened = new ArrayList<>();
    final List<AStarNode> closed = new ArrayList<>();
    opened.add(startNode);

    while (!opened.isEmpty()) {
      // after the first iteration, this will also contained the newly found neighbors that were added in the last iteration
      AStarNode currentNode = findNodeWithLowestCost(opened);

      // add node to closed list after checking it
      opened.remove(currentNode);
      closed.add(currentNode);

      // when the currentNode reaches the targetNode, we've found the path
      if (currentNode.equals(targetNode)) {
        Path path = retracePath(startNode, targetNode);
        clear(opened);
        clear(closed);
        return path;
      }

      this.updateAndOpenNeighborNodes(currentNode, targetNode, opened, closed);
    }

    clear(opened);
    clear(closed);
    return null;
  }

  /**
   * Updates the costs and the predecessor of all neighbors of the specified <code>currentNode</code>.<br>
   * If a neighbor was previously not part of the <code>opened</code> list it will be added to it.<br>
   * If a neighbor is already closed, it will be ignored.<br>
   * If the {@link AStarNode#isWalkable()} method of a neighbor returns <code>false</code> it will also not be considered.
   * 
   * @param currentNode
   *          The node for which the neighbors will be searched for.
   * @param targetNode
   *          The target node of the path-finding operation.
   * @param opened
   *          The list of all the opened nodes of the path-finding operation.
   * @param closed
   *          The list of all the closed nodes of the path-finding operation.
   */
  private void updateAndOpenNeighborNodes(AStarNode currentNode, AStarNode targetNode, List<AStarNode> opened, List<AStarNode> closed) {
    // check all neighbors for the potential next one
    for (final AStarNode neighbor : this.grid.getNeighbors(currentNode)) {
      if (!neighbor.equals(targetNode) && !neighbor.isWalkable() || closed.contains(neighbor)) {
        continue;
      }

      final double newGCostOfNeighbor = currentNode.getGCost() + currentNode.getCosts(neighbor);
      if (newGCostOfNeighbor < neighbor.getGCost() || !opened.contains(neighbor)) {
        neighbor.setGCost(newGCostOfNeighbor);
        neighbor.setHCost(neighbor.getCosts(targetNode));
        neighbor.setPredecessor(currentNode);

        if (!opened.contains(neighbor)) {
          opened.add(neighbor);
        }
      }
    }
  }

  private static AStarNode findNodeWithLowestCost(List<AStarNode> openedNodes) {
    AStarNode lowestCostNode = openedNodes.get(0);
    // find node with lowest cost
    // F-cost (aka. total costs) are considered first. If they are equal, the H-cost is checked subsequently
    for (int i = 1; i < openedNodes.size(); i++) {
      if (openedNodes.get(i).getFCost() < lowestCostNode.getFCost() || openedNodes.get(i).getFCost() == lowestCostNode.getFCost() && openedNodes.get(i).getHCost() < lowestCostNode.getHCost()) {
        lowestCostNode = openedNodes.get(i);
      }
    }

    return lowestCostNode;
  }

  private static void clear(List<AStarNode> nodes) {
    for (AStarNode op : nodes) {
      op.clear();
    }
  }

  /**
   * Retraces the found path from the targetNode back to the startNode by making use of the {@link AStarNode#getPredecessor()}.
   * <ol>
   * <li>Adds all predecessors to a list of nodes that will be visited by the path.</li>
   * <li>Invert the list.</li>
   * <li>Create a new {@link Path2D} by iterating all nodes in the list.</li>
   * <li>Wrap the {@link Path2D} object into a {@link Path} to provide information about the start, target and points of the path.</li>
   * </ol>
   * 
   * @param startNode
   *          The start node for the path.
   * @param targetNode
   *          The target node for the path.
   * @return The found {@link Path}
   */
  private static Path retracePath(final AStarNode startNode, final AStarNode targetNode) {
    final List<AStarNode> path = new ArrayList<>();
    AStarNode currentNode = targetNode.getPredecessor();

    while (currentNode != startNode) {
      path.add(currentNode);
      currentNode = currentNode.getPredecessor();
    }
    Collections.reverse(path);

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
