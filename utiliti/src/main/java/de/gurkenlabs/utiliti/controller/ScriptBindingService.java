package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.scripting.EntityScriptBinding;
import de.gurkenlabs.litiengine.scripting.EntityScriptController;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Authoritative editor-side service for reading and updating every persistent script-binding scope. */
public final class ScriptBindingService {
  private static final Logger log = Logger.getLogger(ScriptBindingService.class.getName());
  private static final ScriptBindingService INSTANCE = new ScriptBindingService();

  private final List<Consumer<ScriptBindingTarget>> listeners = new CopyOnWriteArrayList<>();

  private ScriptBindingService() {}

  public static ScriptBindingService instance() {
    return INSTANCE;
  }

  public BindingState getBindings(ScriptBindingTarget target) {
    Objects.requireNonNull(target);
    if (Editor.instance().getGameFile() == null) return new BindingState.Valid(List.of());
    return switch (target) {
      case ScriptBindingTarget.Game ignored ->
        new BindingState.Valid(copyBindings(Editor.instance().getGameFile().getGameScripts()));
      case ScriptBindingTarget.Environment environment -> readSerialized(
        findMap(environment.mapName()), ScriptManager.BINDINGS_PROPERTY);
      case ScriptBindingTarget.EntityType entityType -> new BindingState.Valid(
        findEntityTypeBinding(entityType.type()).map(EntityScriptBinding::getScripts).orElse(List.of()));
      case ScriptBindingTarget.EntityInstance entity -> readSerialized(
        findMapObject(entity.mapName(), entity.entityId()), MapObjectProperty.SCRIPT_BINDINGS);
    };
  }

  public UpdateResult updateBindings(ScriptBindingTarget target, Collection<ScriptBinding> bindings) {
    Objects.requireNonNull(target);
    BindingState current = this.getBindings(target);
    if (current instanceof BindingState.Invalid invalid) {
      return UpdateResult.failure("Repair or reset the invalid script assignment data before editing it.", invalid);
    }
    List<ScriptBinding> proposed = copyBindings(bindings);
    String referenceError = validateReferences(((BindingState.Valid) current).bindings(), proposed);
    if (referenceError != null) return UpdateResult.failure(referenceError, current);
    String duplicateError = validateDuplicates(((BindingState.Valid) current).bindings(), proposed);
    if (duplicateError != null) return UpdateResult.failure(duplicateError, current);

    try {
      switch (target) {
        case ScriptBindingTarget.Game ignored -> this.persistGame(proposed);
        case ScriptBindingTarget.Environment environment -> this.persistEnvironment(environment, proposed);
        case ScriptBindingTarget.EntityType entityType -> this.persistEntityType(entityType, proposed);
        case ScriptBindingTarget.EntityInstance entity -> this.persistEntity(entity, proposed);
      }
      this.fireChanged(target);
      return UpdateResult.success(new BindingState.Valid(proposed));
    } catch (RuntimeException exception) {
      log.log(Level.WARNING, "Could not update script bindings for " + target, exception);
      return UpdateResult.failure(exception.getMessage(), current);
    }
  }

  /** Explicitly replaces malformed serialized data. This is never called by ordinary binding edits. */
  public UpdateResult resetInvalidBindings(ScriptBindingTarget target) {
    BindingState state = this.getBindings(target);
    if (!(state instanceof BindingState.Invalid)) return UpdateResult.failure("The script assignment data is not invalid.", state);
    return this.persistReplacement(target, List.of());
  }

  public List<ScriptDefinition> compatibleDefinitions(ScriptBindingTarget target) {
    if (Editor.instance().getGameFile() == null) return List.of();
    ScriptHostType host = switch (target) {
      case ScriptBindingTarget.Game ignored -> ScriptHostType.GAME;
      case ScriptBindingTarget.Environment ignored -> ScriptHostType.ENVIRONMENT;
      case ScriptBindingTarget.EntityType ignored -> ScriptHostType.ENTITY;
      case ScriptBindingTarget.EntityInstance ignored -> ScriptHostType.ENTITY;
    };
    Class<?> entityType = this.entityType(target);
    return Editor.instance().getGameFile().getScripts().stream()
      .filter(Objects::nonNull)
      .filter(definition -> definition.getHost() == host)
      .filter(definition -> entityType == null || isCompatible(definition, entityType))
      .map(ScriptDefinition::new)
      .toList();
  }

  /** Resolves persisted type defaults, instance overrides, and the effective entity binding list. */
  public EntityBindingState getEntityBindingState(ScriptBindingTarget.EntityInstance target) {
    Objects.requireNonNull(target);
    BindingState explicitState = this.getBindings(target);
    if (!(explicitState instanceof BindingState.Valid explicit)) {
      return new EntityBindingState(List.of(), List.of(), List.of());
    }
    Class<?> concreteType = this.entityType(target);
    List<ScriptBinding> inherited = concreteType == null ? List.of() : this.resolveEntityDefaults(concreteType);
    Map<String, ScriptBinding> inheritedById = new LinkedHashMap<>();
    inherited.forEach(binding -> inheritedById.put(binding.getScript(), new ScriptBinding(binding)));

    List<EntityBindingState.ResolvedBinding> inheritedState = inherited.stream()
      .map(binding -> new EntityBindingState.ResolvedBinding(
        binding, EntityBindingState.BindingOrigin.INHERITED)).toList();
    List<EntityBindingState.ResolvedBinding> overrides = explicit.bindings().stream()
      .map(binding -> new EntityBindingState.ResolvedBinding(binding,
        inheritedById.containsKey(binding.getScript())
          ? EntityBindingState.BindingOrigin.OVERRIDE : EntityBindingState.BindingOrigin.INSTANCE_ONLY))
      .toList();

    Map<String, EntityBindingState.ResolvedBinding> effective = new LinkedHashMap<>();
    inheritedState.forEach(binding -> effective.put(binding.binding().getScript(), binding));
    overrides.forEach(binding -> {
      effective.remove(binding.binding().getScript());
      effective.put(binding.binding().getScript(), binding);
    });
    List<EntityBindingState.ResolvedBinding> effectiveBindings = effective.values().stream()
      .sorted(java.util.Comparator.comparingInt(binding -> binding.binding().getOrder())).toList();
    return new EntityBindingState(inheritedState, overrides, effectiveBindings);
  }

  public UsageIndex findUsages(String scriptId) {
    if (scriptId == null || scriptId.isBlank() || Editor.instance().getGameFile() == null) {
      return new UsageIndex(scriptId, List.of(), List.of());
    }
    List<ScriptUsage> usages = new ArrayList<>();
    List<String> invalidLocations = new ArrayList<>();
    collectUsages(usages, new ScriptBindingTarget.Game(), "Game", Editor.instance().getGameFile().getGameScripts(), scriptId);
    for (EntityScriptBinding defaults : Editor.instance().getGameFile().getEntityScripts()) {
      if (defaults == null || defaults.getTargetType() == null || defaults.getTargetType().isBlank()) {
        invalidLocations.add("Entity defaults / <missing target type>");
        continue;
      }
      collectUsages(usages, new ScriptBindingTarget.EntityType(defaults.getTargetType()),
        "Entity defaults / " + simpleName(defaults.getTargetType()), defaults.getScripts(), scriptId);
    }
    for (IMap map : Editor.instance().getGameFile().getMaps()) {
      if (map == null || map.getName() == null || map.getName().isBlank()) {
        invalidLocations.add("<unnamed map>");
        continue;
      }
      String mapName = map.getName();
      collectSerializedUsages(usages, invalidLocations, new ScriptBindingTarget.Environment(mapName),
        mapName + " / Environment", map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null), scriptId);
      for (IMapObject object : map.getMapObjects()) {
        String label = mapName + " / " + (object.getName() == null || object.getName().isBlank()
          ? "Entity #" + object.getId() : object.getName() + " #" + object.getId());
        collectSerializedUsages(usages, invalidLocations,
          new ScriptBindingTarget.EntityInstance(mapName, object.getId()), label,
          object.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null), scriptId);
      }
    }
    return new UsageIndex(scriptId, usages, invalidLocations);
  }

  /** Returns every decodable script ID referenced anywhere in the project, including inactive maps. */
  public Set<String> referencedScriptIds() {
    if (Editor.instance().getGameFile() == null) return Set.of();
    java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();
    collectReferencedIds(ids, Editor.instance().getGameFile().getGameScripts());
    Editor.instance().getGameFile().getEntityScripts().stream().filter(Objects::nonNull)
      .forEach(defaults -> collectReferencedIds(ids, defaults.getScripts()));
    for (IMap map : Editor.instance().getGameFile().getMaps()) {
      if (map == null) continue;
      collectReferencedIds(ids, readSerialized(map, ScriptManager.BINDINGS_PROPERTY));
      map.getMapObjects().forEach(object ->
        collectReferencedIds(ids, readSerialized(object, MapObjectProperty.SCRIPT_BINDINGS)));
    }
    return java.util.Collections.unmodifiableSet(ids);
  }

  /** Counts every decodable binding reference by script ID in one project-wide pass. */
  public Map<String, Integer> usageCounts() {
    if (Editor.instance().getGameFile() == null) return Map.of();
    Map<String, Integer> result = new LinkedHashMap<>();
    collectUsageCounts(result, Editor.instance().getGameFile().getGameScripts());
    Editor.instance().getGameFile().getEntityScripts().stream().filter(Objects::nonNull)
      .forEach(defaults -> collectUsageCounts(result, defaults.getScripts()));
    for (IMap map : Editor.instance().getGameFile().getMaps()) {
      if (map == null) continue;
      collectUsageCounts(result, readSerialized(map, ScriptManager.BINDINGS_PROPERTY));
      map.getMapObjects().forEach(object ->
        collectUsageCounts(result, readSerialized(object, MapObjectProperty.SCRIPT_BINDINGS)));
    }
    return java.util.Collections.unmodifiableMap(result);
  }

  /** Registers a discovered ordinary project class without taking ownership of its source file. */
  public RegistrationResult registerProjectImplementation(ProjectCodeIntegration.ScriptClassDefinition discovered) {
    if (discovered == null || Editor.instance().getGameFile() == null) {
      return new RegistrationResult(false, "No project implementation was provided.", null);
    }
    ScriptDefinition existingImplementation = Editor.instance().getGameFile().getScripts().stream()
      .filter(Objects::nonNull)
      .filter(definition -> Objects.equals(discovered.className(), definition.getImplementation()))
      .findFirst().orElse(null);
    if (existingImplementation != null) {
      return new RegistrationResult(true, null, existingImplementation);
    }
    ScriptDefinition existingId = findDefinition(discovered.id());
    if (existingId != null) {
      return new RegistrationResult(false,
        "Script ID '" + discovered.id() + "' is already used by " + existingId.getImplementation() + ".", null);
    }

    ScriptDefinition definition = ScriptCatalogService.instance().entries().stream()
      .filter(entry -> entry.state() == ScriptCatalogService.State.DISCOVERED_PROJECT)
      .filter(entry -> discovered.className().equals(entry.implementation().qualifiedClassName()))
      .map(ScriptCatalogService.Entry::definition).findFirst().orElseGet(() -> new ScriptDefinition(
        discovered.id(), languageFor(discovered.sourcePath()), null, discovered.className(), discovered.host()));
    definition.setId(discovered.id());
    definition.setName(discovered.displayName());
    definition.setLanguage(languageFor(discovered.sourcePath()));
    definition.setSource(null);
    definition.setImplementation(discovered.className());
    definition.setHost(discovered.host());
    definition.setTargetType(discovered.targetType());
    List<String> validation = definition.validate();
    if (!validation.isEmpty()) {
      return new RegistrationResult(false, String.join(" ", validation), null);
    }
    Editor.instance().getGameFile().getScripts().add(definition);
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    recordChangesIfPossible();
    this.fireAllChanged();
    return new RegistrationResult(true, null, definition);
  }

  public ScriptMutationPlan planRename(String oldId, String newId) {
    List<String> errors = new ArrayList<>();
    ScriptDefinition definition = findDefinition(oldId);
    if (definition == null) errors.add("Script '" + oldId + "' is not registered.");
    if (newId == null || newId.isBlank()) errors.add("The new script ID must not be blank.");
    if (newId != null && !newId.equals(oldId) && findDefinition(newId) != null) {
      errors.add("Script ID '" + newId + "' is already registered.");
    }
    UsageIndex usages = this.findUsages(oldId);
    if (!usages.invalidLocations().isEmpty()) {
      errors.add("Malformed script assignment data must be repaired before references can be renamed.");
    }
    return new ScriptMutationPlan(MutationKind.RENAME, oldId, newId, usages, errors);
  }

  public ScriptMutationPlan planDelete(String scriptId) {
    List<String> errors = new ArrayList<>();
    if (findDefinition(scriptId) == null) errors.add("Script '" + scriptId + "' is not registered.");
    UsageIndex usages = this.findUsages(scriptId);
    if (!usages.invalidLocations().isEmpty()) {
      errors.add("Malformed script assignment data must be repaired before references can be removed safely.");
    }
    return new ScriptMutationPlan(MutationKind.DELETE, scriptId, null, usages, errors);
  }

  /** Executes a previously validated project-wide definition mutation. */
  public MutationResult execute(ScriptMutationPlan plan) {
    return this.execute(plan, ignored -> {}, null);
  }

  /**
   * Executes a definition mutation together with an optional external resource mutation.
   * The participant is rolled back when project or runtime synchronization fails and is
   * included in the same undo/redo operation as the serialized project data.
   */
  public MutationResult execute(ScriptMutationPlan plan, Consumer<ScriptDefinition> definitionMutation,
                                MutationParticipant participant) {
    if (plan == null) return new MutationResult(false, "No mutation plan was provided.");
    if (!plan.errors().isEmpty()) return new MutationResult(false, String.join("\n", plan.errors()));
    if (Editor.instance().getGameFile() == null) return new MutationResult(false, "No project is loaded.");

    ScriptMutationPlan validated = plan.kind() == MutationKind.RENAME
      ? this.planRename(plan.scriptId(), plan.replacementId()) : this.planDelete(plan.scriptId());
    if (!validated.valid()) return new MutationResult(false, String.join("\n", validated.errors()));
    ScriptDefinition definition = findDefinition(plan.scriptId());
    if (definition == null) return new MutationResult(false, "The script definition no longer exists.");
    ProjectSnapshot snapshot = ProjectSnapshot.capture();
    boolean participantApplied = false;
    try {
      if (plan.kind() == MutationKind.RENAME) {
        replaceReferences(plan.scriptId(), plan.replacementId());
        definition.setId(plan.replacementId());
        if (Objects.equals(definition.getName(), plan.scriptId())) definition.setName(plan.replacementId());
      } else {
        removeReferences(plan.scriptId());
        Editor.instance().getGameFile().getScripts().remove(definition);
      }
      if (definitionMutation != null) definitionMutation.accept(definition);
      if (participant != null) {
        participantApplied = true;
        participant.apply();
      }
      this.refreshRuntimeState();
      this.registerMutationUndo(snapshot, ProjectSnapshot.capture(), participant);
      this.fireMutationChanged(validated);
      return new MutationResult(true, null);
    } catch (RuntimeException | LinkageError exception) {
      String message = exception.getMessage() == null ? "Script mutation failed." : exception.getMessage();
      if (participantApplied) {
        try {
          participant.rollback();
        } catch (RuntimeException rollbackFailure) {
          exception.addSuppressed(rollbackFailure);
          message += " The associated source file could not be restored: "
            + Objects.toString(rollbackFailure.getMessage(), rollbackFailure.getClass().getSimpleName()) + ".";
        }
      }
      try {
        snapshot.restore();
        this.refreshRuntimeState();
        this.fireAllChanged();
      } catch (RuntimeException | LinkageError rollbackFailure) {
        exception.addSuppressed(rollbackFailure);
        message += " The project data was restored, but the runtime could not be refreshed: "
          + Objects.toString(rollbackFailure.getMessage(), rollbackFailure.getClass().getSimpleName());
      }
      return new MutationResult(false, message);
    }
  }

  public boolean isEntityTypeInherited(String type) {
    return this.findEntityTypeBinding(type).map(EntityScriptBinding::isInherited).orElse(true);
  }

  public UpdateResult setEntityTypeInherited(String type, boolean inherited) {
    if (Editor.instance().getGameFile() == null) {
      return UpdateResult.failure("No project is loaded.", new BindingState.Valid(List.of()));
    }
    if (type == null || type.isBlank()) {
      return UpdateResult.failure("The entity target type must not be blank.", new BindingState.Valid(List.of()));
    }
    var existing = this.findEntityTypeBinding(type);
    boolean created = existing.isEmpty();
    EntityScriptBinding binding = existing.orElseGet(() -> {
      EntityScriptBinding newBinding = new EntityScriptBinding(type);
      Editor.instance().getGameFile().getEntityScripts().add(newBinding);
      return newBinding;
    });
    boolean previous = binding.isInherited();
    binding.setInherited(inherited);
    try {
      Game.scripts().setEntityBindings(Editor.instance().getGameFile().getEntityScripts());
      this.refreshLoadedEntityDefaults();
    } catch (RuntimeException | LinkageError exception) {
      if (created) Editor.instance().getGameFile().getEntityScripts().remove(binding);
      else binding.setInherited(previous);
      Game.scripts().setEntityBindings(Editor.instance().getGameFile().getEntityScripts());
      try {
        this.refreshLoadedEntityDefaults();
      } catch (RuntimeException | LinkageError rollbackFailure) {
        exception.addSuppressed(rollbackFailure);
      }
      return UpdateResult.failure(Objects.toString(exception.getMessage(), "Could not apply inheritance."),
        new BindingState.Valid(binding.getScripts()));
    }
    recordChangesIfPossible();
    ScriptBindingTarget target = new ScriptBindingTarget.EntityType(type);
    this.fireChanged(target);
    return UpdateResult.success(this.getBindings(target));
  }

  public void addChangeListener(Consumer<ScriptBindingTarget> listener) {
    if (listener != null) this.listeners.add(listener);
  }

  public void removeChangeListener(Consumer<ScriptBindingTarget> listener) {
    this.listeners.remove(listener);
  }

  private UpdateResult persistReplacement(ScriptBindingTarget target, List<ScriptBinding> bindings) {
    try {
      switch (target) {
        case ScriptBindingTarget.Game ignored -> this.persistGame(bindings);
        case ScriptBindingTarget.Environment environment -> this.persistEnvironment(environment, bindings);
        case ScriptBindingTarget.EntityType entityType -> this.persistEntityType(entityType, bindings);
        case ScriptBindingTarget.EntityInstance entity -> this.persistEntity(entity, bindings);
      }
      this.fireChanged(target);
      return UpdateResult.success(new BindingState.Valid(bindings));
    } catch (RuntimeException exception) {
      return UpdateResult.failure(exception.getMessage(), this.getBindings(target));
    }
  }

  private void persistGame(List<ScriptBinding> bindings) {
    var gameFile = Editor.instance().getGameFile();
    gameFile.getGameScripts().clear();
    gameFile.getGameScripts().addAll(copyBindings(bindings));
    this.synchronizeGameBindings(bindings);
    recordChangesIfPossible();
  }

  private void persistEnvironment(ScriptBindingTarget.Environment target, List<ScriptBinding> bindings) {
    IMap map = requireMap(target.mapName());
    UndoManager undo = UndoManager.forMap(map);
    undo.mapChanging(map);
    writeSerialized(map, ScriptManager.BINDINGS_PROPERTY, bindings);
    undo.mapChanged(map);
    Environment active = Game.world().environment();
    if (active != null && active.getMap() == map) {
      Game.scripts().detach(active);
      Game.scripts().attachAll(active, bindings);
    }
  }

  private void persistEntityType(ScriptBindingTarget.EntityType target, List<ScriptBinding> bindings) {
    var all = Editor.instance().getGameFile().getEntityScripts();
    EntityScriptBinding existing = this.findEntityTypeBinding(target.type()).orElse(null);
    if (bindings.isEmpty()) {
      if (existing != null) existing.getScripts().clear();
    } else if (existing == null) {
      existing = new EntityScriptBinding(target.type());
      existing.getScripts().addAll(copyBindings(bindings));
      all.add(existing);
    } else {
      existing.getScripts().clear();
      existing.getScripts().addAll(copyBindings(bindings));
    }
    Game.scripts().setEntityBindings(all);
    this.refreshLoadedEntityDefaults();
    recordChangesIfPossible();
  }

  private void persistEntity(ScriptBindingTarget.EntityInstance target, List<ScriptBinding> bindings) {
    IMap map = requireMap(target.mapName());
    IMapObject object = map.getMapObject(target.entityId());
    if (object == null) throw new IllegalArgumentException("Entity #" + target.entityId() + " does not exist on " + target.mapName() + ".");
    UndoManager undo = UndoManager.forMap(map);
    undo.mapObjectChanging(object);
    writeSerialized(object, MapObjectProperty.SCRIPT_BINDINGS, bindings);
    undo.mapObjectChanged(object);

    Environment active = Game.world().environment();
    if (active == null || active.getMap() != map) return;
    IEntity entity = active.get(target.entityId());
    if (entity == null) return;
    EntityScriptController<?> controller = entity.scripts();
    if (controller != null) {
      controller.setBindings(bindings);
    } else if (!bindings.isEmpty()) {
      EntityScriptController<IEntity> created = new EntityScriptController<>(entity, bindings);
      entity.addController(created);
    }
  }

  private void refreshLoadedEntityDefaults() {
    Environment active = Game.world().environment();
    if (active == null) return;
    active.getEntities().forEach(Game.scripts()::configure);
  }

  private void refreshRuntimeState() {
    var bundle = Editor.instance().getGameFile();
    Game.scripts().setDefinitions(bundle.getScripts());
    this.synchronizeGameBindings(bundle.getGameScripts());
    Game.scripts().setEntityBindings(bundle.getEntityScripts());
    this.refreshLoadedEntityDefaults();

    Environment active = Game.world().environment();
    if (active == null || active.getMap() == null) return;
    BindingState environment = readSerialized(active.getMap(), ScriptManager.BINDINGS_PROPERTY);
    if (environment instanceof BindingState.Valid validEnvironment) {
      Game.scripts().detach(active);
      Game.scripts().attachAll(active, validEnvironment.bindings());
    }
    for (IEntity entity : active.getEntities()) {
      IMapObject object = active.getMap().getMapObject(entity.getMapId());
      if (object == null) continue;
      BindingState explicit = readSerialized(object, MapObjectProperty.SCRIPT_BINDINGS);
      if (explicit instanceof BindingState.Valid valid) {
        EntityScriptController<?> controller = entity.scripts();
        if (controller != null) controller.setBindings(valid.bindings());
      }
    }
  }

  private void synchronizeGameBindings(Collection<ScriptBinding> bindings) {
    // utiLITI owns the engine loop, so Game can be started while no editor environment is loaded.
    // Attaching game scripts in that transient state can execute user code against a null world.
    if (Game.hasStarted() && Game.world().environment() == null) return;
    Game.scripts().setGameBindings(bindings);
  }

  private void registerMutationUndo(ProjectSnapshot before, ProjectSnapshot after,
                                    MutationParticipant participant) {
    Environment environment = Game.world().environment();
    if (environment == null || environment.getMap() == null) return;
    UndoManager.forMap(environment.getMap()).resourceChanged(
      () -> {
        if (participant != null) participant.undo();
        this.restoreSnapshot(before);
      },
      () -> {
        if (participant != null) participant.redo();
        this.restoreSnapshot(after);
      });
  }

  private void restoreSnapshot(ProjectSnapshot snapshot) {
    snapshot.restore();
    this.refreshRuntimeState();
    this.fireAllChanged();
  }

  private void fireMutationChanged(ScriptMutationPlan plan) {
    this.fireChanged(new ScriptBindingTarget.Game());
    plan.usages().usages().stream().map(ScriptUsage::target).distinct()
      .filter(target -> !(target instanceof ScriptBindingTarget.Game)).forEach(this::fireChanged);
  }

  private void fireAllChanged() {
    this.fireChanged(new ScriptBindingTarget.Game());
    if (Editor.instance().getGameFile() == null) return;
    Editor.instance().getGameFile().getEntityScripts().stream()
      .filter(Objects::nonNull).map(EntityScriptBinding::getTargetType)
      .filter(type -> type != null && !type.isBlank())
      .map(ScriptBindingTarget.EntityType::new).forEach(this::fireChanged);
    for (IMap map : Editor.instance().getGameFile().getMaps()) {
      if (map.getName() == null || map.getName().isBlank()) continue;
      this.fireChanged(new ScriptBindingTarget.Environment(map.getName()));
      map.getMapObjects().forEach(object ->
        this.fireChanged(new ScriptBindingTarget.EntityInstance(map.getName(), object.getId())));
    }
  }

  private static void recordChangesIfPossible() {
    Environment environment = Game.world().environment();
    if (environment != null && environment.getMap() != null) {
      UndoManager.forMap(environment.getMap()).recordChanges();
    }
  }

  private static void replaceReferences(String oldId, String newId) {
    mutateAllReferences(oldId, binding -> binding.setScript(newId));
  }

  private static void removeReferences(String scriptId) {
    mutateAllBindingLists(scriptId, bindings -> bindings.removeIf(binding -> scriptId.equals(binding.getScript())));
  }

  private static void mutateAllReferences(String scriptId, Consumer<ScriptBinding> mutation) {
    mutateAllBindingLists(scriptId, bindings -> bindings.stream()
      .filter(binding -> scriptId.equals(binding.getScript())).forEach(mutation));
  }

  private static void mutateAllBindingLists(String scriptId, Consumer<List<ScriptBinding>> mutation) {
    var bundle = Editor.instance().getGameFile();
    mutation.accept(bundle.getGameScripts());
    bundle.getEntityScripts().forEach(defaults -> mutation.accept(defaults.getScripts()));
    for (IMap map : bundle.getMaps()) {
      mutateSerialized(map, ScriptManager.BINDINGS_PROPERTY, mutation);
      for (IMapObject object : map.getMapObjects()) {
        mutateSerialized(object, MapObjectProperty.SCRIPT_BINDINGS, mutation);
      }
    }
  }

  private static void mutateSerialized(de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider target,
                                       String property, Consumer<List<ScriptBinding>> mutation) {
    String raw = target.getStringValue(property, null);
    if (raw == null || raw.isBlank()) return;
    List<ScriptBinding> bindings = new ArrayList<>(ScriptBindingCodec.decode(raw));
    mutation.accept(bindings);
    writeSerialized(target, property, bindings);
  }

  private static ScriptDefinition findDefinition(String id) {
    if (Editor.instance().getGameFile() == null || id == null) return null;
    return Editor.instance().getGameFile().getScripts().stream()
      .filter(Objects::nonNull)
      .filter(definition -> id.equals(definition.getId())).findFirst().orElse(null);
  }

  private java.util.Optional<EntityScriptBinding> findEntityTypeBinding(String type) {
    if (Editor.instance().getGameFile() == null || type == null) return java.util.Optional.empty();
    return Editor.instance().getGameFile().getEntityScripts().stream()
      .filter(binding -> type.equals(binding.getTargetType())).findFirst();
  }

  public Class<?> entityType(ScriptBindingTarget target) {
    String typeName = switch (target) {
      case ScriptBindingTarget.EntityType entityType -> entityType.type();
      case ScriptBindingTarget.EntityInstance entity -> {
        IMapObject object = findMapObject(entity.mapName(), entity.entityId());
        Class<?> resolved = ScriptBindingTypeResolver.resolve(object);
        yield resolved == null ? null : resolved.getName();
      }
      default -> null;
    };
    if (typeName == null) return null;
    try {
      ClassLoader loader = Editor.instance().getProjectCodeIntegration().getClassLoader();
      if (loader == null) loader = ScriptBindingService.class.getClassLoader();
      return Class.forName(typeName, false, loader);
    } catch (ClassNotFoundException | LinkageError exception) {
      return null;
    }
  }

  private List<ScriptBinding> resolveEntityDefaults(Class<?> concreteType) {
    List<ResolvedEntityDefault> matching = new ArrayList<>();
    List<EntityScriptBinding> defaults = Editor.instance().getGameFile().getEntityScripts();
    for (int index = 0; index < defaults.size(); index++) {
      EntityScriptBinding binding = defaults.get(index);
      if (binding == null || binding.getTargetType() == null || binding.getTargetType().isBlank()) continue;
      try {
        ClassLoader loader = concreteType.getClassLoader();
        if (loader == null) loader = Editor.instance().getProjectCodeIntegration().getClassLoader();
        if (loader == null) loader = ScriptBindingService.class.getClassLoader();
        Class<?> target = Class.forName(binding.getTargetType(), false, loader);
        int distance = typeDistance(concreteType, target);
        if (distance < 0 || !binding.isInherited() && distance != 0) continue;
        matching.add(new ResolvedEntityDefault(binding, distance, index));
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Unresolvable defaults remain stored and are surfaced by integrity validation.
      }
    }
    matching.sort(java.util.Comparator.comparingInt(ResolvedEntityDefault::distance).reversed()
      .thenComparingInt(ResolvedEntityDefault::index));
    Map<String, ScriptBinding> merged = new LinkedHashMap<>();
    for (ResolvedEntityDefault resolved : matching) {
      for (ScriptBinding binding : resolved.binding().getScripts()) {
        if (binding == null || binding.getScript() == null) continue;
        merged.remove(binding.getScript());
        merged.put(binding.getScript(), new ScriptBinding(binding));
      }
    }
    return merged.values().stream().sorted(java.util.Comparator.comparingInt(ScriptBinding::getOrder)).toList();
  }

  private static int typeDistance(Class<?> concrete, Class<?> target) {
    if (!target.isAssignableFrom(concrete)) return -1;
    List<Class<?>> current = List.of(concrete);
    java.util.Set<Class<?>> visited = new java.util.HashSet<>();
    for (int distance = 0; !current.isEmpty(); distance++) {
      if (current.contains(target)) return distance;
      List<Class<?>> next = new ArrayList<>();
      for (Class<?> type : current) {
        if (!visited.add(type)) continue;
        Class<?> parent = type.getSuperclass();
        if (parent != null) next.add(parent);
        next.addAll(List.of(type.getInterfaces()));
      }
      current = next;
    }
    return -1;
  }

  private static boolean isCompatible(ScriptDefinition definition, Class<?> entityType) {
    if (definition.getTargetType() == null || definition.getTargetType().isBlank()) return true;
    try {
      ClassLoader loader = entityType.getClassLoader();
      if (loader == null) loader = ScriptBindingService.class.getClassLoader();
      return Class.forName(definition.getTargetType(), false, loader).isAssignableFrom(entityType);
    } catch (ClassNotFoundException | LinkageError exception) {
      return definition.getTargetType().equals(entityType.getName());
    }
  }

  private static BindingState readSerialized(de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider source, String property) {
    if (source == null) return new BindingState.Valid(List.of());
    String raw = source.getStringValue(property, null);
    try {
      return new BindingState.Valid(ScriptBindingCodec.decode(raw));
    } catch (IllegalArgumentException exception) {
      return new BindingState.Invalid(raw, exception.getMessage());
    }
  }

  private static void writeSerialized(de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider target,
                                      String property, List<ScriptBinding> bindings) {
    if (bindings == null || bindings.isEmpty()) target.removeProperty(property);
    else target.setValue(property, ScriptBindingCodec.encode(bindings));
  }

  private IMap requireMap(String name) {
    IMap map = findMap(name);
    if (map == null) throw new IllegalArgumentException("Map '" + name + "' does not exist.");
    return map;
  }

  private static IMap findMap(String name) {
    if (Editor.instance().getGameFile() == null) return null;
    return Editor.instance().getGameFile().getMaps().stream()
      .filter(map -> Objects.equals(name, map.getName())).findFirst().orElse(null);
  }

  private static IMapObject findMapObject(String mapName, int entityId) {
    IMap map = findMap(mapName);
    return map == null ? null : map.getMapObject(entityId);
  }

  private void fireChanged(ScriptBindingTarget target) {
    for (Consumer<ScriptBindingTarget> listener : this.listeners) {
      try {
        listener.accept(target);
      } catch (RuntimeException exception) {
        log.log(Level.WARNING, "Script binding listener failed", exception);
      }
    }
  }

  private static List<ScriptBinding> copyBindings(Collection<ScriptBinding> bindings) {
    return bindings == null ? new ArrayList<>() : bindings.stream().filter(Objects::nonNull).map(ScriptBinding::new)
      .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  }

  private static String validateDuplicates(List<ScriptBinding> current, List<ScriptBinding> proposed) {
    Map<String, Integer> currentCounts = counts(current);
    Map<String, Integer> proposedCounts = counts(proposed);
    for (var entry : proposedCounts.entrySet()) {
      if (entry.getValue() > 1 && entry.getValue() > currentCounts.getOrDefault(entry.getKey(), 0)) {
        return "Script '" + entry.getKey() + "' is already attached to this target.";
      }
    }
    return null;
  }

  private static String validateReferences(List<ScriptBinding> current, List<ScriptBinding> proposed) {
    Map<String, Integer> currentCounts = counts(current);
    Map<String, Integer> proposedCounts = counts(proposed);
    for (ScriptBinding binding : proposed) {
      if (binding.getScript() == null || binding.getScript().isBlank()) {
        return "Every script assignment must reference a script ID.";
      }
    }
    for (var entry : proposedCounts.entrySet()) {
      if (findDefinition(entry.getKey()) == null
          && entry.getValue() > currentCounts.getOrDefault(entry.getKey(), 0)) {
        return "Script '" + entry.getKey() + "' is not registered in this project.";
      }
    }
    return null;
  }

  private static Map<String, Integer> counts(List<ScriptBinding> bindings) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (ScriptBinding binding : bindings) {
      if (binding.getScript() != null) counts.merge(binding.getScript(), 1, Integer::sum);
    }
    return counts;
  }

  private static void collectSerializedUsages(List<ScriptUsage> usages, List<String> invalidLocations,
                                              ScriptBindingTarget target, String label, String raw, String scriptId) {
    try {
      collectUsages(usages, target, label, ScriptBindingCodec.decode(raw), scriptId);
    } catch (IllegalArgumentException exception) {
      invalidLocations.add(label);
    }
  }

  private static void collectUsages(List<ScriptUsage> usages, ScriptBindingTarget target, String label,
                                    Collection<ScriptBinding> bindings, String scriptId) {
    int index = 0;
    for (ScriptBinding binding : bindings) {
      if (scriptId.equals(binding.getScript())) usages.add(new ScriptUsage(target, label, index));
      index++;
    }
  }

  private static void collectReferencedIds(Set<String> ids, BindingState state) {
    if (state instanceof BindingState.Valid valid) collectReferencedIds(ids, valid.bindings());
  }

  private static void collectReferencedIds(Set<String> ids, Collection<ScriptBinding> bindings) {
    if (bindings == null) return;
    bindings.stream().filter(Objects::nonNull).map(ScriptBinding::getScript)
      .filter(id -> id != null && !id.isBlank()).forEach(ids::add);
  }

  private static void collectUsageCounts(Map<String, Integer> result, BindingState state) {
    if (state instanceof BindingState.Valid valid) collectUsageCounts(result, valid.bindings());
  }

  private static void collectUsageCounts(Map<String, Integer> result, Collection<ScriptBinding> bindings) {
    if (bindings == null) return;
    bindings.stream().filter(Objects::nonNull).map(ScriptBinding::getScript)
      .filter(id -> id != null && !id.isBlank()).forEach(id -> result.merge(id, 1, Integer::sum));
  }

  private static String simpleName(String name) {
    return name == null ? "Unknown" : name.substring(name.lastIndexOf('.') + 1);
  }

  private static String languageFor(java.nio.file.Path source) {
    String name = source == null ? "" : source.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
    if (name.endsWith(".groovy")) return "groovy";
    if (name.endsWith(".kt")) return "kotlin";
    return "java";
  }

  public record UpdateResult(boolean success, String message, BindingState state) {
    static UpdateResult success(BindingState state) {
      return new UpdateResult(true, null, state);
    }

    static UpdateResult failure(String message, BindingState state) {
      return new UpdateResult(false, message == null ? "Script assignment update failed." : message, state);
    }
  }

  public record ScriptUsage(ScriptBindingTarget target, String label, int bindingIndex) {}

  public record UsageIndex(String scriptId, List<ScriptUsage> usages, List<String> invalidLocations) {
    public UsageIndex {
      usages = usages == null ? List.of() : List.copyOf(usages);
      invalidLocations = invalidLocations == null ? List.of() : List.copyOf(invalidLocations);
    }
  }

  public enum MutationKind { RENAME, DELETE }

  public record ScriptMutationPlan(MutationKind kind, String scriptId, String replacementId,
                                   UsageIndex usages, List<String> errors) {
    public ScriptMutationPlan {
      usages = usages == null ? new UsageIndex(scriptId, List.of(), List.of()) : usages;
      errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public boolean valid() { return this.errors.isEmpty(); }
  }

  public record MutationResult(boolean success, String message) {}

  /** External resource operation that participates in a project mutation transaction. */
  public interface MutationParticipant {
    void apply();

    void rollback();

    default void undo() {
      this.rollback();
    }

    default void redo() {
      this.apply();
    }
  }

  public record RegistrationResult(boolean success, String message, ScriptDefinition definition) {}

  private record ResolvedEntityDefault(EntityScriptBinding binding, int distance, int index) {}

  private record ProjectSnapshot(List<DefinitionSnapshot> definitions, List<ScriptBinding> gameBindings,
                                  List<EntityScriptBinding> entityBindings, Map<IMap, Map<String, String>> serialized) {
    private static ProjectSnapshot capture() {
      var bundle = Editor.instance().getGameFile();
      Map<IMap, Map<String, String>> serialized = new LinkedHashMap<>();
      for (IMap map : bundle.getMaps()) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("map", map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null));
        for (IMapObject object : map.getMapObjects()) {
          values.put("entity:" + object.getId(), object.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null));
        }
        serialized.put(map, values);
      }
      return new ProjectSnapshot(bundle.getScripts().stream().filter(Objects::nonNull)
        .map(definition -> new DefinitionSnapshot(definition, new ScriptDefinition(definition))).toList(),
        copyBindings(bundle.getGameScripts()),
        bundle.getEntityScripts().stream().filter(Objects::nonNull).map(EntityScriptBinding::new).toList(), serialized);
    }

    private void restore() {
      var bundle = Editor.instance().getGameFile();
      List<ScriptDefinition> restored = new ArrayList<>();
      for (DefinitionSnapshot snapshot : this.definitions) {
        copyDefinition(snapshot.value(), snapshot.identity());
        restored.add(snapshot.identity());
      }
      bundle.getScripts().clear();
      bundle.getScripts().addAll(restored);
      bundle.getGameScripts().clear();
      bundle.getGameScripts().addAll(copyBindings(this.gameBindings));
      bundle.getEntityScripts().clear();
      this.entityBindings.forEach(binding -> bundle.getEntityScripts().add(new EntityScriptBinding(binding)));
      this.serialized.forEach((map, values) -> {
        restoreRaw(map, ScriptManager.BINDINGS_PROPERTY, values.get("map"));
        for (IMapObject object : map.getMapObjects()) {
          restoreRaw(object, MapObjectProperty.SCRIPT_BINDINGS, values.get("entity:" + object.getId()));
        }
      });
    }

    private static void copyDefinition(ScriptDefinition source, ScriptDefinition target) {
      target.setId(source.getId());
      target.setName(source.getName());
      target.setLanguage(source.getLanguage());
      target.setSource(source.getSource());
      target.setImplementation(source.getImplementation());
      target.setHost(source.getHost());
      target.setTargetType(source.getTargetType());
    }

    private static void restoreRaw(de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider target,
                                   String property, String raw) {
      if (raw == null || raw.isBlank()) target.removeProperty(property);
      else target.setValue(property, raw);
    }
  }

  private record DefinitionSnapshot(ScriptDefinition identity, ScriptDefinition value) {}
}
