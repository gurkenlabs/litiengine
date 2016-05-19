package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.abilities.Ability;

public class CombatEntityHitArgument {
  private final ICombatEntity entity;
  private final float damage;
  private final Ability ability;

  public CombatEntityHitArgument(final ICombatEntity entity, final float damage, final Ability ability) {
    this.entity = entity;
    this.damage = damage;
    this.ability = ability;
  }

  public float getDamage() {
    return this.damage;
  }

  public ICombatEntity getEntity() {
    return this.entity;
  }

  public Ability getAbility() {
    return this.ability;
  }
}