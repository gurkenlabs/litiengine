package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;

/** Base implementation that provides typed access to a script host, context, and global state. */
public abstract class AbstractScript<T> implements ScriptInstance {
  private ScriptContext<T> context;
  protected final ScriptGlobals globals = Game.scripts().globals();

  @Override
  @SuppressWarnings("unchecked")
  public final void attach(ScriptContext<?> context) throws Exception {
    if (this.context != null) throw new IllegalStateException("The script is already attached.");
    this.context = (ScriptContext<T>) context;
    this.attached();
  }

  @Override
  public final void detach() throws Exception {
    if (this.context == null) return;
    try {
      this.detached();
    } finally {
      this.context.close();
      this.context = null;
    }
  }

  protected final ScriptContext<T> context() {
    if (this.context == null) throw new IllegalStateException("The script is not attached.");
    return this.context;
  }

  protected final T host() {
    return this.context().host();
  }

  /** Returns the host's current environment, or {@code null} for game scripts without one. */
  protected final Environment environment() {
    return this.context().environment();
  }

  protected final ScriptGlobals globals() {
    return this.globals;
  }

  protected void attached() throws Exception {}

  protected void detached() throws Exception {}
}
