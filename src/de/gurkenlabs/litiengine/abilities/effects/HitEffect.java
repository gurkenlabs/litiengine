/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.OffensiveAbility;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

/**
 * The Class HitEffect.
 */
public class HitEffect extends Effect {

  /** The offensive ability. */
  private final OffensiveAbility offensiveAbility;

  /**
   * Instantiates a new hurt effect.
   *
   * @param ability
   *          the ability
   */
  public HitEffect(final IEnvironment environment, final OffensiveAbility ability) {
    super(environment, ability, new EffectTarget[] { EffectTarget.ENEMY });
    this.offensiveAbility = ability;
  }

  @Override
  public void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    affectedEntity.hit(this.offensiveAbility.getAttackDamage());
  }
}
