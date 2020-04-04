package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;

@TmxType(MapObjectType.SPAWNPOINT)
public class Spawnpoint extends Entity {
  private final Collection<EntitySpawnedListener> spawnedListeners = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.SPAWN_DIRECTION)
  private Direction direction;

  @TmxProperty(name = MapObjectProperty.SPAWN_TYPE)
  private String spawnType;

  @TmxProperty(name = MapObjectProperty.SPAWN_PIVOT)
  private EntityPivotType spawnPivotType;

  @TmxProperty(name = MapObjectProperty.SPAWN_PIVOT_OFFSETX)
  private double spawnOffsetX;

  @TmxProperty(name = MapObjectProperty.SPAWN_PIVOT_OFFSETY)
  private double spawnOffsetY;

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   */
  public Spawnpoint() {
    this.setSize(1, 1);
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   */
  public Spawnpoint(double x, double y) {
    this(0, x, y);
  }

  public Spawnpoint(Point2D location) {
    this(0, location);
  }

  public Spawnpoint(double x, double y, Direction direction) {
    this(0, x, y, direction);
  }

  public Spawnpoint(Point2D location, Direction direction) {
    this(0, location, direction);
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param mapId
   *          The map id of this instance.
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   */
  public Spawnpoint(int mapId, double x, double y) {
    this(mapId, new Point2D.Double(x, y));
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param mapId
   *          The map id of this instance.
   * @param location
   *          The location of this instance.
   */
  public Spawnpoint(int mapId, Point2D location) {
    super(mapId);
    this.setSize(1, 1);
    this.setLocation(location);
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param mapId
   *          The map id of this instance.
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param direction
   *          The direction in which entities will be spawned by this instance.
   */
  public Spawnpoint(int mapId, double x, double y, Direction direction) {
    this(mapId, new Point2D.Double(x, y), direction);
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param mapId
   *          The map id of this instance.
   * @param location
   *          The location of this instance.
   * @param direction
   *          The direction in which entities will be spawned by this instance.
   */
  public Spawnpoint(int mapId, Point2D location, Direction direction) {
    this(mapId, location);
    this.setDirection(direction);
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param direction
   *          The direction in which entities will be spawned by this instance.
   */
  public Spawnpoint(Direction direction) {
    this.setDirection(direction);
  }

  /**
   * Instantiates a new <code>Spawnpoint</code> entity.
   *
   * @param direction
   *          The direction in which entities will be spawned by this instance.
   * @param spawnType
   *          The type that defines additional information about the entities spawned by this instance.
   */
  public Spawnpoint(Direction direction, String spawnType) {
    this(direction);
    this.setSpawnType(spawnType);
  }

  /**
   * Adds the specified entity spawned listener to receive events when entities are spawned by this instance.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onSpawned(EntitySpawnedListener listener) {
    this.spawnedListeners.add(listener);
  }

  /**
   * Removes the specified entity spawned listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeSpawnedListener(EntitySpawnedListener listener) {
    this.spawnedListeners.remove(listener);
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

  public EntityPivotType getSpawnPivotType() {
    return this.spawnPivotType;
  }

  public void setSpawnPivotType(EntityPivotType spawnPivotType) {
    this.spawnPivotType = spawnPivotType;
  }

  public double getSpawnOffsetX() {
    return this.spawnOffsetX;
  }

  public void setSpawnOffsetX(double spawnOffsetX) {
    this.spawnOffsetX = spawnOffsetX;
  }

  public double getSpawnOffsetY() {
    return this.spawnOffsetY;
  }

  public void setSpawnOffsetY(double spawnOffsetY) {
    this.spawnOffsetY = spawnOffsetY;
  }

  /**
   * Spawns the specified entity to the <code>Environment</code> of the <code>Spawnpoint</code> or the currently active <code>Environment</code>.
   * 
   * <p>
   * Spawning will set the location of the entity to the location defined by the spawnpoint and optionally also set the angle of the entity,
   * if a spawn direction is defined.
   * </p>
   * 
   * @param entity
   *          The entity to spawn at the specified location.
   * @return True if the entity was spawned; otherwise false, which is typically the case if no environment is loaded.
   * 
   * @see GameWorld#environment()
   */
  public boolean spawn(IEntity entity) {
    Environment env = this.getEnvironment();
    if (env == null) {
      env = Game.world().environment();
    }

    if (env == null) {
      return false;
    }

    entity.setLocation(this.getEntityLocationByPivot(entity));

    if (this.getDirection() != null && this.getDirection() != Direction.UNDEFINED) {
      entity.setAngle(this.getDirection().toAngle());
    }

    if (env.get(entity.getMapId()) == null) {
      env.add(entity);
    }

    final EntitySpawnedEvent event = new EntitySpawnedEvent(this, entity);
    for (EntitySpawnedListener listener : this.spawnedListeners) {
      listener.spawned(event);
    }

    return true;
  }

  private Point2D getEntityLocationByPivot(IEntity entity) {
    if (this.getSpawnPivotType() == null || this.getSpawnPivotType() == EntityPivotType.LOCATION) {
      return this.getLocation();
    }
    
    EntityPivot pivot = new EntityPivot(entity, this.getSpawnPivotType(), this.getSpawnOffsetX(), this.getSpawnOffsetY());

    Point2D pivotPoint = pivot.getPoint();
    return new Point2D.Double(this.getX() - (pivotPoint.getX() - entity.getX()), this.getY() - (pivotPoint.getY() - entity.getY()));
  }

  @FunctionalInterface
  public interface EntitySpawnedListener extends EventListener {
    void spawned(EntitySpawnedEvent event);
  }
}