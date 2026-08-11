package de.gurkenlabs.utiliti.controller;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Gradle-wrapper implementation of project discovery and external application launching. */
public final class GradleProjectBuildService implements ProjectBuildService {
  private static final Logger log = Logger.getLogger(GradleProjectBuildService.class.getName());
  private static final String MODEL_MARKER = "UTILITI_PROJECT_MODEL:";
  private static final Pattern MAIN_CLASS = Pattern.compile(
      "(?m)\\bmainClass(?:Name)?\\s*=\\s*['\"]([^'\"]+)['\"]");
  private static final Pattern TOOLCHAIN_VERSION = Pattern.compile(
      "(?m)\\blanguageVersion\\s*=\\s*JavaLanguageVersion\\.of\\((\\d+)\\)");
  private static final Pattern SOURCE_VERSION = Pattern.compile(
      "(?m)\\bsourceCompatibility\\s*=\\s*(?:JavaVersion\\.VERSION_)?['\"]?(\\d+)");
  private static final List<Path> OUTPUT_DIRECTORIES = List.of(
      Path.of("build", "classes", "java", "main"),
      Path.of("build", "classes", "kotlin", "main"),
      Path.of("build", "classes", "groovy", "main"),
      Path.of("build", "resources", "main"),
      Path.of("bin", "main"),
      Path.of("bin"));

  private final AtomicReference<GradleProjectSession> activeSession = new AtomicReference<>();

  @Override
  public ProjectModel resolve(Path projectLocation) {
    Path root = findProjectRoot(projectLocation);
    if (root == null) {
      return new ProjectModel(null, null, ":run", null, Runtime.version().feature(),
          List.of(), List.of(), List.of(), List.of());
    }

    Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
    if (!Files.isRegularFile(wrapper)) wrapper = null;
    List<Path> sourceRoots = List.of(root.resolve("src/main/java"), root.resolve("src"))
        .stream().filter(Files::isDirectory).distinct().toList();
    List<Path> outputs = OUTPUT_DIRECTORIES.stream().map(root::resolve).filter(Files::isDirectory).toList();
    String mainClass = readMainClass(root);
    return new ProjectModel(root, wrapper, ":run", mainClass, readJavaVersion(root),
        sourceRoots, outputs, outputs, outputs);
  }

  @Override
  public ProjectModel refresh(Path projectLocation) {
    ProjectModel fallback = this.resolve(projectLocation);
    if (!fallback.canRun()) return fallback;
    Path initScript = null;
    try {
      initScript = Files.createTempFile("utiliti-gradle-model-", ".gradle");
      try (var source = GradleProjectBuildService.class.getResourceAsStream("/gradle/utiliti-project-model.gradle")) {
        if (source == null) return fallback;
        Files.copy(source, initScript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }
      List<String> command = modelCommand(fallback, projectLocation, initScript);
      Process process = new ProcessBuilder(command)
          .directory(fallback.projectRoot().toFile())
          .redirectErrorStream(true)
          .start();
      StringBuilder output = new StringBuilder();
      Thread reader = Thread.ofPlatform().daemon().name("utiliti-gradle-model").start(() -> {
        try (BufferedReader lines = process.inputReader(StandardCharsets.UTF_8)) {
          String line;
          while ((line = lines.readLine()) != null) output.append(line).append('\n');
        } catch (IOException ignored) {
        }
      });
      if (!process.waitFor(60, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        log.warning("Timed out while resolving the Gradle project model; using conventional project paths.");
        return fallback;
      }
      reader.join(1000);
      if (process.exitValue() != 0) {
        log.log(Level.WARNING, "Could not resolve the Gradle project model; using conventional project paths. {0}", output);
        return fallback;
      }
      return parseModel(output.toString(), fallback);
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not resolve the Gradle project model; using conventional project paths.", e);
      return fallback;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return fallback;
    } finally {
      if (initScript != null) {
        try {
          Files.deleteIfExists(initScript);
        } catch (IOException ignored) {
        }
      }
    }
  }

  @Override
  public ProjectSession launch(ProjectLaunchRequest request) throws IOException {
    ProjectModel model = request.project();
    if (!model.canRun()) throw new IOException("No Gradle wrapper was found for this project.");
    GradleProjectSession existing = this.activeSession.get();
    if (existing != null && existing.isActive()) throw new IOException("The project is already running.");

    List<Path> reusableArtifacts = reusableArtifacts(
        model, System.getProperty("java.class.path", ""));
    Path runInitScript = reusableArtifacts.isEmpty()
        ? null
        : copyInitScript("/gradle/utiliti-project-run.gradle", "utiliti-gradle-run-");
    List<String> command = command(request, runInitScript, reusableArtifacts);
    ProcessBuilder builder = new ProcessBuilder(command).directory(model.projectRoot().toFile());
    builder.redirectErrorStream(true);
    builder.environment().putAll(request.environment());
    GradleProjectSession session = new GradleProjectSession(builder, runInitScript);
    this.activeSession.set(session);
    session.onStateChanged(state -> {
      if (state == ProjectSession.State.EXITED || state == ProjectSession.State.FAILED) {
        this.activeSession.compareAndSet(session, null);
      }
    });
    try {
      session.start();
    } catch (IOException error) {
      deleteQuietly(runInitScript);
      throw error;
    }
    return session;
  }

  @Override
  public void close() {
    GradleProjectSession session = this.activeSession.getAndSet(null);
    if (session != null) session.stop();
  }

  static Path findProjectRoot(Path location) {
    if (location == null) return null;
    Path current = location.toAbsolutePath().normalize();
    if (!Files.isDirectory(current)) current = current.getParent();
    Path buildRoot = null;
    while (current != null) {
      if (Files.isRegularFile(current.resolve("gradlew"))
          || Files.isRegularFile(current.resolve("gradlew.bat"))) {
        return current;
      }
      if (buildRoot == null && (Files.isRegularFile(current.resolve("settings.gradle"))
          || Files.isRegularFile(current.resolve("settings.gradle.kts"))
          || Files.isRegularFile(current.resolve("build.gradle"))
          || Files.isRegularFile(current.resolve("build.gradle.kts")))) {
        buildRoot = current;
      }
      current = current.getParent();
    }
    return buildRoot;
  }

  static List<String> command(ProjectLaunchRequest request) {
    return command(request, null, List.of());
  }

  static List<String> command(
      ProjectLaunchRequest request, Path runInitScript, List<Path> reusableArtifacts) {
    ProjectModel model = request.project();
    List<String> command = new ArrayList<>();
    if (isWindows()) {
      command.add(System.getenv().getOrDefault("COMSPEC", "cmd.exe"));
      command.add("/d");
      command.add("/c");
      command.add(model.wrapper().toString());
    } else {
      command.add(model.wrapper().toString());
    }
    command.add("--console=plain");
    command.add("--no-daemon");
    if (runInitScript != null && reusableArtifacts != null && !reusableArtifacts.isEmpty()) {
      String paths = reusableArtifacts.stream()
          .map(Path::toAbsolutePath)
          .map(Path::normalize)
          .map(Path::toString)
          .map(path -> path.toLowerCase(java.util.Locale.ROOT))
          .collect(java.util.stream.Collectors.joining("\n"));
      command.add("-Dutiliti.reusableArtifacts="
          + Base64.getEncoder().encodeToString(paths.getBytes(StandardCharsets.UTF_8)));
      command.add("--init-script");
      command.add(runInitScript.toString());
    }
    command.add(model.runTask());
    if (request.mode() == ProjectLaunchRequest.Mode.DEBUG) command.add("--debug-jvm");
    command.addAll(request.buildArguments());
    if (!request.gameArguments().isEmpty()) {
      command.add("--args=" + String.join(" ", request.gameArguments()));
    }
    return List.copyOf(command);
  }

  static List<Path> reusableArtifacts(ProjectModel model, String processClasspath) {
    if (model == null || processClasspath == null || processClasspath.isBlank()) return List.of();
    var loadedArtifacts = List.of(processClasspath.split(Pattern.quote(java.io.File.pathSeparator)))
        .stream()
        .filter(entry -> !entry.isBlank())
        .map(Path::of)
        .map(Path::toAbsolutePath)
        .map(Path::normalize)
        .collect(java.util.stream.Collectors.toSet());
    return model.runtimeClasspath().stream()
        .filter(loadedArtifacts::contains)
        .distinct()
        .toList();
  }

  static ProjectModel parseModel(String output, ProjectModel fallback) {
    if (output == null) return fallback;
    for (String line : output.lines().toList()) {
      int marker = line.indexOf(MODEL_MARKER);
      if (marker < 0) continue;
      try {
        String encoded = line.substring(marker + MODEL_MARKER.length()).strip();
        String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        try (var reader = Json.createReader(new StringReader(json))) {
          JsonObject model = reader.readObject();
          return new ProjectModel(
              Path.of(model.getString("projectRoot", fallback.projectRoot().toString())),
              fallback.wrapper(),
              model.getString("runTask", fallback.runTask()),
              nullableString(model, "mainClass", fallback.mainClass()),
              model.getInt("javaVersion", fallback.javaVersion()),
              paths(model.getJsonArray("sourceRoots")),
              paths(model.getJsonArray("outputDirectories")),
              paths(model.getJsonArray("compileClasspath")),
              paths(model.getJsonArray("runtimeClasspath")));
        }
      } catch (RuntimeException e) {
        log.log(Level.WARNING, "Could not parse the Gradle project model; using conventional project paths.", e);
        return fallback;
      }
    }
    return fallback;
  }

  private static List<String> modelCommand(
      ProjectModel model, Path projectLocation, Path initScript) {
    List<String> command = new ArrayList<>();
    if (isWindows()) {
      command.add(System.getenv().getOrDefault("COMSPEC", "cmd.exe"));
      command.add("/d");
      command.add("/c");
    }
    command.add(model.wrapper().toString());
    command.add("--console=plain");
    command.add("--no-daemon");
    command.add("--quiet");
    command.add(
        "-PutilitiProjectLocation="
            + projectLocation.toAbsolutePath().normalize());
    command.add("--init-script");
    command.add(initScript.toString());
    command.add("utilitiProjectModel");
    return command;
  }

  private static Path copyInitScript(String resourceName, String prefix) throws IOException {
    Path script = Files.createTempFile(prefix, ".gradle");
    try (var source = GradleProjectBuildService.class.getResourceAsStream(resourceName)) {
      if (source == null) throw new IOException("Missing Gradle init script: " + resourceName);
      Files.copy(source, script, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      return script;
    } catch (IOException error) {
      deleteQuietly(script);
      throw error;
    }
  }

  private static void deleteQuietly(Path file) {
    if (file == null) return;
    try {
      Files.deleteIfExists(file);
    } catch (IOException ignored) {
    }
  }

  private static List<Path> paths(JsonArray values) {
    if (values == null) return List.of();
    return values.getValuesAs(value -> Path.of(((jakarta.json.JsonString) value).getString()));
  }

  private static String nullableString(JsonObject object, String name, String fallback) {
    return object.isNull(name) ? fallback : object.getString(name, fallback);
  }

  private static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
  }

  private static String readMainClass(Path root) {
    String buildSource = readBuildSource(root);
    if (buildSource == null) return null;
    Matcher matcher = MAIN_CLASS.matcher(buildSource);
    return matcher.find() ? matcher.group(1) : null;
  }

  private static int readJavaVersion(Path root) {
    String buildSource = readBuildSource(root);
    if (buildSource == null) return Runtime.version().feature();
    for (Pattern pattern : List.of(TOOLCHAIN_VERSION, SOURCE_VERSION)) {
      Matcher matcher = pattern.matcher(buildSource);
      if (matcher.find()) return Integer.parseInt(matcher.group(1));
    }
    return Runtime.version().feature();
  }

  private static String readBuildSource(Path root) {
    for (String name : List.of("build.gradle", "build.gradle.kts")) {
      Path buildFile = root.resolve(name);
      if (!Files.isRegularFile(buildFile)) continue;
      try {
        return Files.readString(buildFile);
      } catch (IOException ignored) {
      }
    }
    return null;
  }

  private static final class GradleProjectSession implements ProjectSession {
    private final ProcessBuilder builder;
    private final Path runInitScript;
    private final List<String> output = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> outputListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<State>> stateListeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<State> state = new AtomicReference<>(State.STARTING);
    private volatile Process process;
    private volatile Integer exitCode;

    private GradleProjectSession(ProcessBuilder builder, Path runInitScript) {
      this.builder = builder;
      this.runInitScript = runInitScript;
    }

    private void start() throws IOException {
      this.process = this.builder.start();
      this.setState(State.RUNNING);
      Thread outputThread = Thread.ofPlatform().daemon().name("utiliti-project-output").start(this::readOutput);
      Thread.ofPlatform().daemon().name("utiliti-project-wait").start(() -> this.waitForExit(outputThread));
    }

    @Override public State state() { return this.state.get(); }

    @Override
    public OptionalInt exitCode() {
      return this.exitCode == null ? OptionalInt.empty() : OptionalInt.of(this.exitCode);
    }

    @Override
    public void writeInput(String input) throws IOException {
      Process current = this.process;
      if (current == null || !current.isAlive()) throw new IOException("The project is not running.");
      Writer writer = new OutputStreamWriter(current.getOutputStream(), StandardCharsets.UTF_8);
      writer.write(input == null ? "" : input);
      writer.flush();
    }

    @Override
    public void stop() {
      Process current = this.process;
      if (current == null || !current.isAlive()) return;
      this.setState(State.STOPPING);
      List<ProcessHandle> descendants = current.descendants().toList();
      descendants.forEach(ProcessHandle::destroy);
      current.destroy();
      Thread.ofPlatform().daemon().name("utiliti-project-stop").start(() -> {
        try {
          if (!current.waitFor(3, TimeUnit.SECONDS)) {
            current.destroyForcibly();
          }
          descendants.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    @Override
    public void onOutput(Consumer<String> listener) {
      if (listener == null) return;
      synchronized (this.output) {
        this.outputListeners.add(listener);
        this.output.forEach(listener);
      }
    }

    @Override
    public void onStateChanged(Consumer<State> listener) {
      if (listener == null) return;
      this.stateListeners.add(listener);
      listener.accept(this.state());
    }

    private void readOutput() {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          synchronized (this.output) {
            this.output.add(line);
            if (this.output.size() > 2000) this.output.removeFirst();
            for (Consumer<String> listener : this.outputListeners) listener.accept(line);
          }
        }
      } catch (IOException e) {
        if (this.isActive()) {
          String message = "Could not read project output: " + e.getMessage();
          this.output.add(message);
          for (Consumer<String> listener : this.outputListeners) listener.accept(message);
        }
      }
    }

    private void waitForExit(Thread outputThread) {
      try {
        this.exitCode = this.process.waitFor();
        outputThread.join(1000);
        this.setState(State.EXITED);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        this.setState(State.FAILED);
      } finally {
        deleteQuietly(this.runInitScript);
      }
    }

    private void setState(State newState) {
      this.state.set(newState);
      for (Consumer<State> listener : this.stateListeners) listener.accept(newState);
    }
  }
}
