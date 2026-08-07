package de.gurkenlabs.litiengine.scripting;

/** Base class for scripts attached to the game lifecycle. */
public abstract class GameScript extends AbstractScript<Object> {
  @Override protected final void attached() throws Exception { this.onStarted(); }
  @Override protected final void detached() throws Exception { this.onStopped(); }

  /** Called after the binding enters the running game lifecycle. */
  protected void onStarted() throws Exception { this.started(); }

  /** Called before the binding leaves the game lifecycle. */
  protected void onStopped() throws Exception { this.stopped(); }

  /** @deprecated Override {@link #onStarted()} in new scripts. */
  @Deprecated
  protected void started() throws Exception {}

  /** @deprecated Override {@link #onStopped()} in new scripts. */
  @Deprecated
  protected void stopped() throws Exception {}
}
