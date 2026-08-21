package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.nio.file.Path;

/** Resolves and launches an external game project without exposing build-tool APIs to the editor. */
public interface ProjectBuildService extends AutoCloseable {
  ProjectModel resolve(Path projectLocation);

  /** Resolves the authoritative build model, falling back to inexpensive project inspection. */
  default ProjectModel refresh(Path projectLocation) {
    return this.resolve(projectLocation);
  }

  ProjectSession launch(ProjectLaunchRequest request) throws IOException;

  default void cancelCurrentBuild() {}

  @Override
  default void close() {}
}
