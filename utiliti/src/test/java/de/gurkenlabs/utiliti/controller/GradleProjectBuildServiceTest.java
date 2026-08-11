package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GradleProjectBuildServiceTest {

  @Test
  void resolvesGradleApplicationProjectFromGameFile(@TempDir Path root) throws Exception {
    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    Files.writeString(wrapper, "");
    Files.writeString(root.resolve("build.gradle"), """
      plugins {
        id 'java'
        id 'application'
      }
      application {
        mainClass = 'example.Game'
      }
      java {
        toolchain {
          languageVersion = JavaLanguageVersion.of(21)
        }
      }
      """);
    Files.createDirectories(root.resolve("src/main/java"));
    Files.createDirectories(root.resolve("build/classes/java/main"));
    Path gameFile = root.resolve("game.litidata");
    Files.writeString(gameFile, "");

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectModel model = service.resolve(gameFile);

      assertEquals(root.toAbsolutePath().normalize(), model.projectRoot());
      assertEquals(wrapper.toAbsolutePath().normalize(), model.wrapper());
      assertEquals("example.Game", model.mainClass());
      assertEquals(":run", model.runTask());
      assertEquals(21, model.javaVersion());
      assertTrue(model.canRun());
      assertTrue(
          model.sourceRoots().contains(root.resolve("src/main/java").toAbsolutePath().normalize()));
      assertTrue(
          model
              .outputDirectories()
              .contains(root.resolve("build/classes/java/main").toAbsolutePath().normalize()));
    }
  }

  @Test
  void unresolvedLocationProducesNonRunnableModel(@TempDir Path root) {
    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectModel model = service.resolve(root.resolve("missing/game.litidata"));

      assertNull(model.projectRoot());
      assertFalse(model.canRun());
    }
  }

  @Test
  void launchCommandKeepsApplicationRunContract(@TempDir Path root) throws Exception {
    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    Files.writeString(wrapper, "");
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());
    ProjectLaunchRequest request = new ProjectLaunchRequest(model, ProjectLaunchRequest.Mode.DEBUG,
        List.of("--level", "hospital"), List.of("--stacktrace"), Map.of());

    List<String> command = GradleProjectBuildService.command(request);

    assertTrue(command.contains(":run"));
    assertTrue(command.contains("--no-daemon"));
    assertTrue(command.contains("--debug-jvm"));
    assertTrue(command.contains("--stacktrace"));
    assertTrue(command.contains("--args=--level hospital"));
  }

  @Test
  void reusesIncludedBuildJarAlreadyLoadedByEditor(@TempDir Path root) throws Exception {
    Path gameRoot = Files.createDirectories(root.resolve("game"));
    Path gameWrapper = gameRoot.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    Files.writeString(gameWrapper, "");
    Path engineRoot = Files.createDirectories(root.resolve("litiengine"));
    Files.writeString(engineRoot.resolve("settings.gradle"), "");
    Path engineProject = Files.createDirectories(engineRoot.resolve("litiengine"));
    Path engineJar = Files.createDirectories(engineProject.resolve("build/libs")).resolve("litiengine.jar");
    Files.writeString(engineJar, "");
    ProjectModel model = new ProjectModel(
        gameRoot,
        gameWrapper,
        ":run",
        "example.Game",
        25,
        List.of(),
        List.of(),
        List.of(),
        List.of(engineJar));

    assertEquals(
        List.of(engineJar.toAbsolutePath().normalize()),
        GradleProjectBuildService.reusableArtifacts(model, engineJar.toString()));

    Path initScript = root.resolve("run.gradle");
    List<String> command = GradleProjectBuildService.command(
        ProjectLaunchRequest.run(model), initScript, List.of(engineJar));
    assertTrue(
        command.stream().anyMatch(argument -> argument.startsWith("-Dutiliti.reusableArtifacts=")));
    assertTrue(command.contains("--init-script"));
    assertTrue(command.contains(initScript.toString()));
    assertTrue(command.contains(":run"));
    assertFalse(command.contains("-x"));
  }

  @Test
  void parsesAuthoritativeGradleModel(@TempDir Path root) throws Exception {
    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    Files.writeString(wrapper, "");
    ProjectModel fallback = new ProjectModel(root, wrapper, ":run", null, 25,
        List.of(), List.of(), List.of(), List.of());
    Path classes = root.resolve("custom/classes");
    Path dependency = root.resolve("dependency.jar");
    String json = jakarta.json.Json.createObjectBuilder()
        .add("projectRoot", root.toString())
        .add("runTask", ":game:run")
        .add("mainClass", "example.Game")
        .add("javaVersion", 21)
        .add("sourceRoots", jakarta.json.Json.createArrayBuilder().add(root.resolve("source").toString()))
        .add("outputDirectories", jakarta.json.Json.createArrayBuilder().add(classes.toString()))
        .add("compileClasspath", jakarta.json.Json.createArrayBuilder().add(dependency.toString()))
        .add("runtimeClasspath", jakarta.json.Json.createArrayBuilder().add(classes.toString()))
        .build().toString();
    String output = "noise\nUTILITI_PROJECT_MODEL:"
        + Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

    ProjectModel model = GradleProjectBuildService.parseModel(output, fallback);

    assertEquals(":game:run", model.runTask());
    assertEquals("example.Game", model.mainClass());
    assertEquals(21, model.javaVersion());
    assertEquals(List.of(dependency.toAbsolutePath().normalize()), model.compileClasspath());
    assertEquals(
        List.of(classes.toAbsolutePath().normalize(), dependency.toAbsolutePath().normalize()),
        model.scriptCompilationClasspath());
  }

  private static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
  }
}
