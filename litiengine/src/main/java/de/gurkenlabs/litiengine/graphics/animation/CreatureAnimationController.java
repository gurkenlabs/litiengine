package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This {@link AnimationController} implementation provides animation rules that use naming conventions to provide {@link Animation}s for
 * {@link Creature} implementations.
 *
 * <p>
 * The spritesheet images need to be named according to the following conventions in order to be automatically used by this controller:
 *
 * <ul>
 * <li>{@link #getSpritePrefix()}-idle-{DIRECTION}.{EXTENSION}
 * <li>{@link #getSpritePrefix()}-walk-{DIRECTION}.{EXTENSION}
 * </ul>
 * <p>
 * Where {DIRECTION} refers to a value of the {@link Direction} enum and {@link #getSpritePrefix()} refers to the
 * current sprite prefix of the entity. {EXTENSION} refers to a value of the
 * {@link de.gurkenlabs.litiengine.resources.ImageFormat} enum.
 *
 * @param <T> The type of the creature for which animations are managed by this controller.
 * @see de.gurkenlabs.litiengine.entities.Creature
 * @see de.gurkenlabs.litiengine.Direction
 * @see de.gurkenlabs.litiengine.entities.IEntity#getName()
 */
public class CreatureAnimationController<T extends Creature> extends EntityAnimationController<T> {
  private String[] customDeathAnimations;
  private String randomDeathSprite;

  /**
   * Initializes a new instance of the {@code CreatureAnimationController} class.
   *
   * @param creature                    The creature related to this controller.
   * @param useFlippedSpritesAsFallback A flag indicating whether this controller should flip the provided spritesheet horizontally to provide a
   *                                    fallback animation for left or right directions.
   * @see #getEntity()
   */
  public CreatureAnimationController(T creature, boolean useFlippedSpritesAsFallback) {
    super(creature);
    this.init(useFlippedSpritesAsFallback);
  }

  /**
   * Initializes a new instance of the {@code CreatureAnimationController} class.
   *
   * @param creature         The creature related to this controller.
   * @param defaultAnimation The default animation for this controller.
   * @see #getEntity()
   * @see #getDefault()
   */
  public CreatureAnimationController(T creature, Animation defaultAnimation) {
    this(creature, true, defaultAnimation);
  }

  /**
   * Initializes a new instance of the {@code CreatureAnimationController} class.
   *
   * @param creature                    The creature related to this controller.
   * @param useFlippedSpritesAsFallback A flag indicating whether this controller should flip the provided spritesheet horizontally to provide a
   *                                    fallback animation for left or right directions.
   * @param defaultAnimation            The default animation for this controller.
   * @param animations                  Additional animations that are managed by this controller instance.
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
   * @param creature The creature to retrieve the sprite name for.
   * @param state    The current animation state.
   * @return A string representing the sprite name for the specified creature in the defined animation state.
   * @see Creature#getSpritesheetName()
   */
  public static String getSpriteName(Creature creature, CreatureAnimationState state) {
    return creature.getSpritesheetName() + "-" + state.spriteString();
  }

  /**
   * Gets the sprite name for the specified creature, animation state.and direction.
   *
   * @param creature  The creature to retrieve the sprite name for.
   * @param state     The current animation state.
   * @param direction The direction in which the creature is facing.
   * @return A string representing the sprite name for the specified creature, animation state and facing direction.
   * @see Creature#getSpritesheetName()
   */
  public static String getSpriteName(Creature creature, CreatureAnimationState state, Direction direction) {
    return getSpriteName(creature, state) + "-" + direction.name().toLowerCase();
  }

  @Override public boolean isAutoScaling() {
    return this.getEntity().isScaling() || super.isAutoScaling();
  }

  @Override protected String getSpritePrefix() {
    return this.getEntity().getSpritesheetName();
  }

  /**
   * This method evaluates the current animation name that depends on certain properties of the {@link #getEntity()}. Overwriting this method allows
   * to specify more sophisticated animations.
   *
   * @return The name of the current animation that should be played
   */
  protected String getCurrentAnimationName() {
    Creature entity = getEntity();
    Direction direction = entity.getFacingDirection();

    if (getEntity().isDead()) {
      String deadName = getDeadSpriteName(direction);
      return hasAnimation(deadName) ? deadName : chooseRandomDeathAnimation();
    } else if (entity.isIdle()) {
      String idleName = getIdleSpriteName(direction);
      return hasAnimation(idleName) ? idleName : getWalkSpriteName(direction);
    } else {
      String walkName = getWalkSpriteName(direction);
      return hasAnimation(walkName) ? walkName : getIdleSpriteName(direction);
    }
  }

  /**
   * Chooses a random death animation from the available custom death animations.
   *
   * @return The name of the chosen random death animation, or null if no custom death animations are available.
   */
  private String chooseRandomDeathAnimation() {
    if (customDeathAnimations.length == 0) {
      return null;
    }

    String randomDeathAnimation = Game.random().choose(customDeathAnimations);
    String prefixedDeathAnimation = getSpritePrefix() + "-" + randomDeathAnimation;
    if (randomDeathSprite == null && randomDeathAnimation != null && !randomDeathAnimation.isEmpty() && hasAnimation(prefixedDeathAnimation)) {
      this.randomDeathSprite = prefixedDeathAnimation;
    }
    return randomDeathSprite;
  }

  /**
   * Initializes the available animations for the creature. This method sets up walking, idle, and dead animations for all directions.
   */

  private void initializeAvailableAnimations() {
    for (CreatureAnimationState state : CreatureAnimationState.values()) {
      initializeBaseAnimation(state, null);
      for (Direction dir : Direction.values()) {
        initializeBaseAnimation(state, dir);
      }
    }
  }

  private void initializeBaseAnimation(CreatureAnimationState state, Direction dir) {
    Spritesheet sprite = Resources.spritesheets().get(dir == null ? getSpriteName(state) : getSpriteName(state, dir));
    if (sprite != null) {
      add(new Animation(sprite, true));
    }
  }

  /**
   * Initializes flipped animations for the creature. This method creates a map of animations for the left and right directions, and if an animation
   * is missing for one direction, it uses the flipped version of the animation from the opposite direction.
   */
  private void initializeFlippedAnimations() {
    Map<String, Optional<Animation>> animations = new HashMap<>();

    for (Direction direction : new Direction[] {Direction.LEFT, Direction.RIGHT}) {
      for (CreatureAnimationState state : CreatureAnimationState.values()) {
        String spriteName = getSpriteName(state, direction);
        animations.put(spriteName, getAll().stream().filter(x -> x.getName().equals(spriteName)).findFirst());
      }
    }

    animations.forEach((spriteName, animation) -> {
      String oppositeSpriteName = spriteName.endsWith("left") ? spriteName.replace("left", "right") : spriteName.replace("right", "left");
      Optional<Animation> oppositeAnimation = animations.get(oppositeSpriteName);

      if (animation.isEmpty() && oppositeAnimation.isPresent()) {
        add(flippedAnimation(oppositeAnimation.get(), spriteName, false));
      }
    });
  }

  private String getDeadSpriteName(Direction dir) {
    return this.getSpriteNameWithDirection(CreatureAnimationState.DEAD, dir);
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
      initializeFlippedAnimations();
    }

    addRule(e -> true, e -> getCurrentAnimationName());

    AnimationInfo info = this.getEntity().getClass().getAnnotation(AnimationInfo.class);
    if (info != null) {
      this.customDeathAnimations = info.deathAnimations();
    } else {
      this.customDeathAnimations = new String[0];
    }
  }
}
