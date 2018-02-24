package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.gurkenlabs.litiengine.Game;
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
 * <li>{entity-name}-idle-{DIRECTION}.{EXTENSION}</li>
 * <li>{entity-name}-walk-{DIRECTION}.{EXTENSION}</li>
 * </ul>
 * Where {DIRECTION} refers to a value of the {@link Direction} enum and
 * {entity-name} refers to the name of the entity. {EXTENSION} refers to a value
 * of the {@link de.gurkenlabs.litiengine.graphics.ImageFormat} enum.
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
    this.setSpritePrefix(entity.getSpritePrefix());
    
    this.getAnimations().addAll(this.initializeAvailableAnimations());

    if (useFlippedSpritesAsFallback) {
      this.getAnimations().addAll(this.initializeFlippedAnimations());
    }
  }

  public CreatureAnimationController(T entity, Animation defaultAnimation) {
    this(entity, defaultAnimation, true);
  }

  public CreatureAnimationController(T entity, Animation defaultAnimation, boolean useFlippedSpritesAsFallback) {
    super(entity, defaultAnimation);
    this.setSpritePrefix(entity.getSpritePrefix());
    
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

    if (this.getCurrentAnimation() != null && !this.getCurrentAnimation().isLoop() && this.getCurrentAnimation().isPlaying()) {
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
}
