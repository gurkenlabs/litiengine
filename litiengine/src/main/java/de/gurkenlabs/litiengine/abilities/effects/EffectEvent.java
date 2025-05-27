package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.util.EventObject;

/**
 * Represents an event related to an {@code Effect} being applied to a combat entity. This class provides information about the effect and the entity
 * it is applied to.
 */
public class EffectEvent extends EventObject {
  private static final long serialVersionUID = -6911987630602502891L;

  /**
   * The combat entity associated with this event. This field is transient to avoid serialization issues.
   */
  private final transient ICombatEntity combatEntity;

  /**
   * The effect associated with this event. This field is transient to avoid serialization issues.
   */
  private final transient Effect effect;

  /**
   * Initializes a new instance of the {@code EffectEvent} class.
   *
   * @param effect       The effect that triggered this event.
   * @param combatEntity The combat entity affected by the effect.
   */
  EffectEvent(final Effect effect, final ICombatEntity combatEntity) {
    super(effect);
    this.effect = effect;
    this.combatEntity = combatEntity;
  }

  /**
   * Gets the combat entity associated with this event.
   *
   * @return The {@code ICombatEntity} affected by the effect.
   */
  public ICombatEntity getCombatEntity() {
    return this.combatEntity;
  }

  /**
   * Gets the effect associated with this event.
   *
   * @return The {@code Effect} that triggered this event.
   */
  public Effect getEffect() {
    return this.effect;
  }
}
