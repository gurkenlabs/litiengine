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
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

public class AnimationController implements IAnimationController {
  private static final int MAX_IMAGE_EFFECTS = 20;
  private final List<Animation> animations;
  private Animation currentAnimation;
  private final Animation defaultAnimation;
  private final List<IImageEffect> imageEffects;
  private final List<Consumer<Animation>> playbackConsumer;
  private final List<Consumer<Animation>> playbackFinishedConsumer;

  private AnimationController(final Animation defaultAnimation) {
    this.animations = new CopyOnWriteArrayList<>();
    this.imageEffects = new CopyOnWriteArrayList<>();
    this.playbackFinishedConsumer = new CopyOnWriteArrayList<>();
    this.playbackConsumer = new CopyOnWriteArrayList<>();
    this.defaultAnimation = defaultAnimation;
    if (this.defaultAnimation != null) {
      this.animations.add(this.defaultAnimation);
    }
  }

  public AnimationController(final Spritesheet sprite) {
    this(sprite, true);
  }

  public AnimationController(final Spritesheet sprite, boolean loop) {
    this(new Animation(sprite, loop, Spritesheet.getCustomKeyFrameDurations(sprite)));
  }

  public AnimationController(final Animation defaultAnimation, final Animation... animations) {
    this(defaultAnimation);

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

  @Override
  public void dispose() {
    Game.getLoop().detach(this);
    for (final Animation animation : this.getAnimations()) {
      Game.getLoop().detach(animation);
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

    final String cacheKey = buildCurrentCacheKey();
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    BufferedImage sprite = this.getCurrentAnimation().getSpritesheet().getSprite(this.getCurrentAnimation().getCurrentKeyFrame().getSpriteIndex());
    for (final IImageEffect effect : this.getImageEffects()) {
      sprite = effect.apply(sprite);
    }

    return sprite;
  }

  @Override
  public BufferedImage getCurrentSprite(final int width, final int height) {
    if (this.getCurrentSprite() == null) {
      return null;
    }

    final String cacheKey = buildCurrentCacheKey() + "_" + width + "_" + height;
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    return ImageProcessing.scaleImage(this.getCurrentSprite(), width, height);

  }

  @Override
  public Animation getDefaultAnimation() {
    return this.defaultAnimation;
  }

  @Override
  public List<IImageEffect> getImageEffects() {
    this.removeFinishedImageEffects();
    return this.imageEffects;
  }

  @Override
  public void onPlayback(final Consumer<Animation> cons) {
    this.playbackConsumer.add(cons);
  }

  @Override
  public void onPlaybackEnded(final Consumer<Animation> cons) {
    this.playbackFinishedConsumer.add(cons);
  }

  @Override
  public void playAnimation(final String animationName) {
    // if we have no animation with the name or it is already playing, do
    // nothing
    if (this.getAnimations() == null || !this.getAnimations().stream().anyMatch(x -> x != null && x.getName() != null && x.getName().equalsIgnoreCase(animationName))
        || this.getCurrentAnimation() != null && this.getCurrentAnimation().getName() != null && this.getCurrentAnimation().getName().equalsIgnoreCase(animationName)) {
      return;
    }

    // ensure that only one animation is playing at a time
    if (this.getCurrentAnimation() != null) {
      this.getCurrentAnimation().terminate();
    }

    final Optional<Animation> opt = this.getAnimations().stream().filter(x -> x != null && x.getName().equalsIgnoreCase(animationName)).findFirst();
    if (!opt.isPresent()) {
      return;
    }

    final Animation anim = opt.get();

    this.currentAnimation = anim;
    this.currentAnimation.start();

    for (final Consumer<Animation> cons : this.playbackConsumer) {
      cons.accept(this.getCurrentAnimation());
    }
  }

  @Override
  public void update(final IGameLoop loop) {
    if (this.getCurrentAnimation() != null && this.getCurrentAnimation().isPaused()) {
      return;
    }
    final boolean playbackFinished = this.getCurrentAnimation() != null && !this.getCurrentAnimation().isPlaying();
    if (playbackFinished) {
      for (final Consumer<Animation> cons : this.playbackFinishedConsumer) {
        cons.accept(this.getCurrentAnimation());
      }
    }

    if (this.getCurrentAnimation() == null || playbackFinished) {
      if (this.defaultAnimation != null) {
        this.playAnimation(this.defaultAnimation.getName());
      } else {
        this.currentAnimation = null;
      }
    }

  }

  protected String buildCurrentCacheKey() {
    if (this.getCurrentAnimation() == null || this.getCurrentAnimation().getCurrentKeyFrame() == null) {
      return null;
    }
    final StringBuilder cacheKey = new StringBuilder();
    cacheKey.append(this.getCurrentAnimation().getSpritesheet().hashCode());
    cacheKey.append('_');
    cacheKey.append(this.getCurrentAnimation().getCurrentKeyFrame().getSpriteIndex());
    cacheKey.append('_');

    final StringBuilder effectsString = new StringBuilder();
    this.getImageEffects().forEach(x -> effectsString.append(x.getName()));
    cacheKey.append(effectsString.toString().hashCode());
    return cacheKey.toString();
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
}
