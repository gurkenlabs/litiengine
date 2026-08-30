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
public final class EntityQuery<T> {
  private final Collection<T> source;
  private Predicate<T> predicate = entity -> true;
  private Comparator<T> comparator;

  public EntityQuery(Collection<T> source) {
    this.source = source == null ? List.of() : source;
  }

  public EntityQuery<T> matching(Predicate<T> filter) {
    this.predicate = this.predicate.and(Objects.requireNonNull(filter));
    return this;
  }

  public EntityQuery<T> tagged(String tag) {
    return this.matching(entity -> entity instanceof IEntity candidate && candidate.hasTag(tag));
  }

  public EntityQuery<T> named(String name) {
    return this.matching(entity -> entity instanceof IEntity candidate && Objects.equals(candidate.getName(), name));
  }

  public EntityQuery<T> alive() {
    return this.matching(entity -> !(entity instanceof ICombatEntity combat) || !combat.isDead());
  }

  public EntityQuery<T> dead() {
    return this.matching(entity -> entity instanceof ICombatEntity combat && combat.isDead());
  }

  public EntityQuery<T> team(int team) {
    return this.matching(entity -> entity instanceof ICombatEntity combat && combat.getTeam() == team);
  }

  public EntityQuery<T> enemyOf(ICombatEntity entity) {
    Objects.requireNonNull(entity);
    return this.matching(candidate -> candidate instanceof ICombatEntity combat && combat.getTeam() != entity.getTeam());
  }

  public EntityQuery<T> within(Point2D point, double distance) {
    Objects.requireNonNull(point);
    if (distance < 0) throw new IllegalArgumentException("Distance must not be negative.");
    return this.matching(entity -> entity instanceof IEntity candidate && candidate.getCenter().distance(point) <= distance);
  }

  public EntityQuery<T> nearestTo(Point2D point) {
    Objects.requireNonNull(point);
    this.comparator = Comparator.comparingDouble(entity -> entity instanceof IEntity candidate
      ? GeometricUtilities.distance(candidate.getCenter(), point) : Double.POSITIVE_INFINITY);
    return this;
  }

  public EntityQuery<T> sorted(Comparator<T> order) {
    this.comparator = Objects.requireNonNull(order);
    return this;
  }

  public List<T> list() {
    Stream<T> stream = this.source.stream().filter(this.predicate);
    if (this.comparator != null) stream = stream.sorted(this.comparator);
    return stream.toList();
  }

  public Optional<T> first() {
    if (this.comparator != null) {
      return this.list().stream().findFirst();
    }
    return this.source.stream().filter(this.predicate).findFirst();
  }

  public long count() {
    return this.source.stream().filter(this.predicate).count();
  }

  public boolean isEmpty() {
    return !this.source.stream().anyMatch(this.predicate);
  }

  public static <T> EntityQuery<T> in(Environment environment, Class<? extends T> type) {
    Objects.requireNonNull(environment);
    Objects.requireNonNull(type);
    return new EntityQuery<>(environment.getEntities(type));
  }
}
