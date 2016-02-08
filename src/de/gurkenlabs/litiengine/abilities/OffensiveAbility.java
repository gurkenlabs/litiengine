/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;

/**
 * The Class OffensiveAbility.
 */
public abstract class OffensiveAbility extends Ability {

  /**
   * Instantiates a new offensive ability.
   *
   * @param executingMob
   *          the executing mob
   */
  protected OffensiveAbility(final IMovableCombatEntity executingMob) {
    super(executingMob);
  }

  /**
   * Gets the attack damage.
   *
   * @return the attack damage
   */
  public int getAttackDamage() {
    return Math.round(this.getAttributes().getValue().getCurrentValue() * this.getExecutor().getAttributes().getDamageMultiplier().getCurrentValue());
  }
}
