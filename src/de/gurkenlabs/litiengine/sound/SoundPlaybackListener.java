package de.gurkenlabs.litiengine.sound;

import java.util.EventListener;

/**
 * This event listener implementation provides callbacks for when a
 * {@link ISoundPlayback} instance gets cancelled or finished.
 *
 */
public interface SoundPlaybackListener extends EventListener {

  /**
   * This method gets called when a <code>SoundPlayback</code> is cancelled.
   *
   * @param event
   *          a {@link SoundEvent} object describing the
   *          event source and the related {@link Sound}.
   */
  public default void cancelled(SoundEvent event) {}

  /**
   * This method gets called when a <code>SoundPlayback</code> is finished.
   *
   * @param event
   *          a {@link SoundEvent} object describing the
   *          event source and the related {@link Sound}.
   */
  public default void finished(SoundEvent event) {}
}