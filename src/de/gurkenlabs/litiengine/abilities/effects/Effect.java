/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.EntityDistanceComparator;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class Effect.
 */
public abstract class Effect implements IEffect {
  private final List<Consumer<EffectArgument>> appliedConsumer;
  private final List<Consumer<EffectArgument>> ceasedConsumer;

  private final List<ICombatEntity> affectedEntities;
  private final List<IEffect> followUpEffects;

  private final IEnvironment environment;

  private final Ability ability;

  /** The effect targets. */
  private final EffectTarget[] effectTargets;

  /** The delay. */
  private int delay;

  /** The duration. */
  private int duration;

  private boolean active;

  /**
   * Instantiates a new effect.
   *
   * @param ability
   *          the ability
   * @param targets
   *          the targets
   */
  protected Effect(final IEnvironment environment, final Ability ability, final EffectTarget... targets) {
    this.appliedConsumer = new CopyOnWriteArrayList<>();
    this.ceasedConsumer = new CopyOnWriteArrayList<>();
    this.affectedEntities = new CopyOnWriteArrayList<>();
    this.followUpEffects = new CopyOnWriteArrayList<>();

    this.environment = environment;
    this.ability = ability;

    this.duration = ability.getAttributes().getDuration().getCurrentValue();
    if (targets == null || targets.length == 0) {
      this.effectTargets = new EffectTarget[] { EffectTarget.NONE };
    } else {
      this.effectTargets = targets;
    }
  }

  /**
   * Apply.
   */
  @Override
  public void apply(final Shape impactArea) {    
    for (final ICombatEntity affectedEntity : this.lookForAffectedEntities(impactArea)) {
      // cannot affect the entity with the effect while it is still affected
      if (this.affectedEntities.contains(affectedEntity)) {
        return;
      }

      this.affectedEntities.add(affectedEntity);
      this.apply(affectedEntity);
    }

    this.active = true;
  }

  @Override
  public void cease() {
    for (final ICombatEntity affectedEntity : this.affectedEntities) {
      this.cease(affectedEntity);
    }

    this.affectedEntities.clear();
    this.active = false;
  }

  /**
   * Gets the ability.
   *
   * @return the ability
   */
  public Ability getAbility() {
    return this.ability;
  }

  public List<ICombatEntity> getAffectedEntities() {
    return this.affectedEntities;
  }

  /**
   * Gets the delay in milliseconds.
   *
   * @return the delay
   */
  @Override
  public int getDelay() {
    return this.delay;
  }

  /**
   * Gets the duration.
   *
   * @return the duration
   */
  @Override
  public int getDuration() {
    return this.duration;
  }

  /**
   * Gets the effect targets.
   *
   * @return the effect targets
   */
  @Override
  public EffectTarget[] getEffectTargets() {
    return this.effectTargets;
  }

  /**
   * Gets the follow up effects.
   *
   * @return the follow up effects
   */
  @Override
  public List<IEffect> getFollowUpEffects() {
    return this.followUpEffects;
  }

  public IEnvironment getEnvironment(){
    return this.environment;
  }
  
  @Override
  public boolean isActive() {
    return this.active;
  }

  @Override
  public void onEffectApplied(final Consumer<EffectArgument> consumer) {
    if (!this.appliedConsumer.contains(consumer)) {
      this.appliedConsumer.add(consumer);
    }
  }

  @Override
  public void onEffectCeased(final Consumer<EffectArgument> consumer) {
    if (!this.ceasedConsumer.contains(consumer)) {
      this.ceasedConsumer.add(consumer);
    }
  }

  /**
   * Sets the delay.
   *
   * @param delay
   *          the new delay
   */
  public void setDelay(final int delay) {
    this.delay = delay;
  }

  /**
   * Sets the duration.
   *
   * @param duration
   *          the new duration
   */
  public void setDuration(final int duration) {
    this.duration = duration;
  }

  /**
   * Can attack entity.
   *
   * @return the predicate<? super attackable entity>
   */
  private Predicate<? super ICombatEntity> canAttackEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && !entity.isFriendly(this.getAbility().getExecutor()) && !entity.isIndestructible() && !entity.isDead();
  }

  /**
   * Checks if is alive friendly entity.
   *
   * @return the predicate<? super attackable entity>
   */
  private Predicate<? super ICombatEntity> isAliveFriendlyEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  protected void apply(final ICombatEntity entity) {
    final EffectArgument arg = new EffectArgument(this, entity);
    for (final Consumer<EffectArgument> consumer : this.appliedConsumer) {
      consumer.accept(arg);
    }
  }

  protected void cease(final ICombatEntity entity) {
    final EffectArgument arg = new EffectArgument(this, entity);
    for (final Consumer<EffectArgument> consumer : this.ceasedConsumer) {
      consumer.accept(arg);
    }
  }

  /**
   * Gets the total duration.
   *
   * @return the total duration
   */
  protected long getTotalDuration() {
    return this.getDuration() + this.getDelay();
  }

  /**
   * Look for affected entities.
   *
   * @return the list
   */
  protected List<ICombatEntity> lookForAffectedEntities(final Shape impactArea) {
    List<ICombatEntity> affectedEntities = new ArrayList<>();

    for (final EffectTarget target : this.effectTargets) {
      switch (target) {
      case EXECUTINGENTITY:
        affectedEntities.add(this.getAbility().getExecutor());
        break;
      case ENEMY:
        affectedEntities.addAll(this.environment.findCombatEntities(impactArea));
        affectedEntities = affectedEntities.stream().filter(this.canAttackEntity()).collect(Collectors.toList());
        break;
      case FRIENDLY:
        affectedEntities.addAll(this.environment.findCombatEntities(impactArea));
        affectedEntities = affectedEntities.stream().filter(this.isAliveFriendlyEntity()).collect(Collectors.toList());
        break;
      default:
        break;
      }
    }

    affectedEntities.removeAll(Collections.singleton(null));

    if (!this.getAbility().isMultiTarget()) {
      affectedEntities.sort(new EntityDistanceComparator(this.getAbility().getExecutor()));
      if (affectedEntities.size() > 0) {
        final ICombatEntity closest = affectedEntities.get(0);
        affectedEntities = new ArrayList<>();
        affectedEntities.add(closest);
      }
    }

    return affectedEntities;
  }

  protected Collection<ICombatEntity> getEntitiesInImpactArea(final Shape impactArea) {
    return this.environment.findCombatEntities(impactArea);
  }
}