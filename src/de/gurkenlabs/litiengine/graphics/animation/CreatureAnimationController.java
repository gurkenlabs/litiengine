package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.Optional;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

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
 * {@link de.gurkenlabs.litiengine.resources.ImageFormat} enum.
 * 
 * @param <T>
 *          The type of the creature for which animations are managed by this controller.
 * @see de.gurkenlabs.litiengine.entities.Creature
 * @see de.gurkenlabs.litiengine.Direction
 * @see de.gurkenlabs.litiengine.entities.IEntity#getName()
 */
public class CreatureAnimationController<T extends Creature> extends EntityAnimationController<T> {
  private String[] customDeathAnimations;
  private String randomDeathSprite;

  /**
   * Initializes a new instance of the <code>CreatureAnimationController</code> class.
   * 
   * @param creature
   *          The creature related to this controller.
   * 
   * @param useFlippedSpritesAsFallback
   *          A flag indicating whether this controller should flip the provided spritesheet
   *          horizontally to provide a fallback animation for left or right directions.
   * 
   * @see #getEntity()
   */
  public CreatureAnimationController(T creature, boolean useFlippedSpritesAsFallback) {
    super(creature);
    this.init(useFlippedSpritesAsFallback);
  }

  /**
   * Initializes a new instance of the <code>CreatureAnimationController</code> class.
   * 
   * @param creature
   *          The creature related to this controller.
   * 
   * @param defaultAnimation
   *          The default animation for this controller.
   * 
   * @see #getEntity()
   * @see #getDefault()
   */
  public CreatureAnimationController(T creature, Animation defaultAnimation) {
    this(creature, true, defaultAnimation);
  }

  /**
   * Initializes a new instance of the <code>CreatureAnimationController</code> class.
   * 
   * @param creature
   *          The creature related to this controller.
   * 
   * @param useFlippedSpritesAsFallback
   *          A flag indicating whether this controller should flip the provided spritesheet
   *          horizontally to provide a fallback animation for left or right directions.
   * 
   * @param defaultAnimation
   *          The default animation for this controller.
   * 
   * @param animations
   *          Additional animations that are managed by this controller instance.
   * 
   * @see #getEntity()
   * @see #getDefault()
   * @see #getAll()
   */
  public CreatureAnimationController(T creature, boolean useFlippedSpritesAsFallback, Animation defaultAnimation, final Animation... animations) {
    super(creature, defaultAnimation, animations);
    this.init(useFlippedSpritesAsFallback);
  }

  /**
   * Gets the sprite name for the specified creature and animation state.
   * 
   * @param creature
   *          The creature to retrieve the sprite name for.
   * 
   * @param state
   *          The current animation state.
   * 
   * @return A string representing the sprite name for the specified creature in the defined animation state.
   * 
   * @see Creature#getSpritesheetName()
   */
  public static String getSpriteName(Creature creature, CreatureAnimationState state) {
    return creature.getSpritesheetName() + "-" + state.spriteString();
  }

  /**
   * Gets the sprite name for the specified creature, animation state.and direction.
   * 
   * @param creature
   *          The creature to retrieve the sprite name for.
   * 
   * @param state
   *          The current animation state.
   * 
   * @param direction
   *          The direction in which the creature is facing.
   * 
   * @return A string representing the sprite name for the specified creature, animation state and facing direction.
   * 
   * @see Creature#getSpritesheetName()
   */
  public static String getSpriteName(Creature creature, CreatureAnimationState state, Direction direction) {
    return getSpriteName(creature, state) + "-" + direction.name().toLowerCase();
  }

  @Override
  public boolean isAutoScaling() {
    return this.getEntity().isScaling();
  }

  @Override
  protected String getSpritePrefix() {
    return this.getEntity().getSpritesheetName();
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
      return this.getDeathAnimationName();
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

  private String getDeathAnimationName() {
    if (this.customDeathAnimations.length > 0) {
      if (this.randomDeathSprite != null) {
        return this.randomDeathSprite;
      }

      String randomDeathAnim = Game.random().chose(this.customDeathAnimations);
      if (randomDeathAnim != null && !randomDeathAnim.isEmpty()) {
        String randomDeathAnmimation = this.getSpritePrefix() + "-" + randomDeathAnim;
        if (this.hasAnimation(randomDeathAnmimation)) {
          this.randomDeathSprite = randomDeathAnmimation;
          return this.randomDeathSprite;
        }
      }
    }

    String deadName = this.getSpriteName(CreatureAnimationState.DEAD);
    if (this.hasAnimation(deadName)) {
      return deadName;
    }

    return null;
  }

  private void initializeAvailableAnimations() {
    for (Direction dir : Direction.values()) {
      // initialize walking animations
      Spritesheet walkSprite = Resources.spritesheets().get(this.getSpriteName(CreatureAnimationState.WALK, dir));
      if (walkSprite != null) {
        this.add(new Animation(walkSprite, true));
      }

      // initialize idle animations
      Spritesheet idleSprite = Resources.spritesheets().get(this.getSpriteName(CreatureAnimationState.IDLE, dir));
      if (idleSprite != null) {
        this.add(new Animation(idleSprite, true));
      }
    }

    Spritesheet deadSprite = Resources.spritesheets().get(this.getSpriteName(CreatureAnimationState.DEAD));
    if (deadSprite != null) {
      this.add(new Animation(deadSprite, true));
    }

    Spritesheet baseIdle = Resources.spritesheets().get(this.getSpriteName(CreatureAnimationState.IDLE));
    if (baseIdle != null) {
      this.add(new Animation(baseIdle, true));
    }

    Spritesheet baseWalk = Resources.spritesheets().get(this.getSpriteName(CreatureAnimationState.WALK));
    if (baseWalk != null) {
      this.add(new Animation(baseWalk, true));
    }
  }

  private void initializeFlippedAnimations() {
    String leftIdle = this.getSpriteName(CreatureAnimationState.IDLE, Direction.LEFT);
    String leftWalk = this.getSpriteName(CreatureAnimationState.WALK, Direction.LEFT);
    Optional<Animation> leftIdleAnimation = this.getAll().stream().filter(x -> x.getName().equals(leftIdle)).findFirst();
    Optional<Animation> leftWalkAnimation = this.getAll().stream().filter(x -> x.getName().equals(leftWalk)).findFirst();

    String rightIdle = this.getSpriteName(CreatureAnimationState.IDLE, Direction.RIGHT);
    String rightWalk = this.getSpriteName(CreatureAnimationState.WALK, Direction.RIGHT);
    Optional<Animation> rightIdleAnimation = this.getAll().stream().filter(x -> x.getName().equals(rightIdle)).findFirst();
    Optional<Animation> rightWalkAnimation = this.getAll().stream().filter(x -> x.getName().equals(rightWalk)).findFirst();

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

  private String getIdleSpriteName(Direction dir) {
    return this.getSpriteNameWithDirection(CreatureAnimationState.IDLE, dir);
  }

  private String getWalkSpriteName(Direction dir) {
    return getSpriteNameWithDirection(CreatureAnimationState.WALK, dir);
  }

  private String getSpriteNameWithDirection(CreatureAnimationState state, Direction dir) {
    String name = this.getSpriteName(state, dir);
    if (this.hasAnimation(name)) {
      return name;
    }

    return getFallbackSpriteName(state, dir);
  }

  private String getFallbackSpriteName(CreatureAnimationState state, Direction dir) {
    String fallbackStateName = this.getSpriteName(state.getOpposite(), dir);
    if (this.hasAnimation(fallbackStateName)) {
      return fallbackStateName;
    }

    String baseName = this.getSpriteName(state);
    if (this.hasAnimation(baseName)) {
      return baseName;
    }

    // search for any animation for the specified state with dir information
    for (Direction d : Direction.values()) {
      final String name = this.getSpriteName(state, d);
      if (this.hasAnimation(name)) {
        return name;
      }
    }

    for (Direction d : Direction.values()) {
      final String name = this.getSpriteName(state.getOpposite(), d);
      if (this.hasAnimation(name)) {
        return name;
      }
    }

    return this.getDefault() != null ? this.getDefault().getName() : null;
  }

  private String getSpriteName(CreatureAnimationState state) {
    return getSpriteName(this.getEntity(), state);
  }

  private String getSpriteName(CreatureAnimationState state, Direction direction) {
    return getSpriteName(this.getEntity(), state, direction);
  }

  private void init(boolean useFlippedSpritesAsFallback) {
    this.initializeAvailableAnimations();

    if (useFlippedSpritesAsFallback) {
      this.initializeFlippedAnimations();
    }

    this.addRule(e -> true, e -> this.getCurrentAnimationName());

    AnimationInfo info = this.getEntity().getClass().getAnnotation(AnimationInfo.class);
    if (info != null) {
      this.customDeathAnimations = info.deathAnimations();
    } else {
      this.customDeathAnimations = new String[0];
    }
  }

  private Animation flipAnimation(Spritesheet spriteToFlip, String newSpriteName) {
    final BufferedImage leftIdleSprite = Imaging.flipSpritesHorizontally(spriteToFlip);
    Spritesheet leftIdleSpritesheet = Resources.spritesheets().load(leftIdleSprite, newSpriteName, spriteToFlip.getSpriteWidth(), spriteToFlip.getSpriteHeight());
    return new Animation(leftIdleSpritesheet, true);
  }
}
