package de.gurkenlabs.utiliti.controller;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Build-tool-neutral description of the Java project currently opened in utiLITI. */
public record ProjectModel(
    Path projectRoot,
    Path wrapper,
    String runTask,
    String mainClass,
    int javaVersion,
    List<Path> sourceRoots,
    List<Path> outputDirectories,
    List<Path> compileClasspath,
    List<Path> runtimeClasspath) {

  public ProjectModel {
    projectRoot = projectRoot == null ? null : projectRoot.toAbsolutePath().normalize();
    wrapper = wrapper == null ? null : wrapper.toAbsolutePath().normalize();
    runTask = runTask == null || runTask.isBlank() ? ":run" : runTask;
    sourceRoots = copy(sourceRoots);
    outputDirectories = copy(outputDirectories);
    compileClasspath = copy(compileClasspath);
    runtimeClasspath = copy(runtimeClasspath);
  }

  public boolean canRun() {
    return this.projectRoot != null && this.wrapper != null;
  }

  /** Compiler inputs for scripts, including the project's own already-compiled classes. */
  public List<Path> scriptCompilationClasspath() {
    return Stream.concat(this.outputDirectories.stream(), this.compileClasspath.stream())
        .distinct()
        .toList();
  }

  private static List<Path> copy(List<Path> paths) {
    return paths == null
        ? List.of()
        : paths.stream()
            .filter(java.util.Objects::nonNull)
            .map(Path::toAbsolutePath)
            .map(Path::normalize)
            .distinct()
            .toList();
  }
}
