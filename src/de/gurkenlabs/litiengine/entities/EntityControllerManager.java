package de.gurkenlabs.litiengine.entities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ai.IBehaviorController;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.physics.IMovementController;

/**
 * This class holds all controllers for the entities in the game. It is used as
 * a single hub to access and manage all the controllers.
 */
public class EntityControllerManager {
  private final Map<IEntity, IBehaviorController> behaviorControllers;
  private final Map<IEntity, IEntityAnimationController> animationControllers;
  private final Map<IEntity, IMovementController> movementControllers;

  public EntityControllerManager() {
    this.behaviorControllers = new ConcurrentHashMap<>();
    this.animationControllers = new ConcurrentHashMap<>();
    this.movementControllers = new ConcurrentHashMap<>();
  }

  /**
   * Adds the animation controller for the specified entity and detaches
   * previously added controllers from the game loop.
   * 
   * @param entity
   *          The entity for which the controller is added.
   * @param controller
   *          The {@link IAnimationController} that is added to this manager.
   */
  public void addController(final IEntity entity, final IEntityAnimationController controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.animationControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getAnimationController(entity));
    }

    this.animationControllers.put(entity, controller);
  }

  public void addController(final IEntity entity, final IBehaviorController controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.behaviorControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getBehaviorController(entity));
    }

    this.behaviorControllers.put(entity, controller);
  }

  public void addController(final IMobileEntity entity, final IMovementController controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.movementControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getMovementController(entity));
    }

    this.movementControllers.put(entity, controller);
  }

  public void disposeControllers(final IEntity entity) {
    final IBehaviorController behaviorController = this.getBehaviorController(entity);
    if (behaviorController != null) {

      Game.getLoop().detach(behaviorController);
      this.behaviorControllers.remove(entity);
    }

    if (entity instanceof IMobileEntity) {
      final IMovementController controller = this.getMovementController((IMobileEntity) entity);
      if (controller != null) {

        Game.getLoop().detach(controller);
        this.movementControllers.remove(entity);
      }
    }

    final IAnimationController animationController = this.getAnimationController(entity);
    if (animationController != null) {
      animationController.dispose();
      this.animationControllers.remove(entity);
    }
  }

  public IBehaviorController getBehaviorController(final IEntity entity) {
    if (this.behaviorControllers.containsKey(entity)) {
      return this.behaviorControllers.get(entity);
    }

    return null;
  }

  public IEntityAnimationController getAnimationController(final IEntity entity) {
    if (this.animationControllers.containsKey(entity)) {
      return this.animationControllers.get(entity);
    }

    return null;
  }

  public IMovementController getMovementController(final IMobileEntity entity) {
    if (this.movementControllers.containsKey(entity)) {
      return this.movementControllers.get(entity);
    }

    return null;
  }
}