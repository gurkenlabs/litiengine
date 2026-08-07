package de.gurkenlabs.utiliti.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/** Locates IntelliJ installations and opens ordinary project source files at a caret position. */
public final class IntellijIntegration {
  public static final String EXECUTABLE_PROPERTY = "utiliti.intellij.path";
  public static final String EXECUTABLE_ENVIRONMENT = "LITIENGINE_INTELLIJ_PATH";

  private IntellijIntegration() {}

  public static void open(Path projectRoot, Path source, int line, int column) throws IOException {
    Path target = source != null ? source.toAbsolutePath().normalize()
      : projectRoot == null ? null : projectRoot.toAbsolutePath().normalize();
    if (target == null) throw new IOException("No project or script is selected.");
    Optional<Path> executable = findExecutable();
    if (executable.isPresent()) {
      List<String> command = new ArrayList<>();
      command.add(executable.get().toString());
      if (source != null) {
        command.add("--line");
        command.add(Integer.toString(Math.max(1, line)));
        command.add("--column");
        command.add(Integer.toString(Math.max(1, column)));
      }
      command.add(target.toString());
      new ProcessBuilder(command).directory(projectRoot == null ? null : projectRoot.toFile()).start();
      return;
    }
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.EDIT) && source != null) {
      Desktop.getDesktop().edit(source.toFile());
      return;
    }
    throw new IOException("IntelliJ was not found. Set -D" + EXECUTABLE_PROPERTY + " or " + EXECUTABLE_ENVIRONMENT + ".");
  }

  public static Optional<Path> findExecutable() {
    for (String configured : List.of(System.getProperty(EXECUTABLE_PROPERTY, ""),
      System.getenv().getOrDefault(EXECUTABLE_ENVIRONMENT, ""))) {
      if (!configured.isBlank()) {
        Path candidate = Path.of(configured).toAbsolutePath().normalize();
        if (Files.isRegularFile(candidate)) return Optional.of(candidate);
      }
    }

    List<Path> conventional = new ArrayList<>();
    String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    if (os.contains("win")) {
      addWindowsInstallations(conventional, System.getenv("LOCALAPPDATA"));
      addWindowsInstallations(conventional, System.getenv("PROGRAMFILES"));
    } else if (os.contains("mac")) {
      conventional.add(Path.of("/Applications/IntelliJ IDEA.app/Contents/MacOS/idea"));
      conventional.add(Path.of(System.getProperty("user.home"), "Applications/IntelliJ IDEA.app/Contents/MacOS/idea"));
    } else {
      conventional.add(Path.of("/opt/idea/bin/idea.sh"));
      conventional.add(Path.of(System.getProperty("user.home"), ".local/share/JetBrains/Toolbox/apps"));
    }
    return conventional.stream().filter(Files::isRegularFile).findFirst()
      .or(() -> findToolboxExecutable(os));
  }

  private static void addWindowsInstallations(List<Path> candidates, String root) {
    if (root == null || root.isBlank()) return;
    candidates.add(Path.of(root, "Programs", "IntelliJ IDEA Ultimate", "bin", "idea64.exe"));
    candidates.add(Path.of(root, "Programs", "IntelliJ IDEA Community Edition", "bin", "idea64.exe"));
    candidates.add(Path.of(root, "JetBrains", "IntelliJ IDEA", "bin", "idea64.exe"));
  }

  private static Optional<Path> findToolboxExecutable(String os) {
    Path root = os.contains("win")
      ? Path.of(System.getenv().getOrDefault("LOCALAPPDATA", System.getProperty("user.home")), "JetBrains", "Toolbox", "apps")
      : os.contains("mac")
        ? Path.of(System.getProperty("user.home"), "Library", "Application Support", "JetBrains", "Toolbox", "apps")
        : Path.of(System.getProperty("user.home"), ".local", "share", "JetBrains", "Toolbox", "apps");
    if (!Files.isDirectory(root)) return Optional.empty();
    String fileName = os.contains("win") ? "idea64.exe" : os.contains("mac") ? "idea" : "idea.sh";
    try (Stream<Path> paths = Files.find(root, 8,
      (path, attributes) -> attributes.isRegularFile() && path.getFileName().toString().equalsIgnoreCase(fileName))) {
      return paths.sorted(Comparator.comparingLong(IntellijIntegration::lastModified).reversed()).findFirst();
    } catch (IOException ignored) {
      return Optional.empty();
    }
  }

  private static long lastModified(Path path) {
    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException ignored) {
      return 0;
    }
  }
}
