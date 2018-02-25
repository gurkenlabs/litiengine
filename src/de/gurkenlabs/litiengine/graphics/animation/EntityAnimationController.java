package de.gurkenlabs.litiengine.graphics.animation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.IEntity;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController<T> {
  private final Map<Predicate<T>, Function<T, String>> animationRules;
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
  public void addAnimationRule(Predicate<T> rule, Function<T, String> animationName) {
    this.animationRules.put(rule, animationName);
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  @Override
  public void update() {
    super.update();

    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return;
    }

    if (this.getCurrentAnimation() != null && !this.getCurrentAnimation().isLoop() && this.getCurrentAnimation().isPlaying()) {
      return;
    }

    if (this.getEntity() == null) {
      return;
    }

    for (Entry<Predicate<T>, Function<T, String>> animationRule : this.animationRules.entrySet()) {
      if (animationRule.getKey().test(this.getEntity())) {
        final String animationName = animationRule.getValue().apply(this.getEntity());
        if (this.getCurrentAnimation() == null || animationName != null && !animationName.isEmpty() && !this.getCurrentAnimation().getName().equalsIgnoreCase(animationName)) {
          this.playAnimation(animationName);
        }
        
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
