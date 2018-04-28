package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.abilities.Ability;

public class CombatEntityHitEvent extends CombatEntityEvent {
  private static final long serialVersionUID = 6427715457530701892L;
  private final transient Ability ability;
  private final float damage;

  public CombatEntityHitEvent(final ICombatEntity entity, final float damage, final Ability ability) {
    super(entity, entity);
    this.damage = damage;
    this.ability = ability;
  }

  public Ability getAbility() {
    return this.ability;
  }

  public float getDamage() {
    return this.damage;
  }
}