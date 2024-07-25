package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;

/**
 * Represents an event that is triggered when an entity is spawned.
 */
public class EntitySpawnedEvent extends EventObject {

  private static final long serialVersionUID = 3168131857377255247L;
  private final transient Spawnpoint spawnpoint;
  private final transient IEntity spawnedEntity;

  /**
   * Constructs a new EntitySpawnedEvent.
   *
   * @param source The spawnpoint that triggered this event.
   * @param entity The entity that was spawned.
   */
  EntitySpawnedEvent(Spawnpoint source, IEntity entity) {
    super(source);
    this.spawnpoint = source;
    this.spawnedEntity = entity;
  }

  /**
   * Gets the spawnpoint that triggered this event.
   *
   * @return The spawnpoint that triggered this event.
   */
  public Spawnpoint getSpawnpoint() {
    return this.spawnpoint;
  }

  /**
   * Gets the entity that was spawned.
   *
   * @return The entity that was spawned.
   */
  public IEntity getSpawnedEntity() {
    return this.spawnedEntity;
  }
}
