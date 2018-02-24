package de.gurkenlabs.litiengine.graphics.animation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.IEntity;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController<T> {
  private final Map<Predicate<T>, String> animationRules;
  private final T entity;
  private String spritePrefix;

  public EntityAnimationController(final T entity) {
    super();
    this.animationRules = new ConcurrentHashMap<>();
    this.entity = entity;

    AnimationInfo info = entity.getClass().getAnnotation(AnimationInfo.class);
    this.spritePrefix = info != null ? info.spritePrefix() : null;
  }

  public EntityAnimationController(final T entity, final Animation defaultAnimation, final Animation... animations) {
    super(defaultAnimation, animations);
    this.animationRules = new ConcurrentHashMap<>();
    this.entity = entity;

    AnimationInfo info = entity.getClass().getAnnotation(AnimationInfo.class);
    this.spritePrefix = info != null ? info.spritePrefix() : null;
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
  public void update() {
    super.update();

    if (this.getCurrentAnimation() != null && !this.getCurrentAnimation().isLoop() && this.getCurrentAnimation().isPlaying()) {
      return;
    }

    for (Entry<Predicate<T>, String> animationRule : this.animationRules.entrySet()) {
      if (animationRule.getKey().test(this.getEntity())) {
        this.playAnimation(animationRule.getValue());
        break;
      }
    }
  }

  protected String getSpritePrefix() {
    return this.spritePrefix;
  }

  protected void setSpritePrefix(String prefix) {
    this.spritePrefix = prefix;
  }
}
