package de.gurkenlabs.litiengine.sound;

import java.util.EventListener;

/// This event listener implementation provides callbacks for when a [SoundPlayback] instance gets cancelled or
/// finished.
public interface SoundPlaybackListener extends EventListener {

  /// This method gets called when a `SoundPlayback` is cancelled.
  ///
  /// @param event
  /// a [SoundEvent] object describing the event source and the related [Sound].
  default void cancelled(SoundEvent event) {}

  /// This method gets called when a `SoundPlayback` is finished.
  ///
  /// @param event
  /// a [SoundEvent] object describing the event source and the related [Sound].
  default void finished(SoundEvent event) {}
}
