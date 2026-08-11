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

  public enum Mode {
    RUN,
    DEBUG
  }
}
