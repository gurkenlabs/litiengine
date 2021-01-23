package de.gurkenlabs.litiengine.sound;

import java.util.EventListener;

/**
 * This event listener implementation provides callbacks for when a
 * {@link SoundPlayback} instance gets cancelled or finished.
 */
public interface SoundPlaybackListener extends EventListener {

  /**
   * This method gets called when a {@code SoundPlayback} is cancelled.
   *
   * @param event
   *          a {@link SoundEvent} object describing the
   *          event source and the related {@link Sound}.
   */
  default void cancelled(SoundEvent event) {}

  /**
   * This method gets called when a {@code SoundPlayback} is finished.
   *
   * @param event
   *          a {@link SoundEvent} object describing the
   *          event source and the related {@link Sound}.
   */
  default void finished(SoundEvent event) {}
}