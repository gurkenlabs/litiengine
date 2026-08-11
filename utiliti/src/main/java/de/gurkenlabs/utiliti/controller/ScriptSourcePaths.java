package de.gurkenlabs.utiliti.controller;

import java.util.Locale;

/** Conventional project-relative locations for runtime-compiled script sources. */
public final class ScriptSourcePaths {
  private static final String SCRIPT_ROOT = "scripts/";

  private ScriptSourcePaths() {}

  public static String create(String language, String className) {
    String normalizedLanguage = normalizeLanguage(language);
    return SCRIPT_ROOT + normalizedLanguage + "/" + className + extension(normalizedLanguage);
  }

  /** Keeps renamed legacy scripts in their current directory while changing the file name. */
  public static String rename(String currentSource, String language, String className) {
    if (currentSource == null || currentSource.isBlank()) {
      return create(language, className);
    }
    String normalized = currentSource.replace('\\', '/');
    int separator = normalized.lastIndexOf('/');
    String directory = separator < 0 ? "" : normalized.substring(0, separator + 1);
    String fileName = separator < 0 ? normalized : normalized.substring(separator + 1);
    int extension = fileName.lastIndexOf('.');
    String suffix = extension < 0 ? extension(normalizeLanguage(language)) : fileName.substring(extension);
    return directory + className + suffix;
  }

  private static String normalizeLanguage(String language) {
    return language == null || language.isBlank() ? "java" : language.toLowerCase(Locale.ROOT);
  }

  private static String extension(String language) {
    return "." + ("groovy".equals(language) ? "groovy" : "java");
  }
}
