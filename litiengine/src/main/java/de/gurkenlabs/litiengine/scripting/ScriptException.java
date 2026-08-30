package de.gurkenlabs.litiengine.scripting;

import java.util.List;

/** Indicates that a script could not be compiled, instantiated, configured, or executed. */
public class ScriptException extends Exception {
  private final List<ScriptDiagnostic> diagnostics;

  public ScriptException(String message) {
    this(message, null, List.of());
  }

  public ScriptException(String message, Throwable cause) {
    this(message, cause, List.of());
  }

  public ScriptException(String message, List<ScriptDiagnostic> diagnostics) {
    this(message, null, diagnostics);
  }

  public ScriptException(String message, Throwable cause, List<ScriptDiagnostic> diagnostics) {
    super(message, cause);
    this.diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
  }

  public List<ScriptDiagnostic> getDiagnostics() {
    return this.diagnostics;
  }
}
