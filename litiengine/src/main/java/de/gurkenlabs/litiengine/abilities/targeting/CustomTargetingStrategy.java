package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * A custom targeting strategy that allows defining a specific condition for selecting targets.
 *
 * <p>This strategy uses a {@link BiPredicate} to determine whether a combat entity
 * should be targeted based on the executor and the potential target. It supports multi-targeting and sorting by distance.
 */
public class CustomTargetingStrategy extends TargetingStrategy {

  private final BiPredicate<ICombatEntity, ICombatEntity> targetingCondition;

  /**
   * Creates a new {@code CustomTargetingStrategy} with the specified targeting condition.
   *
   * @param customPredicate The condition to determine valid targets. It takes the executor and a potential target as arguments and returns
   *                        {@code true} if the target is valid.
   * @param multiTarget     Whether the strategy supports targeting multiple entities.
   * @param sortByDistance  Whether the targets should be sorted by their distance to the executor.
   */
  public CustomTargetingStrategy(BiPredicate<ICombatEntity, ICombatEntity> customPredicate, boolean multiTarget, boolean sortByDistance) {
    super(multiTarget, sortByDistance);
    this.targetingCondition = customPredicate;
  }

  /**
   * Finds the targets within the specified impact area based on the targeting condition.
   *
   * <p>If the targeting condition or the game environment is not available, this method
   * returns an empty collection.
   *
   * @param impactArea The area in which to search for targets.
   * @param executor   The combat entity executing the ability.
   * @return A collection of combat entities that match the targeting condition.
   */
  @Override
  public Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    if (targetingCondition == null || Game.world() == null || Game.world().environment() == null) {
      return List.of();
    }

    return Game.world().environment().findCombatEntities(impactArea, e -> this.targetingCondition.test(executor, e));
  }
}
