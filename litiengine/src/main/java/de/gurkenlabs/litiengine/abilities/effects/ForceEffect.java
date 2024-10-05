package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.targeting.TargetingStrategy;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.Force;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The `ForceEffect` represents an effect that applies a physical force to mobile entities.
 * It extends the `Effect` class and is used to manipulate the movement of entities based on the applied force,
 * such as knockbacks or pulls.
 * <p>
 * This effect can be applied to entities that implement the `IMobileEntity` interface.
 * The `ForceEffect` uses a targeting strategy to determine which entities are affected
 * and applies a force with a given strength to them.
 */
public abstract class ForceEffect extends Effect {

  private final float strength;
  private final Map<IMobileEntity, Force> appliedForces;

  /**
   * Constructs a new `ForceEffect` with the specified targeting strategy and force strength.
   *
   * @param targetingStrategy The strategy used to select which entities will be affected by the effect.
   * @param strength          The strength of the applied force.
   */
  protected ForceEffect(final TargetingStrategy targetingStrategy, final float strength) {
    this(targetingStrategy, null, strength);
  }

  /**
   * Constructs a new `ForceEffect` with the specified targeting strategy, executing entity, and force strength.
   *
   * @param targetingStrategy The strategy used to select which entities will be affected by the effect.
   * @param executingEntity   The entity executing the effect (e.g., a player or NPC).
   * @param strength          The strength of the applied force.
   */
  protected ForceEffect(final TargetingStrategy targetingStrategy, final ICombatEntity executingEntity, final float strength) {
    super(targetingStrategy, executingEntity);
    this.strength = strength;
    this.appliedForces = new ConcurrentHashMap<>();
  }

  /**
   * Applies the effect to a specified combat entity. If the affected entity is mobile, a force is created
   * and applied to manipulate its movement. The applied force is stored in a map for later reference.
   *
   * @param affectedEntity The combat entity that will be affected by the force.
   */
  @Override
  public void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    if (affectedEntity instanceof IMobileEntity mobileEntity) {
      Force force = createForce(mobileEntity);
      appliedForces.put(mobileEntity, force);
      mobileEntity.movement().apply(force);
    }
  }

  /**
   * Gets the strength of the applied force.
   *
   * @return The force strength.
   */
  public float getStrength() {
    return this.strength;
  }

  /**
   * Abstract method that must be implemented to define how the force is created for each entity.
   *
   * @param affectedEntity The mobile entity to which the force is applied.
   * @return The created `Force` object representing the force to be applied.
   */
  protected abstract Force createForce(final IMobileEntity affectedEntity);

  /**
   * Stops the effect on a specified entity. If a force was applied to the entity, it is ended and
   * removed from the map tracking applied forces.
   *
   * @param entity The entity on which the effect will cease.
   */
  @Override
  public void cease(ICombatEntity entity) {
    super.cease(entity);
    if (entity instanceof IMobileEntity mobileEntity && appliedForces.containsKey(mobileEntity)) {
      appliedForces.get(mobileEntity).end();
      appliedForces.remove(mobileEntity);
    }
  }

  /**
   * Checks whether the effect has ended. The effect is considered ended if it has naturally
   * ended according to its duration, or if all forces applied have ended.
   *
   * @param appliance The current application of the effect.
   * @return `true` if the effect has ended, `false` otherwise.
   */
  @Override
  protected boolean hasEnded(final EffectApplication appliance) {
    return super.hasEnded(appliance) || appliedForces.isEmpty() || appliedForces.values().stream()
      .allMatch(Force::hasEnded);
  }
}
