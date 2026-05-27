package de.gurkenlabs.litiengine.entities;

import java.io.Serial;
import java.util.EventObject;

/**
 * Event dispatched whenever an {@link IMobileEntity} moves. Carries the moved entity along with the X / Y delta and the resulting travelled
 * distance.
 */
public class EntityMovedEvent extends EventObject {
  @Serial private static final long serialVersionUID = 2931711179495514204L;
  private final transient IMobileEntity entity;
  private final double deltaX;
  private final double deltaY;
  private final double distance;

  /**
   * Constructs a new {@code EntityMovedEvent}.
   *
   * @param entity the moved entity
   * @param deltaX the X delta of the movement
   * @param deltaY the Y delta of the movement
   */
  public EntityMovedEvent(IMobileEntity entity, double deltaX, double deltaY) {
    super(entity);
    this.entity = entity;
    this.deltaX = deltaX;
    this.deltaY = deltaY;
    this.distance = Math.sqrt(Math.pow(this.deltaX, 2) + Math.pow(this.deltaY, 2));
  }

  /**
   * Gets the entity that moved.
   *
   * @return the moved entity
   */
  public IMobileEntity getEntity() {
    return this.entity;
  }

  /**
   * Gets the X delta of the movement.
   *
   * @return the X delta
   */
  public double getDeltaX() {
    return this.deltaX;
  }

  /**
   * Gets the Y delta of the movement.
   *
   * @return the Y delta
   */
  public double getDeltaY() {
    return this.deltaY;
  }

  /**
   * Gets the travelled Euclidean distance.
   *
   * @return the distance
   */
  public double getDistance() {
    return this.distance;
  }
}
