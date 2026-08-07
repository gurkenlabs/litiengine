package de.gurkenlabs.litiengine.scripting;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/** Resolves precompiled Java or JVM-language script classes from the project classpath. */
final class ClasspathScriptProvider implements ScriptProvider {
  @Override public String language() { return "java"; }

  @Override
  public CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) throws ScriptException {
    try {
      Class<?> type = Class.forName(definition.getImplementation(), false, parent);
      Class<? extends ScriptInstance> scriptType = type.asSubclass(ScriptInstance.class);
      scriptType.getConstructor();
      return new CompiledScript() {
        @Override public ScriptInstance create() throws ScriptException {
          try {
            return scriptType.getConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ScriptException("Could not instantiate script " + definition.getId() + ".", e);
          }
        }

        @Override public Class<? extends ScriptInstance> implementationType() { return scriptType; }
      };
    } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException e) {
      throw new ScriptException("Could not resolve script implementation " + definition.getImplementation() + ".", e);
    }
  }
}
