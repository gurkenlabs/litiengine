package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.CompiledScript;
import de.gurkenlabs.litiengine.scripting.JavaScriptProvider;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.EntityScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptInstance;
import de.gurkenlabs.litiengine.scripting.ScriptProvider;
import de.gurkenlabs.litiengine.scripting.ScriptManager;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.util.List;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class ScriptBindingServiceTest {
  private static final java.util.concurrent.atomic.AtomicInteger MAP_IDS =
    new java.util.concurrent.atomic.AtomicInteger();
  private final ScriptBindingService service = ScriptBindingService.instance();
  private TmxMap map;

  @BeforeEach
  void setUp() {
    Game.world().unloadEnvironment();
    Game.scripts().detachAll();
    Game.scripts().setGameBindings(List.of());
    Game.scripts().setEntityBindings(List.of());
    Game.scripts().setDefinitions(List.of());
    Game.scripts().clearDiagnostics();
    Game.scripts().setProjectClassLoader(null);
    Game.scripts().setProjectClasspath(List.of());
    Game.scripts().registerProvider(new JavaScriptProvider());
    UndoManager.clearAll();
    Editor.instance().load(null, false);
    Editor.instance().getGameFile().getMaps().clear();
    Editor.instance().getGameFile().getScripts().clear();
    Editor.instance().getGameFile().getGameScripts().clear();
    Editor.instance().getGameFile().getEntityScripts().clear();
    this.map = new TmxMap(MapOrientations.ORTHOGONAL);
    this.map.setName("scripts-test-" + MAP_IDS.incrementAndGet());
    this.map.setWidth(10);
    this.map.setHeight(10);
    this.map.setTileWidth(16);
    this.map.setTileHeight(16);
    Editor.instance().getGameFile().getMaps().add(this.map);
  }

  @AfterEach
  void tearDown() {
    Game.world().unloadEnvironment();
    UndoManager.clearAll();
  }

  @Test
  void updatePreservesOrderAndUnknownParameters() {
    ScriptBinding first = new ScriptBinding("first");
    first.setOrder(4);
    first.setParameter("future-value", "keep-me");
    ScriptBinding second = new ScriptBinding("second");
    second.setOrder(9);
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(List.of(first, second)));

    var target = new ScriptBindingTarget.Environment(this.map.getName());
    BindingState.Valid state = assertInstanceOf(BindingState.Valid.class, this.service.getBindings(target));
    List<ScriptBinding> edited = state.bindings().stream().map(ScriptBinding::new)
      .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    edited.getFirst().setEnabled(false);

    assertTrue(this.service.updateBindings(target, edited).success());
    List<ScriptBinding> persisted = ScriptBindingCodec.decode(
      this.map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null));
    assertEquals(4, persisted.getFirst().getOrder());
    assertEquals(9, persisted.get(1).getOrder());
    assertEquals("keep-me", persisted.getFirst().getParameters().get("future-value"));
    assertFalse(persisted.getFirst().isEnabled());
  }

  @Test
  void gameBindingUpdateSynchronizesTheRuntimeManager() {
    ScriptDefinition definition = definition(
      "live-game", LiveGameScript.class, ScriptHostType.GAME, null);
    Editor.instance().getGameFile().getScripts().add(definition);
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());

    var result = this.service.updateBindings(
      new ScriptBindingTarget.Game(), List.of(new ScriptBinding(definition.getId())));

    assertTrue(result.success(), result.message());
    assertEquals(List.of("live-game"), Game.scripts().getGameBindings().stream()
      .map(ScriptBinding::getScript).toList());
  }

  @Test
  void activeEnvironmentBindingUpdateReattachesTheRuntimeHost() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    RecordingProvider provider = new RecordingProvider();
    Game.scripts().registerProvider(provider);
    ScriptDefinition definition = definition(
      "live-environment", LiveEnvironmentScript.class, ScriptHostType.ENVIRONMENT, null);
    definition.setLanguage(provider.language());
    Editor.instance().getGameFile().getScripts().add(definition);
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    Game.world().loadEnvironment(this.map);

    var result = this.service.updateBindings(
      new ScriptBindingTarget.Environment(this.map.getName()),
      List.of(new ScriptBinding(definition.getId())));

    assertTrue(result.success(), result.message());
    assertTrue(provider.hosts.contains(Game.world().environment()),
      Game.scripts().getDiagnostics().toString());
  }

  @Test
  void activeEntityBindingUpdateReplacesTheRuntimeControllerBindings() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    RecordingProvider provider = new RecordingProvider();
    Game.scripts().registerProvider(provider);
    ScriptDefinition definition = definition(
      "live-entity", LiveEntityScript.class, ScriptHostType.ENTITY, Prop.class.getName());
    definition.setLanguage(provider.language());
    Editor.instance().getGameFile().getScripts().add(definition);
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    Game.world().loadEnvironment(this.map);
    MapObject object = new MapObject();
    object.setId(64);
    object.setType(MapObjectType.PROP.name());
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(object);
    this.map.addLayer(layer);
    Prop prop = new Prop("");
    prop.setMapId(object.getId());
    Game.world().environment().add(prop);

    var result = this.service.updateBindings(
      new ScriptBindingTarget.EntityInstance(this.map.getName(), object.getId()),
      List.of(new ScriptBinding(definition.getId())));

    assertTrue(result.success(), result.message());
    assertTrue(provider.hosts.contains(prop),
      Game.scripts().getDiagnostics().toString());
    assertEquals("live-entity", prop.scripts().getBindings().getFirst().getScript());
  }

  @Test
  void malformedDataCannotBeOverwrittenByOrdinaryUpdate() {
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY, "not-json");
    var target = new ScriptBindingTarget.Environment(this.map.getName());

    assertInstanceOf(BindingState.Invalid.class, this.service.getBindings(target));
    assertFalse(this.service.updateBindings(target, List.of(new ScriptBinding("replacement"))).success());
    assertEquals("not-json", this.map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null));
  }

  @Test
  void legacyDuplicatesSurviveButNewDuplicateIsRejected() {
    ScriptBinding duplicate1 = new ScriptBinding("legacy");
    duplicate1.setOrder(0);
    ScriptBinding duplicate2 = new ScriptBinding("legacy");
    duplicate2.setOrder(1);
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(List.of(duplicate1, duplicate2)));
    var target = new ScriptBindingTarget.Environment(this.map.getName());

    BindingState.Valid current = assertInstanceOf(BindingState.Valid.class, this.service.getBindings(target));
    assertTrue(this.service.updateBindings(target, current.bindings()).success());

    List<ScriptBinding> added = new java.util.ArrayList<>(current.bindings());
    added.add(new ScriptBinding("legacy"));
    assertFalse(this.service.updateBindings(target, added).success());
    assertEquals(2, ScriptBindingCodec.decode(
      this.map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null)).size());
  }

  @Test
  void usageIndexIncludesInactiveEnvironmentAndEntityBindings() {
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY,
      ScriptBindingCodec.encode(List.of(new ScriptBinding("used"))));
    MapObject object = new MapObject();
    object.setId(42);
    object.setValue(MapObjectProperty.SCRIPT_BINDINGS,
      ScriptBindingCodec.encode(List.of(new ScriptBinding("used"))));
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName("objects");
    layer.addMapObject(object);
    this.map.addLayer(layer);

    var usages = this.service.findUsages("used");
    assertEquals(2, usages.usages().size());
    assertEquals(2, this.service.usageCounts().get("used"));
    assertTrue(usages.usages().stream().anyMatch(usage -> usage.target() instanceof ScriptBindingTarget.Environment));
    assertTrue(usages.usages().stream().anyMatch(usage -> usage.target() instanceof ScriptBindingTarget.EntityInstance));
  }

  @Test
  void renameUpdatesEveryReferenceWithoutRenamingImplementation() {
    ScriptDefinition definition = new ScriptDefinition(
      "old-id", "java", "scripts/java/GameController.java", "com.game.GameController", ScriptHostType.GAME);
    Editor.instance().getGameFile().getScripts().add(definition);
    Editor.instance().getGameFile().getGameScripts().add(new ScriptBinding("old-id"));
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY,
      ScriptBindingCodec.encode(List.of(new ScriptBinding("old-id"))));

    var plan = this.service.planRename("old-id", "new-id");
    assertTrue(plan.valid());
    var result = this.service.execute(plan);
    assertTrue(result.success(), result.message());

    assertEquals("new-id", definition.getId());
    assertEquals("com.game.GameController", definition.getImplementation());
    assertEquals("new-id", Editor.instance().getGameFile().getGameScripts().getFirst().getScript());
    assertEquals("new-id", ScriptBindingCodec.decode(
      this.map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null)).getFirst().getScript());
  }

  @Test
  void deleteRemovesDefinitionAndReferencesButNotUnrelatedBindings() {
    ScriptDefinition definition = new ScriptDefinition(
      "remove-me", "java", null, "com.game.ExistingController", ScriptHostType.GAME);
    Editor.instance().getGameFile().getScripts().add(definition);
    Editor.instance().getGameFile().getGameScripts().add(new ScriptBinding("keep-me"));
    Editor.instance().getGameFile().getGameScripts().add(new ScriptBinding("remove-me"));
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY,
      ScriptBindingCodec.encode(List.of(new ScriptBinding("remove-me"))));

    var plan = this.service.planDelete("remove-me");
    assertTrue(plan.valid());
    var result = this.service.execute(plan);
    assertTrue(result.success(), result.message());

    assertFalse(Editor.instance().getGameFile().getScripts().contains(definition));
    assertEquals(List.of("keep-me"), Editor.instance().getGameFile().getGameScripts().stream()
      .map(ScriptBinding::getScript).toList());
    assertFalse(this.map.hasCustomProperty(ScriptManager.BINDINGS_PROPERTY));
  }

  @Test
  void malformedBindingsBlockProjectWideMutations() {
    Editor.instance().getGameFile().getScripts().add(new ScriptDefinition(
      "blocked", "java", null, "com.game.Blocked", ScriptHostType.GAME));
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY, "not-json");

    var plan = this.service.planDelete("blocked");
    assertFalse(plan.valid());
    assertFalse(this.service.execute(plan).success());
    assertEquals("not-json", this.map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null));
  }

  @Test
  void mutationPlanIsRevalidatedBeforeExecution() {
    Editor.instance().getGameFile().getScripts().add(new ScriptDefinition(
      "rename-me", "java", null, "com.game.RenameMe", ScriptHostType.GAME));
    var plan = this.service.planRename("rename-me", "target-id");
    assertTrue(plan.valid());
    Editor.instance().getGameFile().getScripts().add(new ScriptDefinition(
      "target-id", "java", null, "com.game.Target", ScriptHostType.GAME));

    assertFalse(this.service.execute(plan).success());
    assertTrue(Editor.instance().getGameFile().getScripts().stream()
      .anyMatch(definition -> "rename-me".equals(definition.getId())));
  }

  @Test
  void discoveredProjectImplementationCanBeRegisteredWithoutOwningItsSource() {
    var discovered = new ProjectCodeIntegration.ScriptClassDefinition(
      "existing-game", "Existing Game", "com.game.ExistingGame", ScriptHostType.GAME,
      null, List.of(), Path.of("src/main/java/com/game/ExistingGame.java"));

    var result = this.service.registerProjectImplementation(discovered);

    assertTrue(result.success(), result.message());
    assertEquals("existing-game", result.definition().getId());
    assertEquals("com.game.ExistingGame", result.definition().getImplementation());
    assertEquals("Existing Game", result.definition().getName());
    assertEquals(null, result.definition().getSource());
    assertTrue(Editor.instance().getGameFile().getScripts().contains(result.definition()));
  }

  @Test
  void catalogSharesStableDefinitionsAndActualProjectImplementationMetadata() {
    ScriptDefinition registered = new ScriptDefinition(
      "registered", "java", null, "com.game.Registered", ScriptHostType.GAME);
    Editor.instance().getGameFile().getScripts().add(registered);
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY,
      ScriptBindingCodec.encode(List.of(new ScriptBinding("missing-legacy"))));
    Path registeredSource = Path.of("src/main/java/com/game/Registered.java");
    Path discoveredSource = Path.of("src/main/java/com/game/Discovered.java");
    List<ProjectCodeIntegration.ScriptClassDefinition> discovered = List.of(
      new ProjectCodeIntegration.ScriptClassDefinition(
        "registered", "Registered", "com.game.Registered", ScriptHostType.GAME,
        null, List.of(), registeredSource),
      new ProjectCodeIntegration.ScriptClassDefinition(
        "discovered", "Discovered", "com.game.Discovered", ScriptHostType.ENTITY,
        Prop.class.getName(), List.of(), discoveredSource));

    List<ScriptCatalogService.Entry> catalog = ScriptCatalogService.instance().entries(discovered);
    ScriptCatalogService.Entry registeredEntry = catalog.stream()
      .filter(entry -> entry.state() == ScriptCatalogService.State.REGISTERED_PROJECT).findFirst().orElseThrow();
    ScriptCatalogService.Entry discoveredEntry = catalog.stream()
      .filter(entry -> entry.state() == ScriptCatalogService.State.DISCOVERED_PROJECT).findFirst().orElseThrow();
    ScriptCatalogService.Entry unresolvedEntry = catalog.stream()
      .filter(entry -> entry.state() == ScriptCatalogService.State.UNRESOLVED).findFirst().orElseThrow();

    assertSame(registered, registeredEntry.definition());
    assertEquals("com.game.Registered", registeredEntry.implementation().qualifiedClassName());
    assertEquals(registeredSource.toAbsolutePath().normalize(), registeredEntry.implementation().sourcePath());
    assertEquals("com.game.Discovered", discoveredEntry.definition().getImplementation());
    assertEquals(discoveredSource.toAbsolutePath().normalize(), discoveredEntry.implementation().sourcePath());
    assertEquals("missing-legacy", unresolvedEntry.definition().getId());
    ScriptDefinition stableProjection = ScriptCatalogService.instance().entries(discovered).stream()
      .filter(entry -> entry.state() == ScriptCatalogService.State.DISCOVERED_PROJECT)
      .map(ScriptCatalogService.Entry::definition).findFirst().orElseThrow();
    assertSame(discoveredEntry.definition(), stableProjection);
  }

  @Test
  void emptyEntityDefaultsRetainExplicitInheritanceSetting() {
    String type = "com.game.Enemy";

    assertTrue(this.service.setEntityTypeInherited(type, false).success());
    assertFalse(this.service.isEntityTypeInherited(type));
    assertTrue(this.service.updateBindings(new ScriptBindingTarget.EntityType(type), List.of()).success());
    assertFalse(this.service.isEntityTypeInherited(type));
  }

  @Test
  void malformedScopeMetadataBlocksMutationPlanningInsteadOfThrowing() {
    Editor.instance().getGameFile().getScripts().add(new ScriptDefinition(
      "safe", "java", null, "com.game.Safe", ScriptHostType.GAME));
    Editor.instance().getGameFile().getEntityScripts().add(new EntityScriptBinding());
    TmxMap unnamed = new TmxMap();
    Editor.instance().getGameFile().getMaps().add(unnamed);

    var plan = this.service.planDelete("safe");

    assertFalse(plan.valid());
    assertTrue(plan.usages().invalidLocations().contains("Entity defaults / <missing target type>"));
    assertTrue(plan.usages().invalidLocations().contains("<unnamed map>"));
  }

  @Test
  void newUnresolvedReferencesAreRejectedButLegacyReferencesRemainEditable() {
    var target = new ScriptBindingTarget.Environment(this.map.getName());
    assertFalse(this.service.updateBindings(target, List.of(new ScriptBinding("missing"))).success());

    ScriptBinding legacy = new ScriptBinding("missing");
    this.map.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(List.of(legacy)));
    legacy.setEnabled(false);

    assertTrue(this.service.updateBindings(target, List.of(legacy)).success());
    assertFalse(ScriptBindingCodec.decode(
      this.map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null)).getFirst().isEnabled());
  }

  @Test
  void mutationUndoPreservesDefinitionIdentityForOpenEditors() {
    ScriptDefinition definition = new ScriptDefinition(
      "before", "java", null, "com.game.Identity", ScriptHostType.GAME);
    Editor.instance().getGameFile().getScripts().add(definition);
    Editor.instance().getGameFile().getGameScripts().add(new ScriptBinding("before"));
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Game.world().loadEnvironment(this.map);

    assertTrue(this.service.execute(this.service.planRename("before", "after")).success());
    assertSame(definition, Editor.instance().getGameFile().getScripts().getFirst());

    UndoManager.forMap(this.map).undo();
    assertSame(definition, Editor.instance().getGameFile().getScripts().getFirst());
    assertEquals("before", definition.getId());
    assertEquals("before", Editor.instance().getGameFile().getGameScripts().getFirst().getScript());

    UndoManager.forMap(this.map).redo();
    assertSame(definition, Editor.instance().getGameFile().getScripts().getFirst());
    assertEquals("after", definition.getId());
  }

  @Test
  void mutationParticipantSharesRollbackAndUndoRedoWithProjectData() {
    ScriptDefinition definition = new ScriptDefinition(
      "before", "java", "before.java", "com.game.Before", ScriptHostType.GAME);
    Editor.instance().getGameFile().getScripts().add(definition);
    Editor.instance().getGameFile().getGameScripts().add(new ScriptBinding("before"));
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Game.world().loadEnvironment(this.map);
    java.util.concurrent.atomic.AtomicInteger applies = new java.util.concurrent.atomic.AtomicInteger();
    java.util.concurrent.atomic.AtomicInteger rollbacks = new java.util.concurrent.atomic.AtomicInteger();
    ScriptBindingService.MutationParticipant participant = new ScriptBindingService.MutationParticipant() {
      @Override public void apply() { applies.incrementAndGet(); }
      @Override public void rollback() { rollbacks.incrementAndGet(); }
    };

    var result = this.service.execute(this.service.planRename("before", "after"), renamed -> {
      renamed.setImplementation("com.game.After");
      renamed.setSource("after.java");
    }, participant);

    assertTrue(result.success(), result.message());
    assertEquals(1, applies.get());
    assertEquals("com.game.After", definition.getImplementation());
    UndoManager.forMap(this.map).undo();
    assertEquals(1, rollbacks.get());
    assertEquals("before", definition.getId());
    assertEquals("com.game.Before", definition.getImplementation());
    UndoManager.forMap(this.map).redo();
    assertEquals(2, applies.get());
    assertEquals("after", definition.getId());
    assertEquals("com.game.After", definition.getImplementation());
  }

  @Test
  void failingMutationParticipantRestoresProjectReferences() {
    ScriptDefinition definition = new ScriptDefinition(
      "before", "java", null, "com.game.Before", ScriptHostType.GAME);
    Editor.instance().getGameFile().getScripts().add(definition);
    Editor.instance().getGameFile().getGameScripts().add(new ScriptBinding("before"));
    java.util.concurrent.atomic.AtomicBoolean rolledBack = new java.util.concurrent.atomic.AtomicBoolean();
    ScriptBindingService.MutationParticipant participant = new ScriptBindingService.MutationParticipant() {
      @Override public void apply() { throw new IllegalStateException("source failure"); }
      @Override public void rollback() { rolledBack.set(true); }
    };

    var result = this.service.execute(this.service.planRename("before", "after"), ignored -> {}, participant);

    assertFalse(result.success());
    assertTrue(rolledBack.get());
    assertEquals("before", definition.getId());
    assertEquals("before", Editor.instance().getGameFile().getGameScripts().getFirst().getScript());
  }

  @Test
  void entityInstanceCompatibilityUsesItsActualMapObjectType() {
    MapObject prop = new MapObject();
    prop.setId(77);
    prop.setType(MapObjectType.PROP.name());
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(prop);
    this.map.addLayer(layer);

    assertEquals(Prop.class,
      this.service.entityType(new ScriptBindingTarget.EntityInstance(this.map.getName(), prop.getId())));
  }

  @Test
  void entityDefaultsCanBeOverriddenEditedAndResetWithoutFlattening() {
    ScriptDefinition definition = new ScriptDefinition(
      "movement", "java", null, "com.game.Movement", ScriptHostType.ENTITY);
    definition.setTargetType(Prop.class.getName());
    Editor.instance().getGameFile().getScripts().add(definition);
    ScriptBinding inherited = new ScriptBinding("movement");
    inherited.setParameter("speed", "2");
    EntityScriptBinding defaults = new EntityScriptBinding(Prop.class);
    defaults.getScripts().add(inherited);
    Editor.instance().getGameFile().getEntityScripts().add(defaults);

    MapObject prop = new MapObject();
    prop.setId(88);
    prop.setType(MapObjectType.PROP.name());
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(prop);
    this.map.addLayer(layer);
    var target = new ScriptBindingTarget.EntityInstance(this.map.getName(), prop.getId());

    EntityBindingState initial = this.service.getEntityBindingState(target);
    assertEquals(1, initial.inherited().size());
    assertTrue(initial.overrides().isEmpty());
    assertEquals(EntityBindingState.BindingOrigin.INHERITED, initial.effective().getFirst().origin());

    ScriptBinding override = new ScriptBinding(inherited);
    override.setParameter("speed", "5");
    var overrideResult = this.service.updateBindings(target, List.of(override));
    assertTrue(overrideResult.success(), overrideResult.message());
    EntityBindingState overridden = this.service.getEntityBindingState(target);
    assertEquals(EntityBindingState.BindingOrigin.OVERRIDE, overridden.overrides().getFirst().origin());
    assertEquals("5", overridden.effective().getFirst().binding().getParameters().get("speed"));

    assertTrue(this.service.updateBindings(target, List.of()).success());
    EntityBindingState reset = this.service.getEntityBindingState(target);
    assertTrue(reset.overrides().isEmpty());
    assertEquals(EntityBindingState.BindingOrigin.INHERITED, reset.effective().getFirst().origin());
    assertEquals("2", reset.effective().getFirst().binding().getParameters().get("speed"));
  }

  private static ScriptDefinition definition(
      String id, Class<?> implementation, ScriptHostType host, String targetType) {
    ScriptDefinition definition = new ScriptDefinition(
      id, "java", null, implementation.getName(), host);
    definition.setTargetType(targetType);
    return definition;
  }

  public static final class LiveGameScript
      extends de.gurkenlabs.litiengine.scripting.GameScript {}

  public static final class LiveEnvironmentScript
      extends de.gurkenlabs.litiengine.scripting.EnvironmentScript {}

  public static final class LiveEntityScript
      extends de.gurkenlabs.litiengine.scripting.EntityScript<Prop> {}

  private static final class RecordingProvider implements ScriptProvider {
    private final List<Object> hosts = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Override
    public String language() {
      return "java";
    }

    @Override
    public CompiledScript compile(ScriptDefinition definition, java.net.URL source, ClassLoader parent) {
      return new CompiledScript() {
        @Override
        public ScriptInstance create() {
          return new ScriptInstance() {
            @Override
            public void attach(de.gurkenlabs.litiengine.scripting.ScriptContext<?> context) {
              hosts.add(context.host());
            }

            @Override
            public void detach() {}
          };
        }

        @Override
        public Class<? extends ScriptInstance> implementationType() {
          return ScriptInstance.class;
        }
      };
    }
  }
}
