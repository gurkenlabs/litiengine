package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.physics.Collision;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an A* grid used for pathfinding.
 *
 * <p>This class implements the {@link IRenderable} interface and provides methods for
 * managing and rendering a grid of A* nodes. It supports diagonal movement and allows updating the walkable state of nodes based on collisions.</p>
 */
public class AStarGrid implements IRenderable {
  /**
   * The penalty value assigned to nodes that intersect with static, indestructible props.
   */
  public static final double PENALTY_STATIC_PROP = 5;
  /**
   * The penalty value assigned to nodes that have non-walkable neighboring nodes.
   */
  public static final double PENALTY_NOT_WALKABLE_NEIGHBOR = 4;

  private final AStarNode[][] grid;
  private final int nodeSize;
  private final Dimension size;

  private boolean allowDiagonalMovement = true;
  private boolean allowCuttingCorners;

  /**
   * Constructs an AStarGrid with the specified width, height, and node size.
   *
   * @param width    The width of the grid.
   * @param height   The height of the grid.
   * @param nodeSize The size of each node in the grid.
   */
  public AStarGrid(int width, int height, final int nodeSize) {
    this(new Dimension(width, height), nodeSize);
  }

  /**
   * Constructs an AStarGrid with the specified size and node size.
   *
   * @param size     The dimension of the grid.
   * @param nodeSize The size of each node in the grid.
   */
  public AStarGrid(final Dimension size, final int nodeSize) {
    this.size = size;
    this.nodeSize = nodeSize;
    final int gridSizeX = this.size.width / nodeSize;
    final int gridSizeY = this.size.height / nodeSize;
    this.grid = new AStarNode[gridSizeX][gridSizeY];
    this.populateGrid(gridSizeX, gridSizeY);
  }

  /**
   * Checks if diagonal movement is allowed in the grid.
   *
   * @return True if diagonal movement is allowed; otherwise false.
   */
  public boolean isDiagonalMovementAllowed() {
    return this.allowDiagonalMovement;
  }

  /**
   * Checks if diagonal corner movement is allowed in the grid.
   *
   * @return True if diagonal corner movement is allowed; otherwise false.
   */
  public boolean isDiagonalCornerMovementAllowed() {
    return this.allowCuttingCorners;
  }

  /**
   * Gets the grid of A* nodes.
   *
   * @return A 2D array representing the grid of A* nodes.
   */
  public AStarNode[][] getGrid() {
    return this.grid;
  }

  /**
   * Gets the list of A* nodes that intersect with the specified rectangle.
   *
   * @param rectangle The rectangle to check for intersecting nodes.
   * @return A list of A* nodes that intersect with the specified rectangle.
   */
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

  /**
   * Gets the list of neighboring A* nodes for the specified node.
   *
   * @param node The node for which to get the neighbors.
   * @return A list of neighboring A* nodes.
   */
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

  /**
   * Gets the A* node at the specified point.
   *
   * @param point The point for which to get the corresponding A* node.
   * @return The A* node at the specified point, or null if the point is outside the grid.
   */
  public AStarNode getNode(final Point2D point) {
    return this.getNode(point.getX(), point.getY());
  }

  /**
   * Gets the A* node at the specified coordinates.
   *
   * @param x The x-coordinate of the point.
   * @param y The y-coordinate of the point.
   * @return The A* node at the specified coordinates, or null if the coordinates are outside the grid.
   */
  public AStarNode getNode(final double x, final double y) {
    int xNode = (int) (x / this.nodeSize);
    int yNode = (int) (y / this.nodeSize);

    if (xNode >= this.getGrid().length || yNode >= this.getGrid()[0].length) {
      return null;
    }

    return this.getNode(xNode, yNode);
  }

  /**
   * Gets the size of each node in the grid.
   *
   * @return The size of each node in the grid.
   */
  public int getNodeSize() {
    return this.nodeSize;
  }

  /**
   * Gets the dimension of the grid.
   *
   * @return The dimension of the grid.
   */
  public Dimension getSize() {
    return this.size;
  }

  @Override public void render(Graphics2D g) {
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
          Game.graphics().renderShape(g, new Rectangle2D.Double(node.getLocation().x - 0.25, node.getLocation().y - 0.25, 0.5, 0.5));
        } else {
          Game.graphics().renderShape(g, node.getBounds());
        }
      }
    }
  }

  /**
   * Sets whether diagonal movement is allowed in the grid.
   *
   * @param allowDiagonalMovement True to allow diagonal movement; otherwise false.
   */
  public void setAllowDiagonalMovement(final boolean allowDiagonalMovement) {
    this.allowDiagonalMovement = allowDiagonalMovement;
  }

  /**
   * Sets whether cutting corners during diagonal movement is allowed in the grid.
   *
   * @param allowCuttingCorners True to allow cutting corners; otherwise false.
   */
  public void setAllowCuttingCorners(final boolean allowCuttingCorners) {
    this.allowCuttingCorners = allowCuttingCorners;
  }

  /**
   * Updates the walkable attribute of nodes intersected by the specified rectangle.
   *
   * @param rectangle The rectangle within which the nodes should be updated.
   */
  public void updateWalkable(final Rectangle2D rectangle) {
    for (final AStarNode node : this.getIntersectedNodes(rectangle)) {
      node.setWalkable(!Game.physics().collides(node.getBounds(), Collision.STATIC));
    }
  }

  /**
   * Assigns a penalty to the specified A* node based on collisions and neighboring nodes.
   *
   * <p>If the node's location collides with a dynamic object, a penalty is calculated.
   * The penalty is increased if the node intersects with indestructible props or has non-walkable neighbors.</p>
   *
   * @param node The A* node to which the penalty will be assigned.
   */
  protected void assignPenalty(AStarNode node) {
    if (!Game.physics().collides(node.getLocation(), Collision.DYNAMIC)) {
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

  /**
   * Adds the specified node to the list of neighbors if it is walkable.
   *
   * @param neighbors The list of neighboring A* nodes.
   * @param node      The A* node to be added to the neighbors list.
   */
  private static void addNode(final List<AStarNode> neighbors, AStarNode node) {
    if (node != null && node.isWalkable()) {
      neighbors.add(node);
    }
  }

  /**
   * Adds the specified diagonal node to the list of neighbors if it is walkable and not on a corner.
   *
   * @param neighbors         The list of neighboring A* nodes.
   * @param node              The diagonal A* node to be added to the neighbors list.
   * @param diagonalNeighbor1 The first neighboring node to check for corner condition.
   * @param diagonalNeighbor2 The second neighboring node to check for corner condition.
   */
  private void addDiagonalNode(final List<AStarNode> neighbors, AStarNode node, AStarNode diagonalNeighbor1, AStarNode diagonalNeighbor2) {
    // only add diagonal neighbors when they are not on a corner
    if (node != null && this.isDiagonalCornerMovementAllowed()
      || node != null && diagonalNeighbor1 != null && diagonalNeighbor1.isWalkable() && diagonalNeighbor2 != null && diagonalNeighbor2.isWalkable()) {
      neighbors.add(node);
    }
  }

  /**
   * Clamps the x-coordinate to ensure it is within the valid range of the grid.
   *
   * @param x The x-coordinate to clamp.
   * @return The clamped x-coordinate.
   */
  private int clampX(int x) {
    return Math.clamp(x, 0, this.getGrid().length - 1);
  }

  /**
   * Clamps the y-coordinate to ensure it is within the valid range of the grid.
   *
   * @param y The y-coordinate to clamp.
   * @return The clamped y-coordinate.
   */
  private int clampY(int y) {
    return Math.clamp(y, 0, this.getGrid()[0].length - 1);
  }

  /**
   * Gets the A* node at the specified grid coordinates.
   *
   * @param x The x-coordinate of the node in the grid.
   * @param y The y-coordinate of the node in the grid.
   * @return The A* node at the specified coordinates, or null if the coordinates are outside the grid.
   */
  private AStarNode getNode(final int x, final int y) {
    if (x >= 0 && x < this.getGrid().length && y >= 0 && y < this.getGrid()[0].length) {
      return this.getGrid()[x][y];
    }

    return null;
  }

  /**
   * Populates the grid with A* nodes, initializing each node's walkable state and penalty.
   *
   * @param gridSizeX The number of nodes along the x-axis of the grid.
   * @param gridSizeY The number of nodes along the y-axis of the grid.
   */
  private void populateGrid(final int gridSizeX, final int gridSizeY) {
    for (int x = 0; x < gridSizeX; x++) {
      for (int y = 0; y < gridSizeY; y++) {
        final Rectangle nodeBounds = new Rectangle(x * this.nodeSize, y * this.nodeSize, this.nodeSize, this.nodeSize);
        final AStarNode node = new AStarNode(!Game.physics().collides(nodeBounds, Collision.STATIC), nodeBounds, x, y);
        this.assignPenalty(node);
        this.getGrid()[x][y] = node;
      }
    }
  }
}
