package de.gurkenlabs.utiliti.controller.debug;

import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/** Controls a remote runtime-script debugging session. */
public interface ScriptDebuggerBackend extends AutoCloseable {
  enum State { DISCONNECTED, ATTACHING, RUNNING, PAUSED, FAILED }

  interface Listener {
    default void stateChanged(State state, String detail) {}

    default void paused(ScriptDebugSnapshot snapshot) {}
  }

  void attach(String host, int port, Collection<ScriptDefinition> definitions) throws IOException;

  void setBreakpoints(Collection<ScriptBreakpoint> breakpoints);

  void resume();

  void pause();

  void stepInto();

  void stepOver();

  void stepOut();

  /** Loads one level of children for an object reference while the VM is paused. */
  List<ScriptDebugSnapshot.Variable> expandVariable(String reference);

  State state();

  @Override
  void close();
}
