package de.gurkenlabs.litiengine.abilities.targeting;

/**
 * A targeting strategy that selects enemy entities as ability targets.
 *
 * <p>This strategy extends {@link OtherEntityTargetingStrategy} and is preconfigured to exclude
 * the executing entity itself as well as any friendly entities, thereby restricting the set of valid targets to enemies only.
 *
 * @see OtherEntityTargetingStrategy
 */
public class EnemyTargetingStrategy extends OtherEntityTargetingStrategy {

  /**
   * Creates a new {@code EnemyTargetingStrategy}.
   *
   * @param multiTarget     if {@code true}, the strategy may return multiple matching targets;
   *                        otherwise only a single target is selected.
   * @param sortByDistance  if {@code true}, the resulting targets are ordered by their distance
   *                        to the executing entity (closest first).
   * @param includeDead     if {@code true}, dead entities are considered valid targets;
   *                        otherwise only living entities are selected.
   */
  public EnemyTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean includeDead) {
    super(multiTarget, sortByDistance, false, false, includeDead);
  }
}
