package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.util.EventObject;

public class EffectEvent extends EventObject {
  private static final long serialVersionUID = -6911987630602502891L;
  private final transient ICombatEntity combatEntity;
  private final transient Effect effect;

  EffectEvent(final Effect effect, final ICombatEntity combatEntity) {
    super(effect);
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
