package de.gurkenlabs.litiengine.pathfinding.astar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class AStarGrid implements IRenderable {
  public static final double PENALTY_STATIC_PROP = 5;
  public static final double PENALTY_NOT_WALKABLE_NEIGHBOR = 4;
  private final AStarNode[][] grid;
  private final int nodeSize;
  private final Dimension size;

  private boolean allowDiagonalMovement = true;
  private boolean allowCuttingCorners;

  public AStarGrid(int width, int height, final int nodeSize) {
    this(new Dimension(width, height), nodeSize);
  }

  public AStarGrid(final Dimension size, final int nodeSize) {
    this.size = size;
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
    return this.allowCuttingCorners;
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
    final List<AStarNode> newNeighbors = new ArrayList<>();
    final int x = node.getGridX();
    final int y = node.getGridY();

    final AStarNode top = this.getNode(x, y - 1);
    final AStarNode bottom = this.getNode(x, y + 1);
    final AStarNode left = this.getNode(x - 1, y);
    final AStarNode right = this.getNode(x + 1, y);

    addNode(newNeighbors, top);
    addNode(newNeighbors, bottom);
    addNode(newNeighbors, right);
    addNode(newNeighbors, left);

    if (this.isDiagonalMovementAllowed()) {
      final AStarNode topLeft = this.getNode(x - 1, y - 1);
      final AStarNode topRight = this.getNode(x + 1, y - 1);
      final AStarNode bottomLeft = this.getNode(x - 1, y + 1);
      final AStarNode bottomRight = this.getNode(x + 1, y + 1);
      this.addDiagonalNode(newNeighbors, topLeft, top, left);
      this.addDiagonalNode(newNeighbors, topRight, top, right);
      this.addDiagonalNode(newNeighbors, bottomLeft, bottom, left);
      this.addDiagonalNode(newNeighbors, bottomRight, bottom, right);
    }

    return newNeighbors;
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
    final Rectangle2D viewport = Game.world().camera().getViewport();

    final AStarNode startNode = this.getNode(viewport.getX(), viewport.getY());
    final AStarNode endNode = this.getNode(viewport.getMaxX(), viewport.getMaxY());
    final int startX = startNode == null ? 0 : clampX(startNode.getGridX());
    final int endX = endNode == null ? this.getGrid().length - 1 : clampX(endNode.getGridX());
    final int startY = startNode == null ? 0 : clampY(startNode.getGridY());
    final int endY = endNode == null ? this.getGrid()[0].length - 1 : clampY(endNode.getGridY());

    g.setColor(new Color(255, 0, 255, 100));
    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        AStarNode node = this.getGrid()[x][y];
        if (node.isWalkable()) {
          RenderEngine.renderShape(g, new Rectangle2D.Double(node.getLocation().x - 0.25, node.getLocation().y - 0.25, 0.5, 0.5));
        } else {
          RenderEngine.renderShape(g, node.getBounds());
        }
      }
    }
  }

  private int clampX(int x) {
    return MathUtilities.clamp(x, 0, this.getGrid().length - 1);
  }

  private int clampY(int y) {
    return MathUtilities.clamp(y, 0, this.getGrid()[0].length - 1);
  }

  public void setAllowDiagonalMovement(final boolean allowDiagonalMovement) {
    this.allowDiagonalMovement = allowDiagonalMovement;
  }

  public void setAllowCuttingCorners(final boolean allowCuttingCorners) {
    this.allowCuttingCorners = allowCuttingCorners;
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
      node.setWalkable(!Game.physics().collides(node.getBounds(), CollisionType.STATIC));
    }
  }

  protected void assignPenalty(AStarNode node) {
    if (!Game.physics().collides(node.getLocation(), CollisionType.DYNAMIC)) {
      return;
    }

    // by default we calculate a penalty for props that cannot be destroyed
    int penalty = 0;
    for (Prop prop : Game.world().environment().getProps()) {
      if (!prop.hasCollision() || !prop.isIndestructible() || !prop.getBoundingBox().intersects(node.getBounds())) {
        continue;
      }

      penalty += PENALTY_STATIC_PROP;
    }

    // if neighbors are not walkable, we try to avoid this node
    for (AStarNode neighbor : this.getNeighbors(node)) {
      if (!neighbor.isWalkable()) {
        penalty += PENALTY_NOT_WALKABLE_NEIGHBOR;
      }
    }

    node.setPenalty(penalty);
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
        final AStarNode node = new AStarNode(!Game.physics().collides(nodeBounds, CollisionType.STATIC), nodeBounds, x, y);
        this.assignPenalty(node);
        this.getGrid()[x][y] = node;
      }
    }
  }
}
