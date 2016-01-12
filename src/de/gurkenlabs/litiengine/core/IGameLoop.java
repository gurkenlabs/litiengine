/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.core;

/**
 * The interface IGameLoop defines methods for the basic loop of a game. All
 * entities that need to update themselves, can register and will be called with
 * every update tick.
 */
public interface IGameLoop {
  /**
   * Gets the ticks.
   *
   * @return the ticks
   */
  public long getTicks();

  /**
   * Register for update.
   *
   * @param updatable
   *          the updatable
   */
  public void registerForUpdate(IUpdateable updatable);

  /**
   * Unregister from update.
   *
   * @param updatable
   *          the updatable
   */
  public void unregisterFromUpdate(IUpdateable updatable);
}
