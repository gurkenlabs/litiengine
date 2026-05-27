package de.gurkenlabs.litiengine.tweening;

import java.util.EventListener;

/**
 * This listener interface receives lifecycle events from a {@code Tween}.
 *
 * @see Tween#addListener(TweenListener)
 */
public interface TweenListener extends EventListener {
  /**
   * Invoked when a Tween is started (or restarted).
   *
   * @param tween the Tween that has been started.
   */
  default void started(Tween tween) {
  }

  /**
   * Invoked when a Tween is stopped, either manually via {@link Tween#stop()} or by completing its duration when no looping
   * is configured.
   *
   * @param tween the Tween that has been stopped.
   */
  default void stopped(Tween tween) {
  }

  /**
   * Invoked when a Tween completes a full duration cycle. For non-looping Tweens, this is fired right before the
   * {@link #stopped(Tween)} event. For looping Tweens, this is fired on every cycle completion.
   *
   * @param tween the Tween that has completed.
   */
  default void completed(Tween tween) {
  }
}
