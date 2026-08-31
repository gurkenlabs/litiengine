package de.gurkenlabs.litiengine.scripting;

import java.net.URL;
import java.util.Optional;

/// Compiles or resolves scripts for one source language.
public interface ScriptProvider {
  String language();

  CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) throws ScriptException;

  /// Compiles a script with classpath and language-level information supplied by the project build.
  default CompiledScript compile(ScriptDefinition definition, URL source, ScriptCompilationContext context)
      throws ScriptException {
    return this.compile(definition, source, context.parent());
  }

  /// Creates semantic tooling for this language when the provider supports editor integration.
  default Optional<ScriptLanguageService> createLanguageService(ScriptLanguageService.Workspace workspace) {
    return Optional.empty();
  }
}
