package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.Direction;

public class Spawnpoint extends MapLocation {
  private Direction direction;
  private String spawnType;
  
  public Spawnpoint(int mapId, Point2D point) {
    super(mapId, point);
  }

  public Spawnpoint(int mapId, Point2D point, Direction direction) {
    super(mapId, point);
    this.setDirection(direction);
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public String getSpawnType() {
    return spawnType;
  }

  public void setSpawnType(String spawnType) {
    this.spawnType = spawnType;
  }

}
