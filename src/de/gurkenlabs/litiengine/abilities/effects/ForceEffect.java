package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.physics.Force;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

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

  @Override
  protected void cease(final IGameLoop loop, final EffectAppliance appliance) {
    super.cease(loop, appliance);
    if (this.getAppliedForce() != null) {
      this.getAppliedForce().end();
      this.appliedForce = null;
    }
  }

  @Override
  protected boolean hasEnded(final IGameLoop loop, final EffectAppliance appliance) {
    return super.hasEnded(loop, appliance) || this.getAppliedForce() == null;
  }

  public float getStrength() {
    return this.strength;
  }

  protected abstract Force applyForce(final ICombatEntity affectedEntity);

  protected Force getAppliedForce() {
    return this.appliedForce;
  }
}
