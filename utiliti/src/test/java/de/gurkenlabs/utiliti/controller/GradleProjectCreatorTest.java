package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
  }

  @Test
  void createsKotlinDslFiles(@TempDir Path parent) throws Exception {
    GradleProjectCreator.Options options = new GradleProjectCreator.Options(
      parent, "demo", "Demo", "1.0.0", "org.example.demo", "0.12.0", GradleProjectCreator.BuildScript.KOTLIN);

    new GradleProjectCreator().create(options);

    assertTrue(Files.isRegularFile(parent.resolve("demo/build.gradle.kts")));
    assertTrue(Files.isRegularFile(parent.resolve("demo/settings.gradle.kts")));
    assertTrue(Files.readString(parent.resolve("demo/build.gradle.kts"))
      .contains("implementation(\"de.gurkenlabs:litiengine:0.12.0\")"));
    assertTrue(Files.readString(parent.resolve("demo/build.gradle.kts"))
      .contains("mavenLocal()"));
    assertTrue(Files.readString(parent.resolve("demo/build.gradle.kts"))
      .contains("mavenCentral()"));
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
    assertThrows(IllegalArgumentException.class, () -> new GradleProjectCreator.Options(
      parent, "demo:game", "Demo", "1.0.0", "org.example.demo", "0.12.0", GradleProjectCreator.BuildScript.GROOVY));
  }

  @Test
  void rejectsInvalidJavaNamespace(@TempDir Path parent) {
    assertThrows(IllegalArgumentException.class, () -> new GradleProjectCreator.Options(
      parent, "demo", "Demo", "1.0.0", "org.example.class", "0.12.0", GradleProjectCreator.BuildScript.GROOVY));
  }
}
