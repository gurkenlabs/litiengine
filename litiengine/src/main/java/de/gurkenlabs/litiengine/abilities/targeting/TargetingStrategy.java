package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.EntityDistanceComparator;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;

import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * The abstract class `TargetingStrategy` defines the logic for selecting targets
 * based on various strategies. It allows for different ways to filter, sort, and
 * select one or more targets in a game environment based on factors like distance
 * or custom conditions.
 */
public abstract class TargetingStrategy {

  private boolean multiTarget; // Determines if multiple targets can be selected
  private boolean prioritizeByDistance; // Whether to prioritize targets based on their distance
  private BiPredicate<ICombatEntity, ICombatEntity> additionalCondition; // Custom comparator for target sorting

  private Comparator<IEntity> customTargetPriorityComparator;

  /**
   * Constructor for the `TargetingStrategy` class.
   *
   * @param multiTarget Specifies if the strategy supports selecting multiple targets.
   * @param sortByDistance Specifies if the strategy should prioritize targets by distance.
   */
  protected TargetingStrategy(boolean multiTarget, boolean sortByDistance) {
    this.multiTarget = multiTarget;
    this.prioritizeByDistance = sortByDistance;
  }

  /**
   * Finds and returns a collection of combat entities that match the target criteria
   * defined by the strategy. This method applies internal logic for finding targets
   * and then optionally filters and sorts them based on the additional condition and
   * distance priority settings.
   *
   * @param impactArea The area where the effect or action is applied.
   * @param executor The entity executing the action (e.g., the player or an NPC).
   * @return A collection of `ICombatEntity` instances representing the selected targets.
   */
  public Collection<ICombatEntity> findTargets(Shape impactArea, ICombatEntity executor) {
    var entities = findTargetsInternal(impactArea, executor);

    // Apply additional custom condition if set
    if (additionalCondition != null) {
      entities = entities.stream().filter(e -> additionalCondition.test(executor, e)).collect(Collectors.toList());
    }

    // Return if no entities match
    if (entities.isEmpty()) {
      return entities;
    }

    // Optionally prioritize by custom comparator or distance from executor
    if (executor != null) {
      Comparator<IEntity> comparator = null;
      if (this.customTargetPriorityComparator != null) {
        comparator = this.customTargetPriorityComparator;
      } else if (this.prioritizeByDistance) {
        comparator = new EntityDistanceComparator(executor);
      }

      if (comparator != null) {
        var list = entities.stream().sorted(comparator).collect(Collectors.toList());
        entities = list;
      }
    }

    // Return all targets if multi-targeting is enabled, otherwise return the first
    if (this.isMultiTarget()) {
      return entities;
    }

    return List.of(entities.stream().findFirst().get());
  }

  /**
   * Finds target entities in the impact area.
   * This is implemented by the individual strategies.
   *
   * @param impactArea The area where the effect is applied.
   * @param executor The entity executing the action.
   * @return A collection of `ICombatEntity` instances that match the strategy criteria.
   */
  protected abstract Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor);

  public boolean isMultiTarget() {
    return multiTarget;
  }

  public void setMultiTarget(boolean multiTarget) {
    this.multiTarget = multiTarget;
  }

  public boolean prioritizeByDistance() {
    return prioritizeByDistance;
  }

  public void setPrioritizeByDistance(boolean prioritizeByDistance) {
    this.prioritizeByDistance = prioritizeByDistance;
  }

  public void setCustomTargetPriorityComparator(Comparator<IEntity> customTargetPriorityComparator) {
    this.customTargetPriorityComparator = customTargetPriorityComparator;
  }

  /**
   * Adds a custom condition to the targeting strategy.
   *
   * @param customCondition A condition that adds further filtering based on the executor and target.
   * @return The current `TargetingStrategy` instance, allowing for method chaining.
   */
  public TargetingStrategy withCondition(BiPredicate<ICombatEntity, ICombatEntity> customCondition) {
    this.additionalCondition = customCondition;
    return this;
  }

  /**
   * Returns a strategy that targets enemy entities.
   *
   * @return An instance of `EnemyTargetingStrategy`.
   */
  public static TargetingStrategy enemies() {
    return new EnemyTargetingStrategy(true, false, false);
  }

  /**
   * Returns a strategy that targets the entity executing the action.
   *
   * @return An instance of `ExecutingEntityTargetingStrategy`.
   */
  public static TargetingStrategy executingEntity() {
    return new ExecutingEntityTargetingStrategy();
  }

  /**
   * Returns a strategy that targets friendly entities.
   *
   * @return An instance of `FriendlyTargetingStrategy`.
   */
  public static TargetingStrategy friendly() {
    return new FriendlyTargetingStrategy(true, false, false);
  }

  /**
   * Returns a strategy that targets dead friendly entities.
   *
   * @return An instance of `FriendlyTargetingStrategy`.
   */
  public static TargetingStrategy friendlyDead() {
    return new FriendlyTargetingStrategy(true, false, true);
  }

  /**
   * Returns a strategy that uses a custom condition to filter targets.
   *
   * @param customPredicate A condition to define custom logic for target selection.
   * @return An instance of `CustomTargetingStrategy`.
   */
  public static TargetingStrategy custom(BiPredicate<ICombatEntity, ICombatEntity> customPredicate) {
    return new CustomTargetingStrategy(customPredicate, true, false);
  }

  /**
   * Returns a strategy for a fixed list of target entities.
   *
   * @param fixedTargets An array of target entities to always include as the targets.
   * @return An instance of `StaticTargetingStrategy` initialized with the given entities.
   */
  public static TargetingStrategy fixed(ICombatEntity... fixedTargets) {
    return new FixedTargetingStrategy(fixedTargets);
  }

  /**
   * Returns a strategy that targets no entities.
   *
   * @return A `TargetingStrategy` that selects no entities.
   */
  public static TargetingStrategy none() {
    return custom((e, f) -> false);
  }
}
