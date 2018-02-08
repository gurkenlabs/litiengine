package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class AttackSpeedEffect extends AttributeStateEffect<Float> {

  public AttackSpeedEffect(final Ability ability, final float delta, final Modification modifictaion, final EffectTarget... targtes) {
    super(ability, modifictaion, delta, targtes);
  }

  @Override
  protected Attribute<Float> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getAttackSpeed();
  }
}
