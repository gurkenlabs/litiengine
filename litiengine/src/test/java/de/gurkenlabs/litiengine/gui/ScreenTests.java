package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.awt.Dimension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class ScreenTests {

  @BeforeAll
  static void initialize() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void onResolutionChangedUpdatesScreenDimensions() {
    // arrange
    TestScreen screen = new TestScreen("test");
    Dimension newResolution = new Dimension(1920, 1080);

    // act
    screen.onResolutionChanged(newResolution);

    // assert
    assertEquals(1920, screen.getWidth(), 0.001);
    assertEquals(1080, screen.getHeight(), 0.001);
  }

  @Test
  void onResolutionChangedCanBeOverriddenToRescaleChildren() {
    // arrange
    TestScreenWithChild screen = new TestScreenWithChild("test");
    Dimension newResolution = new Dimension(800, 600);

    // act
    screen.onResolutionChanged(newResolution);

    // assert - the screen itself is resized
    assertEquals(800, screen.getWidth(), 0.001);
    assertEquals(600, screen.getHeight(), 0.001);
    // and the override had a chance to react
    assertEquals(newResolution, screen.lastResolution);
  }

  private static class TestScreen extends Screen {
    protected TestScreen(String name) {
      super(name);
    }
  }

  private static class TestScreenWithChild extends Screen {
    Dimension lastResolution;

    protected TestScreenWithChild(String name) {
      super(name);
    }

    @Override
    public void onResolutionChanged(Dimension newResolution) {
      super.onResolutionChanged(newResolution);
      this.lastResolution = newResolution;
    }
  }
}
