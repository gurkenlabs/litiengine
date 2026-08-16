package de.gurkenlabs.litiengine.scripting;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/** Resolves precompiled Java or JVM-language script classes from the project classpath. */
final class ClasspathScriptProvider implements ScriptProvider {
  @Override public String language() { return "java"; }

  @Override
  public CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) throws ScriptException {
    Class<?> type = null;
    String impl = definition.getImplementation();
    try {
      if (impl != null && !impl.isBlank()) {
        type = Class.forName(impl, false, parent);
      }
    } catch (ClassNotFoundException ignored) {
    }

    if (type == null && definition.getSource() != null) {
      String derived = definition.getSource().replace('\\', '/')
          .replaceFirst("^(?:.*?/)?(?:src/main/java|src/main/groovy|src/main|src|scripts)/(?:java|groovy)?/?", "")
          .replaceFirst("\\.[^.]+$", "")
          .replace('/', '.');
      if (!derived.isBlank() && !derived.equals(impl)) {
        try {
          type = Class.forName(derived, false, parent);
          definition.setImplementation(derived);
        } catch (ClassNotFoundException ignored) {
        }
      }
    }

    if (type == null) {
      throw new ScriptException("Could not resolve script implementation " + (impl != null ? impl : definition.getId()) + ".");
    }

    try {
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
    } catch (ClassCastException | NoSuchMethodException e) {
      throw new ScriptException("Could not resolve script implementation " + definition.getImplementation() + ".", e);
    }
  }
}
