package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import java.awt.Shape;
import java.util.List;

/**
 * The ICombatEntity interface defines the methods required for an entity that can engage in combat. It extends the ICollisionEntity interface.
 */
public interface ICombatEntity extends ICollisionEntity {
  /**
   * Adds a CombatEntityListener to this entity.
   *
   * @param listener the listener to add
   */
  void addCombatEntityListener(CombatEntityListener listener);

  /**
   * Removes a CombatEntityListener from this entity.
   *
   * @param listener the listener to remove
   */
  void removeCombatEntityListener(CombatEntityListener listener);

  /**
   * Registers a listener to be called when this entity is hit.
   *
   * @param listener the listener to register
   */
  void onHit(CombatEntityHitListener listener);

  /**
   * Removes a hit listener from this entity.
   *
   * @param listener the listener to remove
   */
  void removeListener(CombatEntityHitListener listener);

  /**
   * Registers a listener to be called when this entity dies.
   *
   * @param listener the listener to register
   */
  void onDeath(CombatEntityDeathListener listener);

  /**
   * Removes a death listener from this entity.
   *
   * @param listener the listener to remove
   */
  void removeListener(CombatEntityDeathListener listener);

  /**
   * Registers a listener to be called when this entity is resurrected.
   *
   * @param listener the listener to register
   */
  void onResurrect(CombatEntityResurrectListener listener);

  /**
   * Removes a resurrect listener from this entity.
   *
   * @param listener the listener to remove
   */
  void removeListener(CombatEntityResurrectListener listener);

  /**
   * Causes this entity to die.
   */
  void die();

  /**
   * Gets the list of effects currently applied to this entity.
   *
   * @return a list of applied effects
   */
  List<Effect> getAppliedEffects();

  /**
   * Gets the hit points of this entity.
   *
   * @return the hit points as a RangeAttribute
   */
  RangeAttribute<Integer> getHitPoints();

  /**
   * Gets the hit box of this entity.
   *
   * @return the hit box as a Shape
   */
  Shape getHitBox();

  /**
   * Gets the current target of this entity.
   *
   * @return the target entity
   */
  ICombatEntity getTarget();

  /**
   * Gets the team of this entity.
   *
   * @return the team as an integer
   */
  int getTeam();

  /**
   * Inflicts damage to this entity.
   *
   * @param damage the amount of damage to inflict
   */
  void hit(int damage);

  /**
   * Inflicts damage to this entity with a specific ability.
   *
   * @param damage  the amount of damage to inflict
   * @param ability the ability causing the damage
   */
  void hit(int damage, Ability ability);

  /**
   * Checks if this entity is dead.
   *
   * @return true if the entity is dead, false otherwise
   */
  boolean isDead();

  /**
   * Checks if this entity is friendly to another entity.
   *
   * @param entity the entity to check against
   * @return true if the entity is friendly, false otherwise
   */
  boolean isFriendly(final ICombatEntity entity);

  /**
   * Checks if this entity is indestructible.
   *
   * @return true if the entity is indestructible, false otherwise
   */
  boolean isIndestructible();

  /**
   * Checks if this entity is neutral.
   *
   * @return true if the entity is neutral, false otherwise
   */
  boolean isNeutral();

  /**
   * Resurrects this entity.
   */
  void resurrect();

  /**
   * Sets the indestructible state of this entity.
   *
   * @param indestructible the indestructible state to set
   */
  void setIndestructible(final boolean indestructible);

  /**
   * Sets the target of this entity.
   *
   * @param target the target entity to set
   */
  void setTarget(final ICombatEntity target);

  /**
   * Sets the team of this entity.
   *
   * @param team the team to set
   */
  void setTeam(int team);

  /**
   * Checks if this entity was hit within a specific time span.
   *
   * @param timeSpan the time span to check
   * @return true if the entity was hit within the time span, false otherwise
   */
  boolean wasHit(int timeSpan);
}
