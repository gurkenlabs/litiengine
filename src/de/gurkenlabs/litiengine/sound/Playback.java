package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.entities.IEntity;

public class Playback {
  private final String name;
  private final IEntity entity;

  public Playback(final IEntity entity, final String name) {
    this.name = name;
    this.entity = entity;
  }

  public IEntity getEntity() {
    return this.entity;
  }

  public String getName() {
    return this.name;
  }
}
