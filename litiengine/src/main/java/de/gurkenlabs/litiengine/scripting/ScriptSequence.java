package de.gurkenlabs.litiengine.scripting;

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
