package de.gurkenlabs.litiengine.launch;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** Standalone launcher to bootstrap and run LITIengine games and script-based projects without custom Java entry points. */
public final class GameLauncher {
  private static final Logger log = Logger.getLogger(GameLauncher.class.getName());
  private static final java.util.regex.Pattern PACKAGE_DECLARATION = java.util.regex.Pattern.compile(
    "(?m)^\\s*package\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\s*;");
  private static final java.util.regex.Pattern SCRIPT_INFO = java.util.regex.Pattern.compile(
    "@ScriptInfo\\s*\\((.*?)\\)", java.util.regex.Pattern.DOTALL);
  private static final java.util.regex.Pattern SCRIPT_HOST = java.util.regex.Pattern.compile(
    "\\bhost\\s*=\\s*ScriptHostType\\.(GAME|ENVIRONMENT|ENTITY)");
  private static final java.util.regex.Pattern SCRIPT_TARGET = java.util.regex.Pattern.compile(
    "\\btarget\\s*=\\s*([A-Za-z_$][\\w$.]*)\\s*\\.class");
  private static final java.util.regex.Pattern IMPORT = java.util.regex.Pattern.compile(
    "(?m)^\\s*import\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\s*;");

  private GameLauncher() {}

  public static void main(String[] args) {
    launch(args);
  }

  public static void launch(String... args) {
    launch(null, args);
  }

  public static void launch(Path explicitProjectRoot, String... args) {
    LaunchOptions options = parseArgs(explicitProjectRoot, args);
    if (options.helpRequested) {
      printHelp();
      return;
    }
    if (options.versionRequested) {
      System.out.println("LITIengine " + Game.info().getVersion());
      return;
    }
    prepare(explicitProjectRoot, args);
    Game.start();
  }

  public static void prepare(Path explicitProjectRoot, String... args) {
    LaunchOptions options = parseArgs(explicitProjectRoot, args);

    if (options.title != null && !options.title.isBlank()) {
      Game.info().setName(options.title);
    }

    Game.init(args);
    Game.scripts().setEnabled(true);
    Game.physics().setEnabled(true);
    if (options.renderScale > 0) {
      Game.graphics().setBaseRenderScale(options.renderScale);
    }
    if (options.gravity != null) {
      Game.world().setGravity(options.gravity);
    }

    Path root = options.projectRoot != null ? options.projectRoot : Path.of(".").toAbsolutePath().normalize();
    Game.scripts().setProjectRoot(root);
    if (options.javaRelease > 0) {
      Game.scripts().setProjectJavaVersion(options.javaRelease);
    }

    loadProjectResources(root, options);
    discoverProjectScripts(root, options);

    if (options.startupScript != null && !options.startupScript.isBlank()) {
      ScriptBinding binding = new ScriptBinding(options.startupScript);
      binding.setEnabled(true);
      binding.setOrder(0);
      Game.scripts().setGameBindings(List.of(binding));
    } else {
      // Auto-detect startup game scripts if none explicitly provided
      List<ScriptBinding> autoGameBindings = new ArrayList<>(Game.scripts().getGameBindings());
      for (ScriptDefinition def : Game.scripts().getDefinitions()) {
        if (def.getHost() == ScriptHostType.GAME && autoGameBindings.stream().noneMatch(b -> b.getScript().equalsIgnoreCase(def.getId()))) {
          ScriptBinding binding = new ScriptBinding(def.getId());
          binding.setEnabled(true);
          autoGameBindings.add(binding);
        }
      }
      if (!autoGameBindings.isEmpty()) {
        Game.scripts().setGameBindings(autoGameBindings);
      }
    }

    if (options.startupMap != null && !options.startupMap.isBlank()) {
      Game.world().loadEnvironment(options.startupMap);
    } else if (Game.world().environment() == null) {
      Collection<IMap> maps = Resources.maps().getAll();
      if (!maps.isEmpty()) {
        Game.world().loadEnvironment(maps.iterator().next());
      }
    }
  }

  public static void printHelp() {
    System.out.println("LITIengine Game Launcher");
    System.out.println("Usage: java -jar litiengine.jar [OPTIONS] [PROJECT_DIR_OR_LITIDATA]");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -p, --project <path>         Specify game project root folder");
    System.out.println("  -s, --startup-script <name>  Specify initial GameScript to execute");
    System.out.println("  -m, --map <name>             Specify initial map/environment to load");
    System.out.println("  -t, --title <title>          Set game window title");
    System.out.println("      --scale <float>          Set base render scale");
    System.out.println("      --gravity <int>          Set global physics gravity (pixels/sec)");
    System.out.println("      --release <int>          Set Java language level for runtime compilation");
    System.out.println("  -h, --help                   Display this help message");
    System.out.println("  -v, --version                Display engine version");
  }

  private static void loadProjectResources(Path root, LaunchOptions options) {
    if (options.litidataPath != null && Files.isRegularFile(options.litidataPath)) {
      Resources.load(options.litidataPath.toString());
      return;
    }
    Path defaultLitidata = root.resolve("game.litidata");
    if (Files.isRegularFile(defaultLitidata)) {
      Resources.load(defaultLitidata.toString());
    }
  }

  private static void discoverProjectScripts(Path root, LaunchOptions options) {
    List<Path> scriptDirs = List.of(root.resolve("scripts"), root.resolve("src/main/java"));

    List<ScriptDefinition> discovered = new ArrayList<>(Game.scripts().getDefinitions());
    for (Path dir : scriptDirs) {
      if (!Files.isDirectory(dir)) continue;
      try (Stream<Path> stream = Files.walk(dir)) {
        stream.filter(Files::isRegularFile).forEach(file -> {
          String filename = file.getFileName().toString();
          if (filename.endsWith(".java")) {
            discoverProjectScript(root, file).ifPresent(candidate -> {
              var existing = discovered.stream().filter(d -> d.getId().equalsIgnoreCase(candidate.id())).findFirst();
              if (existing.isPresent()) {
                log.warning("Duplicate script id '" + candidate.id() + "' discovered in "
                  + candidate.source() + " (already registered from " + existing.get().getSource() + "). Keeping first.");
              } else {
                ScriptDefinition definition = new ScriptDefinition(candidate.id(), "java", candidate.source(),
                  candidate.implementation(), candidate.host());
                definition.setTargetType(candidate.targetType());
                discovered.add(definition);
              }
            });
          }
        });
      } catch (IOException e) {
        log.log(Level.FINE, "Could not scan for scripts in " + dir, e);
      }
    }
    Game.scripts().setDefinitions(discovered);
  }

  private static Optional<ScriptCandidate> discoverProjectScript(Path root, Path javaFile) {
    try {
      String content = Files.readString(javaFile);
      String cleanContent = stripComments(content);
      Optional<ScriptMetadata> metadata = scriptMetadata(cleanContent);
      Optional<ScriptHostType> host = metadata.map(ScriptMetadata::host).or(() -> inferHostType(cleanContent));
      if (host.isEmpty()) return Optional.empty();
      String simpleName = javaFile.getFileName().toString().replaceFirst("\\.java$", "");
      var packageMatcher = PACKAGE_DECLARATION.matcher(cleanContent);
      String implementation = packageMatcher.find() ? packageMatcher.group(1) + "." + simpleName : simpleName;
      String id = metadata.map(ScriptMetadata::id).orElse(implementation);
      String source = root.relativize(javaFile.toAbsolutePath()).toString().replace('\\', '/');
      return Optional.of(new ScriptCandidate(id, source, implementation, host.get(),
        metadata.map(ScriptMetadata::targetType).orElse(null)));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  /**
   * Strips comments from Java source while preserving string and character literal contents.
   * Uses a simple lexical state machine rather than regexes so that:
   * <ul>
   *   <li>Multi-line block comments ({@code /* ... * /}) are removed even across newlines</li>
   *   <li>Comment-like sequences inside string literals ({@code "https://..."}) are preserved</li>
   *   <li>Character literals ({@code '/'}) do not trigger comment detection</li>
   * </ul>
   */
  static String stripComments(String content) {
    StringBuilder out = new StringBuilder(content.length());
    int i = 0;
    int len = content.length();
    // States: 0 = code, 1 = line comment, 2 = block comment, 3 = string literal, 4 = char literal
    int state = 0;
    while (i < len) {
      char c = content.charAt(i);
      char next = i + 1 < len ? content.charAt(i + 1) : 0;
      switch (state) {
        case 0 -> { // CODE
          if (c == '/' && next == '/') { state = 1; i += 2; }
          else if (c == '/' && next == '*') { state = 2; i += 2; }
          else if (c == '"') { state = 3; out.append(c); i++; }
          else if (c == '\'') { state = 4; out.append(c); i++; }
          else { out.append(c); i++; }
        }
        case 1 -> { // LINE_COMMENT — skip until newline, preserve the newline
          if (c == '\n') { out.append(c); state = 0; }
          i++;
        }
        case 2 -> { // BLOCK_COMMENT — skip until */
          if (c == '*' && next == '/') { state = 0; i += 2; }
          else { i++; }
        }
        case 3 -> { // STRING_LITERAL — copy verbatim, handle escapes
          out.append(c);
          if (c == '\\') { if (i + 1 < len) { out.append(content.charAt(i + 1)); i++; } }
          else if (c == '"') { state = 0; }
          i++;
        }
        case 4 -> { // CHAR_LITERAL — copy verbatim, handle escapes
          out.append(c);
          if (c == '\\') { if (i + 1 < len) { out.append(content.charAt(i + 1)); i++; } }
          else if (c == '\'') { state = 0; }
          i++;
        }
      }
    }
    return out.toString();
  }

  private static Optional<ScriptHostType> inferHostType(String content) {
    var matcher = java.util.regex.Pattern.compile(
      "\\bclass\\s+[A-Za-z_$][\\w$]*(?:\\s*<[^>]+>)?\\s+extends\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)")
      .matcher(content);
    if (matcher.find()) {
      String superclass = matcher.group(1);
      if (superclass.endsWith("GameScript")) return Optional.of(ScriptHostType.GAME);
      if (superclass.endsWith("EnvironmentScript")) return Optional.of(ScriptHostType.ENVIRONMENT);
      if (superclass.endsWith("EntityScript") || superclass.endsWith("CreatureScript")) return Optional.of(ScriptHostType.ENTITY);
    }
    return Optional.empty();
  }

  private static Optional<ScriptMetadata> scriptMetadata(String content) {
    var annotation = SCRIPT_INFO.matcher(content);
    if (!annotation.find()) return Optional.empty();
    String values = annotation.group(1);
    var id = java.util.regex.Pattern.compile("\\bid\\s*=\\s*\\\"([^\\\"]+)\\\"").matcher(values);
    var host = SCRIPT_HOST.matcher(values);
    if (!id.find()) return Optional.empty();
    String targetType = null;
    var target = SCRIPT_TARGET.matcher(values);
    if (target.find()) targetType = resolveTypeName(content, target.group(1));
    return Optional.of(new ScriptMetadata(id.group(1),
      host.find() ? ScriptHostType.valueOf(host.group(1)) : ScriptHostType.ENTITY, targetType));
  }

  /**
   * Resolves a simple type name from {@code @ScriptInfo(target = Foo.class)} to a fully qualified
   * class name using only source-level heuristics.
   *
   * <p>Supported resolution paths:
   * <ul>
   *   <li>Fully qualified names ({@code com.example.Player.class}) — returned as-is</li>
   *   <li>Explicit single-type imports ({@code import com.example.Player;})</li>
   *   <li>Engine entity types ({@code Prop}, {@code Creature}, etc. from {@code de.gurkenlabs.litiengine.entities})</li>
   *   <li>Same-package types (inferred from the source file's {@code package} declaration)</li>
   * </ul>
   *
   * <p><strong>Not supported</strong> (use a fully qualified name instead):
   * <ul>
   *   <li>Wildcard imports ({@code import foo.bar.*;})</li>
   *   <li>Nested/inner types ({@code Types.Player.class})</li>
   *   <li>Static imports</li>
   * </ul>
   */
  private static String resolveTypeName(String content, String typeName) {
    if ("Object".equals(typeName) || "java.lang.Object".equals(typeName)) return null;
    if (typeName.contains(".")) {
      log.warning("@ScriptInfo target '" + typeName
        + "' looks like a nested type or partial qualifier, which is not supported by source discovery. "
        + "Use a fully qualified name (e.g. com.example." + typeName.replace('.', '$') + ".class) instead.");
      return typeName;
    }
    var imports = IMPORT.matcher(content);
    while (imports.find()) {
      String imported = imports.group(1);
      if (imported.endsWith("." + typeName)) return imported;
    }
    try {
      Class<?> loaded = Class.forName("de.gurkenlabs.litiengine.entities." + typeName);
      return loaded.getName();
    } catch (ClassNotFoundException ignored) {
    }
    var packageMatcher = PACKAGE_DECLARATION.matcher(content);
    return packageMatcher.find() ? packageMatcher.group(1) + "." + typeName : typeName;
  }

  private record ScriptCandidate(String id, String source, String implementation, ScriptHostType host, String targetType) {}
  private record ScriptMetadata(String id, ScriptHostType host, String targetType) {}

  private static LaunchOptions parseArgs(Path explicitProjectRoot, String[] args) {
    LaunchOptions options = new LaunchOptions();
    options.projectRoot = explicitProjectRoot == null ? null : explicitProjectRoot.toAbsolutePath().normalize();
    if (args == null) return options;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (("--project".equalsIgnoreCase(arg) || "-p".equalsIgnoreCase(arg)) && i + 1 < args.length) {
        options.projectRoot = Path.of(args[++i]).toAbsolutePath().normalize();
      } else if (("--startup-script".equalsIgnoreCase(arg) || "-s".equalsIgnoreCase(arg)) && i + 1 < args.length) {
        options.startupScript = args[++i];
      } else if (("--map".equalsIgnoreCase(arg) || "-m".equalsIgnoreCase(arg)) && i + 1 < args.length) {
        options.startupMap = args[++i];
      } else if (("--title".equalsIgnoreCase(arg) || "-t".equalsIgnoreCase(arg)) && i + 1 < args.length) {
        options.title = args[++i];
      } else if ("--scale".equalsIgnoreCase(arg) && i + 1 < args.length) {
        try { options.renderScale = Float.parseFloat(args[++i]); } catch (NumberFormatException ignored) {}
      } else if ("--gravity".equalsIgnoreCase(arg) && i + 1 < args.length) {
        try { options.gravity = Integer.parseInt(args[++i]); } catch (NumberFormatException ignored) {}
      } else if ("--release".equalsIgnoreCase(arg) && i + 1 < args.length) {
        try { options.javaRelease = Integer.parseInt(args[++i]); } catch (NumberFormatException ignored) {}
      } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
        options.helpRequested = true;
      } else if ("--version".equalsIgnoreCase(arg) || "-v".equalsIgnoreCase(arg)) {
        options.versionRequested = true;
      } else if (!arg.startsWith("-")) {
        Path path = Path.of(arg);
        if (arg.endsWith(".litidata") && Files.isRegularFile(path)) {
          options.litidataPath = path.toAbsolutePath().normalize();
          if (options.projectRoot == null) {
            options.projectRoot = options.litidataPath.getParent();
          }
        } else if (Files.isDirectory(path) && options.projectRoot == null) {
          options.projectRoot = path.toAbsolutePath().normalize();
        }
      }
    }
    return options;
  }

  private static final class LaunchOptions {
    private Path projectRoot;
    private Path litidataPath;
    private String startupScript;
    private String startupMap;
    private String title;
    private float renderScale = -1;
    private Integer gravity;
    private int javaRelease = 0;
    private boolean helpRequested;
    private boolean versionRequested;
  }
}
