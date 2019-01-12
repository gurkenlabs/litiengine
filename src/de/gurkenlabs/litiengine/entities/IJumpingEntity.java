package de.gurkenlabs.litiengine.entities;

/**
 * The Interface IJumpingEntity provides a starting point for entities that implement a jump mechanic.
 * There is no 'default way' of implementing a jump in LITIengine, yet it makes sense to include this as a basic common ground for jumping entities.
 */
public interface IJumpingEntity extends IMobileEntity {

  /**
   * Make the entity jump.
   */
  public void jump();

}
