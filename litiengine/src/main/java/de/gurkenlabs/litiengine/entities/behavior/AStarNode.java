package de.gurkenlabs.litiengine.entities.behavior;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Represents a node in the A* pathfinding algorithm. Each node contains information about its position, costs, and walkability.
 */
public class AStarNode {
  private static final double DIAGONAL_COST = 1.4;
  private final Rectangle bound;
  private final int gridX;
  private final int gridY;
  private double gCost;
  private double hCost;
  private double penalty;
  private AStarNode predecessor;
  private boolean walkable;

  /**
   * Constructs a new AStarNode with the specified properties.
   *
   * @param walkable Whether the node is walkable.
   * @param bound    The rectangular bounds of the node.
   * @param gridX    The x-coordinate of the node in the grid.
   * @param gridY    The y-coordinate of the node in the grid.
   */
  public AStarNode(
    final boolean walkable, final Rectangle bound, final int gridX, final int gridY) {
    this.bound = bound;
    this.gridX = gridX;
    this.gridY = gridY;
    this.walkable = walkable;
  }

  /**
   * Gets the rectangular bounds of this node.
   *
   * @return The bounds of the node.
   */
  public Rectangle getBounds() {
    return this.bound;
  }

  /**
   * Calculates the movement cost from this node to the target node.
   *
   * @param target The target node.
   * @return The movement cost to the target node.
   */
  public double getCosts(final AStarNode target) {
    final int dstX = Math.abs(this.getGridX() - target.getGridX());
    final int dstY = Math.abs(this.getGridY() - target.getGridY());

    if (dstX > dstY) {
      return (DIAGONAL_COST * dstY) + (dstX - dstY) + this.getPenalty();
    }

    return (DIAGONAL_COST * dstX) + (dstY - dstX) + this.getPenalty();
  }

  /**
   * Gets the total cost (f-cost) for this node. The f-cost is the sum of g-cost and h-cost.
   *
   * @return The total cost.
   */
  public double getFCost() {
    return this.getGCost() + this.getHCost();
  }

  /**
   * Gets the cost from the start node to this node (g-cost).
   *
   * @return The g-cost.
   */
  public double getGCost() {
    return this.gCost;
  }

  /**
   * Gets the x-coordinate of this node in the grid.
   *
   * @return The x-coordinate.
   */
  public int getGridX() {
    return this.gridX;
  }

  /**
   * Gets the y-coordinate of this node in the grid.
   *
   * @return The y-coordinate.
   */
  public int getGridY() {
    return this.gridY;
  }

  /**
   * Gets the estimated cost from this node to the target node (h-cost).
   *
   * @return The h-cost.
   */
  public double getHCost() {
    return this.hCost;
  }

  /**
   * Gets the center location of this node as a Point.
   *
   * @return The center location of the node.
   */
  public Point getLocation() {
    return new Point((int) this.getBounds().getCenterX(), (int) this.getBounds().getCenterY());
  }

  /**
   * Gets the penalty cost for this node.
   *
   * @return The penalty cost.
   */
  public double getPenalty() {
    return this.penalty;
  }

  /**
   * Gets the predecessor node in the path.
   *
   * @return The predecessor node.
   */
  public AStarNode getPredecessor() {
    return this.predecessor;
  }

  /**
   * Checks if this node is walkable.
   *
   * @return True if the node is walkable, false otherwise.
   */
  public boolean isWalkable() {
    return this.walkable;
  }

  /**
   * Sets the cost from the start node to this node (g-cost).
   *
   * @param gCost The g-cost to set.
   */
  public void setGCost(final double gCost) {
    this.gCost = gCost;
  }

  /**
   * Sets the estimated cost from this node to the target node (h-cost).
   *
   * @param hCost The h-cost to set.
   */
  public void setHCost(final double hCost) {
    this.hCost = hCost;
  }

  /**
   * Sets the penalty cost for this node.
   *
   * @param penalty The penalty cost to set.
   */
  public void setPenalty(final double penalty) {
    this.penalty = penalty;
  }

  /**
   * Sets the predecessor node in the path.
   *
   * @param predecessor The predecessor node to set.
   */
  public void setPredecessor(final AStarNode predecessor) {
    this.predecessor = predecessor;
  }

  /**
   * Sets whether this node is walkable.
   *
   * @param walkable True if the node is walkable, false otherwise.
   */
  public void setWalkable(final boolean walkable) {
    this.walkable = walkable;
  }

  /**
   * Clears the assigned costs and the predecessor for this node.
   */
  public void clear() {
    this.setGCost(0);
    this.setHCost(0);
    this.setPredecessor(null);
  }

  /**
   * Returns a string representation of this node, including its grid position and costs.
   *
   * @return A string representation of the node.
   */
  @Override
  public String toString() {
    return "["
      + this.getGridX()
      + ","
      + this.getGridY()
      + "] - (f:"
      + this.getFCost()
      + ", g:"
      + this.getGCost()
      + ", h:"
      + this.getHCost()
      + ")";
  }
}
