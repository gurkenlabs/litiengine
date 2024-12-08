package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.targeting.TargetingStrategy;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The `Effect` class represents an abstract base class for applying effects to combat entities. Effects are applied based on a `TargetingStrategy`
 * and may affect multiple entities within a defined impact area. Effects are applied for a set duration and may have follow-up effects.
 * <p>
 * This class handles effect application, event listeners for when effects are applied or ceased, and the management of active appliances (ongoing
 * effect instances).
 */
public abstract class Effect implements IUpdateable {

  private final List<EffectApplication> appliances;
  private final Collection<EffectAppliedListener> appliedListeners;
  private final Collection<EffectCeasedListener> ceasedListeners;
  private final List<Effect> followUpEffects;

  private final TargetingStrategy targetingStrategy;
  private final ICombatEntity executor;
  private int delay;
  private int duration;

  /**
   * Constructs a new `Effect` with the specified targeting strategy.
   *
   * @param targetingStrategy The strategy used to target entities for this effect.
   */
  protected Effect(final TargetingStrategy targetingStrategy) {
    this(targetingStrategy, null);
  }

  /**
   * Constructs a new `Effect` with the specified targeting strategy and executor.
   *
   * @param targetingStrategy The strategy used to target entities for this effect.
   * @param executor          The entity applying the effect, or null if none.
   */
  protected Effect(final TargetingStrategy targetingStrategy, final ICombatEntity executor) {
    this(targetingStrategy, executor, 0);
  }

  /**
   * Constructs a new `Effect` with the specified targeting strategy, executor, and duration.
   *
   * @param targetingStrategy The strategy used to target entities for this effect.
   * @param executor          The entity applying the effect, or null if none.
   * @param duration          The duration the effect will last.
   */
  protected Effect(final TargetingStrategy targetingStrategy, final ICombatEntity executor, int duration) {
    this.appliedListeners = ConcurrentHashMap.newKeySet();
    this.ceasedListeners = ConcurrentHashMap.newKeySet();
    this.appliances = new ArrayList<>();
    this.followUpEffects = new CopyOnWriteArrayList<>();

    this.executor = executor;
    this.duration = duration;
    this.targetingStrategy = targetingStrategy;
  }

  /**
   * Registers a listener for when the effect is applied.
   *
   * @param listener The listener to register.
   */
  public void onEffectApplied(final EffectAppliedListener listener) {
    this.appliedListeners.add(listener);
  }

  public void removeEffectAppliedListener(final EffectAppliedListener listener) {
    this.appliedListeners.remove(listener);
  }

  /**
   * Registers a listener for when the effect ceases.
   *
   * @param listener The listener to register.
   */
  public void onEffectCeased(final EffectCeasedListener listener) {
    this.ceasedListeners.add(listener);
  }

  public void removeEffectCeasedListener(final EffectCeasedListener listener) {
    this.ceasedListeners.remove(listener);
  }

  /**
   * Applies the effect to all entities within the specified impact area.
   *
   * @param impactArea The area where the effect is applied.
   */
  public void apply(final Shape impactArea) {
    final var affected = this.targetingStrategy.findTargets(impactArea, this.getExecutingEntity());
    for (final ICombatEntity affectedEntity : affected) {
      this.apply(affectedEntity);
    }

    this.appliances.add(new EffectApplication(affected, impactArea));

    // Register for updates if this is the first application
    if (this.appliances.size() == 1) {
      Game.loop().attach(this);
    }
  }

  /**
   * Ceases the effect for the specified entity.
   *
   * @param entity The entity for which the effect will cease.
   */
  public void cease(final ICombatEntity entity) {
    entity.getAppliedEffects().remove(this);
    final EffectEvent event = new EffectEvent(this, entity);
    for (final EffectCeasedListener listener : this.ceasedListeners) {
      listener.ceased(event);
    }
  }

  /**
   * Returns the entity that is executing this effect.
   *
   * @return The executor of this effect.
   */
  public ICombatEntity getExecutingEntity() {
    return this.executor;
  }

  /**
   * Returns the list of active effect applications.
   *
   * @return The list of active effect applications.
   */
  public List<EffectApplication> getActiveAppliances() {
    return this.appliances;
  }

  /**
   * Returns the delay before this effect is applied.
   *
   * @return The delay in ticks.
   */
  public int getDelay() {
    return this.delay;
  }

  /**
   * Returns the duration this effect lasts once applied.
   *
   * @return The effect duration in ticks.
   */
  public int getDuration() {
    return this.duration;
  }

  /**
   * Returns the targeting strategy used by this effect.
   *
   * @return The targeting strategy.
   */
  public TargetingStrategy getTargetingStrategy() {
    return this.targetingStrategy;
  }

  /**
   * Returns the list of follow-up effects that are applied after the main effect ends.
   *
   * @return The list of follow-up effects.
   */
  public List<Effect> getFollowUpEffects() {
    return this.followUpEffects;
  }

  /**
   * Checks if the effect is active on the specified entity.
   *
   * @param entity The entity to check.
   * @return True if the effect is active on the entity, false otherwise.
   */
  public boolean isActive(final ICombatEntity entity) {
    return getActiveAppliances().stream()
      .anyMatch(a -> a.getAffectedEntities().stream().anyMatch(e -> e.equals(entity)));
  }

  /**
   * Sets the delay before this effect is applied.
   *
   * @param delay the delay in ticks
   */
  public void setDelay(final int delay) {
    this.delay = delay;
  }

  /**
   * Sets the duration this effect lasts once applied.
   *
   * @param duration the effect duration in ticks
   */
  public void setDuration(final int duration) {
    this.duration = duration;
  }

  /**
   * Updates the effect, checking for appliances that have reached their duration and applying follow-up effects if needed. Removes appliances that
   * have ended and detaches the effect from the game loop if no active appliances remain.
   */
  @Override
  public void update() {

    for (final Iterator<EffectApplication> iterator = this.getActiveAppliances().iterator();
      iterator.hasNext(); ) {
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

  /**
   * Applies the effect to the specified entity, adding this effect to the entity's list of applied effects and notifying all registered listeners
   * that the effect has been applied.
   *
   * @param entity the entity to which the effect is applied
   */
  protected void apply(final ICombatEntity entity) {
    entity.getAppliedEffects().add(this);
    final EffectEvent event = new EffectEvent(this, entity);
    for (final EffectAppliedListener listener : this.appliedListeners) {
      listener.applied(event);
    }
  }

  /**
   * Ceases the effect for the specified application and applies follow-up effects.
   *
   * @param appliance The application to cease.
   */
  protected void cease(final EffectApplication appliance) {
    for (final ICombatEntity entity : appliance.getAffectedEntities()) {
      this.cease(entity);
    }

    this.getFollowUpEffects().forEach(followUp -> followUp.apply(appliance.getImpactArea()));
  }

  /**
   * Returns the total duration of the effect, including the delay.
   *
   * @return The total effect duration.
   */
  protected long getTotalDuration() {
    return this.getDuration() + (long) this.getDelay();
  }

  /**
   * Checks if the specified effect application has ended.
   *
   * @param appliance The application to check.
   * @return True if the effect duration has passed, false otherwise.
   */
  protected boolean hasEnded(final EffectApplication appliance) {
    if (this.getDuration() <= 0) {
      return false;
    }

    final long effectDuration = Game.time().since(appliance.getAppliedTicks());
    return effectDuration > this.getDuration();
  }

  /**
   * Listener interface for receiving notifications when an effect is applied.
   */
  @FunctionalInterface
  public interface EffectAppliedListener extends EventListener {

    /**
     * Invoked when an effect is applied.
     *
     * @param event the event containing information about the applied effect
     */
    void applied(EffectEvent event);
  }

  /**
   * Listener interface for receiving notifications when an effect ceases.
   */
  @FunctionalInterface
  public interface EffectCeasedListener extends EventListener {

    /**
     * Invoked when an effect ceases.
     *
     * @param event the event containing information about the ceased effect
     */
    void ceased(EffectEvent event);
  }
}
