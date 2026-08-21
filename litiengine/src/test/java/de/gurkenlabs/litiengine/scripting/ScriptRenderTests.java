package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityRenderEvent;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.RenderType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptRenderTests {
  private Creature host;
  private Environment environment;

  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(200, 200));
    when(map.getSizeInTiles()).thenReturn(new Dimension(20, 20));
    this.environment = new Environment(map);
    this.environment.init();

    this.host = new Creature();
    this.environment.add(this.host);
    Game.world().loadEnvironment(this.environment);
    this.environment.load();
  }

  @AfterEach
  void cleanUp() {
    if (this.host != null) {
      Game.scripts().detach(this.host);
    }
    if (this.environment != null) {
      Game.scripts().detach(this.environment);
    }
    Game.scripts().clearDiagnostics();
    TestRenderScript.reset();
    FailingRenderScript.reset();
  }

  @Test
  void testEntityScriptRenderHook() {
    ScriptDefinition definition = new ScriptDefinition("render-test", "java", null, TestRenderScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("render-test");

    ScriptInstance instance = Game.scripts().attach(this.host, binding);
    assertNotNull(instance);

    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();

    Game.graphics().renderEntity(g, this.host);
    assertEquals(1, TestRenderScript.renderCount);

    Game.scripts().detach(this.host);
    Game.graphics().renderEntity(g, this.host);
    assertEquals(1, TestRenderScript.renderCount);
  }

  @Test
  void testEnvironmentScriptRenderHook() {
    ScriptDefinition definition = new ScriptDefinition("env-render-test", "java", null, TestEnvironmentRenderScript.class.getName(), ScriptHostType.ENVIRONMENT);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("env-render-test");

    ScriptInstance instance = Game.scripts().attach(this.environment, binding);
    assertNotNull(instance);

    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();

    this.environment.render(g);
    assertTrue(TestEnvironmentRenderScript.renderCount >= 1);
  }

  @Test
  void testRenderFailureDetachesScript() {
    ScriptDefinition definition = new ScriptDefinition("failing-render-test", "java", null, FailingRenderScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("failing-render-test");

    ScriptInstance instance = Game.scripts().attach(this.host, binding);
    assertNotNull(instance);

    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();

    Game.graphics().renderEntity(g, this.host);
    assertEquals(1, FailingRenderScript.renderAttempts);
    assertFalse(Game.scripts().getDiagnostics().isEmpty());
  }


  public static class TestRenderScript extends CreatureScript {
    static int renderCount = 0;

    static void reset() {
      renderCount = 0;
    }

    @Override
    protected void onRender(Graphics2D g) {
      renderCount++;
    }
  }

  public static class TestEnvironmentRenderScript extends EnvironmentScript {
    static int renderCount = 0;

    static void reset() {
      renderCount = 0;
    }

    @Override
    protected void onRender(Graphics2D g) {
      renderCount++;
    }
  }

  public static class FailingRenderScript extends CreatureScript {
    static int renderAttempts = 0;

    static void reset() {
      renderAttempts = 0;
    }

    @Override
    protected void onRender(Graphics2D g) {
      renderAttempts++;
      throw new RuntimeException("Simulated render crash");
    }
  }
}
