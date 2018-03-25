package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

public class GameTest {
  @Test
  public void testGameInitialization() {
    try {
      Game.init(Game.COMMADLINE_ARG_NOGUI);

      assertNotNull(Game.getRenderLoop());
      assertNotNull(Game.getLoop());
      assertNotNull(Game.getCamera());
      assertNotNull(Game.getScreenManager());
      assertNotNull(Game.getPhysicsEngine());
      assertNotNull(Game.getRenderEngine());
    } finally {
      final File configFile = new File(Game.getConfiguration().getFileName());
      if (configFile.exists()) {
        configFile.delete();
      }
    }
  }
}
