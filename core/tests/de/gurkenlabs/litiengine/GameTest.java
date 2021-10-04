package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class GameTest {
  // test-only helper method to call the package-private Game.terminate()
  public static void terminateGame() {
    Game.terminate();
  }

  @AfterEach
  public void cleanup() {
    final File configFile = new File(Game.config().getFileName());
    if (configFile.exists()) {
      configFile.delete();
    }

    terminateGame();
  }

  private class Status {
    boolean wasCalled = false;
  }

  @Test
  public void testStartup() {
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
}
