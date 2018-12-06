package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {
  private static boolean wasCalled;

  @BeforeEach
  public void prepare() {
    wasCalled = false;
  }

  @AfterEach
  public void cleanup() {
    final File configFile = new File(Game.getConfiguration().getFileName());
    if (configFile.exists()) {
      configFile.delete();
    }
  }

  @Test
  public void testGameInitialization() {
    Game.init(Game.COMMADLINE_ARG_NOGUI);

    assertNotNull(Game.getRenderLoop());
    assertNotNull(Game.getLoop());
    assertNotNull(Game.getCamera());
    assertNotNull(Game.getScreenManager());
    assertNotNull(Game.getPhysicsEngine());
    assertNotNull(Game.getRenderEngine());
  }

  @Test
  public void testInitializedListeners() {
    Game.addGameListener(new GameAdapter() {
      @Override
      public void initialized(String... args) {
        wasCalled = true;
      }
    });

    Game.init(Game.COMMADLINE_ARG_NOGUI);

    assertTrue(wasCalled);
  }

  @Test
  public void testStartedListeners() {
    Game.addGameListener(new GameAdapter() {
      @Override
      public void started() {
        wasCalled = true;
      }
    });

    Game.init(Game.COMMADLINE_ARG_NOGUI);
    Game.start();
    assertTrue(wasCalled);
  }
}
