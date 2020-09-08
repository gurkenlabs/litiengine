package de.gurkenlabs.litiengine;

import java.util.EventListener;

/**
 * This listener interface is used for receiving events about the general life-cycle of the {@code Game} (e.g. started/terminated).
 */
public interface GameListener extends EventListener {

  /**
   * This method gets called after the {@code Game.start} method was executed.
   * 
   * @see Game#start()
   */
  public default void started() {}

  /**
   * This method gets called after the {@code Game.init(String...)} method was executed.
   * 
   * @param args
   *          The arguments that were passed to the application.
   * @see Game#init(String...)
   */
  public default void initialized(String... args) {}

  /**
   * This method gets called before the {@code Game} is about to be terminated.
   * Returning false prevents the terminate event to continue.
   *
   * @return Return false to interrupt the termination process.
   */
  public default boolean terminating() {
    return true;
  }

  /**
   * This method is called when the {@code Game} was terminated (just before {@code System.exit} is about to be called).
   */
  public default void terminated() {}
}
