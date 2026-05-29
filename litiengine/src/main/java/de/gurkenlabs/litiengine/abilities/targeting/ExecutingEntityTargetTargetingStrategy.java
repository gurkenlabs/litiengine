package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.Collection;
import java.util.List;

/**
 * A targeting strategy that resolves to the current target of the executing entity.
 *
 * <p>This strategy ignores the impact area and instead delegates to the executor's own
 * {@link ICombatEntity#getTarget() target} reference. It is useful for abilities that should be applied to whatever entity the caster is currently
 * focused on (for example, a tab-targeted spell).
 *
 * <p>Note: if the executor has no current target, the returned collection will contain a single
 * {@code null} element. Callers should be prepared to handle this case.
 *
 * @see TargetingStrategy
 * @see ICombatEntity#getTarget()
 */
public class ExecutingEntityTargetTargetingStrategy extends TargetingStrategy {

  /**
   * Creates a new {@code ExecutingEntityTargetTargetingStrategy}.
   *
   * <p>The strategy is configured as single-target and unsorted, since the resulting collection
   * always contains at most one entity (the executor's current target).
   */
  public ExecutingEntityTargetTargetingStrategy() {
    super(false, false);
  }

  /**
   * Returns the executor's current target as the sole target, regardless of the given impact
   * area.
   *
   * @param impactArea the shape representing the ability's area of effect; ignored by this
   *                   strategy.
   * @param executor   the combat entity executing the ability; its
   *                   {@link ICombatEntity#getTarget() current target} is returned.
   * @return an immutable collection containing exactly the executor's current target. The element
   *         may be {@code null} if the executor has no target set.
   */
  @Override
  public Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return List.of(executor.getTarget());
  }
}
