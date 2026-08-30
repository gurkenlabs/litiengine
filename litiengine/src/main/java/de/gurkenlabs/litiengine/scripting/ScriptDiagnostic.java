package de.gurkenlabs.litiengine.scripting;

/// A compile-time or runtime diagnostic associated with a script.
///
/// @param severity The diagnostic severity.
/// @param scriptId The identifier of the affected script.
/// @param source The source location, when known.
/// @param line The one-based source line, or a negative value when unavailable.
/// @param column The one-based source column, or a negative value when unavailable.
/// @param message The human-readable diagnostic message.
public record ScriptDiagnostic(Severity severity, String scriptId, String source, int line, int column, String message) {
  /// Indicates how a diagnostic should be presented to the user.
  public enum Severity {
    /// Informational feedback that does not prevent execution.
    INFO,
    /// A potential problem that does not prevent execution.
    WARNING,
    /// A problem that prevents compilation or execution.
    ERROR
  }
}
