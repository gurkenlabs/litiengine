package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Unified factory for generating starter script templates and synchronizing script declarations.
 */
public final class ScriptTemplateFactory {

  private ScriptTemplateFactory() {}

  /**
   * Generates initial script source code for the given parameters.
   *
   * @param id The script ID
   * @param host The host type (GAME, ENVIRONMENT, ENTITY)
   * @param targetType The target entity type FQCN (or simple name) if host is ENTITY
   * @param packageName The Java package name (or null / empty for default package)
   * @param className The Java class name
   * @return The complete Java source file contents
   */
  public static String generateTemplate(String id, ScriptHostType host, String targetType, String packageName, String className) {
    String packageHeader = (packageName != null && !packageName.isBlank()) ? "package " + packageName + ";\n\n" : "";

    if (host == ScriptHostType.GAME) {
      String base = "GameScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.GameScript" : "GameScript";
      return packageHeader
          + "import de.gurkenlabs.litiengine.*;\n"
          + "import de.gurkenlabs.litiengine.input.Input;\n"
          + "import de.gurkenlabs.litiengine.resources.*;\n"
          + "import de.gurkenlabs.litiengine.scripting.*;\n"
          + "import java.awt.event.KeyEvent;\n\n"
          + "/**\n"
          + " * Global game lifecycle script controller (entry point).\n"
          + " *\n"
          + " * <p>Responsibilities:\n"
          + " * <ul>\n"
          + " *   <li>Initialize persistent game state: {@code globals.put(\"score\", 0)}</li>\n"
          + " *   <li>Load starting map: {@code loadMap(\"map1\")}</li>\n"
          + " *   <li>Play background soundtracks: {@code playMusic(\"theme\")}</li>\n"
          + " *   <li>Register global inputs: pause, restart, hotkeys</li>\n"
          + " * </ul>\n"
          + " */\n"
          + "@ScriptInfo(id = \"" + id + "\", host = ScriptHostType.GAME)\n"
          + "public class " + className + " extends " + base + " {\n"
          + "  @Override\n"
          + "  public void onStarted() {\n"
          + "    // 1. Initialize persistent global variables across maps\n"
          + "    globals.put(\"score\", 0);\n"
          + "    globals.put(\"lives\", 3);\n\n"
          + "    // 2. Play background soundtrack (optional)\n"
          + "    // playMusic(\"bg_music\");\n\n"
          + "    // 3. Load initial map (if not already loaded by launcher/editor)\n"
          + "    if (Game.world().environment() == null) {\n"
          + "      // loadMap(\"level1\");\n"
          + "    }\n\n"
          + "    // 4. Global input shortcuts (e.g. Pause on ESC)\n"
          + "    Input.keyboard().onKeyTyped(KeyEvent.VK_ESCAPE, event -> {\n"
          + "      // Toggle pause or open menu\n"
          + "    });\n"
          + "  }\n\n"
          + "  @Override\n"
          + "  public void update() {\n"
          + "    // Global game-level update loop (runs continuously across all maps)\n"
          + "  }\n"
          + "}\n";
    }

    if (host == ScriptHostType.ENVIRONMENT) {
      String base = "EnvironmentScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.EnvironmentScript" : "EnvironmentScript";
      return packageHeader
          + "import de.gurkenlabs.litiengine.*;\n"
          + "import de.gurkenlabs.litiengine.entities.*;\n"
          + "import de.gurkenlabs.litiengine.environment.Environment;\n"
          + "import de.gurkenlabs.litiengine.resources.*;\n"
          + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
          + "/**\n"
          + " * Map environment script controller.\n"
          + " *\n"
          + " * <p>Responsibilities:\n"
          + " * <ul>\n"
          + " *   <li>Map initialization & wave spawning on {@code onLoaded()}</li>\n"
          + " *   <li>Objective tracking: {@code onEntityRemoved(IEntity)}</li>\n"
          + " *   <li>Level clear transitions & ambient cinematics</li>\n"
          + " * </ul>\n"
          + " */\n"
          + "@ScriptInfo(id = \"" + id + "\", host = ScriptHostType.ENVIRONMENT)\n"
          + "public class " + className + " extends " + base + " {\n"
          + "  @Override\n"
          + "  public void onLoaded() {\n"
          + "    // Map is loaded and active. Announce level start:\n"
          + "    context().ui().showBanner(\"LEVEL START\", \"Defeat all enemies!\", 2500);\n"
          + "  }\n\n"
          + "  @Override\n"
          + "  protected void onEntityRemoved(IEntity entity) {\n"
          + "    // Check if level objective is complete\n"
          + "    var remainingMonsters = EntityQuery.in(environment(), Creature.class).alive().list();\n"
          + "    if (remainingMonsters.isEmpty()) {\n"
          + "      context().ui().showBanner(\"VICTORY\", \"Stage Cleared!\", 3000);\n"
          + "    }\n"
          + "  }\n\n"
          + "  @Override\n"
          + "  public void update() {\n"
          + "    // Map-level update logic\n"
          + "  }\n"
          + "}\n";
    }

    String fullTarget = targetType != null && !targetType.isBlank() ? targetType : "de.gurkenlabs.litiengine.entities.Creature";
    String targetSimple = fullTarget.substring(fullTarget.lastIndexOf('.') + 1);
    String base = "Creature".equals(targetSimple)
        ? ("CreatureScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.CreatureScript" : "CreatureScript")
        : ("EntityScript<" + targetSimple + ">");
    String targetImport = fullTarget.contains(".") ? "import " + fullTarget + ";\n" : "";

    return packageHeader
        + "import de.gurkenlabs.litiengine.*;\n"
        + targetImport
        + "import de.gurkenlabs.litiengine.entities.*;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n"
        + "import java.awt.Color;\n\n"
        + "/**\n"
        + " * Entity script controller for {@link " + targetSimple + "}.\n"
        + " *\n"
        + " * <p>Responsibilities:\n"
        + " * <ul>\n"
        + " *   <li>AI movement & navigation: {@code moveTowards(target)}</li>\n"
        + " *   <li>Combat abilities & projectiles: {@code createAbility()}, {@code spawnProjectile()}</li>\n"
        + " *   <li>Reactions: {@code onHit(event)}, {@code onDeath(entity, hitEvent)}</li>\n"
        + " * </ul>\n"
        + " */\n"
        + "@ScriptInfo(id = \"" + id + "\", host = ScriptHostType.ENTITY, target = " + targetSimple + ".class)\n"
        + "public class " + className + " extends " + base + " {\n"
        + "  @Override\n"
        + "  public void onLoaded() {\n"
        + "    // Entity spawned and ready in the environment\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    if (isDead()) return;\n\n"
        + "    // Entity AI / movement logic\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  protected void onHit(EntityHitEvent event) {\n"
        + "    // Display floating combat damage number\n"
        + "    context().ui().floatText(\"-\" + event.getDamage(), host(), Color.RED);\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {\n"
        + "    // Entity mortality handling\n"
        + "    remove();\n"
        + "  }\n"
        + "}\n";
  }

  /**
   * Synchronizes script annotation and base class declaration with definition metadata.
   */
  public static String synchronizeDeclaration(String source, ScriptDefinition definition) {
    if (source == null || source.isBlank() || definition == null) return source;
    String className = ScriptSourcePaths.extractClassName(source);
    if (className == null || className.isBlank()) className = definition.getImplementation();

    String annotation = "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType." + definition.getHost()
        + (definition.getHost() == ScriptHostType.ENTITY && definition.getTargetType() != null
        ? ", target = " + definition.getTargetType() + ".class" : "") + ")";
    String updated = source.replaceFirst("(?s)@ScriptInfo\\s*\\(.*?\\)", Matcher.quoteReplacement(annotation));
    String base = scriptBase(definition, className);
    updated = updated.replaceFirst("(?m)(\\bclass\\s+[A-Za-z_$][\\w$]*\\s+extends\\s+)[\\w.$<>?]+",
        "$1" + Matcher.quoteReplacement(base));
    if (definition.getHost() == ScriptHostType.GAME) {
      return updated.replaceAll("\\bvoid\\s+onLoaded\\s*\\(", "void onStarted(")
          .replaceAll("\\bvoid\\s+onUnloaded\\s*\\(", "void onStopped(");
    }
    return updated.replaceAll("\\bvoid\\s+onStarted\\s*\\(", "void onLoaded(")
        .replaceAll("\\bvoid\\s+onStopped\\s*\\(", "void onUnloaded(");
  }

  private static String scriptBase(ScriptDefinition definition, String className) {
    String base = switch (definition.getHost()) {
      case GAME -> "GameScript";
      case ENVIRONMENT -> "EnvironmentScript";
      case ENTITY -> "de.gurkenlabs.litiengine.entities.Creature".equals(definition.getTargetType())
          || "Creature".equals(definition.getTargetType())
          ? "CreatureScript" : "EntityScript<" + Objects.requireNonNullElse(
          definition.getTargetType(), "de.gurkenlabs.litiengine.entities.IEntity") + ">";
    };

    String simpleBase = base.contains("<") ? base.substring(0, base.indexOf('<')) : base;
    if (className != null && className.equals(simpleBase)) {
      return "de.gurkenlabs.litiengine.scripting." + base;
    }
    return base;
  }
}
