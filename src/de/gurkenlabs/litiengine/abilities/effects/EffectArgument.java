package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class EffectArgument {
  private final IEffect effect;
  private final ICombatEntity combatEntity;

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
