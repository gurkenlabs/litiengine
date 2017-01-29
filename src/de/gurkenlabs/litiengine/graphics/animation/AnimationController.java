package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.graphics.IImageEffect;
import de.gurkenlabs.util.image.ImageProcessing;

public abstract class AnimationController implements IAnimationController {
  private static int MAX_IMAGE_EFFECTS = 20;
  private final List<Consumer<Animation>> playbackFinishedConsumer;
  private final List<Consumer<Animation>> playbackConsumer;
  private final List<Animation> animations;
  private final List<IImageEffect> imageEffects;
  private final Animation defaultAnimation;
  private Animation currentAnimation;

  public AnimationController(final Animation defaultAnimation, final Animation... animations) {
    this.animations = new CopyOnWriteArrayList<>();
    this.imageEffects = new CopyOnWriteArrayList<>();
    this.playbackFinishedConsumer = new CopyOnWriteArrayList<>();
    this.playbackConsumer = new CopyOnWriteArrayList<>();
    this.defaultAnimation = defaultAnimation;
    this.animations.add(this.defaultAnimation);

    if (animations != null && animations.length > 0) {
      for (final Animation anim : animations) {
        if (anim != null) {
          this.animations.add(anim);
        }
      }
    }
  }

  @Override
  public void add(final Animation animation) {
    if (animation == null) {
      return;
    }

    final Optional<Animation> oldAnimation = this.animations.stream().filter(x -> x.getName().equalsIgnoreCase(animation.getName())).findFirst();
    if (oldAnimation.isPresent()) {
      this.animations.remove(oldAnimation.get());
    }

    this.animations.add(animation);
  }

  @Override
  public void add(final IImageEffect effect) {
    if (this.getImageEffects().size() >= MAX_IMAGE_EFFECTS) {
      return;
    }

    this.getImageEffects().add(effect);
  }

  protected String buildCurrentCacheKey() {
    if (this.getCurrentAnimation().getCurrentKeyFrame() == null) {
      return null;
    }
    final StringBuilder cacheKey = new StringBuilder();
    cacheKey.append(this.getCurrentAnimation().getSpritesheet().hashCode());
    cacheKey.append('_');
    cacheKey.append(this.getCurrentAnimation().getCurrentKeyFrame().getSpriteIndex());
    cacheKey.append('_');

    StringBuilder imageEffects = new StringBuilder();
    this.getImageEffects().forEach(x -> imageEffects.append(x.getName()));
    cacheKey.append(imageEffects.toString().hashCode());
    return cacheKey.toString();
  }

  @Override
  public void dispose() {
    Game.getLoop().unregisterFromUpdate(this);
    for (final Animation animation : this.getAnimations()) {
      Game.getLoop().unregisterFromUpdate(animation);
    }
    this.getAnimations().clear();
    this.getImageEffects().clear();
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
    if (this.getCurrentAnimation() == null || this.getCurrentAnimation().getSpritesheet() == null || this.getCurrentAnimation().getCurrentKeyFrame() == null) {
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
    if (this.getAnimations() == null || !this.getAnimations().stream().anyMatch(x -> x != null && x.getName() != null && x.getName().equalsIgnoreCase(animationName))
        || (this.getCurrentAnimation() != null && this.getCurrentAnimation().getName() != null && this.getCurrentAnimation().getName().equalsIgnoreCase(animationName))) {
      return;
    }

    // ensure that only one animation is playing at a time
    if (this.getCurrentAnimation() != null) {
      this.getCurrentAnimation().terminate();
    }

    final Animation anim = this.getAnimations().stream().filter(x -> x != null && x.getName().equalsIgnoreCase(animationName)).findFirst().get();
    if (anim == null) {
      return;
    }

    this.currentAnimation = anim;
    this.currentAnimation.start();

    for (Consumer<Animation> cons : this.playbackConsumer) {
      cons.accept(this.getCurrentAnimation());
    }
  }

  private void removeFinishedImageEffects() {
    final List<IImageEffect> effectsToRemove = new ArrayList<>();
    for (final IImageEffect effect : this.imageEffects) {
      if (effect == null) {
        continue;
      }

      if (effect.timeToLiveReached()) {
        effectsToRemove.add(effect);
      }
    }

    this.imageEffects.removeAll(effectsToRemove);
    this.imageEffects.removeAll(Collections.singleton(null));
  }

  @Override
  public void update(final IGameLoop loop) {
    if (this.getCurrentAnimation() != null && this.getCurrentAnimation().isPaused()) {
      return;
    }
    final boolean playbackFinished = this.getCurrentAnimation() != null && !this.getCurrentAnimation().isPlaying();
    if (playbackFinished) {
      for (Consumer<Animation> cons : this.playbackFinishedConsumer) {
        cons.accept(this.getCurrentAnimation());
      }
    }

    if (this.getCurrentAnimation() == null || playbackFinished) {
      this.currentAnimation = null;
      if (this.defaultAnimation != null) {
        this.playAnimation(this.defaultAnimation.getName());
      }
    }
  }

  @Override
  public void onPlaybackEnded(Consumer<Animation> cons) {
    this.playbackFinishedConsumer.add(cons);
  }

  @Override
  public void onPlayback(Consumer<Animation> cons) {
    this.playbackConsumer.add(cons);
  }
}
