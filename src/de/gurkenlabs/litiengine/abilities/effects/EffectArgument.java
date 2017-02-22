package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class EffectArgument {
  private final ICombatEntity combatEntity;
  private final IEffect effect;

  public EffectArgument(final IEffect effect, final ICombatEntity combatEntity) {
    this.effect = effect;
    this.combatEntity = combatEntity;
  }

  public ICombatEntity getCombatEntity() {
    return this.combatEntity;
  }

  public IEffect getEffect() {
    return this.effect;
  }
}
