package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.Optional;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.ImageProcessing;

/**
 * This {@link AnimationController} implementation provides animation rules that
 * use naming conventions to provide {@link Animation}s for {@link Creature}
 * implementations.
 * 
 * The spritesheet images need to be named according to the following
 * conventions in order to be automatically used by this controller:
 * <ul>
 * <li>{@link #getSpritePrefix()}-idle-{DIRECTION}.{EXTENSION}</li>
 * <li>{@link #getSpritePrefix()}-walk-{DIRECTION}.{EXTENSION}</li>
 * </ul>
 * Where {DIRECTION} refers to a value of the {@link Direction} enum and
 * {@link #getSpritePrefix()} refers to the current sprite prefix of the entity.
 * {EXTENSION} refers to a value of the
 * {@link de.gurkenlabs.litiengine.graphics.ImageFormat} enum.
 * 
 * @see de.gurkenlabs.litiengine.entities.Creature
 * @see de.gurkenlabs.litiengine.entities.Direction
 * @see de.gurkenlabs.litiengine.entities.IEntity#getName()
 */
public class CreatureAnimationController<T extends Creature> extends EntityAnimationController<T> {
  public static final String IDLE = "-idle";
  public static final String WALK = "-walk";
  public static final String DEAD = "-dead";

  public CreatureAnimationController(T entity, boolean useFlippedSpritesAsFallback) {
    super(entity);
    this.init(useFlippedSpritesAsFallback);
  }

  public CreatureAnimationController(T entity, Animation defaultAnimation) {
    this(entity, true, defaultAnimation);
  }

  public CreatureAnimationController(T entity, boolean useFlippedSpritesAsFallback, Animation defaultAnimation, final Animation... animations) {
    super(entity, defaultAnimation, animations);
    this.init(useFlippedSpritesAsFallback);
  }

  @Override
  protected String getSpritePrefix() {
    return this.getEntity().getSpritePrefix();
  }

  /**
   * This method evaluates the current animation name that depends on certain
   * properties of the {@link #getEntity()}. Overwriting this method allows to
   * specify more sophisticated animations.
   * 
   * @return The name of the current animation that should be played
   */
  protected String getCurrentAnimationName() {
    if (this.getEntity().isDead()) {
      String deadName = this.getSpritePrefix() + DEAD;
      if (this.hasAnimation(deadName)) {
        return deadName;
      }
    }

    if (this.getEntity().isIdle()) {
      String idleName = this.getIdleSpriteName(this.getEntity().getFacingDirection());
      if (this.hasAnimation(idleName)) {
        return idleName;
      }

      return this.getWalkSpriteName(this.getEntity().getFacingDirection());
    }

    String walkName = this.getWalkSpriteName(this.getEntity().getFacingDirection());
    if (this.hasAnimation(walkName)) {
      return walkName;
    }

    return this.getIdleSpriteName(this.getEntity().getFacingDirection());
  }

  private void initializeAvailableAnimations() {
    for (Direction dir : Direction.values()) {
      // initialize walking animations
      Spritesheet walkSprite = Spritesheet.find(this.getSpriteName(WALK) + "-" + dir.toString().toLowerCase());
      if (walkSprite != null) {
        this.add(new Animation(walkSprite, true));
      }

      // initialize idle animations
      Spritesheet idleSprite = Spritesheet.find(this.getSpriteName(IDLE) + "-" + dir.toString().toLowerCase());
      if (idleSprite != null) {
        this.add(new Animation(idleSprite, true));
      }
    }

    Spritesheet deadSprite = Spritesheet.find(this.getSpritePrefix() + DEAD);
    if (deadSprite != null) {
      this.add(new Animation(deadSprite, true));
    }

    Spritesheet baseIdle = Spritesheet.find(this.getSpriteName(IDLE));
    if (baseIdle != null) {
      this.add(new Animation(baseIdle, true));
    }

    Spritesheet baseWalk = Spritesheet.find(this.getSpriteName(WALK));
    if (baseWalk != null) {
      this.add(new Animation(baseWalk, true));
    }
  }

  private void initializeFlippedAnimations() {
    String leftIdle = this.getSpriteName(IDLE) + "-left";
    String leftWalk = this.getSpriteName(WALK) + "-left";
    Optional<Animation> leftIdleAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(leftIdle)).findFirst();
    Optional<Animation> leftWalkAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(leftWalk)).findFirst();

    String rightIdle = this.getSpriteName(IDLE) + "-right";
    String rightWalk = this.getSpriteName(WALK) + "-right";
    Optional<Animation> rightIdleAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(rightIdle)).findFirst();
    Optional<Animation> rightWalkAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(rightWalk)).findFirst();

    if (!leftIdleAnimation.isPresent() && rightIdleAnimation.isPresent()) {
      this.add(flipAnimation(rightIdleAnimation.get().getSpritesheet(), leftIdle));
    }

    if (!leftWalkAnimation.isPresent() && rightWalkAnimation.isPresent()) {
      this.add(flipAnimation(rightWalkAnimation.get().getSpritesheet(), leftWalk));
    }

    if (!rightIdleAnimation.isPresent() && leftIdleAnimation.isPresent()) {
      this.add(flipAnimation(leftIdleAnimation.get().getSpritesheet(), rightIdle));
    }

    if (!rightWalkAnimation.isPresent() && leftWalkAnimation.isPresent()) {
      this.add(flipAnimation(leftWalkAnimation.get().getSpritesheet(), rightWalk));
    }
  }

  public Animation flipAnimation(Spritesheet spriteToFlip, String newSpriteName) {
    final BufferedImage leftIdleSprite = ImageProcessing.flipSpritesHorizontally(spriteToFlip);
    Spritesheet leftIdleSpritesheet = Spritesheet.load(leftIdleSprite, newSpriteName, spriteToFlip.getSpriteWidth(), spriteToFlip.getSpriteHeight());
    return new Animation(leftIdleSpritesheet, true);
  }

  private String getIdleSpriteName(Direction dir) {
    return this.getSpriteNameWithDirection(IDLE, dir);
  }

  private String getWalkSpriteName(Direction dir) {
    return getSpriteNameWithDirection(WALK, dir);
  }

  private String getSpriteNameWithDirection(String state, Direction dir) {
    String name = this.getSpriteName(state) + "-" + dir.toString().toLowerCase();
    if (this.hasAnimation(name)) {
      return name;
    }

    return getFallbackSpriteName(state, dir);
  }

  private static String getOppositeState(String state) {
    return state.equalsIgnoreCase(IDLE) ? WALK : IDLE;
  }

  private String getFallbackSpriteName(String state, Direction dir) {
    String fallbackStateName = this.getSpriteName(getOppositeState(state)) + "-" + dir.toString().toLowerCase();
    if (this.hasAnimation(fallbackStateName)) {
      return fallbackStateName;
    }

    String baseName = this.getSpriteName(state);
    if (this.hasAnimation(baseName)) {
      return baseName;
    }

    // search for any animation for the specified state with dir information
    for (Direction d : Direction.values()) {
      final String name = this.getSpriteName(state) + "-" + d.toString().toLowerCase();
      if (this.hasAnimation(name)) {
        return name;
      }
    }

    return this.getDefaultAnimation() != null ? this.getDefaultAnimation().getName() : null;
  }

  private String getSpriteName(String state) {
    return this.getSpritePrefix() + state;
  }

  private void init(boolean useFlippedSpritesAsFallback) {
    this.initializeAvailableAnimations();

    if (useFlippedSpritesAsFallback) {
      this.initializeFlippedAnimations();
    }

    this.addAnimationRule(e -> true, e -> this.getCurrentAnimationName());
  }
}
