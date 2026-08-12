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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    ProjectModel model = new ProjectModel(root, wrapper, ":game:play", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());
    ProjectLaunchRequest request = new ProjectLaunchRequest(model, ProjectLaunchRequest.Mode.DEBUG,
        List.of("--level", "hospital"), List.of("--stacktrace"), Map.of("UTILITI_DEBUG_PORT", "51234"));

    Path initScript = root.resolve("run.gradle");
    List<String> command = GradleProjectBuildService.command(request, initScript, List.of());

    assertTrue(command.contains(":game:play"));
    assertTrue(command.contains("--no-daemon"));
    assertTrue(command.contains("-Dutiliti.debugProject=true"));
    assertTrue(command.contains("-Dutiliti.launchTask=:game:play"));
    assertTrue(command.contains("-Dutiliti.debugPort=51234"));
    assertFalse(command.contains("--debug-jvm"));
    assertTrue(command.contains("--init-script"));
    assertTrue(command.contains(initScript.toString()));
    assertTrue(command.contains("--stacktrace"));
    assertTrue(command.contains("--args=--level hospital"));
  }

  @Test
  void unixLaunchUsesShellSoWrapperDoesNotNeedExecutableBit(@TempDir Path root) throws Exception {
    Path wrapper = root.resolve("gradlew");
    Files.writeString(wrapper, "#!/bin/sh\n");
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());

    List<String> command = GradleProjectBuildService.command(
        ProjectLaunchRequest.run(model), root.resolve("run.gradle"), List.of(), false);

    assertEquals("sh", command.getFirst());
    assertEquals(wrapper.toAbsolutePath().normalize().toString(), command.get(1));
    assertFalse(command.contains("cmd.exe"));
    assertFalse(command.contains("/c"));
  }

  @Test
  void artifactPathNormalizationRespectsFilesystemCaseSensitivity() {
    String mixedCase = "/opt/LITIengine/Build/Game.jar";

    assertEquals(mixedCase, GradleProjectBuildService.normalizeArtifactPath(mixedCase, false));
    assertEquals(mixedCase.toLowerCase(java.util.Locale.ROOT),
        GradleProjectBuildService.normalizeArtifactPath(mixedCase, true));
  }

  @Test
  void debugProjectConfiguresTheApplicationJvmInsteadOfGradle() throws Exception {
    try (var source = GradleProjectBuildService.class.getResourceAsStream(
        "/gradle/utiliti-project-run.gradle")) {
      String initScript = new String(source.readAllBytes(), StandardCharsets.UTF_8);

      assertTrue(initScript.contains("withType(JavaExec)"));
      assertTrue(initScript.contains("javaExecTask.path == launchTaskPath"));
      assertTrue(initScript.contains(GradleProjectBuildService.LAUNCH_MARKER));
      assertTrue(initScript.contains("File.separatorChar == '\\\\'"));
      assertTrue(initScript.contains("debugOptions"));
      assertTrue(initScript.contains("port = debugPort"));
      assertTrue(initScript.contains("suspend = true"));
    }
  }

  @Test
  void recognizesOnlyTheStructuredApplicationLaunchMarker() {
    assertTrue(GradleProjectBuildService.isLaunchMarker(
        "  " + GradleProjectBuildService.LAUNCH_MARKER + "  "));
    assertFalse(GradleProjectBuildService.isLaunchMarker("> Task :run"));
    assertFalse(GradleProjectBuildService.isLaunchMarker("> Configure project :litiengine"));
    assertFalse(GradleProjectBuildService.isLaunchMarker("Loading game resources"));
  }

  @Test
  void recognizesWrapperDownloadFailureAndProvidesRecovery(@TempDir Path root) {
    String output = """
        Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip
        Exception in thread \"main\" java.net.SocketException: Unexpected end of file from server
        at org.gradle.wrapper.Install.forceFetch(SourceFile:2)
        """;

    assertTrue(GradleProjectBuildService.isTransientWrapperDownloadFailure(output));
    String message = GradleProjectBuildService.launchFailureMessage(output, root).orElseThrow();
    assertTrue(message.contains("after two attempts"));
    assertTrue(message.contains(isWindows() ? "gradlew.bat --version" : "./gradlew --version"));
    assertTrue(message.contains(root.toString()));
  }

  @Test
  void retriesInterruptedWrapperDownloadBeforeFailingLaunch(@TempDir Path root) throws Exception {
    Path wrapper = createRetryingLaunchWrapper(root);
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectSession session = service.launch(ProjectLaunchRequest.run(model));
      List<String> output = new CopyOnWriteArrayList<>();
      session.onOutput(output::add);

      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (session.state() != ProjectSession.State.RUNNING && System.nanoTime() < deadline) {
        Thread.sleep(20);
      }

      assertEquals(ProjectSession.State.RUNNING, session.state());
      assertTrue(output.contains(GradleProjectBuildService.WRAPPER_RETRY_MESSAGE));
      assertFalse(output.stream().anyMatch(line -> line.contains("SocketException")));
      session.stop();
    }
  }

  @Test
  void cancellationBetweenWrapperAttemptsPreventsRetry(@TempDir Path root) throws Exception {
    Path wrapper = createRetryingLaunchWrapper(root);
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectSession session = service.launch(ProjectLaunchRequest.run(model));
      session.onOutput(line -> {
        if (GradleProjectBuildService.WRAPPER_RETRY_MESSAGE.equals(line)) session.stop();
      });

      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (session.isActive() && System.nanoTime() < deadline) {
        Thread.sleep(20);
      }

      assertFalse(session.isActive());
      assertFalse(Files.exists(root.resolve("second-attempt-started")));
    }
  }

  @Test
  void sessionRemainsBuildingUntilStructuredLaunchMarker(@TempDir Path root) throws Exception {
    Path wrapper = createLaunchWrapper(root);
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectSession session = service.launch(ProjectLaunchRequest.run(model));
      List<ProjectSession.State> states = new CopyOnWriteArrayList<>();
      session.onStateChanged(states::add);

      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (session.state() != ProjectSession.State.RUNNING && System.nanoTime() < deadline) {
        Thread.sleep(20);
      }

      assertTrue(states.contains(ProjectSession.State.BUILDING));
      assertEquals(ProjectSession.State.RUNNING, session.state());
      session.stop();
    }
  }

  @Test
  void debugSessionWaitsInStartingGameStateAfterLaunchMarker(@TempDir Path root) throws Exception {
    Path wrapper = createLaunchWrapper(root);
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());
    ProjectLaunchRequest request = new ProjectLaunchRequest(
        model, ProjectLaunchRequest.Mode.DEBUG, List.of(), List.of(), Map.of());

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectSession session = service.launch(request);
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (session.state() != ProjectSession.State.STARTING_GAME
          && System.nanoTime() < deadline) {
        Thread.sleep(20);
      }

      assertEquals(ProjectSession.State.STARTING_GAME, session.state());
      assertTrue(session.isActive());
      session.stop();
    }
  }

  @Test
  void repeatedWrapperFailureExposesRecoveryWithoutStackNoise(@TempDir Path root) throws Exception {
    Path wrapper = createFailingLaunchWrapper(root);
    ProjectModel model = new ProjectModel(root, wrapper, ":run", "example.Game", 25,
        List.of(), List.of(), List.of(), List.of());

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectSession session = service.launch(ProjectLaunchRequest.run(model));
      List<String> output = new CopyOnWriteArrayList<>();
      session.onOutput(output::add);
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (session.isActive() && System.nanoTime() < deadline) {
        Thread.sleep(20);
      }

      assertEquals(ProjectSession.State.EXITED, session.state());
      assertEquals(1, session.exitCode().orElseThrow());
      assertTrue(session.failureMessage().orElseThrow().contains("after two attempts"));
      assertTrue(output.contains(GradleProjectBuildService.WRAPPER_RETRY_MESSAGE));
      assertFalse(output.stream().anyMatch(line -> line.contains("SocketException")));
      assertFalse(output.stream().anyMatch(line -> line.stripLeading().startsWith("at ")));
    }
  }

  @Test
  void modelResolutionCanBeCancelledWithoutWaitingForItsTimeout(@TempDir Path root) throws Exception {
    Path wrapper = createLaunchWrapper(root);
    Files.writeString(root.resolve("build.gradle"), "plugins { id 'application' }");

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      CompletableFuture<ProjectModel> refresh = CompletableFuture.supplyAsync(() -> service.refresh(root));
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (!service.isRefreshingProjectModel() && System.nanoTime() < deadline) {
        Thread.sleep(10);
      }
      assertTrue(service.isRefreshingProjectModel());

      service.cancelCurrentBuild();

      assertEquals(root.toAbsolutePath().normalize(),
          refresh.get(5, TimeUnit.SECONDS).projectRoot());
    }
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

  private static Path createLaunchWrapper(Path root) throws Exception {
    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    if (isWindows()) {
      Files.writeString(wrapper, "@echo off\r\nping 127.0.0.1 -n 2 > nul\r\n"
          + "echo " + GradleProjectBuildService.LAUNCH_MARKER + "\r\n"
          + "ping 127.0.0.1 -n 30 > nul\r\n");
    } else {
      Files.writeString(wrapper, "#!/bin/sh\nsleep 1\necho '"
          + GradleProjectBuildService.LAUNCH_MARKER + "'\nsleep 30\n");
    }
    return wrapper;
  }

  private static Path createRetryingLaunchWrapper(Path root) throws Exception {
    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    if (isWindows()) {
      Files.writeString(wrapper, "@echo off\r\n"
          + "if exist \"%~dp0retry-complete\" goto success\r\n"
          + "> \"%~dp0retry-complete\" echo first-attempt\r\n"
          + "ping 127.0.0.1 -n 2 > nul\r\n"
          + "echo Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip\r\n"
          + "echo Exception in thread main java.net.SocketException: Unexpected end of file from server\r\n"
          + "echo     at org.gradle.wrapper.Install.forceFetch(SourceFile:2)\r\n"
          + "exit /b 1\r\n"
          + ":success\r\n"
          + "> \"%~dp0second-attempt-started\" echo started\r\n"
          + "echo " + GradleProjectBuildService.LAUNCH_MARKER + "\r\n"
          + "ping 127.0.0.1 -n 30 > nul\r\n");
    } else {
      Files.writeString(wrapper, "#!/bin/sh\n"
          + "if [ ! -f \"$(dirname \"$0\")/retry-complete\" ]; then\n"
          + "  touch \"$(dirname \"$0\")/retry-complete\"\n"
          + "  sleep 1\n"
          + "  echo 'Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip'\n"
          + "  echo 'Exception in thread main java.net.SocketException: Unexpected end of file from server'\n"
          + "  echo '    at org.gradle.wrapper.Install.forceFetch(SourceFile:2)'\n"
          + "  exit 1\n"
          + "fi\n"
          + "touch \"$(dirname \"$0\")/second-attempt-started\"\n"
          + "echo '" + GradleProjectBuildService.LAUNCH_MARKER + "'\n"
          + "sleep 30\n");
    }
    return wrapper;
  }

  private static Path createFailingLaunchWrapper(Path root) throws Exception {
    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    if (isWindows()) {
      Files.writeString(wrapper, "@echo off\r\n"
          + "echo Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip\r\n"
          + "echo Exception in thread main java.net.SocketException: Unexpected end of file from server\r\n"
          + "echo     at org.gradle.wrapper.Install.forceFetch(SourceFile:2)\r\n"
          + "exit /b 1\r\n");
    } else {
      Files.writeString(wrapper, "#!/bin/sh\n"
          + "echo 'Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip'\n"
          + "echo 'Exception in thread main java.net.SocketException: Unexpected end of file from server'\n"
          + "echo '    at org.gradle.wrapper.Install.forceFetch(SourceFile:2)'\n"
          + "exit 1\n");
    }
    return wrapper;
  }
}
