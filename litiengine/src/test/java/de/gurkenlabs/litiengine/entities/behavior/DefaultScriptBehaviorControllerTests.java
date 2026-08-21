package de.gurkenlabs.litiengine.entities.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.scripting.EntityScript;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultScriptBehaviorControllerTests {
  private TestEntity entity;

  public static class TestEntity extends Entity {}

  public static class TestScript extends EntityScript<TestEntity> {
    static int updates;
    static int loaded;
    static int unloaded;

    static void reset() {
      updates = 0;
      loaded = 0;
      unloaded = 0;
    }

    @Override
    protected void onLoaded() {
      loaded++;
    }

    @Override
    protected void onUnloaded() {
      unloaded++;
    }

    @Override
    public void update() {
      updates++;
    }
  }

  @BeforeEach
  void setUp() {
    TestScript.reset();
    Game.scripts().clearDiagnostics();
    Game.scripts().setDefinitions(List.of(
      new ScriptDefinition("test-behavior", "java", null, TestScript.class.getName(), ScriptHostType.ENTITY)
    ));
    this.entity = new TestEntity();
  }

  @Test
  void testConstructorsAndScriptLoading() {
    DefaultScriptBehaviorController<TestEntity> controller = new DefaultScriptBehaviorController<>(this.entity, "test-behavior");

    assertEquals(this.entity, controller.getEntity());
    assertNotNull(controller.getScriptController());
    assertEquals(1, controller.getLoadedScripts().size());
    assertEquals("test-behavior", controller.getLoadedScripts().getFirst().getScript());

    controller.unloadScript("test-behavior");
    assertTrue(controller.getLoadedScripts().isEmpty());

    controller.loadScript("test-behavior");
    assertEquals(1, controller.getLoadedScripts().size());

    controller.unloadAllScripts();
    assertTrue(controller.getLoadedScripts().isEmpty());
  }

  @Test
  void testScriptBehaviorLifecycle() {
    DefaultScriptBehaviorController<TestEntity> controller = new DefaultScriptBehaviorController<>(this.entity);
    this.entity.setController(IBehaviorController.class, controller);
    this.entity.loaded(null);

    controller.loadScript(new ScriptBinding("test-behavior"));
    controller.attach();

    assertTrue(controller.getScriptController().isAttached());
    assertEquals(1, TestScript.loaded);

    controller.update();
    assertEquals(1, TestScript.updates);

    controller.detach();
    assertFalse(controller.getScriptController().isAttached());
    assertEquals(1, TestScript.unloaded);
  }
}
