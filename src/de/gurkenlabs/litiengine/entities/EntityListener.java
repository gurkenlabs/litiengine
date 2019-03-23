package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

import de.gurkenlabs.litiengine.environment.Environment;

public interface EntityListener extends EventListener {

  public default void loaded(IEntity entity, Environment environment) {}

  public default void removed(IEntity entity, Environment environment) {}
}
