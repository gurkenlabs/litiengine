package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import java.io.Serial;
import java.util.EventObject;

/**
 * Event dispatched whenever an {@link ICombatEntity} is hit. Carries the executor of the hit, the hit entity, the optional triggering ability, the
 * dealt damage and the resulting kill flag.
 */
public class EntityHitEvent extends EventObject {
  @Serial private static final long serialVersionUID = 1582822545149624579L;
  /**
   * The dealt damage.
   */
  private final int damage;

  /**
   * Whether the hit killed the target.
   */
  private final boolean kill;
  private final transient ICombatEntity executor;
  private final transient ICombatEntity hitEntity;
  private final transient Ability ability;
  /** Game-time timestamp at which the event was dispatched. */
  private final long time;

  EntityHitEvent(final ICombatEntity hitEntity, final Ability ability, final int damage, final boolean kill) {
    super(hitEntity);
    this.executor = ability != null ? ability.getExecutor() : null;
    this.hitEntity = hitEntity;
    this.ability = ability;
    this.damage = damage;
    this.kill = kill;
    this.time = Game.time().now();
  }

  /**
   * Gets the damage dealt by the hit.
   *
   * @return the damage
   */
  public int getDamage() {
    return this.damage;
  }

  /**
   * Gets the entity that caused the hit (the executor of the triggering ability).
   *
   * @return the executor, or {@code null} if no ability triggered the hit
   */
  public ICombatEntity getExecutor() {
    return this.executor;
  }

  /**
   * Gets the entity that was hit.
   *
   * @return the hit entity
   */
  public ICombatEntity getHitEntity() {
    return this.hitEntity;
  }

  /**
   * Returns whether the hit killed the target.
   *
   * @return {@code true} if the target was killed
   */
  public boolean wasKilled() {
    return this.kill;
  }

  /**
   * Gets the ability that triggered the hit, if any.
   *
   * @return the triggering ability, or {@code null}
   */
  public Ability getAbility() {
    return this.ability;
  }

  /**
   * Gets the game-time timestamp at which the event was dispatched.
   *
   * @return the timestamp
   */
  public long getTime() {
    return time;
  }
}
