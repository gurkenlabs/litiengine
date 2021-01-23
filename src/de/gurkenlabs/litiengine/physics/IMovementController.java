package de.gurkenlabs.litiengine.physics;

import java.util.List;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.entities.IEntityController;
import de.gurkenlabs.litiengine.entities.IMobileEntity;

/**
 * The Interface IMovementController is used for moving entities by applying forces to them.
 */
public interface IMovementController extends IEntityController {

  /**
   * Gets the delta x for each horizontal movement.
   *
   * @return the dx
   */
  float getDx();

  /**
   * Gets the delta x for each horizontal movement.
   *
   * @param dx
   *          the new dx
   */
  void setDx(float dx);

  /**
   * Sets the delta y for each vertical movement.
   *
   * @return the dy
   */
  float getDy();

  /**
   * Sets the delta y for each vertical movement.
   *
   * @param dy
   *          the new dy
   */
  void setDy(float dy);

  /**
   * Get the current velocity.
   *
   * @return The current velocity.
   */
  double getVelocity();

  /**
   * Sets the current velocity.
   *
   * @param velocity The velocity to set.
   */
  void setVelocity(double velocity);

  /**
   * Apply the force to the entity.
   *
   * @param force
   *          the force being applied to the entity
   */
  void apply(Force force);

  /**
   * Gets the active forces.
   *
   * @return the active forces
   */
  List<Force> getActiveForces();
  
  Force getForce(String identifier);

  double getMoveAngle();
  
  /**
   * Checks given conditions before moving.
   *
   * @param predicate
   *          the conditions that need to apply before moving. If they don't apply, the entity won't be moved.
   */
  void onMovementCheck(Predicate<IMobileEntity> predicate);
}
