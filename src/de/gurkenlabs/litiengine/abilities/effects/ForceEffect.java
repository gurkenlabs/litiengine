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
  public void update(final IGameLoop loop) {
    super.update(loop);
    if (this.getAppliedForce() == null) {
      return;
    }

    if (this.getAppliedForce().hasEnded()) {
      this.setActive(false);
    }
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
  public void cease() {
    super.cease();
    this.setActive(false);
    if (this.getAppliedForce() != null) {
      this.getAppliedForce().end();
      this.appliedForce = null;
    }
  }

  public float getStrength() {
    return this.strength;
  }

  protected abstract Force applyForce(final ICombatEntity affectedEntity);

  protected Force getAppliedForce() {
    return this.appliedForce;
  }
}
