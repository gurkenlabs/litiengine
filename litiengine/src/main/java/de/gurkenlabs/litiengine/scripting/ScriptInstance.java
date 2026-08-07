package de.gurkenlabs.litiengine.scripting;

/** One configured runtime instance of a script. */
public interface ScriptInstance {
  void attach(ScriptContext<?> context) throws Exception;

  default void update() throws Exception {}

  void detach() throws Exception;
}
