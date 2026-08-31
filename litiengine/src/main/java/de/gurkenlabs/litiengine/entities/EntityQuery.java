package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/// A reusable, fluent query for selecting entities without game-specific collection boilerplate.
///
/// Filters are cumulative. Calling [#sorted(Comparator)] or [#nearestTo(Point2D)] replaces any
/// previously selected ordering. Terminal operations evaluate the current contents of the source
/// collection; [#list()] returns an unmodifiable snapshot.
///
/// @param <T> The type of value selected by this query.
/// @see Environment#query(Class)
public final class EntityQuery<T> {
  private final Collection<T> source;
  private Predicate<T> predicate = entity -> true;
  private Comparator<T> comparator;

  /// Creates a query over a collection.
  ///
  /// @param source The live source collection, or `null` for an empty query.
  public EntityQuery(Collection<T> source) {
    this.source = source == null ? List.of() : source;
  }

  /// Adds a predicate to the filters already configured on this query.
  ///
  /// @param filter The predicate to add.
  /// @return This query.
  /// @throws NullPointerException if `filter` is `null`.
  public EntityQuery<T> matching(Predicate<T> filter) {
    this.predicate = this.predicate.and(Objects.requireNonNull(filter));
    return this;
  }

  /// Keeps entities that have the specified tag.
  ///
  /// @param tag The tag to match.
  /// @return This query.
  public EntityQuery<T> tagged(String tag) {
    return this.matching(entity -> entity instanceof IEntity candidate && candidate.hasTag(tag));
  }

  /// Keeps entities whose name equals the specified name.
  ///
  /// @param name The name to match; may be `null`.
  /// @return This query.
  public EntityQuery<T> named(String name) {
    return this.matching(entity -> entity instanceof IEntity candidate && Objects.equals(candidate.getName(), name));
  }

  /// Excludes dead combat entities while retaining values that are not combat entities.
  ///
  /// @return This query.
  public EntityQuery<T> alive() {
    return this.matching(entity -> !(entity instanceof ICombatEntity combat) || !combat.isDead());
  }

  /// Keeps only dead combat entities.
  ///
  /// @return This query.
  public EntityQuery<T> dead() {
    return this.matching(entity -> entity instanceof ICombatEntity combat && combat.isDead());
  }

  /// Keeps only combat entities assigned to a team.
  ///
  /// @param team The team identifier to match.
  /// @return This query.
  public EntityQuery<T> team(int team) {
    return this.matching(entity -> entity instanceof ICombatEntity combat && combat.getTeam() == team);
  }

  /// Keeps combat entities whose team differs from the reference entity's team.
  ///
  /// @param entity The entity whose team is considered friendly.
  /// @return This query.
  /// @throws NullPointerException if `entity` is `null`.
  public EntityQuery<T> enemyOf(ICombatEntity entity) {
    Objects.requireNonNull(entity);
    return this.matching(candidate -> candidate instanceof ICombatEntity combat && combat.getTeam() != entity.getTeam());
  }

  /// Keeps entities whose center is at most the specified distance from a point.
  ///
  /// @param point The center of the search area in map coordinates.
  /// @param distance The inclusive maximum distance in map units.
  /// @return This query.
  /// @throws NullPointerException if `point` is `null`.
  /// @throws IllegalArgumentException if `distance` is negative.
  public EntityQuery<T> within(Point2D point, double distance) {
    Objects.requireNonNull(point);
    if (distance < 0) throw new IllegalArgumentException("Distance must not be negative.");
    return this.matching(entity -> entity instanceof IEntity candidate && candidate.getCenter().distance(point) <= distance);
  }

  /// Orders entities by increasing distance from a point.
  ///
  /// Values that are not entities sort after all entities.
  ///
  /// @param point The reference point in map coordinates.
  /// @return This query.
  /// @throws NullPointerException if `point` is `null`.
  public EntityQuery<T> nearestTo(Point2D point) {
    Objects.requireNonNull(point);
    this.comparator = Comparator.comparingDouble(entity -> entity instanceof IEntity candidate
      ? GeometricUtilities.distance(candidate.getCenter(), point) : Double.POSITIVE_INFINITY);
    return this;
  }

  /// Replaces the current ordering with a comparator.
  ///
  /// @param order The comparator to use.
  /// @return This query.
  /// @throws NullPointerException if `order` is `null`.
  public EntityQuery<T> sorted(Comparator<T> order) {
    this.comparator = Objects.requireNonNull(order);
    return this;
  }

  /// Evaluates this query and returns its matching values.
  ///
  /// @return An unmodifiable result list in the configured order.
  public List<T> list() {
    Stream<T> stream = this.source.stream().filter(this.predicate);
    if (this.comparator != null) stream = stream.sorted(this.comparator);
    return stream.toList();
  }

  /// Finds the first matching value in configured or source order.
  ///
  /// @return The first match, or an empty optional when nothing matches.
  public Optional<T> first() {
    if (this.comparator != null) {
      return this.list().stream().findFirst();
    }
    return this.source.stream().filter(this.predicate).findFirst();
  }

  /// Counts matching values without applying the configured ordering.
  ///
  /// @return The number of matching values.
  public long count() {
    return this.source.stream().filter(this.predicate).count();
  }

  /// Tests whether this query has no matching values.
  ///
  /// @return `true` when no value matches.
  public boolean isEmpty() {
    return !this.source.stream().anyMatch(this.predicate);
  }

  /// Creates a query over all entities of a type in an environment.
  ///
  /// @param environment The environment to query.
  /// @param type The requested entity type.
  /// @param <T> The requested entity type.
  /// @return A query over the environment's matching entities.
  /// @throws NullPointerException if `environment` or `type` is `null`.
  public static <T> EntityQuery<T> in(Environment environment, Class<? extends T> type) {
    Objects.requireNonNull(environment);
    Objects.requireNonNull(type);
    return new EntityQuery<>(environment.getEntities(type));
  }
}
