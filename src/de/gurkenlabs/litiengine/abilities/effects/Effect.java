package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.EntityDistanceComparator;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.RelativeEntityComparator;

/**
 * The Class Effect seeks for affected entities in the game's current
 * environment to apply certain effects to them defined by the overwritten
 * implementation of apply/cease.
 */
public abstract class Effect implements IUpdateable {
  private final Ability ability;
  private final List<EffectApplication> appliances;
  private final Collection<EffectAppliedListener> appliedListeners;
  private final Collection<EffectCeasedListener> ceasedListeners;
  private final EffectTarget[] effectTargets;
  private final List<Effect> followUpEffects;

  private int delay;
  private int duration;
  private RelativeEntityComparator targetPriorityComparator;

  /**
   * Instantiates a new effect.
   *
   * @param ability
   *          the ability
   * @param targets
   *          the targets
   */
  protected Effect(final Ability ability, final EffectTarget... targets) {
    this.appliedListeners = ConcurrentHashMap.newKeySet();
    this.ceasedListeners = ConcurrentHashMap.newKeySet();
    this.appliances = new ArrayList<>();
    this.followUpEffects = new CopyOnWriteArrayList<>();

    this.ability = ability;
    this.targetPriorityComparator = new EntityDistanceComparator(this.getAbility().getExecutor());

    this.duration = ability.getAttributes().duration().get();
    if (targets == null || targets.length == 0) {
      this.effectTargets = new EffectTarget[] { EffectTarget.NONE };
    } else {
      this.effectTargets = targets;
    }
  }

  public void onEffectApplied(final EffectAppliedListener listener) {
    this.appliedListeners.add(listener);
  }

  public void removeEffectAppliedListener(final EffectAppliedListener listener) {
    this.appliedListeners.remove(listener);
  }

  public void onEffectCeased(final EffectCeasedListener listener) {
    this.ceasedListeners.add(listener);
  }

  public void removeEffectCeasedListener(final EffectCeasedListener listener) {
    this.ceasedListeners.remove(listener);
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
    final EffectEvent event = new EffectEvent(this, entity);
    for (final EffectCeasedListener listener : this.ceasedListeners) {
      listener.ceased(event);
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

  public RelativeEntityComparator getTargetPriorityComparator() {
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

  public void setDelay(final int delay) {
    this.delay = delay;
  }

  public void setDuration(final int duration) {
    this.duration = duration;
  }

  public void setTargetPriorityComparator(final RelativeEntityComparator targetPriorityComparator) {
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
    final EffectEvent event = new EffectEvent(this, entity);
    for (final EffectAppliedListener listener : this.appliedListeners) {
      listener.applied(event);
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
    final long effectDuration = Game.time().since(appliance.getAppliedTicks());
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
        affectedEntities = affectedEntities.stream().filter(this::canAttackEntity).collect(Collectors.toList());
        break;
      case FRIENDLY:
        affectedEntities.addAll(this.getEntitiesInImpactArea(impactArea));
        affectedEntities = affectedEntities.stream().filter(this::isAliveFriendlyEntity).collect(Collectors.toList());
        break;
      case FRIENDLYDEAD:
        affectedEntities.addAll(this.getEntitiesInImpactArea(impactArea));
        affectedEntities = affectedEntities.stream().filter(this::isDeadFriendlyEntity).collect(Collectors.toList());
        break;
      case CUSTOM:
        affectedEntities.addAll(this.getEntitiesInImpactArea(impactArea));
        affectedEntities = affectedEntities.stream().filter(this::customTarget).collect(Collectors.toList());
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

  /**
   * Overwrite this method to implement a custom target predicate that determines whether an entity can be affected by this effect.
   * The targets of this effect need to include the <code>CUSTOM</code> value in order for this function to be evaluated.
   * 
   * @param entity
   *          The entity to check against the custom target predicate.
   * @return True if the entity can be affected by this effect; otherwise false.
   * 
   * @see EffectTarget#CUSTOM
   */
  protected boolean customTarget(ICombatEntity entity) {
    return entity != null;
  }

  private boolean canAttackEntity(ICombatEntity entity) {
    return !entity.equals(this.getAbility().getExecutor()) && !entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  private boolean isAliveFriendlyEntity(ICombatEntity entity) {
    return !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && !entity.isDead();
  }

  private boolean isDeadFriendlyEntity(ICombatEntity entity) {
    return !entity.equals(this.getAbility().getExecutor()) && entity.isFriendly(this.getAbility().getExecutor()) && entity.isDead();
  }

  @FunctionalInterface
  public interface EffectAppliedListener extends EventListener {
    void applied(EffectEvent event);
  }

  @FunctionalInterface
  public interface EffectCeasedListener extends EventListener {
    void ceased(EffectEvent event);
  }
}