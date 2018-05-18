package de.gurkenlabs.litiengine.entities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds all controllers for the entities in the game. It is used as
 * a single hub to access and manage all the controllers.
 */
public final class EntityControllers {
  private Map<Class<? extends IEntityController>, IEntityController> controllers;

  public EntityControllers() {
    this.controllers = new ConcurrentHashMap<>();
  }

  @SuppressWarnings("unchecked")
  public <T extends IEntityController> T getController(Class<T> clss) {
    // if there's an exact match, return it
    if (this.controllers.containsKey(clss)) {
      IEntityController controller = this.controllers.get(clss);
      if (controller != null && clss.isInstance(controller)) {
        return (T) this.controllers.get(clss);
      }
    }

    // else check for controllers that are an instance of the specified class and return the first
    for (IEntityController controller : this.controllers.values()) {
      if (clss.isInstance(controller)) {
        return (T) controller;
      }
    }

    return null;
  }

  public <T extends IEntityController> void clearControllers(Class<T> clss) {
    this.controllers.entrySet().removeIf(e -> clss.isAssignableFrom(e.getKey()));
  }

  public <T extends IEntityController> void addController(T controller) {
    controllers.put(controller.getClass(), controller);
  }

  public <T extends IEntityController> void setController(Class<T> clss, T controller) {
    this.clearControllers(clss);
    this.addController(controller);
  }

  public void detachAll() {
    for (IEntityController controller : controllers.values()) {
      controller.detach();
    }
  }

  public void attachAll() {
    for (IEntityController controller : controllers.values()) {
      controller.attach();
    }
  }

  public void clear() {
    this.controllers.clear();
  }
}