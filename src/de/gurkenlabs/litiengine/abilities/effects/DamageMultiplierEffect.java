package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class DamageMultiplierEffect extends AttributeStateEffect<Float> {

  public DamageMultiplierEffect(final Ability ability, final Float delta, final Modification modification, final EffectTarget... targtes) {
    super(ability, modification, delta, targtes);
  }

  @Override
  protected Attribute<Float> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getDamageMultiplier();
  }
}