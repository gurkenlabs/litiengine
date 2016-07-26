/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.EntityComparator;
import de.gurkenlabs.litiengine.entities.EntityDistanceComparator;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

/**
 * The Class Effect.
 */
public abstract class Effect implements IEffect {
  private final List<Consumer<EffectArgument>> appliedConsumer;
  private final List<Consumer<EffectArgument>> ceasedConsumer;

  private final List<EffectAppliance> appliances;
  private final List<IEffect> followUpEffects;

  private final IEnvironment environment;

  private final Ability ability;

  /** The effect targets. */
  private final EffectTarget[] effectTargets;

  private EntityComparator targetPriorityComparator;

  /** The delay. */
  private int delay;

  /** The duration. */
  private int duration;

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
    this.appliances = new ArrayList<>();
    this.followUpEffects = new CopyOnWriteArrayList<>();

    this.environment = environment;
    this.ability = ability;
    this.targetPriorityComparator = new EntityDistanceComparator(this.getAbility().getExecutor());

    this.duration = ability.getAttributes().getDuration().getCurrentValue();
    if (targets == null || targets.length == 0) {
      this.effectTargets = new EffectTarget[] { EffectTarget.NONE };
    } else {
      this.effectTargets = targets;
    }
  }

  /**
   * 1. Cease the effect after its duration. 2. apply all follow up effects 3.
   * remove appliance 4. unregister from loop if all appliances are done
   */
  @Override
  public void update(final IGameLoop loop) {

    for (Iterator<EffectAppliance> iterator = this.getActiveAppliances().iterator(); iterator.hasNext();) {
      EffectAppliance appliance = iterator.next();
      // if the effect duration is reached
      if (this.hasEnded(loop, appliance)) {

        iterator.remove();
        this.cease(loop, appliance);
      }
    }

    // 4. unregister if all appliances are finished
    if (this.getActiveAppliances().size() == 0) {
      loop.unregisterFromUpdate(this);
    }
  }

  /**
   * Apply.
   */
  @Override
  public void apply(IGameLoop loop, final Shape impactArea) {
    List<ICombatEntity> affected = this.lookForAffectedEntities(impactArea);
    for (final ICombatEntity affectedEntity : this.lookForAffectedEntities(impactArea)) {
      this.apply(affectedEntity);
    }

    this.appliances.add(new EffectAppliance(loop.getTicks(), affected, impactArea));

    // if it is the first appliance -> register for update
    if (this.appliances.size() == 1) {
      loop.registerForUpdate(this);
    }
  }

  @Override
  public boolean isActive(ICombatEntity entity) {
    for (EffectAppliance app : this.getActiveAppliances()) {
      for (ICombatEntity affected : app.getAffectedEntities()) {
        if (affected.equals(entity)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Gets the ability.
   *
   * @return the ability
   */
  public Ability getAbility() {
    return this.ability;
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

  public IEnvironment getEnvironment() {
    return this.environment;
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

  public EntityComparator getTargetPriorityComparator() {
    return this.targetPriorityComparator;
  }

  public void setTargetPriorityComparator(final EntityComparator targetPriorityComparator) {
    this.targetPriorityComparator = targetPriorityComparator;
  }

  @Override
  public List<EffectAppliance> getActiveAppliances() {
    return this.appliances;
  }

  protected void cease(final IGameLoop loop, final EffectAppliance appliance) {
    // 1. cease the effect for all affected entities
    for (ICombatEntity entity : appliance.getAffectedEntities()) {
      this.cease(entity);
    }

    // 2. apply follow up effects
    this.getFollowUpEffects().forEach(followUp -> {
      followUp.apply(loop, appliance.getImpactArea());
    });
  }

  protected boolean hasEnded(final IGameLoop loop, EffectAppliance appliance) {
    final long effectDuration = loop.getDeltaTime(appliance.getAppliedTicks());
    return effectDuration > this.getDuration();
  }

  /**
   * Can attack entity.
   *
   * @return the predicate<? super attackable entity>
   */
  private Predicate<? super ICombatEntity> canAttackEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && !entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  /**
   * Checks if is alive friendly entity.
   *
   * @return the predicate<? super attackable entity>
   */
  private Predicate<? super ICombatEntity> isAliveFriendlyEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  private Predicate<? super ICombatEntity> isDeadFriendlyEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && entity.isDead();
  }

  protected void apply(final ICombatEntity entity) {
    entity.getAppliedEffects().add(this);
    final EffectArgument arg = new EffectArgument(this, entity);
    for (final Consumer<EffectArgument> consumer : this.appliedConsumer) {
      consumer.accept(arg);
    }
  }

  public void cease(final ICombatEntity entity) {
    entity.getAppliedEffects().remove(this);
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
        return affectedEntities;
      case ENEMY:
        affectedEntities.addAll(this.getEntitiesInImpactArea(impactArea));
        affectedEntities = affectedEntities.stream().filter(this.canAttackEntity()).collect(Collectors.toList());
        break;
      case FRIENDLY:
        affectedEntities.addAll(this.getEntitiesInImpactArea(impactArea));
        affectedEntities = affectedEntities.stream().filter(this.isAliveFriendlyEntity()).collect(Collectors.toList());
        break;
      case FRIENDLYDEAD:
        affectedEntities.addAll(this.getEntitiesInImpactArea(impactArea));
        affectedEntities = affectedEntities.stream().filter(this.isDeadFriendlyEntity()).collect(Collectors.toList());
        break;
      default:
        break;
      }
    }

    affectedEntities.removeAll(Collections.singleton(null));

    if (!this.getAbility().isMultiTarget() && affectedEntities.size() > 0) {
      affectedEntities.sort(this.targetPriorityComparator);
      final ICombatEntity target;
      if (this.getAbility().getExecutor().getTarget() != null) {
        target = this.getAbility().getExecutor().getTarget();
      } else {
        target = affectedEntities.get(0);
      }
      affectedEntities = new ArrayList<>();
      affectedEntities.add(target);

    }

    return affectedEntities;
  }

  protected Collection<ICombatEntity> getEntitiesInImpactArea(final Shape impactArea) {
    return this.getEnvironment().findCombatEntities(impactArea);
  }

}