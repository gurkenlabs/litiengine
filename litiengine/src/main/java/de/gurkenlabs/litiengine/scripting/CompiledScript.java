package de.gurkenlabs.litiengine.scripting;

/** A compiled script generation that can create fresh instances. */
public interface CompiledScript extends AutoCloseable {
  ScriptInstance create() throws ScriptException;

  Class<? extends ScriptInstance> implementationType();

  @Override
  default void close() throws Exception {}
}
