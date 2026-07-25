package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.Java2DPipeline;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RenderComponentTests {
  private Java2DPipeline configuredPipeline;
  private String direct3d;
  private String openGl;

  @BeforeEach
  void saveSystemProperties() {
    configuredPipeline = Game.config().graphics().getJava2DPipeline();
    direct3d = System.getProperty("sun.java2d.d3d");
    openGl = System.getProperty("sun.java2d.opengl");
  }

  @AfterEach
  void restoreSystemProperties() {
    Game.config().graphics().setJava2DPipeline(configuredPipeline);
    restoreProperty("sun.java2d.d3d", direct3d);
    restoreProperty("sun.java2d.opengl", openGl);
  }

  @Test
  void openGlDisablesDirect3d() {
    configure(Java2DPipeline.OPENGL);

    assertEquals("true", System.getProperty("sun.java2d.opengl"));
    assertEquals("false", System.getProperty("sun.java2d.d3d"));
  }

  @Test
  void direct3dDisablesOpenGl() {
    configure(Java2DPipeline.DIRECT3D);

    assertEquals("false", System.getProperty("sun.java2d.opengl"));
    assertEquals("true", System.getProperty("sun.java2d.d3d"));
  }

  @Test
  void softwareDisablesHardwarePipelines() {
    configure(Java2DPipeline.SOFTWARE);

    assertEquals("false", System.getProperty("sun.java2d.opengl"));
    assertEquals("false", System.getProperty("sun.java2d.d3d"));
  }

  private static void restoreProperty(String key, String value) {
    if (value == null) {
      System.clearProperty(key);
    } else {
      System.setProperty(key, value);
    }
  }

  private static void configure(Java2DPipeline pipeline) {
    Game.config().graphics().setJava2DPipeline(pipeline);
    RenderComponent.configurePipeline();
  }
}
