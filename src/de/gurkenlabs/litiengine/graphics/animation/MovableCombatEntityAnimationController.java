package de.gurkenlabs.litiengine.graphics.animation;

import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

/**
 * This {@link AnimationController} implementation provides animation rules that use naming conventions to provide {@link Animation}s for
 * {@link IMovableCombatEntity} implementations.
 * 
 * The spritesheet images need to be named according to the following conventions in order to be automatically used by this controller:
 * <ul>
 * <li>{entity-name}-idle-{DIRECTION}.png</li>
 * <li>{entity-name}-walk-{DIRECTION}.png</li>
 * </ul>
 * Where {DIRECTION} refers to a value of the {@link Direction} enum and {entity-name} refers to the
 * name of the entity.
 * 
 * @see de.gurkenlabs.litiengine.entities.IMovableCombatEntity
 * @see de.gurkenlabs.litiengine.entities.Direction
 * @see de.gurkenlabs.litiengine.entities.IEntity#getName()
 */
public class MovableCombatEntityAnimationController extends EntityAnimationController<IMovableCombatEntity> {
  public static final String WALK_IDENTIFIER = "walk";
  public static final String IDLE_IDENTIFIER = "idle";

  public MovableCombatEntityAnimationController(IMovableCombatEntity entity, Animation defaultAnimation) {
    super(entity, defaultAnimation);
    if (this.getEntity().getName() == null || this.getEntity().getName().isEmpty()) {
      throw new IllegalArgumentException("Make sure the name of the entity is set before using a MovableCombatEntityAnimationController.");
    }

    this.getAnimations().addAll(this.initializeAvailableAnimations(this.getEntity()));
  }

  private List<Animation> initializeAvailableAnimations(IMovableCombatEntity entity) {

    List<Animation> animation = new ArrayList<>();

    for (Direction dir : Direction.values()) {

      // initialize walking animations
      final String walkAnimationName = entity.getName().toLowerCase() + "-" + WALK_IDENTIFIER + "-" + dir.toString().toLowerCase();
      Spritesheet walkSprite = Spritesheet.find(walkAnimationName);
      if (walkSprite != null) {
        animation.add(new Animation(walkSprite, true));
        this.addAnimationRule(e -> !e.isIdle() && e.getFacingDirection() == dir, walkAnimationName);
      }

      // initialize idle animations
      final String idleAnimationName = entity.getName().toLowerCase() + "-" + IDLE_IDENTIFIER + "-" + dir.toString().toLowerCase();
      Spritesheet idleSprite = Spritesheet.find(idleAnimationName);
      if (idleSprite != null) {
        animation.add(new Animation(idleSprite, true));
        this.addAnimationRule(e -> e.isIdle() && e.getFacingDirection() == dir, idleAnimationName);
      }
    }

    return animation;
  }
}
