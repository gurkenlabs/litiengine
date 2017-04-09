package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.physics.Force;

public abstract class ForceEffect extends Effect {
  private Force appliedForce;

  /** The strength. */
  private final float strength;

  protected ForceEffect(final Ability ability, final float strength, final EffectTarget... targets) {
    super(ability, targets);
    this.strength = strength;
    this.setDuration(-1);
  }

  @Override
  public void apply(final ICombatEntity affectedEntity, final IEnvironment environment) {
    super.apply(affectedEntity, environment);
    // only apply one force per effect
    if (this.getAppliedForce() != null) {
      return;
    }

    final Force force = this.applyForce(affectedEntity, environment);
    if (force != null) {
      this.appliedForce = force;
    }
  }

  public float getStrength() {
    return this.strength;
  }

  protected abstract Force applyForce(final ICombatEntity affectedEntity, final IEnvironment environment);

  @Override
  protected void cease(final IGameLoop loop, final EffectApplication appliance) {
    super.cease(loop, appliance);
    if (this.getAppliedForce() != null) {
      this.getAppliedForce().end();
      this.appliedForce = null;
    }
  }

  protected Force getAppliedForce() {
    return this.appliedForce;
  }

  @Override
  protected boolean hasEnded(final IGameLoop loop, final EffectApplication appliance) {
    return super.hasEnded(loop, appliance) || this.getAppliedForce() == null;
  }
}
