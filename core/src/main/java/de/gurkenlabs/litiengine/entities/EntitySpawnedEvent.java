package de.gurkenlabs.litiengine.entities;

import java.io.Serial;
import java.util.EventObject;

public class EntitySpawnedEvent extends EventObject {

  @Serial
  private static final long serialVersionUID = 3168131857377255247L;
  private final transient Spawnpoint spawnpoint;
  private final transient IEntity spawnedEntity;

  EntitySpawnedEvent(Spawnpoint source, IEntity entity) {
    super(source);
    this.spawnpoint = source;
    this.spawnedEntity = entity;
  }

  public Spawnpoint getSpawnpoint() {
    return this.spawnpoint;
  }

  public IEntity getSpawnedEntity() {
    return this.spawnedEntity;
  }
}
