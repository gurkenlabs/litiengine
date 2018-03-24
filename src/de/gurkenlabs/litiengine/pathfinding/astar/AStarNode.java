package de.gurkenlabs.litiengine.pathfinding.astar;

import java.awt.Point;
import java.awt.Rectangle;

public class AStarNode {
  // diagonal length: 1 * Math.sqrt(2) ~ 1.4
  private static final double DIAGONAL_COST = 1.4;
  private final Rectangle bound;
  private final int gridX;
  private final int gridY;
  private double gCost;
  private double hCost;

  private final int penalty;
  private AStarNode predecessor;
  private boolean walkable;

  public AStarNode(final boolean walkable, final Rectangle bound, final int gridX, final int gridY, final int penalty) {
    this.bound = bound;
    this.gridX = gridX;
    this.gridY = gridY;
    this.penalty = penalty;
    this.walkable = walkable;
  }

  public Rectangle getBounds() {
    return this.bound;
  }

  public double getCosts(final AStarNode target) {

    final int dstX = Math.abs(this.getGridX() - target.getGridX());
    final int dstY = Math.abs(this.getGridY() - target.getGridY());

    if (dstX > dstY) {
      return (DIAGONAL_COST * dstY) + (dstX - dstY);
    }

    return (DIAGONAL_COST * dstX) + (dstY - dstX);
  }

  /**
   * Gets the total costs for this node.
   * 
   * @return The total costs.
   */
  public double getFCost() {
    return this.getGCost() + this.getHCost();
  }

  /**
   * Gets the costs so far for this node.
   * 
   * @return The costs so far.
   */
  public double getGCost() {
    return this.gCost;
  }

  public int getGridX() {
    return this.gridX;
  }

  public int getGridY() {
    return this.gridY;
  }

  /**
   * Gets the estimated costs for this node.
   * 
   * @return The estimated costs.
   */
  public double getHCost() {
    return this.hCost;
  }

  public Point getLocation() {
    return new Point((int) this.getBounds().getCenterX(), (int) this.getBounds().getCenterY());
  }

  public int getPenalty() {
    return this.penalty;
  }

  public AStarNode getPredecessor() {
    return this.predecessor;
  }

  public boolean isWalkable() {
    return this.walkable;
  }

  public void setGCost(final double gCost) {
    this.gCost = gCost;
  }

  public void setHCost(final double hCost) {
    this.hCost = hCost;
  }

  public void setPredecessor(final AStarNode predecessor) {
    this.predecessor = predecessor;
  }

  public void setWalkable(final boolean walkable) {
    this.walkable = walkable;
  }

  /**
   * Clears the assigned costs and the predecessor.
   */
  public void clear() {
    this.setGCost(0);
    this.setHCost(0);
    this.setPredecessor(null);
  }

  @Override
  public String toString() {
    return "[" + this.getGridX() + "," + this.getGridY() + "] - (f:" + this.getFCost() + ", g:" + this.getGCost() + ", h:" + this.getHCost() + ")";
  }
}
