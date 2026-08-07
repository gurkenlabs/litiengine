package de.gurkenlabs.litiengine.scripting;

import java.net.URL;
import java.util.Optional;

/** Compiles or resolves scripts for one source language. */
public interface ScriptProvider {
  String language();

  CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) throws ScriptException;

  /** Creates semantic tooling for this language when the provider supports editor integration. */
  default Optional<ScriptLanguageService> createLanguageService(ScriptLanguageService.Workspace workspace) {
    return Optional.empty();
  }
}
