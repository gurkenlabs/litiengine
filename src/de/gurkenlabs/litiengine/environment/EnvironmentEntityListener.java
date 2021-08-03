package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;
import java.util.EventListener;

public interface EnvironmentEntityListener extends EventListener {
  default void entityAdded(IEntity entity) {}

  default void entityRemoved(IEntity entity) {}
}
