package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.Collection;

/**
 * A targeting strategy that selects other combat entities within the ability's impact area, filtered by their relationship to the executor and their
 * alive/dead state.
 *
 * <p>This strategy queries the current environment for combat entities intersecting the given
 * impact area and filters them according to three configurable criteria:
 * <ul>
 *   <li><b>friendly</b> – whether to select allied entities or hostile ones,</li>
 *   <li><b>includeExecutor</b> – whether the executing entity itself may be returned,</li>
 *   <li><b>includeDead</b> – whether dead entities may be returned.</li>
 * </ul>
 *
 * <p>It serves as the common base for {@link FriendlyTargetingStrategy} and
 * {@link EnemyTargetingStrategy}.
 *
 * @see TargetingStrategy
 * @see FriendlyTargetingStrategy
 * @see EnemyTargetingStrategy
 */
public class OtherEntityTargetingStrategy extends TargetingStrategy {

  /**
   * Whether this strategy targets friendly ({@code true}) or hostile ({@code false}) entities.
   */
  private final boolean friendly;

  /** Whether the executing entity itself is a valid target. */
  private final boolean includeExecutor;

  /** Whether dead entities are valid targets. */
  private final boolean includeDead;

  /**
   * Creates a new {@code OtherEntityTargetingStrategy} that excludes both the executor and dead
   * entities.
   *
   * @param multiTarget    if {@code true}, the strategy may return multiple matching targets;
   *                       otherwise only a single target is selected.
   * @param sortByDistance if {@code true}, the resulting targets are ordered by their distance to
   *                       the executing entity (closest first).
   * @param friendly       if {@code true}, only friendly entities are targeted; otherwise only
   *                       hostile entities are targeted.
   */
  public OtherEntityTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean friendly) {
    this(multiTarget, sortByDistance, friendly, false, false);
  }

  /**
   * Creates a new {@code OtherEntityTargetingStrategy} with full control over inclusion rules.
   *
   * @param multiTarget     if {@code true}, the strategy may return multiple matching targets;
   *                        otherwise only a single target is selected.
   * @param sortByDistance  if {@code true}, the resulting targets are ordered by their distance
   *                        to the executing entity (closest first).
   * @param friendly        if {@code true}, only friendly entities are targeted; otherwise only
   *                        hostile entities are targeted.
   * @param includeExecutor if {@code true}, the executing entity itself may be included in the
   *                        result; otherwise it is filtered out.
   * @param includeDead     if {@code true}, dead entities are considered valid targets;
   *                        otherwise only living entities are selected.
   */
  public OtherEntityTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean friendly, boolean includeExecutor, boolean includeDead) {
    super(multiTarget, sortByDistance);

    this.friendly = friendly;
    this.includeExecutor = includeExecutor;
    this.includeDead = includeDead;
  }

  /**
   * Finds all combat entities in the current environment that intersect the given impact area and
   * satisfy this strategy's friendly/executor/dead filters.
   *
   * @param impactArea the shape representing the ability's area of effect; only entities
   *                   intersecting this area are considered.
   * @param executor   the combat entity executing the ability; used as the reference for
   *                   friend/foe classification and (optionally) excluded from the result.
   * @return a collection of combat entities matching all configured filter criteria. May be
   *         empty if no entity qualifies.
   */
  @Override
  protected Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return Game.world().environment().findCombatEntities(impactArea, e ->
      (this.friendly && e.isFriendly(executor) || !this.friendly && !e.isFriendly(executor))
        && (this.includeDead || !e.isDead())
        && (this.includeExecutor || !e.equals(executor)));
  }
}
