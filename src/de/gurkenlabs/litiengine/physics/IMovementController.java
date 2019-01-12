package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;
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
  public float getDx();

  /**
   * Gets the delta x for each horizontal movement.
   *
   * @param dx
   *          the new dx
   */
  public void setDx(float dx);

  /**
   * Sets the delta y for each vertical movement.
   *
   * @return the dy
   */
  public float getDy();

  /**
   * Sets the delta y for each vertical movement.
   *
   * @param dy
   *          the new dy
   */
  public void setDy(float dy);

  /**
   * Apply the force to the entity.
   *
   * @param force
   *          the force being applied to the entity
   */
  public void apply(Force force);

  /**
   * Gets the active forces.
   *
   * @return the active forces
   */
  public List<Force> getActiveForces();

  /**
   * Checks given conditions before moving.
   *
   * @param predicate
   *          the conditions that need to apply before moving. If they don't apply, the entity won't be moved.
   */
  public void onMovementCheck(Predicate<IMobileEntity> predicate);

  /**
   * Calls given logic after the entity has been moved.
   *
   * @param cons
   *          the logic that will be called after the entity has been moved.
   */
  public void onMoved(Consumer<Point2D> cons);
}
