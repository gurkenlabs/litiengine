package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validates and conservatively upgrades conventional Gradle projects for Groovy scripts. */
public final class GradleScriptProjectSupport {
  private static final Pattern ENGINE_DEPENDENCY = Pattern.compile(
    "(?m)(?:implementation|api)\\s*(?:\\(|)\\s*['\"]de\\.gurkenlabs:litiengine:([^'\"]+)['\"]\\s*\\)?");

  private GradleScriptProjectSupport() {}

  public static Result inspect(Path projectRoot) {
    Path buildFile = buildFile(projectRoot);
    if (buildFile == null) return new Result(false, false, List.of("No build.gradle or build.gradle.kts was found."));
    try {
      String source = Files.readString(buildFile);
      boolean groovy = source.matches("(?s).*\\bid\\s*(?:\\(|)\\s*['\"]groovy['\"].*")
        || source.matches("(?s).*\\bapply\\s+plugin:\\s*['\"]groovy['\"].*")
        || source.matches("(?s).*\\bplugins\\s*\\{[^}]*\\bgroovy\\b[^}]*}.*");
      boolean engine = source.contains("litiengine-groovy");
      List<String> issues = new ArrayList<>();
      if (!groovy) issues.add("The Groovy Gradle plugin is missing.");
      if (!engine) issues.add("The litiengine-groovy dependency is missing.");
      return new Result(groovy, engine, issues);
    } catch (IOException error) {
      return new Result(false, false, List.of("Could not read " + buildFile + ": " + error.getMessage()));
    }
  }

  /** Adds only missing declarations to a standard Groovy Gradle build and leaves all other text untouched. */
  public static Result configure(Path projectRoot) throws IOException {
    Path buildFile = buildFile(projectRoot);
    if (buildFile == null) throw new IOException("No build.gradle or build.gradle.kts was found.");
    String source = Files.readString(buildFile);
    boolean kotlin = buildFile.getFileName().toString().endsWith(".kts");
    Result before = inspect(projectRoot);
    String updated = source;
    if (!before.groovyPlugin()) updated = addPlugin(updated, kotlin);
    if (!before.groovyDependency()) updated = addDependency(updated, kotlin);
    if (!updated.equals(source)) Files.writeString(buildFile, updated);
    return inspect(projectRoot);
  }

  private static String addPlugin(String source, boolean kotlin) throws IOException {
    Matcher plugins = Pattern.compile("(?m)^\\s*plugins\\s*\\{").matcher(source);
    if (!plugins.find()) throw new IOException("The Gradle build has no plugins block that utiLITI can update safely.");
    String declaration = kotlin ? "\n  groovy" : "\n  id \"groovy\"";
    return source.substring(0, plugins.end()) + declaration + source.substring(plugins.end());
  }

  private static String addDependency(String source, boolean kotlin) throws IOException {
    Matcher dependencies = Pattern.compile("(?m)^\\s*dependencies\\s*\\{").matcher(source);
    if (!dependencies.find()) throw new IOException("The Gradle build has no dependencies block that utiLITI can update safely.");
    Matcher engine = ENGINE_DEPENDENCY.matcher(source);
    String declaration;
    if (engine.find()) {
      declaration = kotlin ? "\n  implementation(\"de.gurkenlabs:litiengine-groovy:" + engine.group(1) + "\")"
        : "\n  implementation \"de.gurkenlabs:litiengine-groovy:" + engine.group(1) + "\"";
    } else if (source.contains("project(\":litiengine\")") || source.contains("project(':litiengine')")) {
      declaration = kotlin ? "\n  implementation(project(\":litiengine-groovy\"))"
        : "\n  implementation project(\":litiengine-groovy\")";
    } else {
      throw new IOException("Could not determine the LITIENGINE version from the existing dependencies.");
    }
    return source.substring(0, dependencies.end()) + declaration + source.substring(dependencies.end());
  }

  private static Path buildFile(Path projectRoot) {
    if (projectRoot == null) return null;
    for (String name : List.of("build.gradle", "build.gradle.kts")) {
      Path candidate = projectRoot.resolve(name);
      if (Files.isRegularFile(candidate)) return candidate;
    }
    return null;
  }

  public record Result(boolean groovyPlugin, boolean groovyDependency, List<String> issues) {
    public Result {
      issues = List.copyOf(issues);
    }

    public boolean configured() {
      return this.groovyPlugin && this.groovyDependency;
    }
  }
}
