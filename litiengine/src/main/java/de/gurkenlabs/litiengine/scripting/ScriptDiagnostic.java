package de.gurkenlabs.litiengine.scripting;

/** A compile-time or runtime diagnostic associated with a script. */
public record ScriptDiagnostic(Severity severity, String scriptId, String source, int line, int column, String message) {
  public enum Severity {
    INFO,
    WARNING,
    ERROR
  }
}
