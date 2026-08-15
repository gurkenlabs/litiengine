package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A cancellable ordered sequence of delayed actions owned by a script context. */
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

  public ScriptSequence then(Runnable action) {
    if (this.started) throw new IllegalStateException("A running sequence cannot be changed.");
    this.steps.add(new Step(this.pendingDelay, Objects.requireNonNull(action)));
    this.pendingDelay = 0;
    return this;
  }

  public ScriptSequence waitFor(int delay) {
    if (this.started) throw new IllegalStateException("A running sequence cannot be changed.");
    if (delay < 0) throw new IllegalArgumentException("Delay must not be negative.");
    this.pendingDelay = Math.addExact(this.pendingDelay, delay);
    return this;
  }

  /** Schedules a camera pan to a given map location. */
  public ScriptSequence cameraPanTo(Point2D target, int durationTicks) {
    Objects.requireNonNull(target, "Target location must not be null.");
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().pan(target, durationTicks);
      }
    });
  }

  /** Schedules a camera pan to center on a target entity. */
  public ScriptSequence cameraPanTo(IEntity target, int durationTicks) {
    Objects.requireNonNull(target, "Target entity must not be null.");
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().pan(target.getCenter(), durationTicks);
      }
    });
  }

  /** Schedules a smooth camera zoom transition. */
  public ScriptSequence cameraZoom(float targetZoom, int delayMs) {
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().setZoom(targetZoom, delayMs);
      }
    });
  }

  /** Schedules a screen shake effect. */
  public ScriptSequence screenShake(double intensity, int delayMs, int durationTicks) {
    return this.then(() -> {
      if (Game.world().camera() != null) {
        Game.world().camera().shake(intensity, delayMs, durationTicks);
      }
    });
  }

  /** Schedules playing a sound effect by resource name. */
  public ScriptSequence playSound(String soundName) {
    Objects.requireNonNull(soundName, "Sound name must not be null.");
    return this.then(() -> {
      Sound sound = Resources.sounds().get(soundName);
      if (sound != null) {
        Game.audio().playSound(sound);
      }
    });
  }

  /** Schedules playing a sound effect. */
  public ScriptSequence playSound(Sound sound) {
    Objects.requireNonNull(sound, "Sound must not be null.");
    return this.then(() -> Game.audio().playSound(sound));
  }

  public Subscription start() {
    if (this.started) throw new IllegalStateException("The sequence has already started.");
    this.started = true;
    this.context.manage(this);
    this.advance();
    return this;
  }

  public boolean isRunning() {
    return this.started && !this.closed && this.index < this.steps.size();
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
