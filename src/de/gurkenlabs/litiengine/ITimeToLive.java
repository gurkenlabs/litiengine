package de.gurkenlabs.litiengine;

/**
 * The <code>ITimeToLive</code> interface defines methods for instances the have a limited time to live.
 */
public interface ITimeToLive {
  /**
   * Gets the time this instance is alive.
   * 
   * @return Returns how long this instance is alive.
   */
  long getAliveTime();

  /**
   * Gets the total time to live of this instance.
   * 
   * @return The total time to live.
   */
  int getTimeToLive();

  /**
   * Determines whether this instance has exceeded its time to live.
   * 
   * @return True if the time to live was reached; otherwise false.
   */
  boolean timeToLiveReached();
}
