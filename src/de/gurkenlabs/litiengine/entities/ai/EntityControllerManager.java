package de.gurkenlabs.litiengine.entities.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.physics.IMovementController;

public class EntityControllerManager {
  private final Map<IEntity, IEntityController<? extends IEntity>> aiControllers;
  private final Map<IEntity, IAnimationController> animationControllers;
  private final Map<IEntity, IMovementController<? extends IMovableEntity>> movementControllers;

  public EntityControllerManager() {
    this.aiControllers = new ConcurrentHashMap<>();
    this.animationControllers = new ConcurrentHashMap<>();
    this.movementControllers = new ConcurrentHashMap<>();
  }

  public <T extends IEntity> void addController(final T entity, final IEntityController<T> controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.aiControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getAIController(entity));
    }

    this.aiControllers.put(entity, controller);
  }

  public <T extends IMovableEntity> void addController(final T entity, final IMovementController<T> controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.movementControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getMovementController(entity));
    }

    this.movementControllers.put(entity, controller);
  }

  public void addController(final IEntity entity, final IAnimationController controller) {
    if (entity == null || controller == null) {
      return;
    }

    if (this.animationControllers.containsKey(entity)) {
      Game.getLoop().detach(this.getAnimationController(entity));
    }

    this.animationControllers.put(entity, controller);
  }

  public IEntityController<? extends IEntity> getAIController(final IEntity entity) {
    if (this.aiControllers.containsKey(entity)) {
      return this.aiControllers.get(entity);
    }

    return null;
  }

  public IMovementController<? extends IMovableEntity> getMovementController(final IMovableEntity entity) {
    if (this.movementControllers.containsKey(entity)) {
      return this.movementControllers.get(entity);
    }

    return null;
  }

  public IAnimationController getAnimationController(final IEntity entity) {
    if (this.animationControllers.containsKey(entity)) {
      return this.animationControllers.get(entity);
    }

    return null;
  }

  public void disposeControllers(IEntity entity) {
    IEntityController<? extends IEntity> aiController = this.getAIController(entity);
    if (aiController != null) {

      Game.getLoop().detach(aiController);
      this.aiControllers.remove(entity);
    }

    if (entity instanceof IMovableEntity) {
      IMovementController<? extends IMovableEntity> controller = this.getMovementController((IMovableEntity) entity);
      if (controller != null) {

        Game.getLoop().detach(controller);
        this.movementControllers.remove(entity);
      }
    }

    IAnimationController animationController = this.getAnimationController(entity);
    if (animationController != null) {
      animationController.dispose();
      this.animationControllers.remove(entity);
    }
  }
}