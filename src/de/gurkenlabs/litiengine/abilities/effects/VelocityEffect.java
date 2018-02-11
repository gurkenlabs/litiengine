package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class VelocityEffect extends AttributeStateEffect<Float> {

  public VelocityEffect(final Ability ability, final float velocityDelta, final Modification modification, final EffectTarget... targtes) {
    super(ability, modification, velocityDelta, targtes);
  }

  @Override
  protected Attribute<Float> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getVelocity();
  }
}
