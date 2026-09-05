package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GradleProjectCreatorTest {
  @Test
  void createsRunnableGroovyProject(@TempDir Path parent) throws Exception {
    GradleProjectCreator.Options options = new GradleProjectCreator.Options(
      parent, "my-game", "My Game", "1.4.0", "com.example.mygame", "0.12.0", GradleProjectCreator.BuildScript.GROOVY);

    Path gameFile = new GradleProjectCreator().create(options);
    Path root = parent.resolve("my-game");

    assertEquals(root.resolve("game.litidata"), gameFile);
    assertTrue(Files.isRegularFile(root.resolve("gradlew")));
    assertTrue(Files.isRegularFile(root.resolve("gradlew.bat")));
    assertTrue(Files.isRegularFile(root.resolve("gradle/wrapper/gradle-wrapper.jar")));
    assertTrue(Files.readString(root.resolve("build.gradle"))
      .contains("implementation 'de.gurkenlabs:litiengine:0.12.0'"));
    assertTrue(Files.readString(root.resolve("build.gradle"))
      .contains("mavenLocal()"));
    assertTrue(Files.readString(root.resolve("build.gradle"))
      .contains("mavenCentral()"));
    assertTrue(Files.readString(root.resolve("build.gradle"))
      .contains("mainClass = 'com.example.mygame.Main'"));
    assertTrue(Files.readString(root.resolve("build.gradle")).contains("group = 'com.example.mygame'"));
    assertTrue(Files.readString(root.resolve("build.gradle")).contains("version = '1.4.0'"));
    assertTrue(Files.isRegularFile(root.resolve("src/main/java/com/example/mygame/Main.java")));
    String mainSource = Files.readString(root.resolve("src/main/java/com/example/mygame/Main.java"));
    assertTrue(mainSource.contains("Game.info().setName(\"My Game\")"));
    assertTrue(mainSource.contains("Game.info().setVersion(\"1.4.0\")"));
    assertTrue(mainSource.contains("Resources.load(\"game.litidata\")"));
    assertTrue(mainSource.contains("Game.start()"));
    assertTrue(ResourceBundle.load(gameFile.toString()) != null);

    try (GradleProjectBuildService service = new GradleProjectBuildService()) {
      ProjectModel model = service.resolve(gameFile);
      assertEquals(root.toAbsolutePath().normalize(), model.projectRoot());
      assertTrue(model.canRun());
    }

    assertGradleEvaluates(root);
  }

  @Test
  void createsKotlinDslFilesAndEscapesInterpolation(@TempDir Path parent) throws Exception {
    GradleProjectCreator.Options options = new GradleProjectCreator.Options(
      parent, "my$game", "My Game", "1.0$preview", "org.example.demo", "0.12.0", GradleProjectCreator.BuildScript.KOTLIN);

    Path gameFile = new GradleProjectCreator().create(options);
    Path root = parent.resolve("my$game");

    assertEquals(root.resolve("game.litidata"), gameFile);
    assertTrue(Files.isRegularFile(root.resolve("build.gradle.kts")));
    assertTrue(Files.isRegularFile(root.resolve("settings.gradle.kts")));
    assertTrue(Files.readString(root.resolve("settings.gradle.kts"))
      .contains("rootProject.name = \"my\\$game\""));
    assertTrue(Files.readString(root.resolve("build.gradle.kts"))
      .contains("version = \"1.0\\$preview\""));
    assertTrue(Files.readString(root.resolve("build.gradle.kts"))
      .contains("implementation(\"de.gurkenlabs:litiengine:0.12.0\")"));
    assertTrue(Files.readString(root.resolve("build.gradle.kts"))
      .contains("mavenLocal()"));
    assertTrue(Files.readString(root.resolve("build.gradle.kts"))
      .contains("mavenCentral()"));

    assertGradleEvaluates(root);
  }

  @Test
  void refusesToOverwriteExistingDirectory(@TempDir Path parent) throws Exception {
    Files.createDirectory(parent.resolve("demo"));
    GradleProjectCreator.Options options = new GradleProjectCreator.Options(
      parent, "demo", "Demo", "1.0.0", "org.example.demo", "0.12.0", GradleProjectCreator.BuildScript.GROOVY);

    assertThrows(IOException.class, () -> new GradleProjectCreator().create(options));
  }

  @Test
  void rejectsNamesThatCannotBePortableDirectoryNames(@TempDir Path parent) {
    List<String> invalidNames = List.of(
      "demo:game", "demo/game", "demo\\game", "demo<game", "demo>game",
      "demo\"game", "demo|game", "demo?game", "demo*game",
      "CON", "con", "PRN", "prn", "AUX", "aux", "NUL", "nul",
      "COM1", "com1", "COM9", "com9", "LPT1", "lpt1", "LPT9", "lpt9",
      "con.txt", "aux.dir", "NUL.zip", "com1.log",
      " game", "game ", ".game", "game.", ".", "..",
      "demo\u0000game", "demo\u001fgame", "demo\u007fgame",
      "a".repeat(256));

    for (String invalidName : invalidNames) {
      org.junit.jupiter.api.Assertions.assertFalse(
        GradleProjectCreator.isValidPortableDirectoryName(invalidName),
        "Expected invalid portable directory name for: " + invalidName);
      assertThrows(
        IllegalArgumentException.class,
        () -> new GradleProjectCreator.Options(
          parent, invalidName, "Demo", "1.0.0", "org.example.demo", "0.12.0", GradleProjectCreator.BuildScript.GROOVY),
        "Expected Options constructor to reject: " + invalidName);
    }
  }

  @Test
  void acceptsValidPortableDirectoryNames() {
    List<String> validNames = List.of(
      "my-game", "game_project", "Game123", "my$game", "game (edition)", "game-2.0-final");

    for (String validName : validNames) {
      assertTrue(
        GradleProjectCreator.isValidPortableDirectoryName(validName),
        "Expected valid portable directory name for: " + validName);
    }
  }

  @Test
  void rejectsInvalidJavaNamespace(@TempDir Path parent) {
    assertThrows(IllegalArgumentException.class, () -> new GradleProjectCreator.Options(
      parent, "demo", "Demo", "1.0.0", "org.example.class", "0.12.0", GradleProjectCreator.BuildScript.GROOVY));
  }

  private static void assertGradleEvaluates(Path root) throws Exception {
    boolean windows = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    List<String> cmd = new java.util.ArrayList<>();
    if (windows) {
      cmd.add(System.getenv().getOrDefault("COMSPEC", "cmd.exe"));
      cmd.add("/d");
      cmd.add("/c");
      cmd.add(root.resolve("gradlew.bat").toString());
    } else {
      cmd.add("sh");
      cmd.add(root.resolve("gradlew").toString());
    }
    cmd.add("help");
    cmd.add("--offline");
    cmd.add("--no-daemon");

    Process process = new ProcessBuilder(cmd)
        .directory(root.toFile())
        .redirectErrorStream(true)
        .start();
    String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
    assertTrue(finished, "Gradle process timed out: " + output);
    assertEquals(0, process.exitValue(), "Gradle failed with: " + output);
  }
}
