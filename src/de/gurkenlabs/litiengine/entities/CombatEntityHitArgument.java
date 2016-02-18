package de.gurkenlabs.litiengine.entities;

public class CombatEntityHitArgument {
  private final ICombatEntity entity;
  private final float damage;

  public CombatEntityHitArgument(final ICombatEntity entity, final float damage) {
    this.entity = entity;
    this.damage = damage;
  }

  public float getDamage() {
    return this.damage;
  }

  public ICombatEntity getEntity() {
    return this.entity;
  }
}