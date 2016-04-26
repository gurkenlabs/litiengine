package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.graphics.IImageEffect;
import de.gurkenlabs.util.image.ImageProcessing;

public abstract class AnimationController implements IAnimationController {
  private final List<Animation> animations;
  private final List<IImageEffect> imageEffects;
  private final Animation defaultAnimation;
  private Animation currentAnimation;

  public AnimationController(final Animation defaultAnimation, final Animation... animations) {
    this.animations = new CopyOnWriteArrayList<>();
    this.imageEffects = new CopyOnWriteArrayList<>();
    this.defaultAnimation = defaultAnimation;
    this.animations.add(this.defaultAnimation);

    if (animations != null && animations.length > 0) {
      for (final Animation anim : animations) {
        this.animations.add(anim);
      }
    }

    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public void add(final Animation animation) {
    final Optional<Animation> oldAnimation = this.animations.stream().filter(x -> x.getName().equalsIgnoreCase(animation.getName())).findFirst();
    if (oldAnimation.isPresent()) {
      this.animations.remove(oldAnimation.get());
    }

    this.animations.add(animation);
  }

  @Override
  public void add(final IImageEffect effect) {
    final List<IImageEffect> effects = this.getImageEffects();
    if (effects.contains(effect)) {
      effects.remove(effect);
    }

    effects.add(effect);
  }

  @Override
  public List<Animation> getAnimations() {
    return this.animations;
  }

  @Override
  public Animation getCurrentAnimation() {
    return this.currentAnimation;
  }

  @Override
  public BufferedImage getCurrentSprite() {
    if (this.getCurrentAnimation() == null) {
      return null;
    }

    BufferedImage sprite = this.getCurrentAnimation().getSpritesheet().getSprite(this.getCurrentAnimation().getCurrentKeyFrame().getSpriteIndex());
    for (final IImageEffect effect : this.getImageEffects()) {
      sprite = effect.apply(sprite);
    }

    return sprite;
  }

  @Override
  public BufferedImage getCurrentSprite(final int width, final int height) {
    return ImageProcessing.scaleImage(this.getCurrentSprite(), width, height);
  }

  @Override
  public List<IImageEffect> getImageEffects() {
    this.removeFinishedImageEffects();
    return this.imageEffects;
  }

  @Override
  public void playAnimation(final String animationName) {
    // if we have no animation with the name or it is already playing, do
    // nothing
    if (!this.getAnimations().stream().anyMatch(x -> x.getName().equalsIgnoreCase(animationName)) || this.getCurrentAnimation() != null && this.getCurrentAnimation().getName().equalsIgnoreCase(animationName)) {
      return;
    }

    // ensure that only one animation is playing at a time
    if (this.getCurrentAnimation() != null) {
      this.getCurrentAnimation().terminate();
    }

    final Animation anim = this.getAnimations().stream().filter(x -> x.getName().equalsIgnoreCase(animationName)).findFirst().get();
    if (anim == null) {
      return;
    }

    this.currentAnimation = anim;
    this.currentAnimation.start();
  }

  @Override
  public void update(final IGameLoop loop) {
    if (this.getCurrentAnimation() == null || this.getCurrentAnimation() != null && !this.getCurrentAnimation().isPlaying()) {
      this.currentAnimation = this.defaultAnimation;

      if (this.currentAnimation != null) {
        this.currentAnimation.start();
      }
    }
  }

  protected String buildCurrentCacheKey() {
    final StringBuilder cacheKey = new StringBuilder();
    cacheKey.append(this.getCurrentAnimation().getSpritesheet().hashCode());
    cacheKey.append('_');
    cacheKey.append(this.getCurrentAnimation().getCurrentKeyFrame().getSpriteIndex());
    cacheKey.append('_');
    this.getImageEffects().forEach(x -> cacheKey.append(x.getName()));
    return cacheKey.toString();
  }

  private void removeFinishedImageEffects() {
    for (int i = 0; i < this.imageEffects.size(); i++) {
      final IImageEffect effect = this.imageEffects.get(i);
      if (effect == null) {
        continue;
      }

      if (effect.timeToLiveReached()) {
        this.imageEffects.remove(i);
      }
    }

    this.imageEffects.removeAll(Collections.singleton(null));
  }

}
