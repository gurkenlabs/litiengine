package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;

public class Spawnpoint extends Entity {
  @TmxProperty(name = MapObjectProperty.SPAWN_DIRECTION)
  private Direction direction;
  
  @TmxProperty(name = MapObjectProperty.SPAWN_TYPE)
  private String spawnType;

  public Spawnpoint() {
  }

  public Spawnpoint(double x, double y) {
    this(0, x, y);
  }

  public Spawnpoint(int mapId, double x, double y) {
    this(mapId, new Point2D.Double(x, y));
  }

  public Spawnpoint(int mapId, Point2D point) {
    super(mapId);
    this.setLocation(point);
  }

  public Spawnpoint(int mapId, double x, double y, Direction direction) {
    this(mapId, new Point2D.Double(x, y), direction);
  }

  public Spawnpoint(int mapId, Point2D point, Direction direction) {
    this(mapId, point);
    this.setDirection(direction);
  }

  public Spawnpoint(Direction direction) {
    this.setDirection(direction);
  }

  public Spawnpoint(Direction direction, String spawnType) {
    this(direction);
    this.setSpawnType(spawnType);
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