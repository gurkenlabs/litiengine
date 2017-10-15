package de.gurkenlabs.litiengine.graphics.animation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController<T> {
  private final T entity;

  private final Map<Predicate<T>, String> animationRules;

  public EntityAnimationController(final T entity, final Animation defaultAnimation, final Animation... animations) {
    super(defaultAnimation, animations);
    this.animationRules = new ConcurrentHashMap<>();
    this.entity = entity;
  }

  @Override
  public void addAnimationRule(Predicate<T> rule, String animationName) {
    this.animationRules.put(rule, animationName);
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);

    for (Entry<Predicate<T>, String> animationRule : this.animationRules.entrySet()) {
      if (animationRule.getKey().test(this.getEntity())) {
        this.playAnimation(animationRule.getValue());
        break;
      }
    }
  }
}
