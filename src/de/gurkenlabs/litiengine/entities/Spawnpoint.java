package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;

public class Spawnpoint extends Entity {
  private final Collection<EntitySpawnedListener> spawnedListeners = ConcurrentHashMap.newKeySet();

  public void onSpawned(EntitySpawnedListener listener) {
    this.spawnedListeners.add(listener);
  }

  public void removeSpawnedListener(EntitySpawnedListener listener) {
    this.spawnedListeners.remove(listener);
  }

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

  public void spawn(IEntity entity) {
    entity.setLocation(this.getLocation());
    entity.setAngle(this.getDirection().toAngle());

    Environment env = this.getEnvironment();
    if (env == null) {
      env = Game.world().environment();
    }

    if (env != null && env.get(entity.getMapId()) == null) {
      env.add(entity);

      final EntitySpawnedEvent event = new EntitySpawnedEvent(this, entity);
      for (EntitySpawnedListener listener : this.spawnedListeners) {
        listener.spawned(event);
      }
    }
  }

  @FunctionalInterface
  public interface EntitySpawnedListener extends EventListener {
    void spawned(EntitySpawnedEvent event);
  }
}