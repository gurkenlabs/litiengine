package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.EventListener;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@TmxType(MapObjectType.SPAWNPOINT) public class Spawnpoint extends Entity {
  private final Collection<EntitySpawnedListener> spawnedListeners = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.SPAWN_DIRECTION) private Direction direction;

  @TmxProperty(name = MapObjectProperty.SPAWN_INFO) private String spawnInfo;

  @TmxProperty(name = MapObjectProperty.SPAWN_PIVOT) private EntityPivotType spawnPivotType;

  @TmxProperty(name = MapObjectProperty.SPAWN_PIVOT_OFFSETX) private double spawnOffsetX;

  @TmxProperty(name = MapObjectProperty.SPAWN_PIVOT_OFFSETY) private double spawnOffsetY;

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   */
  public Spawnpoint() {
    this.setSize(1, 1);
  }

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param x The x-coordinate of this instance.
   * @param y The y-coordinate of this instance.
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
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param mapId The map id of this instance.
   * @param x     The x-coordinate of this instance.
   * @param y     The y-coordinate of this instance.
   */
  public Spawnpoint(int mapId, double x, double y) {
    this(mapId, new Point2D.Double(x, y));
  }

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param mapId    The map id of this instance.
   * @param location The location of this instance.
   */
  public Spawnpoint(int mapId, Point2D location) {
    super(mapId);
    this.setSize(1, 1);
    this.setLocation(location);
  }

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param mapId     The map id of this instance.
   * @param x         The x-coordinate of this instance.
   * @param y         The y-coordinate of this instance.
   * @param direction The direction in which entities will be spawned by this instance.
   */
  public Spawnpoint(int mapId, double x, double y, Direction direction) {
    this(mapId, new Point2D.Double(x, y), direction);
  }

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param mapId     The map id of this instance.
   * @param location  The location of this instance.
   * @param direction The direction in which entities will be spawned by this instance.
   */
  public Spawnpoint(int mapId, Point2D location, Direction direction) {
    this(mapId, location);
    this.setDirection(direction);
  }

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param direction The direction in which entities will be spawned by this instance.
   */
  public Spawnpoint(Direction direction) {
    this.setDirection(direction);
  }

  /**
   * Instantiates a new {@code Spawnpoint} entity.
   *
   * @param direction The direction in which entities will be spawned by this instance.
   * @param spawnType The type that defines additional information about the entities spawned by this instance.
   */
  public Spawnpoint(Direction direction, String spawnType) {
    this(direction);
    this.setSpawnInfo(spawnType);
  }

  /**
   * Adds the specified entity spawned listener to receive events when entities are spawned by this instance.
   *
   * @param listener The listener to add.
   */
  public void onSpawned(EntitySpawnedListener listener) {
    this.spawnedListeners.add(listener);
  }

  /**
   * Removes the specified entity spawned listener.
   *
   * @param listener The listener to remove.
   */
  public void removeSpawnedListener(EntitySpawnedListener listener) {
    this.spawnedListeners.remove(listener);
  }

  /**
   * Gets the direction in which entities will be spawned by this instance.
   *
   * @return the spawn direction
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Sets the direction in which entities will be spawned by this instance.
   *
   * @param direction the new spawn direction
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * Gets the spawn information for this instance.
   *
   * @return the spawn information
   */
  public String getSpawnInfo() {
    return spawnInfo;
  }

  /**
   * Sets the spawn information for this instance.
   *
   * @param spawnInfo the new spawn information
   */
  public void setSpawnInfo(String spawnInfo) {
    this.spawnInfo = spawnInfo;
  }

  /**
   * Gets the spawn pivot type for this instance.
   *
   * @return the spawn pivot type
   */
  public EntityPivotType getSpawnPivotType() {
    return spawnPivotType;
  }

  /**
   * Sets the spawn pivot type for this instance.
   *
   * @param spawnPivotType the new spawn pivot type
   */
  public void setSpawnPivotType(EntityPivotType spawnPivotType) {
    this.spawnPivotType = spawnPivotType;
  }

  /**
   * Gets the spawn offset on the X-axis for this instance.
   *
   * @return the spawn offset on the X-axis
   */
  public double getSpawnOffsetX() {
    return spawnOffsetX;
  }

  /**
   * Sets the spawn offset on the X-axis for this instance.
   *
   * @param spawnOffsetX the new spawn offset on the X-axis
   */
  public void setSpawnOffsetX(double spawnOffsetX) {
    this.spawnOffsetX = spawnOffsetX;
  }

  /**
   * Gets the spawn offset on the Y-axis for this instance.
   *
   * @return the spawn offset on the Y-axis
   */
  public double getSpawnOffsetY() {
    return spawnOffsetY;
  }

  /**
   * Sets the spawn offset on the Y-axis for this instance.
   *
   * @param spawnOffsetY the new spawn offset on the Y-axis
   */
  public void setSpawnOffsetY(double spawnOffsetY) {
    this.spawnOffsetY = spawnOffsetY;
  }

  /**
   * Spawns the specified entity to the {@code Environment} of the {@code Spawnpoint} or the currently active {@code Environment}.
   *
   * <p>
   * Spawning will set the location of the entity to the location defined by the spawnpoint and optionally also set the angle of the entity, if a
   * spawn direction is defined.
   *
   * @param entity The entity to spawn at the specified location.
   * @return True if the entity was spawned; otherwise false, which is typically the case if no environment is loaded.
   * @see GameWorld#environment()
   */
  public boolean spawn(IEntity entity) {
    Environment env = Optional.ofNullable(getEnvironment()).orElse(Game.world().environment());
    if (env == null) {
      return false;
    }

    entity.setLocation(getEntityLocationByPivot(entity));

    if (getDirection() != null && getDirection() != Direction.UNDEFINED) {
      entity.setAngle(getDirection().toAngle());
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

  /**
   * Gets the location of the entity based on the spawn pivot type.
   *
   * @param entity The entity for which to get the location.
   * @return The location of the entity based on the spawn pivot type.
   */
  private Point2D getEntityLocationByPivot(IEntity entity) {
    if (getSpawnPivotType() == null || getSpawnPivotType() == EntityPivotType.LOCATION) {
      return getLocation();
    }

    EntityPivot pivot = new EntityPivot(this, getSpawnPivotType(), -entity.getWidth() / 2d, -entity.getHeight() / 2d);

    return pivot.getPoint();
  }

  /**
   * Functional interface for listening to entity spawned events.
   */
  @FunctionalInterface
  public interface EntitySpawnedListener extends EventListener {
    /**
     * Invoked when an entity is spawned.
     *
     * @param event The event that contains information about the spawned entity.
     */
    void spawned(EntitySpawnedEvent event);
  }
}
