package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.IImageEffect;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class AnimationController implements IAnimationController {
  private static final int MAX_IMAGE_EFFECTS = 20;
  private final List<Animation> animations;
  private final List<IImageEffect> imageEffects;
  private final List<Consumer<Animation>> playbackConsumer;
  private final List<Consumer<Animation>> playbackFinishedConsumer;

  private Animation currentAnimation;
  private Animation defaultAnimation;
  private AffineTransform affineTransform;

  public AnimationController() {
    this.animations = new CopyOnWriteArrayList<>();
    this.imageEffects = new CopyOnWriteArrayList<>();
    this.playbackFinishedConsumer = new CopyOnWriteArrayList<>();
    this.playbackConsumer = new CopyOnWriteArrayList<>();
  }

  public AnimationController(final Animation defaultAnimation) {
    this();
    this.setDefaultAnimation(defaultAnimation);
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
    Collections.sort(this.getImageEffects());
  }

  public void attach() {
    Game.getLoop().attach(this);
    for (final Animation animation : this.getAnimations()) {
      Game.getLoop().attach(animation);
    }
  }

  public void detach() {
    Game.getLoop().detach(this);
    for (final Animation animation : this.getAnimations()) {
      Game.getLoop().detach(animation);
    }
  }

  @Override
  public AffineTransform getAffineTransform() {
    return this.affineTransform;
  }

  @Override
  public List<Animation> getAnimations() {
    return this.animations;
  }

  @Override
  public Animation getAnimation(String animationName) {
    final Optional<Animation> opt = this.getAnimations().stream().filter(x -> x != null && x.getName().equalsIgnoreCase(animationName)).findFirst();
    if (!opt.isPresent()) {
      return null;
    }

    return opt.get();
  }

  @Override
  public Animation getCurrentAnimation() {
    return this.currentAnimation;
  }

  @Override
  public BufferedImage getCurrentSprite() {
    final Animation current = this.getCurrentAnimation();
    if (current == null || current.getSpritesheet() == null || current.getCurrentKeyFrame() == null) {
      return null;
    }

    final String cacheKey = buildCurrentCacheKey();
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    BufferedImage sprite = current.getSpritesheet().getSprite(current.getCurrentKeyFrame().getSpriteIndex());
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
    if (this.defaultAnimation != null) {
      return this.defaultAnimation;
    }

    if (this.getAnimations().isEmpty()) {
      return null;
    }

    return this.getAnimations().get(0);
  }

  @Override
  public List<IImageEffect> getImageEffects() {
    this.removeFinishedImageEffects();
    return this.imageEffects;
  }

  @Override
  public boolean hasAnimation(String animationName) {
    if (animationName == null || animationName.isEmpty() || this.getAnimations() == null) {
      return false;
    }

    return this.getAnimations().stream().anyMatch(x -> x.getName() != null && x.getName().equalsIgnoreCase(animationName));
  }

  @Override
  public boolean isPlaying(String animationName) {
    return this.getCurrentAnimation() != null && this.getCurrentAnimation().getName() != null && this.getCurrentAnimation().getName().equalsIgnoreCase(animationName);
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
    // if we have no animation with the name or it is already playing, do nothing
    if (this.isPlaying(animationName) || !this.hasAnimation(animationName)) {
      return;
    }

    final Animation anim = this.getAnimation(animationName);
    if (anim == null) {
      return;
    }

    // ensure that only one animation is playing at a time
    if (this.getCurrentAnimation() != null) {
      this.getCurrentAnimation().terminate();
    }

    this.currentAnimation = anim;
    this.currentAnimation.start();

    for (final Consumer<Animation> cons : this.playbackConsumer) {
      cons.accept(this.getCurrentAnimation());
    }
  }

  @Override
  public void remove(Animation animation) {
    if (animation == null) {
      return;
    }

    this.animations.remove(animation);

    if (this.getDefaultAnimation() != null && this.getDefaultAnimation().equals(animation)) {
      this.setDefaultAnimation(!this.getAnimations().isEmpty() ? this.getAnimations().get(0) : null);
    }
  }

  @Override
  public void remove(IImageEffect effect) {
    if (effect == null) {
      return;
    }

    this.imageEffects.remove(effect);
  }

  @Override
  public void setDefaultAnimation(Animation defaultAnimation) {
    if (this.defaultAnimation != null) {
      this.animations.remove(this.defaultAnimation);
    }

    this.defaultAnimation = defaultAnimation;
    if (this.defaultAnimation != null) {
      this.animations.add(this.defaultAnimation);
    }
  }

  @Override
  public void update() {
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
      if (this.getDefaultAnimation() != null) {
        this.playAnimation(this.getDefaultAnimation().getName());
      } else {
        this.currentAnimation = null;
      }
    }
  }

  protected String buildCurrentCacheKey() {
    if (this.getCurrentAnimation() == null || this.getCurrentAnimation().getCurrentKeyFrame() == null || this.getCurrentAnimation().getSpritesheet() == null) {
      return null;
    }
    final StringBuilder cacheKey = new StringBuilder();
    cacheKey.append(this.getCurrentAnimation().getSpritesheet().hashCode());
    cacheKey.append('_');
    cacheKey.append(this.getCurrentAnimation().getCurrentKeyFrame().getSpriteIndex());
    cacheKey.append('_');

    this.getImageEffects().forEach(x -> cacheKey.append(x.getName().hashCode()));
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

  @Override
  public void setAffineTransform(AffineTransform affineTransform) {
    this.affineTransform = affineTransform;
  }
}
