package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.EntityMessageEvent;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.MapObjectLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ScriptRuntimeTests {
  private TestEntity host;

  @AfterEach
  void detachScripts() {
    if (this.host != null) {
      this.host.detachControllers();
      Game.scripts().detach(this.host);
    }
    Game.scripts().clearDiagnostics();
    Game.scripts().setEntityBindings(List.of());
    JavaEntityScript.reset();
  }

  @Test
  void bindingCodecPreservesOrderStateAndParameters() {
    ScriptBinding second = new ScriptBinding("second");
    second.setOrder(20);
    second.setEnabled(false);
    second.setParameter("range", "160");
    ScriptBinding first = new ScriptBinding("first");
    first.setOrder(10);

    List<ScriptBinding> decoded = ScriptBindingCodec.decode(ScriptBindingCodec.encode(List.of(second, first)));

    assertEquals(List.of("first", "second"), decoded.stream().map(ScriptBinding::getScript).toList());
    assertFalse(decoded.get(1).isEnabled());
    assertEquals("160", decoded.get(1).getParameters().get("range"));
  }

  @Test
  void attachesConfiguresUpdatesMessagesAndDetachesAJavaScript() {
    ScriptDefinition definition = new ScriptDefinition("java-test", "java", null, JavaEntityScript.class.getName(), ScriptHostType.ENTITY);
    definition.setTargetType(TestEntity.class.getName());
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("java-test");
    binding.setParameter("speed", "42");
    this.host = new TestEntity();

    ScriptInstance instance = Game.scripts().attach(this.host, binding);
    this.host.sendMessage(this, "hello");
    Game.scripts().update();
    Game.scripts().detach(this.host);

    assertNotNull(instance);
    assertEquals(42, JavaEntityScript.configuredSpeed);
    assertEquals(1, JavaEntityScript.loaded);
    assertEquals(1, JavaEntityScript.messages);
    assertEquals(1, JavaEntityScript.updates);
    assertEquals(1, JavaEntityScript.unloaded);
  }

  @Test
  void missingDefinitionsProduceActionableDiagnostics() {
    Game.scripts().setDefinitions(List.of());
    this.host = new TestEntity();

    assertEquals(null, Game.scripts().attach(this.host, new ScriptBinding("missing")));
    assertTrue(Game.scripts().getDiagnostics().getLast().message().contains("No script definition"));
  }

  @Test
  void genericMapObjectLoadingAttachesEntityScriptsAfterTheEntityIsLoaded() {
    ScriptDefinition definition = new ScriptDefinition("java-test", "java", null, JavaEntityScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("java-test");
    binding.setParameter("speed", "7");
    MapObject mapObject = new MapObject("TEST");
    mapObject.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(List.of(binding)));
    this.host = new TestEntity();

    new TestLoader().afterLoad(List.of(this.host), mapObject);
    assertNotNull(this.host.scripts());
    assertEquals(0, JavaEntityScript.loaded);
    this.host.attachControllers();
    assertEquals(0, JavaEntityScript.loaded);
    this.host.loaded(null);
    this.host.scripts().update();

    assertEquals(1, JavaEntityScript.loaded);
    assertEquals(7, JavaEntityScript.configuredSpeed);
    assertEquals(1, JavaEntityScript.updates);
    this.host.detachControllers();
    assertEquals(1, JavaEntityScript.unloaded);
  }

  @Test
  void scriptControllerCopiesAndReplacesBindingsWithoutExposingMutableConfiguration() {
    ScriptBinding original = new ScriptBinding("first");
    original.setParameter("speed", "4");
    this.host = new TestEntity();
    EntityScriptController<TestEntity> controller = new EntityScriptController<>(this.host, List.of(original));

    original.setParameter("speed", "99");

    assertEquals("4", controller.getBindings().getFirst().getParameters().get("speed"));
  }

  @Test
  void inheritedEntityBindingsApplyToSpawnedSubtypesAndExplicitBindingsOverrideThem() {
    ScriptBinding inherited = new ScriptBinding("java-test");
    inherited.setParameter("speed", "4");
    EntityScriptBinding defaults = new EntityScriptBinding(TestEntity.class);
    defaults.getScripts().add(inherited);
    Game.scripts().setEntityBindings(List.of(defaults));
    this.host = new DerivedTestEntity();
    ScriptBinding explicit = new ScriptBinding("java-test");
    explicit.setParameter("speed", "9");
    this.host.addController(new EntityScriptController<>(this.host, List.of(explicit)));

    Game.scripts().configure(this.host);

    assertNotNull(this.host.scripts());
    assertEquals("9", this.host.scripts().getBindings().getFirst().getParameters().get("speed"));
    assertEquals("9", this.host.scripts().getExplicitBindings().getFirst().getParameters().get("speed"));
  }

  @Test
  void exactEntityBindingsDoNotApplyToSubtypes() {
    EntityScriptBinding exact = new EntityScriptBinding(TestEntity.class);
    exact.setInherited(false);
    exact.getScripts().add(new ScriptBinding("java-test"));
    Game.scripts().setEntityBindings(List.of(exact));
    this.host = new DerivedTestEntity();

    Game.scripts().configure(this.host);

    assertEquals(null, this.host.scripts());
  }

  @Test
  void contextOwnedListenersAreRemovedOnClose() {
    List<Runnable> listeners = new java.util.ArrayList<>();
    ScriptDefinition definition = new ScriptDefinition("owned", "java", null,
      JavaEntityScript.class.getName(), ScriptHostType.ENTITY);
    ScriptContext<TestEntity> context = new ScriptContext<>(definition, new ScriptBinding("owned"), new TestEntity());
    Runnable listener = () -> {};

    context.listen(listeners::add, listeners::remove, listener);
    assertEquals(List.of(listener), listeners);

    context.close();
    assertTrue(listeners.isEmpty());
  }

  @Test
  void reloadKeepsEntityAttachmentsOwnedAndUpdatedByTheirController() {
    ScriptDefinition definition = new ScriptDefinition("controller-reload", "java", null,
      JavaEntityScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding(definition.getId());
    binding.setParameter("speed", "3");
    this.host = new TestEntity();
    this.host.setController(EntityScriptController.class, new EntityScriptController<>(this.host, List.of(binding)));
    this.host.attachControllers();
    this.host.loaded(null);

    assertTrue(Game.scripts().reload(definition.getId()));
    this.host.scripts().update();
    Game.scripts().update();

    assertEquals(2, JavaEntityScript.loaded);
    assertEquals(1, JavaEntityScript.unloaded);
    assertEquals(1, JavaEntityScript.updates);
  }

  @Test
  void changingDefinitionConfigurationInvalidatesTheCompiledGeneration() {
    CountingProvider provider = new CountingProvider();
    Game.scripts().registerProvider(provider);
    ScriptDefinition definition = new ScriptDefinition(
      "cached-test",
      provider.language(),
      "de/gurkenlabs/litiengine/scripting/ScriptRuntimeTests.class",
      JavaEntityScript.class.getName(),
      ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    this.host = new TestEntity();

    Game.scripts().attach(this.host, new ScriptBinding(definition.getId()));
    Game.scripts().detach(this.host);
    definition.setTargetType(TestEntity.class.getName());
    Game.scripts().setDefinitions(List.of(definition));
    Game.scripts().attach(this.host, new ScriptBinding(definition.getId()));

    assertEquals(2, provider.compilations);
  }

  @Test
  void scriptGlobalsStoresAndFiresChangeListeners() {
    ScriptGlobals globals = Game.scripts().globals();
    globals.clear();
    java.util.concurrent.atomic.AtomicInteger changes = new java.util.concurrent.atomic.AtomicInteger();

    globals.onChanged("score", (oldVal, newVal) -> {
      changes.incrementAndGet();
      assertEquals(100, newVal);
    });

    globals.put("score", 100);
    assertEquals(100, globals.get("score", Integer.class));
    assertEquals(1, changes.get());

    globals.clear();
  }

  @Test
  void javaScriptProviderCompilesSourceFiles() throws Exception {
    ScriptDefinition definition = new ScriptDefinition("dyn-java", "java", null, "DynamicScript", ScriptHostType.GAME);
    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class DynamicScript extends GameScript {
        @Override
        protected void onStarted() {
          globals.put("dynTest", 42);
        }
      }
      """;
    CompiledScript compiled = JavaScriptProvider.compileSource(definition, null, code, getClass().getClassLoader());
    assertNotNull(compiled);
    assertEquals("DynamicScript", compiled.implementationType().getSimpleName());

    ScriptInstance instance = compiled.create();
    ScriptContext<Object> context = new ScriptContext<>(definition, new ScriptBinding("dyn-java"), new Object());
    instance.attach(context);
    assertEquals(42, Game.scripts().globals().get("dynTest", Integer.class));
    instance.detach();
  }

  @Test
  void javaLanguageServiceCompletesGlobalsAndHostMembers() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("test-creature", "java", null, "CreatureScript1", ScriptHostType.ENTITY);
    definition.setTargetType(de.gurkenlabs.litiengine.entities.Creature.class.getName());

    String code = """
      import de.gurkenlabs.litiengine.entities.Creature;
      import de.gurkenlabs.litiengine.scripting.*;
      public class CreatureScript1 extends CreatureScript {
        @Override
        protected void onLoaded() {
          globals.
        }
        @Override
        public void update() {
          host().
        }
      }
      """;

    // Position after 'globals.' on line 5 (0-indexed: line 5, col 12)
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptLanguageService.Completion> globalsCompletions = service.complete(doc, new ScriptLanguageService.Position(5, 12));
    assertTrue(globalsCompletions.stream().anyMatch(c -> c.label().equals("put")), "Should offer put on globals.");
    assertTrue(globalsCompletions.stream().anyMatch(c -> c.label().equals("get")), "Should offer get on globals.");

    // Position after 'host().' on line 9 (0-indexed: line 9, col 11)
    List<ScriptLanguageService.Completion> hostCompletions = service.complete(doc, new ScriptLanguageService.Position(9, 11));
    assertTrue(hostCompletions.stream().anyMatch(c -> c.label().equals("getCenter")), "Should offer getCenter on host().");
    assertTrue(hostCompletions.stream().anyMatch(c -> c.label().equals("die")), "Should offer die on host().");
  }

  @Test
  void javaLanguageServiceInfersParameterTypesForNewExpression() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("test-creature", "java", null, "CreatureScript1", ScriptHostType.ENTITY);
    definition.setTargetType(de.gurkenlabs.litiengine.entities.Creature.class.getName());

    String code = """
      import de.gurkenlabs.litiengine.entities.Creature;
      import de.gurkenlabs.litiengine.scripting.*;
      public class CreatureScript1 extends CreatureScript {
        @Override
        public void update() {
          host().addCombatEntityListener(new 
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptLanguageService.Completion> completions = service.complete(doc, new ScriptLanguageService.Position(5, 41));

    assertTrue(completions.stream().anyMatch(c -> c.label().contains("CombatEntityListener")), "Should suggest CombatEntityListener when typing new for addCombatEntityListener.");
    assertFalse(completions.stream().anyMatch(c -> c.label().equals("addController")), "Should NOT suggest host methods when typing new inside argument.");
    assertFalse(completions.stream().anyMatch(c -> c.label().equals("actions")), "Should NOT suggest host fields when typing new inside argument.");
  }

  @Test
  void javaLanguageServiceProvidesImplementAbstractMethodsCodeAction() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("test-creature", "java", null, "CreatureScript1", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.entities.CombatEntityListener;
      public class Listener implements CombatEntityListener {
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptLanguageService.CodeAction> actions = service.codeActions(doc, new ScriptLanguageService.Range(new ScriptLanguageService.Position(1, 15), new ScriptLanguageService.Position(1, 23)), List.of());

    assertTrue(actions.stream().anyMatch(a -> a.title().contains("Implement abstract methods")), "Should offer code action to implement abstract methods for CombatEntityListener.");
  }

  @Test
  void javaLanguageServiceReturnsRichDocumentationOnHover() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("test-game", "java", null, "GameScript1", ScriptHostType.GAME);

    String code = """
      import de.gurkenlabs.litiengine.scripting.GameScript;
      public class GameScript1 extends GameScript {
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    var hover = service.hover(doc, new ScriptLanguageService.Position(1, 37));

    assertTrue(hover.isPresent(), "Hover should be present for GameScript.");
    assertTrue(hover.get().markdown().contains("Lifecycle Callbacks"), "Hover documentation should include GameScript lifecycle callbacks.");
    assertTrue(hover.get().markdown().contains("onLoaded"), "Hover documentation should include onLoaded details.");
  }

  public static final class JavaEntityScript extends EntityScript<TestEntity> {
    static int loaded;
    static int unloaded;
    static int messages;
    static int updates;
    static int configuredSpeed;

    @ScriptProperty(required = true) private int speed;

    public JavaEntityScript() {}

    @Override protected void loaded() { loaded++; configuredSpeed = this.speed; }
    @Override protected void unloaded() { unloaded++; }
    @Override protected void message(EntityMessageEvent event) { messages++; }
    @Override public void update() { updates++; }

    static void reset() {
      loaded = 0;
      unloaded = 0;
      messages = 0;
      updates = 0;
      configuredSpeed = 0;
    }
  }

  public static class TestEntity extends Entity {
    public TestEntity() {}
  }

  public static final class DerivedTestEntity extends TestEntity {
    public DerivedTestEntity() {}
  }

  private static final class TestLoader extends MapObjectLoader {
    private TestLoader() { super("TEST"); }
    @Override public Collection<IEntity> load(Environment environment, IMapObject mapObject) { return List.of(); }
  }

  private static final class CountingProvider implements ScriptProvider {
    private int compilations;

    @Override public String language() { return "counting-test"; }

    @Override
    public CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) {
      this.compilations++;
      return new CompiledScript() {
        @Override public ScriptInstance create() { return new JavaEntityScript(); }
        @Override public Class<? extends ScriptInstance> implementationType() { return JavaEntityScript.class; }
      };
    }
  }

  @Test
  void javaLanguageServiceAutoformatsSourceCode() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    String unformatted = """
      public class CreatureScript3 extends CreatureScript{
      private static int cnt;
      protected void onLoaded(){
      }
      public void update(){
      if(cnt>0){
      cnt++;
      }
      }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, unformatted, 1, null);
    String formatted = service.format(doc);

    assertTrue(formatted.contains("public class CreatureScript3 extends CreatureScript {"));
    assertTrue(formatted.contains("  private static int cnt;"));
    assertTrue(formatted.contains("    if(cnt>0) {"));
    assertTrue(formatted.contains("      cnt++;"));
  }
}
