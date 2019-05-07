package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController {
  private final List<AnimationRule> animationRules = new CopyOnWriteArrayList<>();
  private final T entity;
  private String spritePrefix;
  private boolean autoScaling;

  public EntityAnimationController(final T entity) {
    super();
    this.entity = entity;

    if (entity != null) {
      this.spritePrefix = ArrayUtilities.getRandom(getDefaultSpritePrefixes(entity.getClass()));
    }
  }

  public EntityAnimationController(final T entity, final Animation defaultAnimation, final Animation... animations) {
    super(defaultAnimation, animations);
    this.entity = entity;

    this.spritePrefix = ArrayUtilities.getRandom(getDefaultSpritePrefixes(entity.getClass()));
  }

  public EntityAnimationController(final T entity, final Spritesheet sprite) {
    this(entity, sprite, true);
  }

  public EntityAnimationController(final T entity, final Spritesheet sprite, boolean loop) {
    this(entity, new Animation(sprite, loop, Resources.spritesheets().getCustomKeyFrameDurations(sprite)));
  }

  public static String[] getDefaultSpritePrefixes(Class<?> cls) {
    AnimationInfo animationInfo = cls.getAnnotation(AnimationInfo.class);
    if (animationInfo != null && animationInfo.spritePrefix().length > 0) {
      return animationInfo.spritePrefix();
    } else {
      return new String[] { cls.getSimpleName().toLowerCase() };
    }
  }

  @Override
  public void addAnimationRule(Predicate<? super T> rule, Function<? super T, ? extends String> animationName, int priority) {
    this.animationRules.add(new AnimationRule(rule, animationName, priority));

    Collections.sort(this.animationRules);
  }
  
  @Override
  public void addAnimationRule(Predicate<IEntity> rule, Function<? super T, ? extends String> animationName) {
    this.animationRules.add(new AnimationRule(rule, animationName));

    Collections.sort(this.animationRules);
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  @Override
  public void update() {
    super.update();

    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return;
    }

    if (this.getCurrentAnimation() != null && !this.getCurrentAnimation().isLoop() && this.getCurrentAnimation().isPlaying()) {
      return;
    }

    if (this.getEntity() == null) {
      return;
    }

    for (AnimationRule animationRule : this.animationRules) {
      if (animationRule.getCondition().test(this.getEntity())) {
        final String animationName = animationRule.getAnimationName().apply(this.getEntity());
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

  protected List<AnimationRule> getAnimationRules() {
    return this.animationRules;
  }

  @Override
  public boolean isAutoScaling() {
    return this.autoScaling;
  }

  @Override
  public void setAutoScaling(boolean scaling) {
    this.autoScaling = scaling;
  }

  @Override
  public void scaleSprite(float scaleX, float scaleY) {
    final Point2D point = Game.world().camera().getViewportLocation(this.getEntity());
    double deltaX = (point.getX() - (point.getX() * scaleX));
    double deltaY = (point.getY() - (point.getY() * scaleY));

    BufferedImage img = this.getCurrentSprite();
    if (img != null) {
      double imgDeltaX = (img.getWidth() - (img.getWidth() * scaleX)) / 2.0;
      double imgDeltaY = (img.getHeight() - (img.getHeight() * scaleY)) / 2.0;

      deltaX += imgDeltaX;
      deltaY += imgDeltaY;
    }

    AffineTransform trans = new AffineTransform();
    trans.translate(deltaX, deltaY);
    trans.scale(scaleX, scaleY);

    this.setAffineTransform(trans);
  }

  @Override
  public void scaleSprite(float scale) {
    this.scaleSprite(scale, scale);
  }

  public static class AnimationRule<T extends IEntity> implements Comparable<AnimationRule> {
    private final Predicate<? super T> condition;
    private final Function<? super T, ? extends String> animationName;
    private int priority;

    public AnimationRule(Predicate<? super T> condition, Function<? super T, ? extends String> animationName) {
      this.condition = condition;
      this.animationName = animationName;
    }

    public AnimationRule(Predicate<? super T> condition, Function<? super T, ? extends String> animationName, int priority) {
      this(condition, animationName);
      this.priority = priority;
    }

    Predicate<? super T> getCondition() {
      return this.condition;
    }

    Function<? super T, ? extends String> getAnimationName() {
      return this.animationName;
    }

    public int getPriority() {
      return priority;
    }

    public void setPriority(int priority) {
      this.priority = priority;
    }

    @Override
    public int compareTo(AnimationRule o) {
      if (o == null) {
        return 1;
      }

      if (this.getPriority() == o.getPriority()) {
        return 0;
      }

      if (this.getPriority() < o.getPriority()) {
        return 1;
      }

      return -1;
    }
  }
}
