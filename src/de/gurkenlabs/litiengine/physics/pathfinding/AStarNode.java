package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.Point;
import java.awt.Rectangle;

import de.gurkenlabs.litiengine.physics.IPhysicsEngine;

public class AStarNode {
  private final IPhysicsEngine physicsEngine;
  private final Rectangle bound;
  private final int gridX;
  private final int gridY;
  private final boolean walkable;
  private final int penalty;

  private int gCost;
  private int hCost;
  private AStarNode predecessor;

  public AStarNode(final IPhysicsEngine physicsEngine, final Rectangle bound, final int gridX, final int gridY, final int penalty) {
    this.physicsEngine = physicsEngine;
    this.bound = bound;
    this.gridX = gridX;
    this.gridY = gridY;
    this.penalty = penalty;
    this.walkable = !this.physicsEngine.check(this.getBounds());
  }

  public int getgCost() {
    return this.gCost;
  }

  public void setgCost(int gCost) {
    this.gCost = gCost;
  }

  public int gethCost() {
    return this.hCost;
  }

  public void sethCost(int hCost) {
    this.hCost = hCost;
  }

  public int getfCost() {
    return this.getgCost() + this.gethCost();
  }

  public AStarNode getPredecessor() {
    return this.predecessor;
  }

  public void setPredecessor(AStarNode predecessor) {
    this.predecessor = predecessor;
  }

  public boolean isWalkable() {
    return this.walkable;
  }

  public Rectangle getBounds() {
    return this.bound;
  }

  public Point getLocation() {
    return new Point((int) this.getBounds().getCenterX(), (int) this.getBounds().getCenterY());
  }

  public int getGridX() {
    return this.gridX;
  }

  public int getGridY() {
    return this.gridY;
  }
  
  public int getPenalty(){
    return this.getPenalty();
  }

  public int getCosts(final AStarNode target) {
    final int DIAGONAL_COST = 14;
    final int STRAIGHT_COST = 10;
    int dstX = Math.abs(this.getGridX() - target.getGridX());
    int dstY = Math.abs(this.getGridY() - target.getGridY());

    if (dstX > dstY) {
      return DIAGONAL_COST * dstY + STRAIGHT_COST * (dstX - dstY);
    }

    return DIAGONAL_COST * dstX + STRAIGHT_COST * (dstY - dstX);
  }
}
