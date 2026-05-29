package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.Arrays;
import java.util.Collection;

/**
 * A targeting strategy that always resolves to a predefined, fixed set of combat entities.
 *
 * <p>Unlike dynamic strategies, this implementation ignores both the impact area and the
 * executing entity, and simply returns the collection of targets supplied at construction time. It is useful for scripted scenarios, cutscenes, or
 * abilities that should always affect a known set of entities (for example, a specific boss and its minions).
 *
 * @see TargetingStrategy
 */
public class FixedTargetingStrategy extends TargetingStrategy {

  /**
   * The fixed collection of targets returned by this strategy.
   */
  private Collection<ICombatEntity> fixedTargets;

  /**
   * Creates a new {@code FixedTargetingStrategy} with the given fixed targets.
   *
   * <p>The strategy is automatically configured as multi-target when more than one entity is
   * supplied, and as single-target otherwise. The results are never sorted by distance.
   *
   * @param fixedTargets the combat entities that will always be returned as the targets of this
   *                     strategy. May be empty.
   */
  protected FixedTargetingStrategy(ICombatEntity... fixedTargets) {
    super(fixedTargets.length > 1, false);
    this.fixedTargets = Arrays.asList(fixedTargets);
  }

  /**
   * Returns the predefined fixed targets, ignoring both the impact area and the executor.
   *
   * @param impactArea the shape representing the ability's area of effect; ignored by this
   *                   strategy.
   * @param executor   the combat entity executing the ability; ignored by this strategy.
   * @return the fixed collection of targets supplied at construction time.
   */
  @Override
  protected Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return this.fixedTargets;
  }
}
