package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;

public class AStarGrid {
  private boolean allowDiagonalMovementOnCorners;
  private final AStarNode[][] grid;
  private final int nodeSize;
  private final IPhysicsEngine physicsEngine;

  private final Dimension size;

  public AStarGrid(final IPhysicsEngine physicsEngine, final IMap map, final int nodeSize) {
    this.physicsEngine = physicsEngine;
    this.size = map.getSizeInPixels();
    this.nodeSize = nodeSize;
    final int gridSizeX = this.size.width / nodeSize;
    final int gridSizeY = this.size.height / nodeSize;
    this.grid = new AStarNode[gridSizeX][gridSizeY];
    this.populateGrid(gridSizeX, gridSizeY);
  }

  public boolean diagonalMovementOnCorners() {
    return this.allowDiagonalMovementOnCorners;
  }

  public AStarNode[][] getGrid() {
    return this.grid;
  }

  public List<AStarNode> getIntersectedNodes(final Rectangle2D rectangle) {
    final Point2D start = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
    final Point2D end = new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY());

    final AStarNode startNode = this.getNodeFromMapLocation(start);
    final AStarNode endNode = this.getNodeFromMapLocation(end);

    final List<AStarNode> nodes = new ArrayList<>();
    if (startNode == null || endNode == null) {
      return nodes;
    }

    for (int x = startNode.getGridX(); x <= endNode.getGridX(); x++) {
      for (int y = startNode.getGridY(); y <= endNode.getGridY(); y++) {
        nodes.add(this.getGrid()[x][y]);
      }
    }

    return nodes;
  }

  public List<AStarNode> getNeighbours(final AStarNode node) {
    final List<AStarNode> neighbors = new ArrayList<>();
    final int x = node.getGridX();
    final int y = node.getGridY();

    final AStarNode top = this.getNode(x, y - 1);
    final AStarNode bottom = this.getNode(x, y + 1);
    final AStarNode left = this.getNode(x - 1, y);
    final AStarNode right = this.getNode(x + 1, y);

    // diagonal
    final AStarNode topLeft = this.getNode(x - 1, y - 1);
    final AStarNode topRight = this.getNode(x + 1, y - 1);
    final AStarNode bottomLeft = this.getNode(x - 1, y + 1);
    final AStarNode bottomRight = this.getNode(x + 1, y + 1);

    addNode(neighbors, top);
    addNode(neighbors, bottom);
    addNode(neighbors, right);
    addNode(neighbors, left);

    this.addDiagonalNode(neighbors, topLeft, top, left);
    this.addDiagonalNode(neighbors, topRight, top, right);
    this.addDiagonalNode(neighbors, bottomLeft, bottom, left);
    this.addDiagonalNode(neighbors, bottomRight, bottom, right);

    return neighbors;
  }

  public AStarNode getNodeFromMapLocation(final Point2D point) {
    float percentX = (float) (point.getX() / this.getSize().getWidth());
    float percentY = (float) (point.getY() / this.getSize().getHeight());
    percentX = Math.max(0, Math.min(1, percentX));
    percentY = Math.max(0, Math.min(1, percentY));

    final int x = (int) ((this.getGrid().length - 1) * percentX);
    final int y = (int) (this.getGrid()[0].length * percentY);
    return this.getGrid()[x][y];
  }

  public int getNodeSize() {
    return this.nodeSize;
  }

  public Dimension getSize() {
    return this.size;
  }

  public void setAllowDiagonalMovementOnCorners(final boolean allowDiagonalMovementOnCorners) {
    this.allowDiagonalMovementOnCorners = allowDiagonalMovementOnCorners;
  }

  /**
   * Updates the walkable attribute of nodes intersected by the specified
   * rectangle.
   *
   * @param rectangle
   */
  public void updateWalkable(final Rectangle2D rectangle) {
    for (final AStarNode node : this.getIntersectedNodes(rectangle)) {
      node.setWalkable(!this.physicsEngine.collides(node.getBounds()));
    }
  }

  private static void addNode(final List<AStarNode> neighbors, AStarNode node) {
    if (node != null && node.isWalkable()) {
      neighbors.add(node);
    }
  }

  private void addDiagonalNode(final List<AStarNode> neighbors, AStarNode node, AStarNode diagonalNeighbor1, AStarNode diagonalNeighbor2) {
    // only add diagonal neighbors when they are not on a corner
    if (node != null && this.diagonalMovementOnCorners() || node != null && diagonalNeighbor1 != null && diagonalNeighbor1.isWalkable() && diagonalNeighbor2 != null && diagonalNeighbor2.isWalkable()) {
      neighbors.add(node);
    }
  }

  private AStarNode getNode(final int x, final int y) {
    if (x >= 0 && x < this.getGrid().length && y >= 0 && y < this.getGrid()[0].length) {
      return this.getGrid()[x][y];
    }

    return null;
  }

  private void populateGrid(final int gridSizeX, final int gridSizeY) {
    for (int x = 0; x < gridSizeX; x++) {
      for (int y = 0; y < gridSizeY; y++) {
        final Rectangle nodeBounds = new Rectangle(x * this.nodeSize, y * this.nodeSize, this.nodeSize, this.nodeSize);

        this.getGrid()[x][y] = new AStarNode(!this.physicsEngine.collides(nodeBounds), nodeBounds, x, y, 0);
      }
    }
  }
}
