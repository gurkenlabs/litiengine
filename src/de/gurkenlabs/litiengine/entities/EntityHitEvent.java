package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;

public class EntityHitEvent extends EventObject {
  private static final long serialVersionUID = 1582822545149624579L;
  private final int damage;

  private final boolean kill;
  private final transient ICombatEntity executor;
  private final transient ICombatEntity hitEntity;
  private final transient Ability ability;
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

  public int getDamage() {
    return this.damage;
  }

  public ICombatEntity getExecutor() {
    return this.executor;
  }

  public ICombatEntity getHitEntity() {
    return this.hitEntity;
  }

  public boolean wasKilled() {
    return this.kill;
  }

  public Ability getAbility() {
    return this.ability;
  }

  public long getTime() { return time; }
}
