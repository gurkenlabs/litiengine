package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;

/** Creates the on-disk Gradle project opened by utiLITI's new-project dialog. */
public final class GradleProjectCreator {
  private static final String TEMPLATE_ROOT = "/project-template/";
  private static final List<String> WRAPPER_FILES = List.of(
    "gradlew",
    "gradlew.bat",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties");

  public enum BuildScript {
    GROOVY("Groovy DSL", "build.gradle", "settings.gradle"),
    KOTLIN("Kotlin DSL", "build.gradle.kts", "settings.gradle.kts");

    private final String displayName;
    private final String buildFile;
    private final String settingsFile;

    BuildScript(String displayName, String buildFile, String settingsFile) {
      this.displayName = displayName;
      this.buildFile = buildFile;
      this.settingsFile = settingsFile;
    }

    public String buildFile() {
      return this.buildFile;
    }

    public String settingsFile() {
      return this.settingsFile;
    }

    @Override
    public String toString() {
      return this.displayName;
    }
  }

  public record Options(
      Path location,
      String projectName,
      String gameName,
      String gameVersion,
      String namespace,
      String engineVersion,
      BuildScript buildScript) {
    public Options {
      if (location == null) {
        throw new IllegalArgumentException("A project location is required.");
      }
      projectName = requireValue(projectName, "A project name is required.");
      gameName = requireValue(gameName, "A game name is required.");
      gameVersion = requireValue(gameVersion, "A game version is required.");
      namespace = requireValue(namespace, "A Java namespace is required.");
      engineVersion = requireValue(engineVersion, "A LITIENGINE version is required.");
      if (projectName.equals(".") || projectName.equals("..") || projectName.matches(".*[<>:\"/\\\\|?*].*")
          || projectName.endsWith(".") || projectName.endsWith(" ")) {
        throw new IllegalArgumentException("The project name must be a single directory name.");
      }
      if (!SourceVersion.isName(namespace)) {
        throw new IllegalArgumentException("The namespace must be a valid Java package name.");
      }
      if (buildScript == null) {
        throw new IllegalArgumentException("A Gradle build script type is required.");
      }
      location = location.toAbsolutePath().normalize();
    }

    public Path projectRoot() {
      return this.location.resolve(this.projectName).normalize();
    }

    public String packageName() {
      return this.namespace;
    }

    private static String requireValue(String value, String message) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(message);
      }
      return value.trim();
    }
  }

  public Path create(Options options) throws IOException {
    Path root = options.projectRoot();
    if (Files.exists(root)) {
      throw new IOException("The project directory already exists: " + root);
    }
    Files.createDirectories(options.location());
    Path staging = Files.createTempDirectory(options.location(), ".utiliti-project-");
    try {
      writeProject(staging, options);
      try {
        Files.move(staging, root, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException exception) {
        Files.move(staging, root);
      }
      return root.resolve("game." + ResourceBundle.FILE_EXTENSION);
    } catch (IOException | RuntimeException exception) {
      deleteRecursively(staging);
      throw exception;
    }
  }

  public static List<String> preview(Options options) {
    String source = "src/main/java/" + options.packageName().replace('.', '/') + "/Main.java";
    return List.of(
      options.projectName() + "/",
      "  " + options.buildScript().settingsFile(),
      "  " + options.buildScript().buildFile(),
      "  gradle.properties",
      "  gradlew",
      "  gradlew.bat",
      "  gradle/wrapper/gradle-wrapper.jar",
      "  gradle/wrapper/gradle-wrapper.properties",
      "  game." + ResourceBundle.FILE_EXTENSION,
      "  " + source,
      "  .gitignore");
  }

  private static void writeProject(Path root, Options options) throws IOException {
    Files.writeString(root.resolve(options.buildScript().settingsFile()), settings(options));
    Files.writeString(root.resolve(options.buildScript().buildFile()), build(options));
    Files.writeString(root.resolve("gradle.properties"), "org.gradle.jvmargs=-Xmx1g\n");
    Files.writeString(root.resolve(".gitignore"), ".gradle/\nbuild/\nout/\n.idea/\n*.iml\n");

    Path main = root.resolve("src/main/java")
      .resolve(options.packageName().replace('.', '/'))
      .resolve("Main.java");
    Files.createDirectories(main.getParent());
    Files.writeString(main, mainSource(options));

    for (String wrapperFile : WRAPPER_FILES) {
      copyTemplate(root, wrapperFile);
    }
    makeExecutable(root.resolve("gradlew"));

    Path resourceFile = root.resolve("game." + ResourceBundle.FILE_EXTENSION);
    new ResourceBundle().save(resourceFile.toString(), false);
    if (!Files.isRegularFile(resourceFile)) {
      throw new IOException("Could not create " + resourceFile.getFileName());
    }
  }

  private static String settings(Options options) {
    String name = escape(options.projectName());
    return options.buildScript() == BuildScript.KOTLIN
      ? "rootProject.name = \"" + name + "\"\n"
      : "rootProject.name = '" + name + "'\n";
  }

  private static String build(Options options) {
    String version = escape(options.engineVersion());
    String projectVersion = escape(options.gameVersion());
    String group = escape(options.namespace());
    String mainClass = options.packageName() + ".Main";
    int javaVersion = Runtime.version().feature();
    if (options.buildScript() == BuildScript.KOTLIN) {
      return """
        plugins {
          application
        }

        group = "%s"
        version = "%s"

        repositories {
          mavenLocal()
          mavenCentral()
        }

        dependencies {
          implementation("de.gurkenlabs:litiengine:%s")
        }

        java {
          toolchain {
            languageVersion = JavaLanguageVersion.of(%d)
          }
        }

        application {
          mainClass = "%s"
        }

        tasks.named<JavaExec>("run") {
          workingDir = projectDir
        }
        """.formatted(group, projectVersion, version, javaVersion, mainClass);
    }
    return """
      plugins {
        id 'application'
      }

      group = '%s'
      version = '%s'

      repositories {
        mavenLocal()
        mavenCentral()
      }

      dependencies {
        implementation 'de.gurkenlabs:litiengine:%s'
      }

      java {
        toolchain {
          languageVersion = JavaLanguageVersion.of(%d)
        }
      }

      application {
        mainClass = '%s'
      }

      tasks.named('run', JavaExec) {
        workingDir = projectDir
      }
      """.formatted(group, projectVersion, version, javaVersion, mainClass);
  }

  private static String mainSource(Options options) {
    return """
      package %s;

      import de.gurkenlabs.litiengine.Game;
      import de.gurkenlabs.litiengine.resources.Resources;

      public final class Main {
        private Main() {}

        public static void main(String[] args) {
          Game.info().setName("%s");
          Game.info().setVersion("%s");
          Game.init(args);
          Resources.load("game.litidata");
          Game.start();
        }
      }
      """.formatted(options.packageName(), escapeJava(options.gameName()), escapeJava(options.gameVersion()));
  }

  private static String escapeJava(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"")
      .replace("\r", "\\r").replace("\n", "\\n");
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'");
  }

  private static void copyTemplate(Path root, String relativePath) throws IOException {
    Path target = root.resolve(relativePath);
    Files.createDirectories(target.getParent());
    try (InputStream input = GradleProjectCreator.class.getResourceAsStream(TEMPLATE_ROOT + relativePath)) {
      if (input == null) {
        throw new IOException("Missing bundled Gradle wrapper file: " + relativePath);
      }
      Files.copy(input, target);
    }
  }

  private static void makeExecutable(Path wrapper) {
    try {
      Set<PosixFilePermission> permissions = EnumSet.copyOf(Files.getPosixFilePermissions(wrapper));
      permissions.add(PosixFilePermission.OWNER_EXECUTE);
      permissions.add(PosixFilePermission.GROUP_EXECUTE);
      permissions.add(PosixFilePermission.OTHERS_EXECUTE);
      Files.setPosixFilePermissions(wrapper, permissions);
    } catch (IOException | UnsupportedOperationException ignored) {
      // Windows and other non-POSIX file systems do not expose executable permission bits.
    }
  }

  private static void deleteRecursively(Path root) {
    if (root == null || !Files.exists(root)) {
      return;
    }
    try (var paths = Files.walk(root)) {
      paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
      });
    } catch (IOException ignored) {
    }
  }
}
