package com.litiengine.graphics.animation;

import java.util.EventListener;

/**
 * This listener provides call-backs for when an {@code Animation} is played or the play back was finished.
 */
public interface AnimationListener extends EventListener {
  /**
   * Called when the specified animation has started playing.
   * 
   * @param animation
   *          The animation that is now played.
   */
  public default void played(Animation animation) {}

  /**
   * Called when the specified animation has finished playing.
   * 
   * @param animation
   *          The animation that has just finished playing.
   */
  public default void finished(Animation animation) {}
}
