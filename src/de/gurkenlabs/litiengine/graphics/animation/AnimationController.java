package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.IImageEffect;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class AnimationController implements IAnimationController {
  private static final int MAX_IMAGE_EFFECTS = 20;
  private final Map<String, Animation> animations;
  private final List<IImageEffect> imageEffects;
  private final List<Consumer<Animation>> playbackConsumer;
  private final List<Consumer<Animation>> playbackFinishedConsumer;

  private Animation currentAnimation;
  private Animation defaultAnimation;
  private AffineTransform affineTransform;

  public AnimationController() {
    this.animations = new ConcurrentHashMap<>();
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
    this(new Animation(sprite, loop, Resources.spritesheets().getCustomKeyFrameDurations(sprite)));
  }

  public AnimationController(final Animation defaultAnimation, final Animation... animations) {
    this(defaultAnimation);

    if (animations != null && animations.length > 0) {
      for (final Animation anim : animations) {
        if (anim != null) {
          this.animations.put(anim.getName().toLowerCase(), anim);
        }
      }
    }
  }

  @Override
  public void add(final Animation animation) {
    if (animation == null) {
      return;
    }

    this.animations.put(animation.getName().toLowerCase(), animation);
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
    Game.loop().attach(this);
    for (final Animation animation : this.getAnimations()) {
      Game.loop().attach(animation);
    }
  }

  public void detach() {
    Game.loop().detach(this);
    for (final Animation animation : this.getAnimations()) {
      Game.loop().detach(animation);
    }
  }

  @Override
  public AffineTransform getAffineTransform() {
    return this.affineTransform;
  }

  @Override
  public Collection<Animation> getAnimations() {
    return this.animations.values();
  }

  @Override
  public Animation getAnimation(String animationName) {
    if (animationName == null || animationName.isEmpty()) {
      return null;
    }

    return this.animations.getOrDefault(animationName, null);
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
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
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
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
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

    return this.getAnimations().stream().findFirst().orElse(null);
  }

  @Override
  public List<IImageEffect> getImageEffects() {
    this.removeFinishedImageEffects();
    return this.imageEffects;
  }

  @Override
  public boolean hasAnimation(String animationName) {
    if (animationName == null || animationName.isEmpty()) {
      return false;
    }

    return this.animations.containsKey(animationName.toLowerCase());
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

    this.animations.remove(animation.getName().toLowerCase());

    if (this.getDefaultAnimation() != null && this.getDefaultAnimation().equals(animation)) {
      this.setDefaultAnimation(this.getAnimations().stream().findFirst().orElse(null));
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
      this.animations.remove(this.defaultAnimation.getName().toLowerCase());
    }

    this.defaultAnimation = defaultAnimation;
    if (this.defaultAnimation != null) {
      this.animations.put(this.defaultAnimation.getName().toLowerCase(), this.defaultAnimation);
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
