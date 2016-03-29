/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.effects.EffectArgument;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;
import de.gurkenlabs.litiengine.annotation.AbilityInfo;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class Ability.
 */
@AbilityInfo
public abstract class Ability {
  private final List<Consumer<AbilityExecution>> abilityCastConsumer;

  /** The ability type. */
  private final String name;
  /** The tooltip. */
  private final String description;

  /** The attributes. */
  private final AbilityAttributes attributes;

  /** The current execution. */
  private AbilityExecution currentExecution;

  /** The effects. */
  private final List<IEffect> effects;

  /** The executing mob. */
  private final IMovableCombatEntity executor;

  /** The multi target. */
  private final boolean multiTarget;

  private final CastType castType;

  /**
   * Instantiates a new ability.
   *
   * @param executingMob
   *          the executing mob
   */
  protected Ability(final IMovableCombatEntity executor) {
    this.abilityCastConsumer = new CopyOnWriteArrayList<>();
    this.effects = new CopyOnWriteArrayList<>();

    final AbilityInfo info = this.getClass().getAnnotation(AbilityInfo.class);
    this.attributes = new AbilityAttributes(info);
    this.executor = executor;
    this.name = info.name();
    this.multiTarget = info.multiTarget();
    this.description = info.description();
    this.castType = info.castType();
  }

  public void addEffect(final IEffect effect) {
    this.getEffects().add(effect);
  }

  /**
   * Calculate impact area.
   *
   * @return the shape
   */
  public Shape calculateImpactArea() {
    return this.internalCalculateImpactArea(this.getExecutor().getAngle());
  }

  protected Shape internalCalculateImpactArea(final float angle) {
    final int impact = this.getAttributes().getImpact().getCurrentValue();
    final int impactAngle = this.getAttributes().getImpactAngle().getCurrentValue();
    final double arcX = this.getExecutor().getCollisionBox().getCenterX() - impact / 2;
    final double arcY = this.getExecutor().getCollisionBox().getCenterY() - impact / 2;

    // project
    final Point2D appliedRange = GeometricUtilities.project(new Point2D.Double(arcX, arcY), angle, this.getAttributes().getRange().getCurrentValue() / 2);
    final double start = angle - impactAngle / 2 - 90;
    if (impactAngle % 360 == 0) {
      return new Ellipse2D.Double(appliedRange.getX(), appliedRange.getY(), impact, impact);
    }

    return new Arc2D.Double(appliedRange.getX(), appliedRange.getY(), impact, impact, start, impactAngle, Arc2D.PIE);
  }

  public Ellipse2D calculatePotentialImpactArea() {
    final int range = this.getAttributes().getImpact().getCurrentValue();
    final double arcX = this.getExecutor().getCollisionBox().getCenterX() - range / 2;
    final double arcY = this.getExecutor().getCollisionBox().getCenterY() - range / 2;

    return new Ellipse2D.Double(arcX, arcY, range, range);
  }

  /**
   * Can cast.
   *
   * @return true, if successful
   */
  public boolean canCast(final IGameLoop gameLoop) {
    return !this.getExecutor().isDead() && (this.getCurrentExecution() == null || this.getCurrentExecution().getExecutionTicks() == 0 || gameLoop.getDeltaTime(this.getCurrentExecution().getExecutionTicks()) >= this.getAttributes().getCooldown().getCurrentValue());
  }

  /**
   * Cast.
   */
  public AbilityExecution cast(final IGameLoop gameLoop) {
    if (!this.canCast(gameLoop)) {
      return null;
    }
    this.currentExecution = new AbilityExecution(gameLoop, this);

    for (final Consumer<AbilityExecution> castConsumer : this.abilityCastConsumer) {
      castConsumer.accept(this.currentExecution);
    }

    return this.getCurrentExecution();
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  public AbilityAttributes getAttributes() {
    return this.attributes;
  }

  /**
   * Gets the cooldown in seconds.
   *
   * @return the cooldown in seconds
   */
  public float getCooldownInSeconds() {
    return (float) (this.getAttributes().getCooldown().getCurrentValue() / 1000.0);
  }

  /**
   * Gets the current execution.
   *
   * @return the current execution
   */
  public AbilityExecution getCurrentExecution() {
    return this.currentExecution;
  }

  public String getDescription() {
    return this.description;
  }

  /**
   * Gets the executing mob.
   *
   * @return the executing mob
   */
  public IMovableCombatEntity getExecutor() {
    return this.executor;
  }

  public String getName() {
    return this.name;
  }

  /**
   * Gets the remaining cooldown in seconds.
   *
   * @return the remaining cooldown in seconds
   */
  public float getRemainingCooldownInSeconds(final IGameLoop loop) {
    if (this.getCurrentExecution() == null || this.getExecutor() == null || this.getExecutor().isDead()) {
      return 0;
    }

    // calculate cooldown in seconds
    return (float) (!this.canCast(loop) ? (this.getAttributes().getCooldown().getCurrentValue() - loop.getDeltaTime(this.getCurrentExecution().getExecutionTicks())) / 1000.0 : 0);
  }

  /**
   * Checks if is multi target.
   *
   * @return true, if is multi target
   */
  public boolean isMultiTarget() {
    return this.multiTarget;
  }

  public void onCast(final Consumer<AbilityExecution> castConsumer) {
    if (!this.abilityCastConsumer.contains(castConsumer)) {
      this.abilityCastConsumer.add(castConsumer);
    }
  }

  public void onEffectApplied(final Consumer<EffectArgument> consumer) {
    for (final IEffect effect : this.getEffects()) {
      // registers to all effects and their follow up effects recursively
      this.onEffectApplied(effect, consumer);
    }
  }

  public void onEffectCeased(final Consumer<EffectArgument> consumer) {
    for (final IEffect effect : this.getEffects()) {
      // registers to all effects and their follow up effects recursively
      this.onEffectCeased(effect, consumer);
    }
  }

  public CastType getCastType() {
    return this.castType;
  }

  private void onEffectApplied(final IEffect effect, final Consumer<EffectArgument> consumer) {
    effect.onEffectApplied(consumer);

    for (final IEffect followUp : effect.getFollowUpEffects()) {
      this.onEffectApplied(followUp, consumer);
    }
  }

  private void onEffectCeased(final IEffect effect, final Consumer<EffectArgument> consumer) {
    effect.onEffectCeased(consumer);

    for (final IEffect followUp : effect.getFollowUpEffects()) {
      this.onEffectCeased(followUp, consumer);
    }
  }

  /**
   * Gets the effects.
   *
   * @return the effects
   */
  protected List<IEffect> getEffects() {
    return this.effects;
  }
}
