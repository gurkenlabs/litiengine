package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.Effect.EffectAppliedListener;
import de.gurkenlabs.litiengine.abilities.effects.Effect.EffectCeasedListener;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityPivot;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code Ability} class represents a special skill or power that a {@code Creature} can use in a game. Each ability has a set of effects,
 * attributes, and listeners that define its behavior.
 */
@AbilityInfo
public abstract class Ability implements IRenderable {

  private final Collection<AbilityCastListener> abilityCastListeners;
  private final AbilityAttributes attributes;

  private final List<Effect> effects;
  private final Creature executor;
  private final EntityPivot entityPivot;

  private String name;
  private String description;
  private boolean multiTarget;
  private CastType castType;

  private AbilityExecution currentExecution;

  /**
   * Initializes a new instance of the {@code Ability} class.
   *
   * @param executor The executing entity
   */
  protected Ability(final Creature executor) {
    this.abilityCastListeners = ConcurrentHashMap.newKeySet();
    this.effects = new CopyOnWriteArrayList<>();

    final AbilityInfo info = this.getClass().getAnnotation(AbilityInfo.class);
    this.attributes = new AbilityAttributes(info);
    this.executor = executor;
    this.name = info.name();
    this.multiTarget = info.multiTarget();
    this.description = info.description();
    this.castType = info.castType();
    this.entityPivot =
      new EntityPivot(executor, info.origin(), info.pivotOffsetX(), info.pivotOffsetY());
  }

  /**
   * Adds a listener that will be notified when the ability is cast.
   *
   * @param listener The listener to add
   */
  public void onCast(final AbilityCastListener listener) {
    abilityCastListeners.add(listener);
  }

  /**
   * Removes a listener that was previously added with {@link #onCast}.
   *
   * @param listener The listener to remove
   */
  public void removeAbilityCastListener(AbilityCastListener listener) {
    abilityCastListeners.remove(listener);
  }

  /**
   * Adds a listener that will be notified when an effect of this ability is applied.
   *
   * @param listener The listener to add
   */
  public void onEffectApplied(final EffectAppliedListener listener) {
    for (final Effect effect : getEffects()) {
      // registers to all effects and their follow up effects recursively
      onEffectApplied(effect, listener);
    }
  }

  /**
   * Adds a listener that will be notified when an effect of this ability ceases.
   *
   * @param listener The listener to add
   */
  public void onEffectCeased(final EffectCeasedListener listener) {
    for (final Effect effect : getEffects()) {
      // registers to all effects and their follow up effects recursively
      onEffectCeased(effect, listener);
    }
  }

  /**
   * Adds an effect to this ability.
   *
   * @param effect The effect to add
   */
  public void addEffect(final Effect effect) {
    getEffects().add(effect);
  }

  /**
   * Calculates the area of impact of this ability based on the executor's angle.
   *
   * @return The shape representing the area of impact
   */
  public Shape calculateImpactArea() {
    return internalCalculateImpactArea(getExecutor().getAngle());
  }

  /**
   * Calculates the potential area of impact of this ability.
   *
   * @return The ellipse representing the potential area of impact
   */
  public Ellipse2D calculatePotentialImpactArea() {
    final int range = getAttributes().impact().get();
    final double arcX = getPivot().getPoint().getX() - range * 0.5;
    final double arcY = getPivot().getPoint().getY() - range * 0.5;

    return new Ellipse2D.Double(arcX, arcY, range, range);
  }

  /**
   * Checks if this ability can be cast.
   *
   * @return {@code true} if the executor is not dead and the ability is not on cooldown; {@code false} otherwise
   */
  public boolean canCast() {
    return !getExecutor().isDead() && !isOnCooldown();
  }

  /**
   * Checks if this ability is on cooldown.
   *
   * @return {@code true} if the ability is on cooldown; {@code false} otherwise
   */
  public boolean isOnCooldown() {
    return (getCurrentExecution() != null
      && getCurrentExecution().getExecutionTicks() > 0
      && Game.time().since(getCurrentExecution().getExecutionTicks()) < getAttributes()
      .cooldown().get());
  }

  /**
   * Casts the ability by the temporal conditions of the specified game loop and the spatial circumstances of the specified environment. An ability
   * execution will be taken out that start applying all the effects of this ability.
   *
   * @return An {@link AbilityExecution} object that wraps all information about this execution of the ability.
   */
  public AbilityExecution cast() {
    if (!canCast()) {
      return null;
    }
    this.currentExecution = new AbilityExecution(this);

    for (final AbilityCastListener listener : abilityCastListeners) {
      listener.abilityCast(currentExecution);
    }

    return getCurrentExecution();
  }

  /**
   * Gets the attributes of this ability.
   *
   * @return The attributes of this ability
   */
  public AbilityAttributes getAttributes() {
    return attributes;
  }

  /**
   * Gets the cast type of this ability.
   *
   * @return The cast type of this ability
   */
  public CastType getCastType() {
    return castType;
  }

  /**
   * Gets the cooldown of this ability in seconds.
   *
   * @return The cooldown of this ability in seconds
   */
  public float getCooldownInSeconds() {
    return (float) (getAttributes().cooldown().get() * 0.001);
  }

  /**
   * Gets the current execution of this ability.
   *
   * @return The current execution of this ability
   */
  public AbilityExecution getCurrentExecution() {
    return currentExecution;
  }

  /**
   * Gets the description of this ability.
   *
   * @return The description of this ability
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the executor of this ability.
   *
   * @return The executor of this ability
   */
  public Creature getExecutor() {
    return executor;
  }

  /**
   * Gets the name of this ability.
   *
   * @return The name of this ability
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the pivot of this ability.
   *
   * @return The pivot of this ability
   */
  public EntityPivot getPivot() {
    return entityPivot;
  }

  /**
   * Gets the remaining cooldown of this ability in seconds.
   *
   * @return The remaining cooldown of this ability in seconds
   */
  public float getRemainingCooldownInSeconds() {
    if (getCurrentExecution() == null
      || getExecutor() == null
      || getExecutor().isDead()) {
      return 0;
    }

    // calculate cooldown in seconds
    return (float) (!canCast()
      ? (getAttributes().cooldown().get()
      - Game.time().since(getCurrentExecution().getExecutionTicks()))
      * 0.001
      : 0);
  }

  /**
   * Checks if this ability is active.
   *
   * @return {@code true} if the ability is active; {@code false} otherwise
   */
  public boolean isActive() {
    return getCurrentExecution() != null
      && Game.time().since(getCurrentExecution().getExecutionTicks()) < getAttributes()
      .duration().get();
  }

  /**
   * Checks if this ability is multi-target.
   *
   * @return {@code true} if the ability is multi-target; {@code false} otherwise
   */
  public boolean isMultiTarget() {
    return multiTarget;
  }

  /**
   * Renders the impact area of this ability.
   *
   * @param g The graphics context to render on
   */
  @Override
  public void render(final Graphics2D g) {
    g.setColor(new Color(255, 255, 0, 25));
    Game.graphics().renderShape(g, calculateImpactArea(), true);
    final Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke(2f));
    g.setColor(new Color(255, 255, 0, 50));
    Game.graphics().renderOutline(g, calculateImpactArea(), true);
    g.setStroke(oldStroke);
  }

  /**
   * Sets the name of this ability.
   *
   * @param name The new name of this ability
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the description of this ability.
   *
   * @param description The new description of this ability
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets whether this ability is multi-target.
   *
   * @param multiTarget {@code true} if the ability is multi-target; {@code false} otherwise
   */
  public void setMultiTarget(boolean multiTarget) {
    this.multiTarget = multiTarget;
  }

  /**
   * Sets the cast type of this ability.
   *
   * @param castType The new cast type of this ability
   */
  public void setCastType(CastType castType) {
    this.castType = castType;
  }

  /**
   * Sets the current execution of this ability.
   *
   * @param ae The new current execution of this ability
   */
  void setCurrentExecution(AbilityExecution ae) {
    this.currentExecution = ae;
  }

  /**
   * Gets the effects of this ability.
   *
   * @return The effects of this ability
   */
  public List<Effect> getEffects() {
    return effects;
  }

  /**
   * Calculates the impact area of this ability based on the given angle.
   *
   * @param angle The angle to calculate the impact area
   * @return The shape representing the impact area
   */
  protected Shape internalCalculateImpactArea(final double angle) {
    final int impact = getAttributes().impact().get();
    final int impactAngle = getAttributes().impactAngle().get();
    final double arcX = getPivot().getPoint().getX() - impact;
    final double arcY = getPivot().getPoint().getY() - impact;

    // project
    final Point2D appliedRange =
      GeometricUtilities.project(
        new Point2D.Double(arcX, arcY), angle, getAttributes().range().get() * 0.5);
    final double start = angle - 90 - (impactAngle / 2.0);
    if (impactAngle % 360 == 0) {
      return new Ellipse2D.Double(appliedRange.getX(), appliedRange.getY(), impact * 2d, impact * 2d);
    }

    return new Arc2D.Double(
      appliedRange.getX(), appliedRange.getY(), impact * 2d, impact * 2d, start, impactAngle, Arc2D.PIE);
  }

  /**
   * Adds a listener that will be notified when an effect of this ability is applied.
   *
   * @param effect   The effect to add the listener to
   * @param listener The listener to add
   */
  private void onEffectApplied(final Effect effect, final EffectAppliedListener listener) {
    effect.onEffectApplied(listener);

    for (final Effect followUp : effect.getFollowUpEffects()) {
      onEffectApplied(followUp, listener);
    }
  }

  /**
   * Adds a listener that will be notified when an effect of this ability ceases.
   *
   * @param effect   The effect to add the listener to
   * @param listener The listener to add
   */
  private void onEffectCeased(final Effect effect, final EffectCeasedListener listener) {
    effect.onEffectCeased(listener);

    for (final Effect followUp : effect.getFollowUpEffects()) {
      onEffectCeased(followUp, listener);
    }
  }

  /**
   * The {@code AbilityCastListener} interface defines a method for listening to ability cast events.
   */
  @FunctionalInterface
  public interface AbilityCastListener extends EventListener {
    /**
     * Invoked when an ability is cast.
     *
     * @param execution The execution of the ability
     */
    void abilityCast(AbilityExecution execution);
  }
}
