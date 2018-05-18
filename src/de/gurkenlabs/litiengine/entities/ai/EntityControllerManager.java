package de.gurkenlabs.litiengine.entities.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.physics.IMovementController;

/**
 * This class holds all controllers for the entities in the game. It is used as
 * a single hub to access and manage all the controllers.
 */
public class EntityControllerManager {
  private final Map<IEntity, IBehaviorController<? extends IEntity>> behaviorControllers;
  private final Map<IEntity, IEntityAnimationController<? extends IEntity>> animationControllers;
  private final Map<IEntity, IMovementController<? extends IMobileEntity>> movementControllers;

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
  public <T extends IEntity> void addController(final T entity, final IEntityAnimationController<T> controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.animationControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getAnimationController(entity));
    }

    this.animationControllers.put(entity, controller);
  }

  public <T extends IEntity> void addController(final T entity, final IBehaviorController<T> controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.behaviorControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getBehaviorController(entity));
    }

    this.behaviorControllers.put(entity, controller);
  }

  public <T extends IMobileEntity> void addController(final T entity, final IMovementController<T> controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.movementControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getMovementController(entity));
    }

    this.movementControllers.put(entity, controller);
  }

  public void disposeControllers(final IEntity entity) {
    final IBehaviorController<? extends IEntity> aiController = this.getBehaviorController(entity);
    if (aiController != null) {

      Game.getLoop().detach(aiController);
      this.behaviorControllers.remove(entity);
    }

    if (entity instanceof IMobileEntity) {
      final IMovementController<? extends IMobileEntity> controller = this.getMovementController((IMobileEntity) entity);
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

  public IBehaviorController<? extends IEntity> getBehaviorController(final IEntity entity) {
    if (this.behaviorControllers.containsKey(entity)) {
      return this.behaviorControllers.get(entity);
    }

    return null;
  }

  public IEntityAnimationController<? extends IEntity> getAnimationController(final IEntity entity) {
    if (this.animationControllers.containsKey(entity)) {
      return this.animationControllers.get(entity);
    }

    return null;
  }

  public IMovementController<? extends IMobileEntity> getMovementController(final IMobileEntity entity) {
    if (this.movementControllers.containsKey(entity)) {
      return this.movementControllers.get(entity);
    }

    return null;
  }
}