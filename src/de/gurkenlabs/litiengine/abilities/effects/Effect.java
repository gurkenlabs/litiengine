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

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.EntityComparator;
import de.gurkenlabs.litiengine.entities.EntityDistanceComparator;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

/**
 * The Class Effect seeks for affected entities in the game's current
 * environment to apply certain effects to them defined by the overwritten
 * implementation of apply/cease.
 */
public abstract class Effect implements IUpdateable {
  public static final int NO_DURATION = -1;
  
  private final Ability ability;
  private final List<EffectApplication> appliances;
  private final List<Consumer<EffectArgument>> appliedConsumer;
  private final List<Consumer<EffectArgument>> ceasedConsumer;
  private final EffectTarget[] effectTargets;
  private final List<Effect> followUpEffects;
  
  private int delay;
  private int duration;
  private EntityComparator targetPriorityComparator;

  /**
   * Instantiates a new effect.
   *
   * @param ability
   *          the ability
   * @param targets
   *          the targets
   */
  protected Effect(final Ability ability, final EffectTarget... targets) {
    this.appliedConsumer = new CopyOnWriteArrayList<>();
    this.ceasedConsumer = new CopyOnWriteArrayList<>();
    this.appliances = new ArrayList<>();
    this.followUpEffects = new CopyOnWriteArrayList<>();

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
   * Applies the effect in the specified impact area on the specified environment.
   * 
   * @param impactArea
   *          The impact area
   */
  public void apply(final Shape impactArea) {
    final List<ICombatEntity> affected = this.lookForAffectedEntities(impactArea);
    for (final ICombatEntity affectedEntity : affected) {
      this.apply(affectedEntity);
    }

    this.appliances.add(new EffectApplication(affected, impactArea));

    // if it is the first appliance -> register for update
    if (this.appliances.size() == 1) {
      Game.loop().attach(this);
    }
  }

  public void cease(final ICombatEntity entity) {
    entity.getAppliedEffects().remove(this);
    final EffectArgument arg = new EffectArgument(this, entity);
    for (final Consumer<EffectArgument> consumer : this.ceasedConsumer) {
      consumer.accept(arg);
    }
  }

  public Ability getAbility() {
    return this.ability;
  }

  public List<EffectApplication> getActiveAppliances() {
    return this.appliances;
  }

  public int getDelay() {
    return this.delay;
  }

  public int getDuration() {
    return this.duration;
  }

  public EffectTarget[] getEffectTargets() {
    return this.effectTargets;
  }

  public List<Effect> getFollowUpEffects() {
    return this.followUpEffects;
  }

  public EntityComparator getTargetPriorityComparator() {
    return this.targetPriorityComparator;
  }

  public boolean isActive(final ICombatEntity entity) {
    for (final EffectApplication app : this.getActiveAppliances()) {
      for (final ICombatEntity affected : app.getAffectedEntities()) {
        if (affected.equals(entity)) {
          return true;
        }
      }
    }

    return false;
  }

  public void onEffectApplied(final Consumer<EffectArgument> consumer) {
    if (!this.appliedConsumer.contains(consumer)) {
      this.appliedConsumer.add(consumer);
    }
  }

  public void onEffectCeased(final Consumer<EffectArgument> consumer) {
    if (!this.ceasedConsumer.contains(consumer)) {
      this.ceasedConsumer.add(consumer);
    }
  }

  public void setDelay(final int delay) {
    this.delay = delay;
  }

  public void setDuration(final int duration) {
    this.duration = duration;
  }

  public void setTargetPriorityComparator(final EntityComparator targetPriorityComparator) {
    this.targetPriorityComparator = targetPriorityComparator;
  }

  /**
   * 1. Cease the effect after its duration. 
   * 2. apply all follow up effects 
   * 3. remove appliance 
   * 4. unregister from loop if all appliances are done
   */
  @Override
  public void update() {

    for (final Iterator<EffectApplication> iterator = this.getActiveAppliances().iterator(); iterator.hasNext();) {
      final EffectApplication appliance = iterator.next();
      // if the effect duration is reached
      if (this.hasEnded(appliance)) {

        iterator.remove();
        this.cease(appliance);
      }
    }

    // 4. unregister if all appliances are finished
    if (this.getActiveAppliances().isEmpty()) {
      Game.loop().detach(this);
    }
  }

  protected void apply(final ICombatEntity entity) {
    entity.getAppliedEffects().add(this);
    final EffectArgument arg = new EffectArgument(this, entity);
    for (final Consumer<EffectArgument> consumer : this.appliedConsumer) {
      consumer.accept(arg);
    }
  }

  protected void cease(final EffectApplication appliance) {
    // 1. cease the effect for all affected entities
    for (final ICombatEntity entity : appliance.getAffectedEntities()) {
      this.cease(entity);
    }

    // 2. apply follow up effects
    this.getFollowUpEffects().forEach(followUp -> followUp.apply(appliance.getImpactArea()));
  }

  protected Collection<ICombatEntity> getEntitiesInImpactArea(final Shape impactArea) {
    return Game.world().environment().findCombatEntities(impactArea);
  }

  protected long getTotalDuration() {
    return this.getDuration() + (long) this.getDelay();
  }

  protected boolean hasEnded(final EffectApplication appliance) {
    final long effectDuration = Game.loop().getDeltaTime(appliance.getAppliedTicks());
    return effectDuration > this.getDuration();
  }

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
    affectedEntities.sort(this.targetPriorityComparator);
    
    if (!this.getAbility().isMultiTarget() && !affectedEntities.isEmpty()) {
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

  private Predicate<? super ICombatEntity> canAttackEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && !entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  private Predicate<? super ICombatEntity> isAliveFriendlyEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  private Predicate<? super ICombatEntity> isDeadFriendlyEntity() {
    return entity -> !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && entity.isDead();
  }
}