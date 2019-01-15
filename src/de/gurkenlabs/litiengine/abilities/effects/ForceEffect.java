package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.Force;

public abstract class ForceEffect extends Effect {
  private final float strength;
  private Force appliedForce;

  protected ForceEffect(final Ability ability, final float strength, final EffectTarget... targets) {
    super(ability, targets);
    this.strength = strength;
  }

  @Override
  public void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    // only apply one force per effect
    if (this.getAppliedForce() != null) {
      return;
    }

    IMobileEntity forceEntity = null;
    if (affectedEntity instanceof IMobileEntity) {
      forceEntity = (IMobileEntity) affectedEntity;
    } else {
      return;
    }

    final Force force = this.applyForce(forceEntity);
    if (force != null) {
      this.appliedForce = force;
    }
  }

  public float getStrength() {
    return this.strength;
  }

  protected abstract Force applyForce(final IMobileEntity affectedEntity);

  @Override
  protected void cease(final EffectApplication appliance) {
    super.cease(appliance);
    if (this.getAppliedForce() != null) {
      this.getAppliedForce().end();
      this.appliedForce = null;
    }
  }

  protected Force getAppliedForce() {
    return this.appliedForce;
  }

  @Override
  protected boolean hasEnded(final EffectApplication appliance) {
    return super.hasEnded(appliance) || this.getAppliedForce() == null;
  }
}
