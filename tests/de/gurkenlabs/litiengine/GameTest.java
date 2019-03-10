package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class GameTest {

  @AfterEach
  public void cleanup() {
    final File configFile = new File(Game.config().getFileName());
    if (configFile.exists()) {
      configFile.delete();
    }
    
    Game.terminate();
  }

  private class Status {
    boolean wasCalled = false;
  }

  @Test
  public void testStartup() {
    final Status initialized = new Status();
    final Status started = new Status();

    Game.addGameListener(new GameListener() {
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

    Game.init(Game.COMMADLINE_ARG_NOGUI);

    assertTrue(initialized.wasCalled);
    assertFalse(started.wasCalled);
    assertNotNull(Game.renderLoop());
    assertNotNull(Game.loop());
    assertNotNull(Game.world().camera());
    assertNotNull(Game.screens());
    assertNotNull(Game.physics());
    assertNotNull(Game.graphics());

    Game.start();
    assertTrue(started.wasCalled);
  }
}
