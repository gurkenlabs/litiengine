package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.test.GameTestSuite;
import de.gurkenlabs.litiengine.scripting.GameScript;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.awt.AWTError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
public class GameTest {
  // test-only helper method to call the package-private Game.terminate()
  public static void terminateGame() {
    Game.terminate();
  }

  @AfterEach
  void cleanup() throws IOException {
    final Path configFile = Game.config().getPath();

    Files.deleteIfExists(configFile);

    terminateGame();
    Game.scripts().setGameBindings(List.of());
    Game.scripts().setDefinitions(List.of());
    LifecycleGameScript.stops = 0;
  }

  private static class Status {
    boolean wasCalled = false;
  }

  @Test
  void testStartup() {
    final Status initialized = new Status();
    final Status started = new Status();

    Game.addGameListener(
      new GameListener() {
        @Override
        public void initialized(String... args) {
          initialized.wasCalled = true;
        }

        @Override
        public void started() {
          started.wasCalled = true;
        }
      });

    assertFalse(initialized.wasCalled);

    // other unit tests might also initialize the game and depending on the concurrent execution
    // order, this test might
    // fail then, which is why as a temporary fix, we terminate the game beforehand
    // this doesn't seem to be an issue when running the unit tests with gradl. I assume this has
    // something to do with
    // static classes and the way the tests are executed but I don't know enough about the gradle
    // test execution to be
    // sure about the reason here.
    Game.terminate();
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    assertTrue(initialized.wasCalled);
    assertFalse(started.wasCalled);
    assertNotNull(Game.loop());
    assertNotNull(Game.world().camera());
    assertNotNull(Game.screens());
    assertNotNull(Game.physics());
    assertNotNull(Game.graphics());

    Game.start();
    assertTrue(started.wasCalled);
  }

  @Test
  void vetoedExitPreparationDoesNotDetachRunningGameScripts() {
    Game.terminate();
    configureLifecycleScript();
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Game.start();

    assertEquals(0, LifecycleGameScript.stops);

    GameListener veto = new GameListener() {
      @Override public boolean terminating() { return false; }
    };
    Game.addGameListener(veto);
    try {
      assertFalse(Game.terminating());
      assertEquals(0, LifecycleGameScript.stops);
    } finally {
      Game.removeGameListener(veto);
    }
  }

  @Test
  void terminationDetachesScriptsBeforeTheNextGameLoopStarts() {
    Game.terminate();
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    int initializedUpdatables = Game.loop().getUpdatableCount();
    Game.start();
    int startRegistrations = Game.loop().getUpdatableCount() - initializedUpdatables;
    Game.terminate();

    configureLifecycleScript();
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    int initialUpdatables = Game.loop().getUpdatableCount();
    Game.start();
    assertEquals(initialUpdatables + startRegistrations + 1, Game.loop().getUpdatableCount());

    Game.terminate();
    assertEquals(1, LifecycleGameScript.stops);
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    int restartedUpdatables = Game.loop().getUpdatableCount();
    Game.start();
    assertEquals(restartedUpdatables + startRegistrations + 1, Game.loop().getUpdatableCount());
  }

  private static void configureLifecycleScript() {
    LifecycleGameScript.stops = 0;
    Game.scripts().setDefinitions(List.of(new ScriptDefinition("lifecycle", "java", null,
      LifecycleGameScript.class.getName(), ScriptHostType.GAME)));
    Game.scripts().setGameBindings(List.of(new ScriptBinding("lifecycle")));
  }

  public static final class LifecycleGameScript extends GameScript {
    private static int stops;

    @Override
    protected void onStopped() {
      stops++;
    }
  }

  @Test
  void testSwingThreadAssertionsInsideSwing() {
    assertThrows(AWTError.class, () -> Game.init(
      () -> {
      },
      () -> {
      },
      Game.COMMANDLINE_ARG_NOGUI));
  }

}
