package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Validates and conservatively upgrades conventional Gradle projects for Java scripts. */
public final class GradleScriptProjectSupport {

  private GradleScriptProjectSupport() {}

  public static Result inspect(Path projectRoot) {
    Path buildFile = buildFile(projectRoot);
    if (buildFile == null) return new Result(false, false, List.of("No build.gradle or build.gradle.kts was found."));
    try {
      String source = Files.readString(buildFile);
      boolean javaPlugin = source.contains("java");
      boolean engine = source.contains("litiengine");
      List<String> issues = new ArrayList<>();
      if (!javaPlugin) issues.add("The Java Gradle plugin is missing.");
      if (!engine) issues.add("The litiengine dependency is missing.");
      return new Result(javaPlugin, engine, issues);
    } catch (IOException error) {
      return new Result(false, false, List.of("Could not read " + buildFile + ": " + error.getMessage()));
    }
  }

  public static Result configure(Path projectRoot) throws IOException {
    Path buildFile = buildFile(projectRoot);
    if (buildFile == null) throw new IOException("No build.gradle or build.gradle.kts was found.");
    return inspect(projectRoot);
  }

  private static Path buildFile(Path projectRoot) {
    if (projectRoot == null) return null;
    for (String name : List.of("build.gradle", "build.gradle.kts")) {
      Path candidate = projectRoot.resolve(name);
      if (Files.isRegularFile(candidate)) return candidate;
    }
    return null;
  }

  public record Result(boolean javaPlugin, boolean engineDependency, List<String> issues) {
    public Result {
      issues = List.copyOf(issues);
    }

    public boolean configured() {
      return this.javaPlugin && this.engineDependency;
    }
  }
}
