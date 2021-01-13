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

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILoop;
import de.gurkenlabs.litiengine.graphics.ImageEffect;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

public class AnimationController implements IAnimationController {
  private static final int MAX_IMAGE_EFFECTS = 20;
  private AffineTransform affineTransform;
  private final Map<String, Animation> animations;
  private Animation currentAnimation;

  private Animation defaultAnimation;
  private boolean enabled;
  private final List<ImageEffect> imageEffects;
  private final List<AnimationListener> listeners;

  /**
   * Initializes a new instance of the {@code AnimationController} class.
   */
  public AnimationController() {
    this.animations = new ConcurrentHashMap<>();
    this.imageEffects = new CopyOnWriteArrayList<>();
    this.listeners = new CopyOnWriteArrayList<>();
    this.enabled = true;
  }

  /**
   * Initializes a new instance of the {@code AnimationController} class with the specified default animation.
   * 
   * @param defaultAnimation
   *          The default animation for this controller.
   * 
   * @see #getDefault()
   */
  public AnimationController(final Animation defaultAnimation) {
    this();
    this.setDefault(defaultAnimation);
  }

  /**
   * Initializes a new instance of the {@code AnimationController} class with the specified default animation.
   * 
   * @param defaultAnimation
   *          The default animation for this controller.
   * @param animations
   *          Additional animations that are managed by this controller instance.
   * 
   * @see #getDefault()
   * @see #getAll()
   */
  public AnimationController(final Animation defaultAnimation, final Animation... animations) {
    this(defaultAnimation);

    if (animations != null && animations.length > 0) {
      for (final Animation anim : animations) {
        if (anim != null) {
          this.animations.put(anim.getName(), anim);
        }
      }
    }
  }

  /**
   * Initializes a new instance of the {@code AnimationController} class with the specified default animation.
   * 
   * @param sprite
   *          The sprite sheet used by the default animation of this controller.
   */
  public AnimationController(final Spritesheet sprite) {
    this(sprite, true);
  }

  /**
   * Initializes a new instance of the {@code AnimationController} class with the specified default animation.
   * 
   * @param sprite
   *          The sprite sheet used by the default animation of this controller.
   * 
   * @param loop
   *          A flag indicating whether the default animation should be looped or only played once.
   */
  public AnimationController(final Spritesheet sprite, final boolean loop) {
    this(new Animation(sprite, loop, Resources.spritesheets().getCustomKeyFrameDurations(sprite)));
  }

  public static Animation flipAnimation(Animation anim, String newSpriteName) {
    final BufferedImage flippedImage = Imaging.flipSpritesHorizontally(anim.getSpritesheet());
    Spritesheet flippedSpritesheet = Resources.spritesheets().load(flippedImage, newSpriteName, anim.getSpritesheet().getSpriteWidth(), anim.getSpritesheet().getSpriteHeight());
    return new Animation(flippedSpritesheet, anim.isLooping(), anim.getKeyFrameDurations());
  }

  @Override
  public void add(final Animation animation) {
    if (animation == null) {
      return;
    }

    // the first animation that is added to the controller is defined as default animation
    if (this.defaultAnimation == null) {
      this.defaultAnimation = animation;
    }

    this.animations.put(animation.getName(), animation);
  }

  @Override
  public void add(final ImageEffect effect) {
    if (this.getImageEffects().size() >= MAX_IMAGE_EFFECTS) {
      return;
    }

    this.getImageEffects().add(effect);
    Collections.sort(this.getImageEffects());
  }

  @Override
  public void addListener(final AnimationListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Attach the {@code AnimationController}, as well as all its {@code Animation}s to the Game loop.
   * 
   * @see ILoop
   */
  public void attach() {
    Game.loop().attach(this);
  }

  @Override
  public void clear() {
    this.animations.clear();
  }

  /**
   * Detach the {@code AnimationController}, as well as all its {@code Animation}s from the Game loop.
   * 
   * @see ILoop
   */
  public void detach() {
    Game.loop().detach(this);
  }

  @Override
  public Animation get(final String animationName) {
    if (animationName == null || animationName.isEmpty()) {
      return null;
    }

    return this.animations.getOrDefault(animationName, null);
  }

  @Override
  public AffineTransform getAffineTransform() {
    return this.affineTransform;
  }

  @Override
  public Collection<Animation> getAll() {
    return this.animations.values();
  }

  @Override
  public Animation getCurrent() {
    return this.currentAnimation;
  }

  @Override
  public BufferedImage getCurrentImage() {
    if (!this.isEnabled()) {
      return null;
    }

    final Animation current = this.getCurrent();
    if (current == null || current.getSpritesheet() == null || current.getCurrentKeyFrame() == null) {
      return null;
    }

    final String cacheKey = this.buildCurrentCacheKey();
    final Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    BufferedImage sprite = current.getSpritesheet().getSprite(current.getCurrentKeyFrame().getSpriteIndex());
    for (final ImageEffect effect : this.getImageEffects()) {
      sprite = effect.apply(sprite);
    }

    return sprite;
  }

  @Override
  public BufferedImage getCurrentImage(final int width, final int height) {
    if (this.getCurrentImage() == null) {
      return null;
    }

    final String cacheKey = this.buildCurrentCacheKey() + "_" + width + "_" + height;
    final Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    return Imaging.scale(this.getCurrentImage(), width, height);
  }

  @Override
  public Animation getDefault() {
    if (this.defaultAnimation != null) {
      return this.defaultAnimation;
    }

    if (this.getAll().isEmpty()) {
      return null;
    }

    return this.getAll().stream().findFirst().orElse(null);
  }

  @Override
  public List<ImageEffect> getImageEffects() {
    this.removeFinishedImageEffects();
    return this.imageEffects;
  }

  @Override
  public boolean hasAnimation(final String animationName) {
    if (animationName == null || animationName.isEmpty()) {
      return false;
    }

    return this.animations.containsKey(animationName);
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public boolean isPlaying(final String animationName) {
    return this.getCurrent() != null && this.getCurrent().getName() != null && this.getCurrent().getName().equalsIgnoreCase(animationName);
  }

  @Override
  public void play(final String animationName) {
    // if we have no animation with the name or it is already playing, do nothing
    if (this.isPlaying(animationName) || !this.hasAnimation(animationName)) {
      return;
    }

    final Animation anim = this.get(animationName);
    if (anim == null) {
      return;
    }

    // ensure that only one animation is playing at a time
    if (this.getCurrent() != null) {
      this.getCurrent().terminate();
    }

    this.currentAnimation = anim;
    this.currentAnimation.start();

    for (final AnimationListener listener : this.listeners) {
      listener.played(this.getCurrent());
    }
  }

  @Override
  public void remove(final Animation animation) {
    if (animation == null) {
      return;
    }

    this.animations.remove(animation.getName());
    if (this.currentAnimation != null && this.currentAnimation.equals(animation)) {
      this.currentAnimation = null;
    }

    if (this.getDefault() != null && this.getDefault().equals(animation)) {
      this.setDefault(this.getAll().stream().findFirst().orElse(null));
    }
  }

  @Override
  public void remove(final ImageEffect effect) {
    if (effect == null) {
      return;
    }

    this.imageEffects.remove(effect);
  }

  @Override
  public void removeListener(final AnimationListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public void setAffineTransform(final AffineTransform affineTransform) {
    this.affineTransform = affineTransform;
  }

  @Override
  public void setDefault(final Animation defaultAnimation) {
    if (this.defaultAnimation != null) {
      this.animations.remove(this.defaultAnimation.getName());
      if (this.currentAnimation != null && this.currentAnimation.equals(this.defaultAnimation)) {
        this.currentAnimation = null;
      }
    }

    this.defaultAnimation = defaultAnimation;
    if (this.defaultAnimation != null) {
      this.animations.put(this.defaultAnimation.getName(), this.defaultAnimation);
    }
  }

  @Override
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void update() {
    for (final Animation animation : this.getAll()) {
      animation.update();
    }
    
    if (this.getCurrent() != null && this.getCurrent().isPaused()) {
      return;
    }

    final boolean playbackFinished = this.getCurrent() != null && !this.getCurrent().isPlaying();
    if (playbackFinished) {
      for (final AnimationListener listener : this.listeners) {
        listener.finished(this.getCurrent());
      }
    }

    if (this.getCurrent() == null || playbackFinished) {
      if (this.getDefault() != null) {
        this.play(this.getDefault().getName());
      } else {
        this.currentAnimation = null;
      }
    }
  }

  /**
   * Build a unique cache key for the current frame.
   * The spritesheet's {@code hashCode}, the current keyframe's sprite index, as well as all applied {@code ImageEffect}s' names, are
   * considered when determining the current cache key.
   * 
   * @return the unique cache key for the current key frame
   */
  protected String buildCurrentCacheKey() {
    if (this.getCurrent() == null || this.getCurrent().getCurrentKeyFrame() == null || this.getCurrent().getSpritesheet() == null) {
      return null;
    }
    final StringBuilder cacheKey = new StringBuilder();
    cacheKey.append(this.getCurrent().getSpritesheet().hashCode());
    cacheKey.append('_');
    cacheKey.append(this.getCurrent().getCurrentKeyFrame().getSpriteIndex());
    cacheKey.append('_');

    this.getImageEffects().forEach(x -> cacheKey.append(x.getName().hashCode()));
    return cacheKey.toString();
  }

  private void removeFinishedImageEffects() {
    final List<ImageEffect> effectsToRemove = new ArrayList<>();
    for (final ImageEffect effect : this.imageEffects) {
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
