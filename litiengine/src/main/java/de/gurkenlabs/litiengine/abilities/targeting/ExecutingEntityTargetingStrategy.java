package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

import java.awt.Shape;
import java.util.Collection;
import java.util.List;

/**
 * A targeting strategy that always targets the entity executing the ability.
 *
 * <p>This strategy ignores the impact area entirely and resolves to a single-element collection
 * containing the executor itself. It is useful for self-cast abilities such as heals, buffs, or other effects that should only affect the caster.
 *
 * @see TargetingStrategy
 */
public class ExecutingEntityTargetingStrategy extends TargetingStrategy {

  /**
   * Creates a new {@code ExecutingEntityTargetingStrategy}.
   *
   * <p>The strategy is configured as single-target and unsorted, since the resulting collection
   * always contains exactly one entity (the executor).
   */
  public ExecutingEntityTargetingStrategy() {
    super(false, false);
  }

  /**
   * Returns the executing entity as the sole target, regardless of the given impact area.
   *
   * @param impactArea the shape representing the ability's area of effect; ignored by this
   *                   strategy.
   * @param executor   the combat entity executing the ability; this entity is returned as the
   *                   only target.
   * @return an immutable collection containing exactly the {@code executor}.
   */
  @Override
  public Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return List.of(executor);
  }
}
