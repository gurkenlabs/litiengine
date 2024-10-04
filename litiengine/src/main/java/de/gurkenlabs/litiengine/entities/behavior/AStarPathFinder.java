package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A pathfinder implementation based on the A* algorithm. The A* algorithm is used to find the shortest path between two points on a grid, taking into
 * account obstacles and walkable areas.
 */
public class AStarPathFinder extends PathFinder {

  private final AStarGrid grid;

  /**
   * Instantiates a new A* pathfinder with a predefined grid.
   *
   * @param grid the grid used for pathfinding
   */
  public AStarPathFinder(AStarGrid grid) {
    this.grid = grid;
  }

  /**
   * Instantiates a new A* pathfinder with a grid of the specified size and node size.
   *
   * @param size         the dimensions of the grid
   * @param gridNodeSize the size of each grid node
   */
  public AStarPathFinder(Dimension size, int gridNodeSize) {
    this.grid = new AStarGrid(size, gridNodeSize);
  }

  /**
   * Instantiates a new A* pathfinder using the map's size and a specified grid node size.
   *
   * @param map          the map used for pathfinding
   * @param gridNodeSize the size of each grid node
   */
  public AStarPathFinder(final IMap map, final int gridNodeSize) {
    this(map.getSizeInPixels(), gridNodeSize);
  }

  /**
   * Instantiates a new A* pathfinder using the map's size and the map's tile size as the grid node size.
   *
   * @param map the map used for pathfinding
   */
  public AStarPathFinder(final IMap map) {
    this(map.getSizeInPixels(), map.getTileSize().width);
  }

  /**
   * Finds a path from the entity's current position to the target using the A* algorithm. If no obstacles are present between the start and the
   * target, a direct path is used.
   *
   * @param entity the mobile entity for which the path is calculated
   * @param target the target point of the path
   * @return the calculated path, or null if no path can be found
   */
  @Override public Path findPath(final IMobileEntity entity, final Point2D target) {
    // if there is no collision between the start and the target return a direct
    // path
    final Point2D startLocation = entity.getCollisionBoxCenter();
    if (!this.intersectsWithAnyCollisionBox(entity, startLocation, target)) {
      return this.findDirectPath(startLocation, target);
    }

    final AStarNode startNode = this.getGrid().getNode(startLocation);
    AStarNode targetNode = this.getGrid().getNode(target);
    if (startNode.equals(targetNode) || targetNode == null) {
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

  /**
   * Gets the grid used by this A* pathfinder.
   *
   * @return the grid used for pathfinding
   */
  public AStarGrid getGrid() {
    return this.grid;
  }

  /**
   * Finds the path from the start node to the target node using the A* algorithm. Opens and closes nodes during the process to determine the shortest
   * path.
   *
   * @param startNode  the starting node of the path
   * @param targetNode the target node of the path
   * @return the calculated path, or null if no path is found
   */
  private Path findAStarPath(AStarNode startNode, AStarNode targetNode) {
    final List<AStarNode> opened = new ArrayList<>();
    final List<AStarNode> closed = new ArrayList<>();
    opened.add(startNode);

    while (!opened.isEmpty()) {
      // after the first iteration, this will also contained the newly found neighbors that were
      // added in the last iteration
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
   * Updates the costs and the predecessor of all neighbors of the specified {@code currentNode}. <br> If a neighbor was previously not part of the
   * {@code opened} list it will be added to it.<br> If a neighbor is already closed, it will be ignored.<br> If the {@link AStarNode#isWalkable()}
   * method of a neighbor returns {@code false} it will also not be considered.
   *
   * @param currentNode The node for which the neighbors will be searched for.
   * @param targetNode  The target node of the path-finding operation.
   * @param opened      The list of all the opened nodes of the path-finding operation.
   * @param closed      The list of all the closed nodes of the path-finding operation.
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

  /**
   * Finds the node with the lowest cost in the open list. The cost is determined by the F-cost (G-cost + H-cost). If multiple nodes have the same
   * F-cost, the H-cost is considered.
   *
   * @param openedNodes the list of nodes to evaluate
   * @return the node with the lowest cost
   */
  private static AStarNode findNodeWithLowestCost(List<AStarNode> openedNodes) {
    AStarNode lowestCostNode = openedNodes.getFirst();
    // find node with lowest cost
    // F-cost (aka. total costs) are considered first. If they are equal, the H-cost is checked
    // subsequently
    for (int i = 1; i < openedNodes.size(); i++) {
      if (openedNodes.get(i).getFCost() < lowestCostNode.getFCost()
        || openedNodes.get(i).getFCost() == lowestCostNode.getFCost() && openedNodes.get(i).getHCost() < lowestCostNode.getHCost()) {
        lowestCostNode = openedNodes.get(i);
      }
    }

    return lowestCostNode;
  }

  /**
   * Clears the list of nodes by resetting their state.
   *
   * @param nodes the list of nodes to clear
   */
  private static void clear(List<AStarNode> nodes) {
    for (AStarNode op : nodes) {
      op.clear();
    }
  }

  /**
   * Retraces the found path from the targetNode back to the startNode by making use of the {@link AStarNode#getPredecessor()}.
   *
   * <ol>
   * <li>Adds all predecessors to a list of nodes that will be visited by the path.
   * <li>Invert the list.
   * <li>Create a new {@link Path2D} by iterating all nodes in the list.
   * <li>Wrap the {@link Path2D} object into a {@link Path} to provide information about the start, target and points of
   * the path.
   * </ol>
   *
   * @param startNode  The start node for the path.
   * @param targetNode The target node for the path.
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
    for (final AStarNode current : path) {
      final Point currentPoint = new Point(current.getLocation().x, current.getLocation().y);
      pointsOfPath.add(currentPoint);
      path2D.lineTo(currentPoint.x, currentPoint.y);
    }

    path2D.lineTo(targetNode.getLocation().x, targetNode.getLocation().y);

    return new Path(startNode.getLocation(), targetNode.getLocation(), path2D, pointsOfPath);
  }
}
