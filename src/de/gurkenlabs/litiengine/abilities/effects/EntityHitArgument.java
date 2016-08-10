package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class EntityHitArgument {
  private final ICombatEntity executor;
  private final ICombatEntity hitEntity;
  private final int damage;
  private final boolean kill;

  public EntityHitArgument(final ICombatEntity executor, final ICombatEntity hitEntity, final int damage, final boolean kill) {
    this.executor = executor;
    this.hitEntity = hitEntity;
    this.damage = damage;
    this.kill = kill;
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

  public boolean isKill() {
    return this.kill;
  }
}
