package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.Optional;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

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
  private static final String IDLE = "-idle-";
  private static final String WALK = "-walk-";

  public CreatureAnimationController(T entity, boolean useFlippedSpritesAsFallback) {
    super(entity);
    this.init(useFlippedSpritesAsFallback);
  }

  public CreatureAnimationController(T entity, Animation defaultAnimation) {
    this(entity, defaultAnimation, true);
  }

  public CreatureAnimationController(T entity, Animation defaultAnimation, boolean useFlippedSpritesAsFallback) {
    super(entity, defaultAnimation);
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
    return this.getEntity().isIdle() ? this.getIdleSpriteName(this.getEntity().getFacingDirection()) : this.getWalkSpriteName(this.getEntity().getFacingDirection());
  }

  private void initializeAvailableAnimations() {

    for (Direction dir : Direction.values()) {
      // initialize walking animations
      Spritesheet walkSprite = getWalkSprite(dir);
      if (walkSprite != null) {
        this.add(new Animation(walkSprite, true));
      }

      // initialize idle animations
      Spritesheet idleSprite = getIdleSprite(dir);
      if (idleSprite != null) {
        this.add(new Animation(idleSprite, true));
      }
    }
  }

  private void initializeFlippedAnimations() {
    String leftIdle = getIdleSpriteName(Direction.LEFT);
    String leftWalk = getWalkSpriteName(Direction.LEFT);
    Optional<Animation> leftIdleAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(leftIdle)).findFirst();
    Optional<Animation> leftWalkAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(leftWalk)).findFirst();

    String rightIdle = getIdleSpriteName(Direction.RIGHT);
    String rightWalk = getWalkSpriteName(Direction.RIGHT);
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

  private Spritesheet getIdleSprite(Direction dir) {
    return Spritesheet.find(getIdleSpriteName(dir));
  }

  private Spritesheet getWalkSprite(Direction dir) {
    return Spritesheet.find(getWalkSpriteName(dir));
  }

  private String getIdleSpriteName(Direction dir) {
    return getSpriteName(IDLE, dir);
  }

  private String getWalkSpriteName(Direction dir) {
    return getSpriteName(WALK, dir);
  }

  private String getSpriteName(String state, Direction dir) {
    return this.getSpritePrefix() + state + dir.toString().toLowerCase();
  }

  private void init(boolean useFlippedSpritesAsFallback) {
    this.initializeAvailableAnimations();

    if (useFlippedSpritesAsFallback) {
      this.initializeFlippedAnimations();
    }

    this.addAnimationRule(e -> true, e -> this.getCurrentAnimationName());
  }
}
