package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

/**
 * This {@link AnimationController} implementation provides animation rules that
 * use naming conventions to provide {@link Animation}s for
 * {@link IMovableCombatEntity} implementations.
 * 
 * The spritesheet images need to be named according to the following
 * conventions in order to be automatically used by this controller:
 * <ul>
 * <li>{entity-name}-idle-{DIRECTION}.png</li>
 * <li>{entity-name}-walk-{DIRECTION}.png</li>
 * </ul>
 * Where {DIRECTION} refers to a value of the {@link Direction} enum and
 * {entity-name} refers to the name of the entity.
 * 
 * @see de.gurkenlabs.litiengine.entities.IMovableCombatEntity
 * @see de.gurkenlabs.litiengine.entities.Direction
 * @see de.gurkenlabs.litiengine.entities.IEntity#getName()
 */
public class MovableCombatEntityAnimationController<T extends IMovableCombatEntity> extends EntityAnimationController<T> {

  public MovableCombatEntityAnimationController(T entity, Animation defaultAnimation) {
    this(entity, defaultAnimation, true);
  }

  public MovableCombatEntityAnimationController(T entity, Animation defaultAnimation, boolean useFlippedSpritesAsFallback) {
    super(entity, defaultAnimation);

    // TODO: evaluate a better way to determine the animation name because the
    // name of entities of the same type might differ
    if (this.getEntity().getName() == null || this.getEntity().getName().isEmpty()) {
      throw new IllegalArgumentException("Make sure the name of the entity is set before using a MovableCombatEntityAnimationController.");
    }

    this.getAnimations().addAll(this.initializeAvailableAnimations());

    if (useFlippedSpritesAsFallback) {
      this.getAnimations().addAll(this.initializeFlippedAnimations());
    }
  }

  @Override
  public void update() {
    super.update();

    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return;
    }

    if (this.getCurrentAnimation() != null && !this.getCurrentAnimation().isLoop()) {
      return;
    }

    if (this.getEntity() == null) {
      return;
    }

    String animationName = this.getCurrentAnimationName();
    if (this.getCurrentAnimation() == null || animationName != null && !animationName.isEmpty() && !this.getCurrentAnimation().getName().equalsIgnoreCase(animationName)) {
      this.playAnimation(animationName);
    }
  }

  protected String getCurrentAnimationName() {
    return this.getEntity().isIdle() ? this.getIdleSpriteName(this.getEntity().getFacingDirection()) : this.getWalkSpriteName(this.getEntity().getFacingDirection());
  }

  private List<Animation> initializeAvailableAnimations() {
    List<Animation> animations = new ArrayList<>();

    for (Direction dir : Direction.values()) {

      // initialize walking animations
      Spritesheet walkSprite = getWalkSprite(dir);
      if (walkSprite != null) {
        animations.add(new Animation(walkSprite, true));
        this.addAnimationRule(e -> !e.isIdle() && e.getFacingDirection() == dir, walkSprite.getName());
      }

      // initialize idle animations
      Spritesheet idleSprite = getIdleSprite(dir);
      if (idleSprite != null) {
        animations.add(new Animation(idleSprite, true));
        this.addAnimationRule(e -> e.isIdle() && e.getFacingDirection() == dir, idleSprite.getName());
      }
    }

    return animations;
  }

  private List<Animation> initializeFlippedAnimations() {
    List<Animation> animations = new ArrayList<>();
    String leftIdle = getIdleSpriteName(Direction.LEFT);
    String leftWalk = getWalkSpriteName(Direction.LEFT);
    Optional<Animation> leftIdleAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(leftIdle)).findFirst();
    Optional<Animation> leftWalkAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(leftWalk)).findFirst();

    String rightIdle = getIdleSpriteName(Direction.RIGHT);
    String rightWalk = getWalkSpriteName(Direction.RIGHT);
    Optional<Animation> rightIdleAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(rightIdle)).findFirst();
    Optional<Animation> rightWalkAnimation = this.getAnimations().stream().filter(x -> x.getName().equals(rightWalk)).findFirst();

    if (!leftIdleAnimation.isPresent() && rightIdleAnimation.isPresent()) {
      animations.add(flipAnimation(rightIdleAnimation.get().getSpritesheet(), leftIdle));
    }

    if (!leftWalkAnimation.isPresent() && rightWalkAnimation.isPresent()) {
      animations.add(flipAnimation(rightWalkAnimation.get().getSpritesheet(), leftWalk));
    }

    if (!rightIdleAnimation.isPresent() && leftIdleAnimation.isPresent()) {
      animations.add(flipAnimation(leftIdleAnimation.get().getSpritesheet(), rightIdle));
    }

    if (!rightWalkAnimation.isPresent() && leftWalkAnimation.isPresent()) {
      animations.add(flipAnimation(leftWalkAnimation.get().getSpritesheet(), rightWalk));
    }

    return animations;
  }

  private Animation flipAnimation(Spritesheet spriteToFlip, String newSpriteName) {
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
    final String spriteIdle = "%s-idle-%s";
    return getSpriteName(spriteIdle, dir);
  }

  private String getWalkSpriteName(Direction dir) {
    final String spriteWalk = "%s-walk-%s";
    return getSpriteName(spriteWalk, dir);
  }

  private String getSpriteName(String formatString, Direction dir) {
    return String.format(formatString, this.getEntity().getName().toLowerCase(), dir.toString().toLowerCase());
  }
}
