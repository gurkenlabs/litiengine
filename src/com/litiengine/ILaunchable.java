package com.litiengine;

/**
 * A functional interface that defines methods for instances that need to be launched and terminated externally.
 */
public interface ILaunchable {

  /**
   * Starts the operation of this instance.
   */
  void start();

  /**
   * Terminates the operation of this instance.
   */
  void terminate();
}
