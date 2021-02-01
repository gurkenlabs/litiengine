package com.litiengine.entities;

import java.util.EventListener;

import com.litiengine.environment.Environment;

public interface EntityListener extends EventListener {

  default void loaded(IEntity entity, Environment environment) {}

  default void removed(IEntity entity, Environment environment) {}
}
