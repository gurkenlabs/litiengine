package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.tiled.tmx.IMap;

public class AStarGrid {
  private final Dimension size;
  private final int nodeSize;
  private final AStarNode[][] grid;

  public AStarGrid(final IPhysicsEngine physicsEngine, final IMap map, final int nodeSize) {
    this.size = map.getSizeInPixles();
    this.nodeSize = nodeSize;
    int gridSizeX = this.size.width / nodeSize;
    int gridSizeY = this.size.height / nodeSize;
    this.grid = new AStarNode[gridSizeX][gridSizeY];
    this.populateGrid(physicsEngine, gridSizeX, gridSizeY);
  }

  private void populateGrid(final IPhysicsEngine physicsEngine, final int gridSizeX, final int gridSizeY) {
    for (int x = 0; x < gridSizeX; x++) {
      for (int y = 0; y < gridSizeY; y++) {
        Rectangle nodeBounds = new Rectangle(x * nodeSize, y * nodeSize, nodeSize, nodeSize);
        
        // TODO: add terrain dependent penalty
        this.getGrid()[x][y] = new AStarNode(!physicsEngine.check(nodeBounds), nodeBounds, x, y, 0);
      }
    }
  }

  public List<AStarNode> getNeighbours(AStarNode node) {

    List<AStarNode> neighbours = new ArrayList<AStarNode>();

    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0)
          continue;

        int checkX = node.getGridX() + x;
        int checkY = node.getGridY() + y;

        if (checkX >= 0 && checkX < this.getGrid().length && checkY >= 0 && checkY < this.getGrid()[0].length) {
          neighbours.add(this.getGrid()[checkX][checkY]);
        }
      }
    }

    return neighbours;
  }

  public AStarNode getNodeFromMapLocation(Point2D point) {
    float percentX = (float) (point.getX() / this.getSize().getWidth());
    float percentY = (float) (point.getY() / this.getSize().getHeight());
    percentX = Math.max(0, Math.min(1, percentX));
    percentY = Math.max(0, Math.min(1, percentY));

    int x = (int) ((this.getGrid().length - 1) * percentX);
    int y = (int) ((this.getGrid()[0].length) * percentY);
    return this.getGrid()[x][y];
  }

  public int getNodeSize() {
    return this.nodeSize;
  }

  public AStarNode[][] getGrid() {
    return this.grid;
  }

  public Dimension getSize() {
    return this.size;
  }
}
