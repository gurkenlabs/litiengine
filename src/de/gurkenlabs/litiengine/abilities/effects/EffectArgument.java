package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class EffectArgument {
  private final ICombatEntity combatEntity;
  private final Effect effect;

  public EffectArgument(final Effect effect, final ICombatEntity combatEntity) {
    this.effect = effect;
    this.combatEntity = combatEntity;
  }

  public ICombatEntity getCombatEntity() {
    return this.combatEntity;
  }

  public Effect getEffect() {
    return this.effect;
  }
}
