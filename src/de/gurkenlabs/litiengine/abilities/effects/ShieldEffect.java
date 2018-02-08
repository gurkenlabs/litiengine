package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class ShieldEffect extends AttributeStateEffect<Short> {

  public ShieldEffect(final Ability ability, final short shieldDelta, final Modification modifictaion, final EffectTarget... targtes) {
    super(ability, modifictaion, shieldDelta, targtes);
  }

  @Override
  public void cease(final ICombatEntity affectedEntity) {
    final AttributeModifier<Short> revert = new AttributeModifier<>(this.getModifier().getModification(), -this.getModifier().getModifyValue());
    this.getAttribute(affectedEntity).modifyBaseValue(revert);
  }

  @Override
  protected void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    this.getAttribute(affectedEntity).modifyBaseValue(this.getModifier());
  }

  @Override
  protected Attribute<Short> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getShield();
  }
}
