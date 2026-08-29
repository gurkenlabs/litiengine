package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Completion;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.CompletionKind;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Position;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Range;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.TextEdit;
import de.gurkenlabs.litiengine.scripting.ui.ScriptUiOverlay;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Manages script execution scope, global helper bindings, and type inference. */
final class ScriptScope {
  record RootFunction(String name, Class<?> returnType, String returnTypeSimple, String documentation) {}

  static final List<RootFunction> ROOT_FUNCTIONS = List.of(
    new RootFunction("context", ScriptContext.class, "ScriptContext", "The current script attachment context."),
    new RootFunction("environment", Environment.class, "Environment", "The host's current environment."),
    new RootFunction("globals", ScriptGlobals.class, "ScriptGlobals", "Direct access to the global shared state store."),
    new RootFunction("ui", ScriptUiOverlay.class, "ScriptUiOverlay", "Returns the ScriptUiOverlay service for floating text and banners."),
    new RootFunction("camera", ICamera.class, "ICamera", "Returns the active camera controller for panning, zooming, and shaking.")
  );

  private ScriptScope() {}

  static void addScriptScope(
      List<Completion> result,
      ScriptDefinition definition,
      Set<String> importedFqns,
      int importInsertLine) {

    String host = definition == null || definition.getTargetType() == null ? "Object" : simpleName(definition.getTargetType());
    result.add(new Completion("host", CompletionKind.METHOD, "host()", "The typed object controlled by this script.", "host()", host, List.of(), List.of()));

    for (RootFunction fn : ROOT_FUNCTIONS) {
      result.add(new Completion(fn.name(), CompletionKind.METHOD, fn.name() + "()", fn.documentation(), fn.name() + "()", fn.returnTypeSimple(), List.of(), List.of()));
    }

    List<TextEdit> gameEdits = importedFqns != null && importedFqns.contains(Game.class.getName()) ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import " + Game.class.getName() + ";\n"));
    result.add(new Completion("Game", CompletionKind.CLASS, Game.class.getName(),
      "The central LITIENGINE static entry point for game systems.", "Game", "Game", List.of(), gameEdits));
  }

  static Optional<Class<?>> inferHostType(ScriptDefinition definition, String source, ClassLoader loader) {
    if (source != null && !source.isBlank()) {
      Matcher genericMatcher = Pattern.compile("(?m)\\bextends\\s+(?:EntityScript|CreatureScript|AbstractScript|PropScript)\\s*<\\s*([A-Za-z0-9_$]+)\\s*>").matcher(source);
      if (genericMatcher.find()) {
        Optional<Class<?>> fromExtends = EngineTypeCatalog.findType(genericMatcher.group(1), loader);
        if (fromExtends.isPresent()) return fromExtends;
      }
      if (Pattern.compile("(?m)\\bextends\\s+CreatureScript\\b").matcher(source).find()) {
        return Optional.of(Creature.class);
      }
      if (Pattern.compile("(?m)\\bextends\\s+EnvironmentScript\\b").matcher(source).find()) {
        return Optional.of(Environment.class);
      }
      if (Pattern.compile("(?m)\\bextends\\s+GameScript\\b").matcher(source).find()) {
        return Optional.of(Game.class);
      }
      if (Pattern.compile("(?m)\\bextends\\s+EntityScript\\b").matcher(source).find()) {
        return Optional.of(Entity.class);
      }
      Matcher targetMatcher = Pattern.compile("(?m)@ScriptInfo\\s*\\([^)]*target\\s*=\\s*([A-Za-z0-9_$]+)\\.class").matcher(source);
      if (targetMatcher.find()) {
        Optional<Class<?>> fromAnnotation = EngineTypeCatalog.findType(targetMatcher.group(1), loader);
        if (fromAnnotation.isPresent()) return fromAnnotation;
      }
    }
    if (definition != null && definition.getTargetType() != null && !definition.getTargetType().isBlank()) {
      try {
        return Optional.of(Class.forName(definition.getTargetType(), false, loader));
      } catch (ClassNotFoundException | LinkageError ignored) {
        Optional<Class<?>> resolved = EngineTypeCatalog.findType(definition.getTargetType(), loader);
        if (resolved.isPresent()) return resolved;
      }
    }
    if (definition != null && definition.getHost() != null) {
      return switch (definition.getHost()) {
        case ENTITY -> Optional.of(Creature.class);
        case ENVIRONMENT -> Optional.of(Environment.class);
        case GAME -> Optional.of(Game.class);
      };
    }
    return Optional.empty();
  }

  static Class<?> inferScriptType(ScriptDefinition definition, String source) {
    if (source != null && !source.isBlank()) {
      if (Pattern.compile("(?m)\\bextends\\s+CreatureScript\\b").matcher(source).find()) {
        return CreatureScript.class;
      }
      if (Pattern.compile("(?m)\\bextends\\s+EnvironmentScript\\b").matcher(source).find()) {
        return EnvironmentScript.class;
      }
      if (Pattern.compile("(?m)\\bextends\\s+EntityScript\\b").matcher(source).find()) {
        return EntityScript.class;
      }
      if (Pattern.compile("(?m)\\bextends\\s+GameScript\\b").matcher(source).find()) {
        return GameScript.class;
      }
      if (Pattern.compile("(?m)\\bextends\\s+AbstractScript\\b").matcher(source).find()) {
        return AbstractScript.class;
      }
    }
    if (definition != null && definition.getHost() != null) {
      return switch (definition.getHost()) {
        case ENTITY -> CreatureScript.class;
        case ENVIRONMENT -> EnvironmentScript.class;
        case GAME -> GameScript.class;
      };
    }
    return AbstractScript.class;
  }

  private static String simpleName(String fqn) {
    if (fqn == null) return "";
    int lastDot = fqn.lastIndexOf('.');
    return lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
  }
}
