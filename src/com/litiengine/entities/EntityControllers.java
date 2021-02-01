package com.litiengine.entities;

import com.litiengine.graphics.animation.IEntityAnimationController;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds all controllers for the entities in the game. It is used as
 * a single hub to access and manage all the controllers.
 */
public final class EntityControllers {
  private Map<Class<? extends IEntityController>, IEntityController> controllers;
  private IEntityAnimationController animationController;

  EntityControllers() {
    this.controllers = new ConcurrentHashMap<>();
  }

  public IEntityAnimationController getAnimationController() {
    if (this.animationController == null) {
      this.animationController = this.getController(IEntityAnimationController.class);
    }

    return this.animationController;
  }

  @SuppressWarnings("unchecked")
  public <T extends IEntityController> T getController(Class<T> clss) {
    T explicitController = this.getExplicitController(clss);
    if (explicitController != null) {
      return explicitController;
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
    Optional<Class<? extends IEntityController>> typeKey = this.controllers.keySet().stream().filter(x -> clss.isAssignableFrom(clss)).findFirst();
    if (typeKey.isPresent()) {
      IEntityController controller = this.controllers.get(typeKey.get());
      controller.detach();
      this.controllers.remove(typeKey.get());
      this.animationController = null;
    }
  }

  public <T extends IEntityController> void addController(T controller) {
    controllers.put(controller.getClass(), controller);

    if (controller.getEntity().isLoaded()) {
      controller.attach();
    }

    this.animationController = null;
  }

  public <T extends IEntityController> void setController(Class<T> clss, T controller) {
    this.clearControllers(clss);
    this.addController(controller);
    this.animationController = null;
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

  @SuppressWarnings("unchecked")
  private <T extends IEntityController> T getExplicitController(Class<T> clss) {
    // if there's an exact match, return it
    if (this.controllers.containsKey(clss)) {
      IEntityController controller = this.controllers.get(clss);
      if (controller != null && clss.isInstance(controller)) {
        return (T) this.controllers.get(clss);
      }
    }

    return null;
  }
}