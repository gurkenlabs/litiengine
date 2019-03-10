package de.gurkenlabs.litiengine.environment;

import java.util.EventListener;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface EnvironmentEntityListener extends EventListener {
  public default void entityAdded(IEntity entity) {}

  public default void entityRemoved(IEntity entity) {}
}
