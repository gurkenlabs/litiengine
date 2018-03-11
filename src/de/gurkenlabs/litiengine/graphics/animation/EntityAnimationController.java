package de.gurkenlabs.litiengine.graphics.animation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController<T> {
  private final Map<Predicate<T>, Function<T, String>> animationRules;
  private final List<IRenderable> rendering;
  private final List<IRenderable> rendered;

  private final T entity;
  private String spritePrefix;

  public EntityAnimationController(final T entity) {
    super();
    this.animationRules = new ConcurrentHashMap<>();
    this.rendered = new CopyOnWriteArrayList<>();
    this.rendering = new CopyOnWriteArrayList<>();

    this.entity = entity;

    if (entity != null) {
      this.spritePrefix = ArrayUtilities.getRandom(getDefaultSpritePrefixes(entity.getClass()));
    }
  }

  public EntityAnimationController(final T entity, final Animation defaultAnimation, final Animation... animations) {
    super(defaultAnimation, animations);
    this.animationRules = new ConcurrentHashMap<>();
    this.rendered = new CopyOnWriteArrayList<>();
    this.rendering = new CopyOnWriteArrayList<>();
    this.entity = entity;

    this.spritePrefix = ArrayUtilities.getRandom(getDefaultSpritePrefixes(entity.getClass()));
  }

  public static <T> String[] getDefaultSpritePrefixes(Class<T> cls) {
    AnimationInfo animationInfo = cls.getAnnotation(AnimationInfo.class);
    if (animationInfo != null && animationInfo.spritePrefix().length > 0) {
      return animationInfo.spritePrefix();
    } else {
      return new String[] { cls.getSimpleName().toLowerCase() };
    }
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
