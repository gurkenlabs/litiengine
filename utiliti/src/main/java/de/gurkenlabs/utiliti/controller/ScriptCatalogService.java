package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Builds the canonical editor catalog for registered, discovered, and unresolved scripts. */
public final class ScriptCatalogService {
  private static final ScriptCatalogService INSTANCE = new ScriptCatalogService();

  private final Map<String, ScriptDefinition> projections = new LinkedHashMap<>();

  private ScriptCatalogService() {}

  public static ScriptCatalogService instance() {
    return INSTANCE;
  }

  public List<Entry> entries() {
    Collection<ProjectCodeIntegration.ScriptClassDefinition> discovered =
      Editor.instance().getProjectCodeIntegration() == null
        ? List.of() : Editor.instance().getProjectCodeIntegration().getScriptDefinitions();
    return this.entries(discovered);
  }

  List<Entry> entries(Collection<ProjectCodeIntegration.ScriptClassDefinition> discovered) {
    if (Editor.instance().getGameFile() == null) return List.of();
    List<ScriptDefinition> registered = Editor.instance().getGameFile().getScripts().stream()
      .filter(Objects::nonNull).toList();
    Map<String, ProjectCodeIntegration.ScriptClassDefinition> discoveredByImplementation = new LinkedHashMap<>();
    if (discovered != null) discovered.stream().filter(Objects::nonNull)
      .filter(value -> value.className() != null && !value.className().isBlank())
      .forEach(value -> discoveredByImplementation.putIfAbsent(value.className(), value));

    List<Entry> catalog = new ArrayList<>();
    for (ScriptDefinition definition : registered) {
      var project = discoveredByImplementation.remove(definition.getImplementation());
      if (project != null) {
        catalog.add(new Entry(key(State.REGISTERED_PROJECT, definition.getImplementation()),
          State.REGISTERED_PROJECT, definition,
          new ScriptImplementation(project.className(), normalized(project.sourcePath()), SourceKind.PROJECT)));
      } else {
        catalog.add(new Entry(key(State.REGISTERED_RUNTIME, definition.getId()),
          State.REGISTERED_RUNTIME, definition,
          new ScriptImplementation(definition.getImplementation(), resolveRuntimeSource(definition), SourceKind.RUNTIME)));
      }
    }

    for (ProjectCodeIntegration.ScriptClassDefinition project : discoveredByImplementation.values()) {
      String projectionKey = key(State.DISCOVERED_PROJECT, project.className());
      ScriptDefinition definition = this.projections.computeIfAbsent(projectionKey,
        ignored -> new ScriptDefinition(project.id(), languageFor(project.sourcePath()), null,
          project.className(), project.host()));
      definition.setId(project.id());
      definition.setName(project.displayName());
      definition.setLanguage(languageFor(project.sourcePath()));
      definition.setSource(null);
      definition.setImplementation(project.className());
      definition.setHost(project.host());
      definition.setTargetType(project.targetType());
      catalog.add(new Entry(projectionKey, State.DISCOVERED_PROJECT, definition,
        new ScriptImplementation(project.className(), normalized(project.sourcePath()), SourceKind.PROJECT)));
    }

    var registeredIds = registered.stream().map(ScriptDefinition::getId)
      .filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
    for (String reference : ScriptBindingService.instance().referencedScriptIds()) {
      if (registeredIds.contains(reference)) continue;
      String projectionKey = key(State.UNRESOLVED, reference);
      ScriptDefinition definition = this.projections.computeIfAbsent(projectionKey,
        ignored -> new ScriptDefinition(reference, "java", null, null, ScriptHostType.ENTITY));
      definition.setId(reference);
      definition.setName(reference);
      definition.setSource(null);
      definition.setImplementation(null);
      catalog.add(new Entry(projectionKey, State.UNRESOLVED, definition,
        new ScriptImplementation(null, null, SourceKind.NONE)));
    }
    return catalog.stream().sorted(java.util.Comparator.comparing(
      entry -> displayName(entry.definition()), String.CASE_INSENSITIVE_ORDER)).toList();
  }

  private static Path resolveRuntimeSource(ScriptDefinition definition) {
    if (definition == null || definition.getSource() == null || definition.getSource().isBlank()) return null;
    try {
      Path configured = Path.of(definition.getSource());
      if (configured.isAbsolute()) return configured.normalize();
      if (Editor.instance().getProjectModel() != null
          && Editor.instance().getProjectModel().projectRoot() != null) {
        return Editor.instance().getProjectModel().projectRoot().resolve(configured).normalize();
      }
    } catch (InvalidPathException ignored) {
      // Invalid legacy paths remain represented by the definition itself.
    }
    return null;
  }

  private static Path normalized(Path source) {
    return source == null ? null : source.toAbsolutePath().normalize();
  }

  private static String languageFor(Path source) {
    String name = source == null ? "" : source.getFileName().toString().toLowerCase(Locale.ROOT);
    if (name.endsWith(".groovy")) return "groovy";
    if (name.endsWith(".kt")) return "kotlin";
    return "java";
  }

  private static String key(State state, String identity) {
    return state.name() + ":" + Objects.toString(identity, "");
  }

  private static String displayName(ScriptDefinition definition) {
    if (definition.getName() != null && !definition.getName().isBlank()) return definition.getName();
    return Objects.toString(definition.getId(), "");
  }

  public enum State {
    REGISTERED_RUNTIME,
    REGISTERED_PROJECT,
    DISCOVERED_PROJECT,
    UNRESOLVED
  }

  public enum SourceKind {
    RUNTIME,
    PROJECT,
    NONE
  }

  public record ScriptImplementation(String qualifiedClassName, Path sourcePath, SourceKind sourceKind) {}

  public record Entry(String key, State state, ScriptDefinition definition, ScriptImplementation implementation) {
    public Entry {
      Objects.requireNonNull(key);
      Objects.requireNonNull(state);
      Objects.requireNonNull(definition);
      Objects.requireNonNull(implementation);
    }

    public boolean registered() {
      return this.state == State.REGISTERED_RUNTIME || this.state == State.REGISTERED_PROJECT;
    }
  }
}
