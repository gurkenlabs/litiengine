package de.gurkenlabs.litiengine.scripting;

import java.nio.file.Path;
import java.util.List;

/// Build-tool-neutral inputs required to compile a script source generation.
///
/// @param parent The parent class loader visible to the compiled script; `null` selects the engine
/// class loader.
/// @param classpath Additional normalized class-path entries; null entries are discarded.
/// @param javaVersion The Java language level, or zero to let the provider select its default.
public record ScriptCompilationContext(ClassLoader parent, List<Path> classpath, int javaVersion) {
  public ScriptCompilationContext {
    if (parent == null) parent = ScriptCompilationContext.class.getClassLoader();
    classpath =
        classpath == null
            ? List.of()
            : classpath.stream()
                .filter(java.util.Objects::nonNull)
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .distinct()
                .toList();
    if (javaVersion < 0) throw new IllegalArgumentException("Java version must not be negative.");
  }
}
