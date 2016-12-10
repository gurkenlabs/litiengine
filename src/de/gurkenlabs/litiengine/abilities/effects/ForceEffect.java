package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.physics.Force;

public abstract class ForceEffect extends Effect {
  /** The strength. */
  private final float strength;

  private Force appliedForce;

  protected ForceEffect(final IEnvironment environment, final Ability ability, final float strength, final EffectTarget... targets) {
    super(environment, ability, targets);
    this.strength = strength;
    this.setDuration(-1);
  }

  @Override
  public void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    // only apply one force per effect
    if (this.getAppliedForce() != null) {
      return;
    }

    final Force force = this.applyForce(affectedEntity);
    if (force != null) {
      this.appliedForce = force;
    }
  }

  protected abstract Force applyForce(final ICombatEntity affectedEntity);

  @Override
  protected void cease(final IGameLoop loop, final EffectAppliance appliance) {
    super.cease(loop, appliance);
    if (this.getAppliedForce() != null) {
      this.getAppliedForce().end();
      this.appliedForce = null;
    }
  }

  protected Force getAppliedForce() {
    return this.appliedForce;
  }

  public float getStrength() {
    return this.strength;
  }

  @Override
  protected boolean hasEnded(final IGameLoop loop, final EffectAppliance appliance) {
    return super.hasEnded(loop, appliance) || this.getAppliedForce() == null;
  }
}
