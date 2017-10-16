package de.gurkenlabs.litiengine;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class GameTest {
  @Test
  public void testGameInitialization() {
    try {
      Game.init();

      Assert.assertNotNull(Game.getRenderLoop());
      Assert.assertNotNull(Game.getLoop());
      Assert.assertNotNull(Game.getCamera());
      Assert.assertNotNull(Game.getScreenManager());
      Assert.assertNotNull(Game.getPhysicsEngine());
      Assert.assertNotNull(Game.getRenderEngine());
    } finally {
      final File configFile = new File(Game.getConfiguration().getFileName());
      if (configFile.exists()) {
        configFile.delete();
      }
    }
  }
}
