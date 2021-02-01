package com.litiengine.environment;

import java.util.EventListener;

import com.litiengine.entities.IEntity;

public interface EnvironmentEntityListener extends EventListener {
  default void entityAdded(IEntity entity) {}

  default void entityRemoved(IEntity entity) {}
}
