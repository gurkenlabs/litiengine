package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Keyboard;
import de.gurkenlabs.litiengine.input.KeyboardEntityController;
import de.gurkenlabs.litiengine.input.Mouse;
import de.gurkenlabs.litiengine.input.PlatformingMovementController;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptInputTests {
  private Creature host;
  private Environment environment;

  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();

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
    Game.scripts().clearDiagnostics();
    TestInputCreatureScript.reset();
  }

  @Test
  void testScriptInputLifecycleHooks() {
    ScriptDefinition definition = new ScriptDefinition("input-test", "java", null, TestInputCreatureScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("input-test");

    ScriptInstance instance = Game.scripts().attach(this.host, binding);
    assertNotNull(instance);

    Canvas dummy = new Canvas();
    KeyEvent pressA = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
    KeyEvent releaseA = new KeyEvent(dummy, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
    KeyEvent typeA = new KeyEvent(dummy, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'a');

    MouseEvent clickEvent = new MouseEvent(dummy, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);
    MouseEvent pressEvent = new MouseEvent(dummy, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);
    MouseEvent releaseEvent = new MouseEvent(dummy, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);

    Keyboard keyboard = (Keyboard) Input.keyboard();
    Mouse mouse = (Mouse) Input.mouse();

    keyboard.dispatchKeyEvent(pressA);
    keyboard.update();
    assertEquals(1, TestInputCreatureScript.keyPressedCount);

    keyboard.dispatchKeyEvent(releaseA);
    keyboard.update();
    assertEquals(1, TestInputCreatureScript.keyReleasedCount);
    assertEquals(1, TestInputCreatureScript.keyTypedCount);

    mouse.mouseClicked(clickEvent);
    mouse.update();
    assertEquals(1, TestInputCreatureScript.mouseClickedCount);

    mouse.mousePressed(pressEvent);
    mouse.update();
    assertEquals(1, TestInputCreatureScript.mousePressedCount);

    mouse.mouseReleased(releaseEvent);
    mouse.update();
    assertEquals(1, TestInputCreatureScript.mouseReleasedCount);

    // Detach and ensure events are unhooked
    Game.scripts().detach(this.host);
    keyboard.dispatchKeyEvent(pressA);
    keyboard.update();
    assertEquals(1, TestInputCreatureScript.keyPressedCount);
  }

  @Test
  void testScriptInputHelperAndBindings() {
    ScriptDefinition definition = new ScriptDefinition("input-test", "java", null, TestInputCreatureScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("input-test");

    TestInputCreatureScript script = (TestInputCreatureScript) Game.scripts().attach(this.host, binding);
    assertNotNull(script);

    AtomicInteger customKeyPresses = new AtomicInteger();
    Subscription sub = script.input().bindKey(KeyEvent.VK_SPACE, customKeyPresses::incrementAndGet);

    Canvas dummy = new Canvas();
    KeyEvent spaceEvent = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');

    Keyboard keyboard = (Keyboard) Input.keyboard();
    keyboard.dispatchKeyEvent(spaceEvent);
    keyboard.update();
    assertEquals(1, customKeyPresses.get());

    sub.unsubscribe();
    keyboard.dispatchKeyEvent(spaceEvent);
    keyboard.update();
    assertEquals(1, customKeyPresses.get());


    assertEquals(KeyEvent.VK_SPACE, ScriptInput.resolveKeyCode("SPACE"));
    assertEquals(KeyEvent.VK_ESCAPE, ScriptInput.resolveKeyCode("ESCAPE"));
    assertEquals(KeyEvent.VK_W, ScriptInput.resolveKeyCode("W"));
  }

  @Test
  void testMovementControllerConfigurationAndJumpHook() {
    ScriptDefinition definition = new ScriptDefinition("input-test", "java", null, TestInputCreatureScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("input-test");

    TestInputCreatureScript script = (TestInputCreatureScript) Game.scripts().attach(this.host, binding);
    assertNotNull(script);

    KeyboardEntityController<Creature> topDown = script.enableTopDownMovement();
    assertNotNull(topDown);
    assertTrue(topDown.getUpKeys().contains(KeyEvent.VK_W));

    PlatformingMovementController<Creature> platforming = script.enablePlatformingMovement();
    assertNotNull(platforming);
    assertTrue(platforming.getJumpKeys().contains(KeyEvent.VK_SPACE));

    // Perform jump action on entity
    this.host.perform(PlatformingMovementController.JUMP_ACTION);
    assertEquals(1, TestInputCreatureScript.jumpCount);
    assertEquals(1, TestInputCreatureScript.actionCount);

    script.disableMovementController();
  }

  public static class TestInputCreatureScript extends CreatureScript {
    static int keyPressedCount = 0;
    static int keyReleasedCount = 0;
    static int keyTypedCount = 0;
    static int mouseClickedCount = 0;
    static int mousePressedCount = 0;
    static int mouseReleasedCount = 0;
    static int jumpCount = 0;
    static int actionCount = 0;

    static void reset() {
      keyPressedCount = 0;
      keyReleasedCount = 0;
      keyTypedCount = 0;
      mouseClickedCount = 0;
      mousePressedCount = 0;
      mouseReleasedCount = 0;
      jumpCount = 0;
      actionCount = 0;
    }

    @Override
    protected void onKeyPressed(KeyEvent event) {
      keyPressedCount++;
    }

    @Override
    protected void onKeyReleased(KeyEvent event) {
      keyReleasedCount++;
    }

    @Override
    protected void onKeyTyped(KeyEvent event) {
      keyTypedCount++;
    }

    @Override
    protected void onMouseClicked(MouseEvent event) {
      mouseClickedCount++;
    }

    @Override
    protected void onMousePressed(MouseEvent event) {
      mousePressedCount++;
    }

    @Override
    protected void onMouseReleased(MouseEvent event) {
      mouseReleasedCount++;
    }

    @Override
    protected void onJump() {
      jumpCount++;
    }

    @Override
    protected void onAction(String action) {
      actionCount++;
    }
  }
}
