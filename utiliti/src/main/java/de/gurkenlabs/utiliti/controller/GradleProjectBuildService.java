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
import java.util.Optional;
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
  static final String LAUNCH_MARKER = "UTILITI_LAUNCH_PHASE:STARTING_GAME";
  static final String WRAPPER_RETRY_MESSAGE =
      "Gradle download was interrupted; retrying automatically (2/2)...";
  private static final int MAX_WRAPPER_ATTEMPTS = 2;
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
  private final AtomicReference<Process> activeRefreshProcess = new AtomicReference<>();
  private final java.util.concurrent.atomic.AtomicBoolean refreshCancellationRequested =
      new java.util.concurrent.atomic.AtomicBoolean();

  public void cancelCurrentBuild() {
    this.refreshCancellationRequested.set(true);
    Process refresh = this.activeRefreshProcess.getAndSet(null);
    if (refresh != null && refresh.isAlive()) {
      refresh.descendants().forEach(ProcessHandle::destroyForcibly);
      refresh.destroyForcibly();
    }
    GradleProjectSession session = this.activeSession.get();
    if (session != null) {
      session.stop();
    }
  }

  boolean isRefreshingProjectModel() {
    Process process = this.activeRefreshProcess.get();
    return process != null && process.isAlive();
  }

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
    this.refreshCancellationRequested.set(false);
    ProjectModel fallback = this.resolve(projectLocation);
    if (!fallback.canRun()) return fallback;
    Path initScript = null;
    Process process = null;
    try {
      initScript = Files.createTempFile("utiliti-gradle-model-", ".gradle");
      try (var source = GradleProjectBuildService.class.getResourceAsStream("/gradle/utiliti-project-model.gradle")) {
        if (source == null) return fallback;
        Files.copy(source, initScript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }
      List<String> command = modelCommand(fallback, projectLocation, initScript);
      for (int attempt = 1; attempt <= MAX_WRAPPER_ATTEMPTS; attempt++) {
        if (this.refreshCancellationRequested.get()) return fallback;
        process = new ProcessBuilder(command)
            .directory(fallback.projectRoot().toFile())
            .redirectErrorStream(true)
            .start();
        this.activeRefreshProcess.set(process);
        final Process currentProc = process;
        StringBuffer output = new StringBuffer();
        Thread reader = Thread.ofPlatform().daemon().name("utiliti-gradle-model").start(() -> {
          try (BufferedReader lines = currentProc.inputReader(StandardCharsets.UTF_8)) {
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
        if (process.exitValue() == 0) {
          return parseModel(output.toString(), fallback);
        }
        if (attempt < MAX_WRAPPER_ATTEMPTS && isTransientWrapperDownloadFailure(output.toString())) {
          if (this.refreshCancellationRequested.get()) return fallback;
          log.info(WRAPPER_RETRY_MESSAGE);
          continue;
        }
        Optional<String> diagnosis = launchFailureMessage(output.toString(), fallback.projectRoot());
        log.warning(diagnosis.orElse(
            "Could not resolve the Gradle project model; using conventional project paths."));
        return fallback;
      }
      return fallback;
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not resolve the Gradle project model; using conventional project paths.", e);
      return fallback;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return fallback;
    } finally {
      if (process != null) {
        this.activeRefreshProcess.compareAndSet(process, null);
      }
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
    Path runInitScript = copyInitScript("/gradle/utiliti-project-run.gradle", "utiliti-gradle-run-");
    List<String> command = command(request, runInitScript, reusableArtifacts);
    ProcessBuilder builder = new ProcessBuilder(command).directory(model.projectRoot().toFile());
    builder.redirectErrorStream(true);
    builder.environment().putAll(request.environment());
    GradleProjectSession session = new GradleProjectSession(builder, runInitScript, request.mode());
    this.activeSession.set(session);
    session.onStateChanged(state -> {
      if (state == ProjectSession.State.EXITED || state == ProjectSession.State.FAILED) {
        this.activeSession.compareAndSet(session, null);
      }
    });
    try {
      session.start();
    } catch (IOException error) {
      this.activeSession.compareAndSet(session, null);
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
    return command(request, runInitScript, reusableArtifacts, isWindows());
  }

  static List<String> command(
      ProjectLaunchRequest request, Path runInitScript, List<Path> reusableArtifacts,
      boolean windows) {
    ProjectModel model = request.project();
    List<String> command = new ArrayList<>();
    appendWrapperCommand(command, model.wrapper(), windows);
    command.add("--console=plain");
    command.add("--no-daemon");
    if (request.mode() == ProjectLaunchRequest.Mode.DEBUG) {
      command.add("-Dutiliti.debugProject=true");
      String debugPort = request.environment().get("UTILITI_DEBUG_PORT");
      if (debugPort != null && !debugPort.isBlank()) command.add("-Dutiliti.debugPort=" + debugPort);
    }
    command.add("-Dutiliti.launchTask=" + model.runTask());
    if (runInitScript != null) {
      if (reusableArtifacts != null && !reusableArtifacts.isEmpty()) {
        String paths = reusableArtifacts.stream()
          .map(Path::toAbsolutePath)
          .map(Path::normalize)
          .map(Path::toString)
          .map(path -> normalizeArtifactPath(path, windows))
          .collect(java.util.stream.Collectors.joining("\n"));
        command.add("-Dutiliti.reusableArtifacts="
            + Base64.getEncoder().encodeToString(paths.getBytes(StandardCharsets.UTF_8)));
      }
      command.add("--init-script");
      command.add(runInitScript.toString());
    }
    command.add(model.runTask());
    command.addAll(request.buildArguments());
    if (!request.gameArguments().isEmpty()) {
      command.add("--args=" + String.join(" ", request.gameArguments()));
    }
    return List.copyOf(command);
  }

  static String normalizeArtifactPath(String path, boolean windows) {
    if (path == null) return "";
    return windows ? path.toLowerCase(java.util.Locale.ROOT) : path;
  }

  private static void appendWrapperCommand(List<String> command, Path wrapper, boolean windows) {
    if (windows) {
      command.add(System.getenv().getOrDefault("COMSPEC", "cmd.exe"));
      command.add("/d");
      command.add("/c");
    } else {
      // Invoking the POSIX wrapper through sh also supports projects copied from filesystems
      // that do not preserve Gradle's executable bit.
      command.add("sh");
    }
    command.add(wrapper.toString());
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

  static boolean isLaunchMarker(String line) {
    return line != null && LAUNCH_MARKER.equals(line.strip());
  }

  static boolean isTransientWrapperDownloadFailure(String output) {
    if (output == null || output.isBlank()) return false;
    String normalized = output.toLowerCase(java.util.Locale.ROOT);
    boolean wrapperDownload = normalized.contains("org.gradle.wrapper.install")
        || normalized.contains("services.gradle.org/distributions")
        || normalized.contains("could not download gradle");
    return wrapperDownload && isWrapperNetworkExceptionLine(output);
  }

  private static boolean isWrapperNetworkExceptionLine(String line) {
    if (line == null || line.isBlank()) return false;
    String normalized = line.toLowerCase(java.util.Locale.ROOT);
    return normalized.contains("socketexception")
        || normalized.contains("sockettimeoutexception")
        || normalized.contains("connectexception")
        || normalized.contains("unknownhostexception")
        || normalized.contains("sslexception")
        || normalized.contains("unexpected end of file from server");
  }

  private static boolean isWrapperDownloadLine(String line) {
    if (line == null || line.isBlank()) return false;
    String normalized = line.toLowerCase(java.util.Locale.ROOT);
    return normalized.contains("services.gradle.org/distributions")
        || normalized.contains("org.gradle.wrapper.install")
        || normalized.contains("could not download gradle");
  }

  static Optional<String> launchFailureMessage(String output, Path projectRoot) {
    if (!isTransientWrapperDownloadFailure(output)) return Optional.empty();
    String wrapperCommand = isWindows() ? "gradlew.bat --version" : "./gradlew --version";
    String location = projectRoot == null ? "the project directory" : projectRoot.toString();
    return Optional.of(
        "Gradle could not be downloaded after two attempts. Check access to services.gradle.org "
            + "and GitHub release assets, then retry. You can also run '" + wrapperCommand
            + "' once in " + location + " to install the project's Gradle version.");
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
    appendWrapperCommand(command, model.wrapper(), isWindows());
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
    private final ProjectLaunchRequest.Mode mode;
    private final List<String> output = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> outputListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<State>> stateListeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<State> state = new AtomicReference<>(State.STARTING);
    private final java.util.concurrent.atomic.AtomicBoolean stopRequested =
        new java.util.concurrent.atomic.AtomicBoolean();
    private volatile Process process;
    private volatile Integer exitCode;
    private volatile String failureMessage;

    private GradleProjectSession(ProcessBuilder builder, Path runInitScript,
                                 ProjectLaunchRequest.Mode mode) {
      this.builder = builder;
      this.runInitScript = runInitScript;
      this.mode = mode;
    }

    private void start() throws IOException {
      this.process = this.builder.start();
      this.setState(State.BUILDING);
      List<String> attemptOutput = new CopyOnWriteArrayList<>();
      Thread outputThread = Thread.ofPlatform().daemon().name("utiliti-project-output")
          .start(() -> this.readOutput(attemptOutput));
      Thread.ofPlatform().daemon().name("utiliti-project-wait")
          .start(() -> this.waitForExit(outputThread, attemptOutput));
    }

    @Override public State state() { return this.state.get(); }

    @Override
    public OptionalInt exitCode() {
      return this.exitCode == null ? OptionalInt.empty() : OptionalInt.of(this.exitCode);
    }

    @Override
    public Optional<String> failureMessage() {
      return Optional.ofNullable(this.failureMessage);
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
      if (!this.isActive()) return;
      this.stopRequested.set(true);
      Process current = this.process;
      this.setState(State.STOPPING);
      if (current == null || !current.isAlive()) return;
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

    private void readOutput(List<String> attemptOutput) {
      boolean suppressWrapperStack = false;
      boolean wrapperDownloadSeen = false;
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          attemptOutput.add(line);
          wrapperDownloadSeen |= isWrapperDownloadLine(line);
          if (isLaunchMarker(line)) {
            this.setState(this.mode == ProjectLaunchRequest.Mode.DEBUG
                ? State.STARTING_GAME : State.RUNNING);
            continue;
          }
          if (this.state() == State.BUILDING
              && wrapperDownloadSeen
              && isWrapperNetworkExceptionLine(line)) {
            suppressWrapperStack = true;
            continue;
          }
          if (suppressWrapperStack && (line.isBlank()
              || line.stripLeading().startsWith("at ")
              || line.stripLeading().startsWith("Caused by:"))) continue;
          suppressWrapperStack = false;
          this.emitOutput(line);
        }
      } catch (IOException e) {
        if (this.isActive()) {
          this.emitOutput("Could not read project output: " + e.getMessage());
        }
      }
    }

    private void waitForExit(Thread outputThread, List<String> attemptOutput) {
      try {
        int attempt = 1;
        while (true) {
          this.exitCode = this.process.waitFor();
          outputThread.join(1000);
          String capturedOutput = String.join("\n", attemptOutput);
          if (this.exitCode != 0
              && attempt < MAX_WRAPPER_ATTEMPTS
              && !this.stopRequested.get()
              && isTransientWrapperDownloadFailure(capturedOutput)) {
            attempt++;
            this.emitOutput(WRAPPER_RETRY_MESSAGE);
            if (this.stopRequested.get()) break;
            attemptOutput = new CopyOnWriteArrayList<>();
            this.process = this.builder.start();
            if (this.stopRequested.get()) {
              this.process.destroyForcibly();
              break;
            }
            List<String> retryOutput = attemptOutput;
            outputThread = Thread.ofPlatform().daemon().name("utiliti-project-output")
                .start(() -> this.readOutput(retryOutput));
            continue;
          }
          this.failureMessage = this.exitCode == 0
              ? null
              : launchFailureMessage(capturedOutput, this.builder.directory().toPath()).orElse(null);
          break;
        }
        this.setState(State.EXITED);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        this.setState(State.FAILED);
      } catch (IOException e) {
        this.failureMessage = "Could not retry the Gradle launch: " + e.getMessage();
        this.setState(State.FAILED);
      } finally {
        deleteQuietly(this.runInitScript);
      }
    }

    private void emitOutput(String line) {
      synchronized (this.output) {
        this.output.add(line);
        if (this.output.size() > 2000) this.output.removeFirst();
        for (Consumer<String> listener : this.outputListeners) listener.accept(line);
      }
    }

    private void setState(State newState) {
      this.state.set(newState);
      for (Consumer<State> listener : this.stateListeners) listener.accept(newState);
    }
  }
}
