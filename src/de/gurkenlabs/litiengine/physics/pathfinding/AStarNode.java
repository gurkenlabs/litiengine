package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.Point;
import java.awt.Rectangle;

public class AStarNode {
  private static final int DIAGONAL_COST = 14;
  private static final int STRAIGHT_COST = 10;
  private final Rectangle bound;
  private int gCost;
  private final int gridX;
  private final int gridY;
  private int hCost;

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

  public int getCosts(final AStarNode target) {

    final int dstX = Math.abs(this.getGridX() - target.getGridX());
    final int dstY = Math.abs(this.getGridY() - target.getGridY());

    if (dstX > dstY) {
      return DIAGONAL_COST * dstY + STRAIGHT_COST * (dstX - dstY);
    }

    return DIAGONAL_COST * dstX + STRAIGHT_COST * (dstY - dstX);
  }

  public int getfCost() {
    return this.getgCost() + this.gethCost();
  }

  public int getgCost() {
    return this.gCost;
  }

  public int getGridX() {
    return this.gridX;
  }

  public int getGridY() {
    return this.gridY;
  }

  public int gethCost() {
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

  public void setgCost(final int gCost) {
    this.gCost = gCost;
  }

  public void sethCost(final int hCost) {
    this.hCost = hCost;
  }

  public void setPredecessor(final AStarNode predecessor) {
    this.predecessor = predecessor;
  }

  public void setWalkable(final boolean walkable) {
    this.walkable = walkable;
  }

  @Override
  public String toString() {
    return "[" + this.getGridX() + "," + this.getGridY() + "] - (f:" + this.getfCost() + ", g:" + this.getgCost() + ", h:" + this.gethCost() + ")";
  }
}
