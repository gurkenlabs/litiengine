package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.abilities.Ability;

public class CombatEntityHitArgument {
  private final Ability ability;
  private final float damage;
  private final ICombatEntity entity;

  public CombatEntityHitArgument(final ICombatEntity entity, final float damage, final Ability ability) {
    this.entity = entity;
    this.damage = damage;
    this.ability = ability;
  }

  public Ability getAbility() {
    return this.ability;
  }

  public float getDamage() {
    return this.damage;
  }

  public ICombatEntity getEntity() {
    return this.entity;
  }
}