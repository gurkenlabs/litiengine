package de.gurkenlabs.litiengine.pathfinding;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class AStarGrid implements IRenderable {
  private final AStarNode[][] grid;
  private final int nodeSize;
  private final IPhysicsEngine physicsEngine;
  private final Dimension size;

  private boolean allowDiagonalMovement = true;
  private boolean allowDiagonalCornersMovement;

  public AStarGrid(final IPhysicsEngine physicsEngine, final IMap map, final int nodeSize) {
    this.physicsEngine = physicsEngine;
    this.size = map.getSizeInPixels();
    this.nodeSize = nodeSize;
    final int gridSizeX = this.size.width / nodeSize;
    final int gridSizeY = this.size.height / nodeSize;
    this.grid = new AStarNode[gridSizeX][gridSizeY];
    this.populateGrid(gridSizeX, gridSizeY);
  }

  public boolean isDiagonalMovementAllowed() {
    return this.allowDiagonalMovement;
  }

  public boolean isDiagonalCornerMovementAllowed() {
    return this.allowDiagonalCornersMovement;
  }

  public AStarNode[][] getGrid() {
    return this.grid;
  }

  public List<AStarNode> getIntersectedNodes(final Rectangle2D rectangle) {
    final Point2D start = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
    final Point2D end = new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY());

    final AStarNode startNode = this.getNode(start);
    final AStarNode endNode = this.getNode(end);

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

  public List<AStarNode> getNeighbors(final AStarNode node) {
    final List<AStarNode> neighbors = new ArrayList<>();
    final int x = node.getGridX();
    final int y = node.getGridY();

    final AStarNode top = this.getNode(x, y - 1);
    final AStarNode bottom = this.getNode(x, y + 1);
    final AStarNode left = this.getNode(x - 1, y);
    final AStarNode right = this.getNode(x + 1, y);

    addNode(neighbors, top);
    addNode(neighbors, bottom);
    addNode(neighbors, right);
    addNode(neighbors, left);

    if (this.isDiagonalMovementAllowed()) {
      final AStarNode topLeft = this.getNode(x - 1, y - 1);
      final AStarNode topRight = this.getNode(x + 1, y - 1);
      final AStarNode bottomLeft = this.getNode(x - 1, y + 1);
      final AStarNode bottomRight = this.getNode(x + 1, y + 1);
      this.addDiagonalNode(neighbors, topLeft, top, left);
      this.addDiagonalNode(neighbors, topRight, top, right);
      this.addDiagonalNode(neighbors, bottomLeft, bottom, left);
      this.addDiagonalNode(neighbors, bottomRight, bottom, right);
    }

    return neighbors;
  }

  public AStarNode getNode(final Point2D point) {
    return this.getNode(point.getX(), point.getY());
  }

  public AStarNode getNode(final double x, final double y) {
    int xNode = (int) (x / this.nodeSize);
    int yNode = (int) (y / this.nodeSize);

    if (xNode >= this.getGrid().length || yNode >= this.getGrid()[0].length) {
      return null;
    }

    return this.getNode(xNode, yNode);
  }

  public int getNodeSize() {
    return this.nodeSize;
  }

  public Dimension getSize() {
    return this.size;
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D viewport = Game.getCamera().getViewPort();

    final AStarNode startNode = this.getNode(viewport.getX(), viewport.getY());
    final AStarNode endNode = this.getNode(viewport.getMaxX(), viewport.getMaxY());
    final int startX = MathUtilities.clamp(startNode.getGridX(), 0, this.getGrid().length - 1);
    final int endX = MathUtilities.clamp(endNode.getGridX(), 0, this.getGrid().length - 1);
    final int startY = MathUtilities.clamp(startNode.getGridY(), 0, this.getGrid()[0].length - 1);
    final int endY = MathUtilities.clamp(endNode.getGridY(), 0, this.getGrid()[0].length - 1);

    g.setColor(new Color(255, 0, 255, 100));
    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        AStarNode node = this.getGrid()[x][y];
        if (node.isWalkable()) {
          Game.getRenderEngine().renderShape(g, new Rectangle2D.Double(node.getLocation().x - 0.25, node.getLocation().y - 0.25, 0.5, 0.5));
        } else {
          Game.getRenderEngine().renderShape(g, node.getBounds());
        }
      }
    }
  }

  public void setAllowDiagonalMovementOnCorners(final boolean allowDiagonalMovementOnCorners) {
    this.allowDiagonalMovement = allowDiagonalMovementOnCorners;
  }

  /**
   * Updates the walkable attribute of nodes intersected by the specified
   * rectangle.
   *
   * @param rectangle
   *          The rectangle within which the nodes should be updated.
   */
  public void updateWalkable(final Rectangle2D rectangle) {
    for (final AStarNode node : this.getIntersectedNodes(rectangle)) {
      node.setWalkable(!this.physicsEngine.collides(node.getBounds(), CollisionType.STATIC));
    }
  }

  private static void addNode(final List<AStarNode> neighbors, AStarNode node) {
    if (node != null && node.isWalkable()) {
      neighbors.add(node);
    }
  }

  private void addDiagonalNode(final List<AStarNode> neighbors, AStarNode node, AStarNode diagonalNeighbor1, AStarNode diagonalNeighbor2) {
    // only add diagonal neighbors when they are not on a corner
    if (node != null && this.isDiagonalCornerMovementAllowed() || node != null && diagonalNeighbor1 != null && diagonalNeighbor1.isWalkable() && diagonalNeighbor2 != null && diagonalNeighbor2.isWalkable()) {
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

        this.getGrid()[x][y] = new AStarNode(!this.physicsEngine.collides(nodeBounds, CollisionType.STATIC), nodeBounds, x, y, 0);
      }
    }
  }
}
