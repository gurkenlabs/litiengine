package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    this.host.setMapId(42);
    this.host.setName("Goblin");

    assertEquals(null, Game.scripts().attach(this.host, new ScriptBinding("missing")));
    String msg = Game.scripts().getDiagnostics().getLast().message();
    assertTrue(msg.contains("No script definition"));
    assertTrue(msg.contains("entity #42"));
    assertTrue(msg.contains("'Goblin'"));

    Game.scripts().clearDiagnostics(this.host);
    assertTrue(Game.scripts().getDiagnostics().isEmpty());
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

  public static class Gen1WorkingScript extends EntityScript<TestEntity> {
    static int loadedCount = 0;
    static int updates = 0;

    @Override
    protected void onLoaded() {
      loadedCount++;
    }

    @Override
    public void update() {
      updates++;
    }
  }

  public static class Gen2FailingScript extends EntityScript<TestEntity> {
    @Override
    protected void onLoaded() {
      throw new RuntimeException("oops");
    }
  }

  @Test
  void reloadRestoresLastWorkingGenerationWhenReplacementAttachmentFails() {
    Gen1WorkingScript.loadedCount = 0;
    Gen1WorkingScript.updates = 0;
    java.util.concurrent.atomic.AtomicInteger compileCount = new java.util.concurrent.atomic.AtomicInteger();

    ScriptProvider provider = new ScriptProvider() {
      @Override
      public String language() {
        return "rollback-test";
      }

      @Override
      public CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) {
        int gen = compileCount.incrementAndGet();
        return new CompiledScript() {
          @Override
          public ScriptInstance create() {
            return gen == 2 ? new Gen2FailingScript() : new Gen1WorkingScript();
          }

          @Override
          public Class<? extends ScriptInstance> implementationType() {
            return gen == 2 ? Gen2FailingScript.class : Gen1WorkingScript.class;
          }
        };
      }
    };

    Game.scripts().registerProvider(provider);
    ScriptDefinition definition = new ScriptDefinition("rollback-script", provider.language(), null, "RollbackScript", ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    this.host = new TestEntity();
    ScriptInstance initial = Game.scripts().attach(this.host, new ScriptBinding("rollback-script"));
    assertNotNull(initial);
    assertEquals(1, Gen1WorkingScript.loadedCount);
    Game.scripts().update();
    assertEquals(1, Gen1WorkingScript.updates);

    boolean reloaded = Game.scripts().reload("rollback-script");
    assertFalse(reloaded);

    assertEquals(2, Gen1WorkingScript.loadedCount);
    Game.scripts().update();
    assertEquals(2, Gen1WorkingScript.updates);
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
  void setDefinitionsWithChangedConfigThenFailedReloadPreservesWorkingGeneration() {
    Gen1WorkingScript.loadedCount = 0;
    Gen1WorkingScript.updates = 0;
    java.util.concurrent.atomic.AtomicInteger compileCount = new java.util.concurrent.atomic.AtomicInteger();

    ScriptProvider provider = new ScriptProvider() {
      @Override
      public String language() {
        return "setdef-rollback-test";
      }

      @Override
      public CompiledScript compile(ScriptDefinition definition, java.net.URL source, ClassLoader parent) {
        int gen = compileCount.incrementAndGet();
        return new CompiledScript() {
          @Override
          public ScriptInstance create() {
            return gen == 2 ? new Gen2FailingScript() : new Gen1WorkingScript();
          }

          @Override
          public Class<? extends ScriptInstance> implementationType() {
            return gen == 2 ? Gen2FailingScript.class : Gen1WorkingScript.class;
          }
        };
      }
    };

    Game.scripts().registerProvider(provider);
    ScriptDefinition definition = new ScriptDefinition(
      "setdef-rollback", provider.language(), null, "SetdefRollbackScript", ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    this.host = new TestEntity();
    ScriptInstance initial = Game.scripts().attach(this.host, new ScriptBinding("setdef-rollback"));
    assertNotNull(initial);
    assertEquals(1, Gen1WorkingScript.loadedCount);
    Game.scripts().update();
    assertEquals(1, Gen1WorkingScript.updates);

    // Change the definition config while attachments are still active
    definition.setTargetType(TestEntity.class.getName());
    Game.scripts().setDefinitions(List.of(definition));

    // Reload should fail (Gen2 throws in onLoaded) and roll back to Gen1
    boolean reloaded = Game.scripts().reload("setdef-rollback");
    assertFalse(reloaded);

    // Gen1 must still be attached and functional
    assertEquals(2, Gen1WorkingScript.loadedCount);
    Game.scripts().update();
    assertEquals(2, Gen1WorkingScript.updates);
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
  void javaScriptProviderLoadsPackagedSourceWhenDefinitionUsesSimpleImplementation() throws Exception {
    ScriptDefinition definition = new ScriptDefinition(
        "packaged-java", "java", null, "PackagedScript", ScriptHostType.GAME);
    String code = """
      package example.scripts;
      import de.gurkenlabs.litiengine.scripting.*;
      public final class PackagedScript extends GameScript {}
      """;

    try {
      try (CompiledScript compiled = JavaScriptProvider.compileSource(
          definition, null, code, getClass().getClassLoader())) {
        assertEquals("example.scripts.PackagedScript", compiled.implementationType().getName());
        assertNotNull(compiled.create());
      }
    } catch (ScriptException error) {
      fail(error.getDiagnostics().toString(), error);
    }
  }

  @Test
  void dynamicJavaSourceOverridesAParentClasspathClassWithTheSameName() throws Exception {
    ScriptDefinition definition = new ScriptDefinition(
        "shadowed-java", "java", null, ParentVisibleScript.class.getName(), ScriptHostType.GAME);
    String code = """
      package de.gurkenlabs.litiengine.scripting;
      public class ParentVisibleScript extends GameScript {
        public int marker() { return 2; }
      }
      """;

    try (CompiledScript compiled = JavaScriptProvider.compileSource(
        definition, null, code, getClass().getClassLoader())) {
      ScriptInstance instance = compiled.create();
      assertEquals(2, instance.getClass().getMethod("marker").invoke(instance));
      assertFalse(instance.getClass() == ParentVisibleScript.class);
    }
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
  void javaLanguageServiceCompletesPartiallyTypedStaticMemberInsideIfExpression() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(
        null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);
    ScriptDefinition definition = new ScriptDefinition(
        "test-creature", "java", null, "NewScript", ScriptHostType.ENTITY);
    definition.setTargetType(de.gurkenlabs.litiengine.entities.Creature.class.getName());
    String code = """
      import de.gurkenlabs.litiengine.Game;
      public class NewScript extends CreatureScript {
        public void update() {
          if (Game.ti
        }
      }
      """;
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);

    List<ScriptLanguageService.Completion> completions = service.complete(
        doc, new ScriptLanguageService.Position(3, "    if (Game.ti".length()));

    assertTrue(completions.stream().anyMatch(c -> c.label().equals("time")),
        "Should resolve Game as the static completion receiver.");
    assertFalse(completions.stream().anyMatch(c -> c.label().equals("getTickVelocity")),
        "Should not fall back to Creature host members.");
    assertFalse(completions.stream().anyMatch(c -> c.label().equals("this")),
        "Should not fall back to Java keywords.");
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

  @Test
  void javaLanguageServiceRenamesSymbolsAcrossDocument() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("CreatureScript3", "java", null, "CreatureScript3", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      @ScriptInfo(id = "CreatureScript3", host = ScriptHostType.ENTITY)
      public class CreatureScript3 extends CreatureScript {
        private int counter = 0;
        @Override
        public void update() {
          counter++;
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptLanguageService.TextEdit> edits = service.rename(doc, new ScriptLanguageService.Position(2, 15), "HeroScript");

    assertEquals(2, edits.size(), "Should rename class name in annotation and class header.");
    assertTrue(edits.stream().allMatch(e -> e.text().equals("HeroScript")));
  }

  @Test
  void javaLanguageServiceProvidesSyntaxErrorQuickFixes() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("CreatureScript3", "java", null, "CreatureScript3", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class CreatureScript3 extends CreatureScript {
        @Override
        public void update()
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptLanguageService.CodeAction> actions = service.codeActions(doc, new ScriptLanguageService.Range(new ScriptLanguageService.Position(3, 0), new ScriptLanguageService.Position(3, 20)), List.of());

    assertTrue(actions.stream().anyMatch(a -> a.title().contains("Add method body")), "Should suggest quick fix to add method body when '{' or ';' is expected.");
  }

  @Test
  void javaLanguageServiceSuggestsImportForCommonJdkTypesLikeColor() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("ColorTest", "java", null, "ColorTest", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class ColorTest extends CreatureScript {
        @Override
        public void update() {
          context().ui().floatText("test", host(), Color.RED);
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptDiagnostic> diags = List.of(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR, "ColorTest", null, 5, 45, "cannot find symbol: class Color"));
    List<ScriptLanguageService.CodeAction> actions = service.codeActions(doc, new ScriptLanguageService.Range(new ScriptLanguageService.Position(4, 44), new ScriptLanguageService.Position(4, 49)), diags);

    assertTrue(actions.stream().anyMatch(a -> a.title().equals("Import 'java.awt.Color'")),
        "Should offer exact match quickfix to import java.awt.Color instead of fuzzy renaming to ColorAdapter/ColorLayer.");
  }

  @Test
  void javaLanguageServiceDisplaysAllOverloadsAndDocumentationOnHover() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("HoverTest", "java", null, "HoverTest", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class HoverTest extends CreatureScript {
        @Override
        public void update() {
          context().ui().floatText("bulb", host(), java.awt.Color.RED);
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    // Position on floatText (line 4, col 20)
    var hover = service.hover(doc, new ScriptLanguageService.Position(4, 20));

    assertTrue(hover.isPresent(), "Hover should be present for floatText");
    String md = hover.get().markdown();
    assertTrue(md.contains("floatText(String"), "Hover markdown should include floatText signature: " + md);
    assertTrue(md.contains("Point2D"), "Hover markdown should include Point2D overload: " + md);
    assertTrue(md.contains("IEntity"), "Hover markdown should include IEntity overload: " + md);
    assertTrue(md.contains("Font"), "Hover markdown should include full 6-argument overload: " + md);
    assertTrue(md.contains("ScriptUiOverlay"), "Hover markdown should mention declaring class: " + md);
    assertTrue(md.contains("floating combat text"), "Hover markdown should include rich method documentation: " + md);
  }

  @Test
  void javaLanguageServiceSuggestsParametersInsideMethodCall() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("ShakeTest", "java", null, "ShakeTest", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class ShakeTest extends CreatureScript {
        @Override
        public void update() {
          context().camera().shake(
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    // Position inside shake( (line 4, col 31)
    var completions = service.complete(doc, new ScriptLanguageService.Position(4, 31));

    assertFalse(completions.isEmpty(), "Completions should not be empty inside shake(");
    // Should suggest parameter placeholders
    assertTrue(completions.stream().anyMatch(c -> c.label().contains("intensity") && c.label().contains("delay") && c.label().contains("duration")),
        "Should suggest parameter placeholders for shake(intensity, delay, duration)");
    // Should NOT suggest @ScriptProperty inside method arguments
    assertFalse(completions.stream().anyMatch(c -> c.label().startsWith("@ScriptProperty")),
        "Should not suggest @ScriptProperty inside method arguments");
  }

  @Test
  void javaLanguageServiceSuggestsColorConstantsInsideColorParameter() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("ColorParamTest", "java", null, "ColorParamTest", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class ColorParamTest extends CreatureScript {
        @Override
        public void update() {
          context().ui().floatText("bulb", host(), 
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    // Position inside floatText("bulb", host(),  (line 4, col 47)
    var completions = service.complete(doc, new ScriptLanguageService.Position(4, 47));

    assertTrue(completions.stream().anyMatch(c -> c.label().equals("Color.RED")),
        "Should suggest Color.RED for floatText color parameter");
  }

  @Test
  void javaLanguageServiceMethodCompletionInsertsParameterSnippets() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("SnippetTest", "java", null, "SnippetTest", ScriptHostType.ENTITY);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class SnippetTest extends CreatureScript {
        @Override
        public void update() {
          context().camera().sh
        }
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    // Position at context().camera().sh (line 4, col 27)
    var completions = service.complete(doc, new ScriptLanguageService.Position(4, 27));

    var shakeCompletion = completions.stream().filter(c -> c.label().equals("shake")).findFirst();
    assertTrue(shakeCompletion.isPresent(), "shake completion should be present");
    assertTrue(shakeCompletion.get().insertText().contains("${1:intensity}"),
        "shake completion insertText should be a snippet with tab-stops: " + shakeCompletion.get().insertText());
  }

  @Test
  void javaLanguageServiceFixesCyclicInheritance() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("EnvironmentScript", "java", null, "EnvironmentScript", ScriptHostType.ENVIRONMENT);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class EnvironmentScript extends EnvironmentScript {
        @Override
        public void onLoaded() {}
        @Override
        public void update() {}
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptDiagnostic> diags = List.of(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR, "EnvironmentScript", null, 2, 1, "cyclic inheritance involving EnvironmentScript"));
    List<ScriptLanguageService.CodeAction> actions = service.codeActions(doc, new ScriptLanguageService.Range(new ScriptLanguageService.Position(1, 0), new ScriptLanguageService.Position(1, 50)), diags);

    assertTrue(actions.stream().anyMatch(a -> a.title().contains("Qualify superclass")), "Should offer quick fix to qualify EnvironmentScript superclass.");
  }

  @Test
  void javaLanguageServiceFixesPublicClassNameMismatch() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(null, getClass().getClassLoader(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("EnvironmentScript2", "java", null, "EnvironmentScript2", ScriptHostType.ENVIRONMENT);

    String code = """
      import de.gurkenlabs.litiengine.scripting.*;
      public class HospitalMap extends EnvironmentScript {
        @Override
        public void onLoaded() {}
        @Override
        public void update() {}
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptDiagnostic> diags = List.of(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR, "EnvironmentScript2", null, 2, 1, "class HospitalMap is public, should be declared in a file named HospitalMap.java"));
    List<ScriptLanguageService.CodeAction> actions = service.codeActions(doc, new ScriptLanguageService.Range(new ScriptLanguageService.Position(1, 0), new ScriptLanguageService.Position(1, 50)), diags);

    assertTrue(actions.stream().anyMatch(a -> a.title().contains("Rename class in editor")), "Should offer quick fix to revert class name to match file name.");
  }
  @Test
  void javaLanguageServiceIncludesProjectClasspathInCompilerOptions() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(
        null, getClass().getClassLoader(), List.of(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("CreatureScript3", "java", null, "CreatureScript3", ScriptHostType.ENTITY);
    String code = """
      import de.gurkenlabs.litiengine.entities.Creature;
      import de.gurkenlabs.litiengine.scripting.*;
      @ScriptInfo(id = "CreatureScript3", host = ScriptHostType.ENTITY, target = Creature.class)
      public class CreatureScript3 extends CreatureScript {
        @Override
        public void update() {}
      }
      """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    ScriptLanguageService.Analysis analysis = service.analyze(doc);
    assertTrue(analysis.diagnostics().isEmpty(), "Compilation diagnostics should be empty: " + analysis.diagnostics());
  }
  @Test
  void javaLanguageServiceCompletesTypeSpecificMembersOnVariablesAndCasts() {
    ScriptLanguageService.Workspace workspace = new ScriptLanguageService.Workspace(
        null, getClass().getClassLoader(), List.of(), java.util.Map.of());
    JavaLanguageService service = new JavaLanguageService(workspace);

    ScriptDefinition definition = new ScriptDefinition("CreatureScript3", "java", null, "CreatureScript3", ScriptHostType.ENTITY);
    String code = """
      import de.gurkenlabs.litiengine.entities.Prop;
      import de.gurkenlabs.litiengine.scripting.*;
      public class CreatureScript3 extends EntityScript<Prop> {
        @Override
        public void update() {
          var prop = (Prop)host();
          prop.
        }
      }
      """;

    // Position after 'prop.' on line 6 (0-indexed: line 6, col 15)
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(null, code, 1, definition);
    List<ScriptLanguageService.Completion> completions = service.complete(doc, new ScriptLanguageService.Position(6, 9));

    assertTrue(completions.stream().anyMatch(c -> c.label().equals("getMaterial")),
        "Should offer Prop-specific method getMaterial on 'var prop = (Prop)host()'.");
    assertTrue(completions.stream().anyMatch(c -> c.label().equals("getState")),
        "Should offer Prop-specific method getState on 'var prop = (Prop)host()'.");

    // Also test direct host() completion when generic type Prop is in extends clause
    String hostCode = """
      import de.gurkenlabs.litiengine.entities.Creature;
      import de.gurkenlabs.litiengine.entities.Prop;
      import de.gurkenlabs.litiengine.scripting.*;
      @ScriptInfo(id = "CreatureScript3", host = ScriptHostType.ENTITY, target = Creature.class)
      public class CreatureScript3 extends EntityScript<Prop> {
        @Override
        public void update() {
          host().
        }
      }
      """;
    ScriptLanguageService.Document hostDoc = new ScriptLanguageService.Document(null, hostCode, 1, definition);
    List<ScriptLanguageService.Completion> hostCompletions = service.complete(hostDoc, new ScriptLanguageService.Position(7, 11));
    assertTrue(hostCompletions.stream().anyMatch(c -> c.label().equals("getMaterial")),
        "Should offer Prop-specific method getMaterial directly on host().");
  }

  @Test
  void doesNotAttachOrUpdateWhenScriptsDisabled() {
    try {
      Game.scripts().setEnabled(false);
      assertFalse(Game.scripts().isEnabled());

      ScriptDefinition definition = new ScriptDefinition("java-disabled", "java", null, JavaEntityScript.class.getName(), ScriptHostType.ENTITY);
      definition.setTargetType(TestEntity.class.getName());
      Game.scripts().setDefinitions(List.of(definition));
      ScriptBinding binding = new ScriptBinding("java-disabled");
      this.host = new TestEntity();

      ScriptInstance instance = Game.scripts().attach(this.host, binding);
      assertEquals(null, instance);
      assertEquals(0, JavaEntityScript.loaded);

      EntityScriptController<TestEntity> controller = new EntityScriptController<>(this.host, List.of(binding));
      this.host.addController(controller);
      controller.attach();
      assertFalse(controller.isAttached());

      Game.scripts().update();
      assertEquals(0, JavaEntityScript.updates);
    } finally {
      Game.scripts().setEnabled(true);
    }
  }
}

class ParentVisibleScript extends GameScript {
  public int marker() {
    return 1;
  }
}
