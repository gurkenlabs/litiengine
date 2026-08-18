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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** Standalone launcher to bootstrap and run LITIengine games and script-based projects without custom Java entry points. */
public final class GameLauncher {
  private static final Logger log = Logger.getLogger(GameLauncher.class.getName());

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
            String scriptId = filename.substring(0, filename.length() - ".java".length());
            if (discovered.stream().noneMatch(d -> d.getId().equalsIgnoreCase(scriptId))) {
              String relativeSource = root.relativize(file.toAbsolutePath()).toString().replace('\\', '/');
              ScriptHostType host = inferHostType(file, scriptId);
              discovered.add(new ScriptDefinition(scriptId, "java", relativeSource, scriptId, host));
            }
          }
        });
      } catch (IOException e) {
        log.log(Level.FINE, "Could not scan for scripts in " + dir, e);
      }
    }
    Game.scripts().setDefinitions(discovered);
  }

  private static ScriptHostType inferHostType(Path javaFile, String scriptId) {
    try {
      String content = Files.readString(javaFile);
      if (content.contains("extends GameScript")) return ScriptHostType.GAME;
      if (content.contains("extends EnvironmentScript")) return ScriptHostType.ENVIRONMENT;
      if (content.contains("extends EntityScript") || content.contains("extends CreatureScript")) return ScriptHostType.ENTITY;
    } catch (Exception ignored) {
    }
    if (scriptId.toLowerCase().contains("game")) return ScriptHostType.GAME;
    if (scriptId.toLowerCase().contains("env") || scriptId.toLowerCase().contains("map")) return ScriptHostType.ENVIRONMENT;
    return ScriptHostType.ENTITY;
  }

  private static LaunchOptions parseArgs(Path explicitProjectRoot, String[] args) {
    LaunchOptions options = new LaunchOptions();
    options.projectRoot = explicitProjectRoot;
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
