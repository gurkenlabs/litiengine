package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A cancellable ordered sequence of delayed actions owned by a script context.
///
/// Build a sequence with [#then(Runnable)] and [#waitFor(int)], then call [#start()]. Delays are
/// accumulated until the next action. Closing the sequence cancels its pending scheduled action.
public final class ScriptSequence implements Subscription {
  private final ScriptContext<?> context;
  private final List<Step> steps = new ArrayList<>();
  private int pendingDelay;
  private int index;
  private Subscription scheduled;
  private boolean started;
  private boolean closed;

  ScriptSequence(ScriptContext<?> context) {
    this.context = context;
  }

  /// Appends an action after the currently accumulated delay.
  ///
  /// @param action The action to invoke.
  /// @return This sequence.
  /// @throws IllegalStateException if the sequence has started.
  public ScriptSequence then(Runnable action) {
    if (this.started) throw new IllegalStateException("A running sequence cannot be changed.");
    this.steps.add(new Step(this.pendingDelay, Objects.requireNonNull(action)));
    this.pendingDelay = 0;
    return this;
  }

  /// Adds a delay before the next appended action.
  ///
  /// @param delay The non-negative delay in milliseconds.
  /// @return This sequence.
  /// @throws IllegalStateException if the sequence has started.
  /// @throws IllegalArgumentException if `delay` is negative.
  /// @throws ArithmeticException if the accumulated delay overflows.
  public ScriptSequence waitFor(int delay) {
    if (this.started) throw new IllegalStateException("A running sequence cannot be changed.");
    if (delay < 0) throw new IllegalArgumentException("Delay must not be negative.");
    this.pendingDelay = Math.addExact(this.pendingDelay, delay);
    return this;
  }

  /// Schedules a camera pan to a given map location.
  ///
  /// @param target The target in map coordinates.
  /// @param durationTicks The transition duration in update ticks.
  /// @return This sequence.
  public ScriptSequence cameraPanTo(Point2D target, int durationTicks) {
    Objects.requireNonNull(target, "Target location must not be null.");
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().pan(target, durationTicks);
      }
    });
  }

  /// Schedules a camera pan to center on a target entity.
  ///
  /// @param target The target entity.
  /// @param durationTicks The transition duration in update ticks.
  /// @return This sequence.
  public ScriptSequence cameraPanTo(IEntity target, int durationTicks) {
    Objects.requireNonNull(target, "Target entity must not be null.");
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().pan(target.getCenter(), durationTicks);
      }
    });
  }

  /// Schedules a smooth camera zoom transition.
  ///
  /// @param targetZoom The target zoom factor.
  /// @param delayMs The transition duration in milliseconds.
  /// @return This sequence.
  public ScriptSequence cameraZoom(float targetZoom, int delayMs) {
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().setZoom(targetZoom, delayMs);
      }
    });
  }

  /// Schedules a screen shake effect.
  ///
  /// @param intensity The shake intensity.
  /// @param intervalMs The minimum interval between generated shake offsets, in milliseconds.
  /// @param durationMs The shake duration in milliseconds.
  /// @return This sequence.
  public ScriptSequence screenShake(double intensity, int intervalMs, int durationMs) {
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().shake(intensity, intervalMs, durationMs);
      }
    });
  }

  /// Schedules playing a sound effect by resource name.
  ///
  /// Missing resources are silently skipped when the action runs.
  ///
  /// @param soundName The sound resource name.
  /// @return This sequence.
  public ScriptSequence playSound(String soundName) {
    Objects.requireNonNull(soundName, "Sound name must not be null.");
    return this.then(() -> {
      Sound sound = Resources.sounds().get(soundName);
      if (sound != null) {
        Game.audio().playSound(sound);
      }
    });
  }

  /// Schedules playing a sound effect.
  ///
  /// @param sound The sound to play.
  /// @return This sequence.
  public ScriptSequence playSound(Sound sound) {
    Objects.requireNonNull(sound, "Sound must not be null.");
    return this.then(() -> Game.audio().playSound(sound));
  }

  /// Starts the sequence and transfers cancellation ownership to its context.
  ///
  /// @return This sequence as a cancellable subscription.
  /// @throws IllegalStateException if it has already started.
  public Subscription start() {
    if (this.started) throw new IllegalStateException("The sequence has already started.");
    this.started = true;
    this.context.manage(this);
    this.advance();
    return this;
  }

  /// Returns whether the sequence has pending actions.
  ///
  /// @return `true` while a started sequence has not completed or been closed.
  public boolean isRunning() {
    return this.started && !this.closed;
  }

  @Override
  public void close() {
    this.closed = true;
    if (this.scheduled != null) this.scheduled.close();
    this.scheduled = null;
  }

  private void advance() {
    if (this.closed || this.index >= this.steps.size()) {
      this.close();
      return;
    }
    Step step = this.steps.get(this.index++);
    this.scheduled = this.context.schedule(step.delay, () -> {
      if (this.closed) return;
      step.action.run();
      this.advance();
    });
  }

  private record Step(int delay, Runnable action) {}
}
