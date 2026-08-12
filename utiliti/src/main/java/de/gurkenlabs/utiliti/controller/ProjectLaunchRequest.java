package de.gurkenlabs.utiliti.controller;

import java.util.List;
import java.util.Map;

/** Options for launching the current game project. */
public record ProjectLaunchRequest(
    ProjectModel project,
    Mode mode,
    List<String> gameArguments,
    List<String> buildArguments,
    Map<String, String> environment) {

  public ProjectLaunchRequest {
    java.util.Objects.requireNonNull(project, "Project model must not be null.");
    mode = mode == null ? Mode.RUN : mode;
    gameArguments = gameArguments == null ? List.of() : List.copyOf(gameArguments);
    buildArguments = buildArguments == null ? List.of() : List.copyOf(buildArguments);
    environment = environment == null ? Map.of() : Map.copyOf(environment);
  }

  public static ProjectLaunchRequest run(ProjectModel project) {
    return new ProjectLaunchRequest(project, Mode.RUN, List.of(), List.of(), Map.of());
  }

  /** Splits optional user-supplied Gradle arguments while preserving quoted values. */
  public static List<String> parseBuildArguments(String commandLine) {
    if (commandLine == null || commandLine.isBlank()) return List.of();
    List<String> arguments = new java.util.ArrayList<>();
    StringBuilder current = new StringBuilder();
    char quote = 0;
    boolean tokenStarted = false;
    for (int index = 0; index < commandLine.length(); index++) {
      char character = commandLine.charAt(index);
      if (character == '\\' && index + 1 < commandLine.length()) {
        char next = commandLine.charAt(index + 1);
        if (next == '\'' || next == '"' || Character.isWhitespace(next)) {
          current.append(next);
          tokenStarted = true;
          index++;
        } else {
          current.append(character);
          tokenStarted = true;
        }
      } else if (character == '\'' || character == '"') {
        if (quote == 0) {
          quote = character;
          tokenStarted = true;
        } else if (quote == character) {
          quote = 0;
        } else {
          current.append(character);
        }
      } else if (Character.isWhitespace(character) && quote == 0) {
        if (tokenStarted) {
          arguments.add(current.toString());
          current.setLength(0);
          tokenStarted = false;
        }
      } else {
        current.append(character);
        tokenStarted = true;
      }
    }
    if (quote != 0) {
      throw new IllegalArgumentException("Unclosed quote in Gradle launch arguments.");
    }
    if (tokenStarted) arguments.add(current.toString());
    return List.copyOf(arguments);
  }

  public enum Mode {
    RUN,
    DEBUG
  }
}
