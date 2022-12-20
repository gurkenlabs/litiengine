package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.test.GameTestSuite;

public class GameTestNoSwing {


  @BeforeAll
  private void onTestStart() {
    GameTestSuite.GameLock.lock();
  }

  @AfterAll
  private void onTestEnd() {
    GameTestSuite.GameLock.unlock();
  }

  @Test
  public void testSwingThreadAssertionsOutsideSwing() {
    Game.init(
        () -> {
          assertTrue(SwingUtilities.isEventDispatchThread());
        },
        () -> {
          assertTrue(SwingUtilities.isEventDispatchThread());
        },
        Game.COMMANDLINE_ARG_NOGUI);
  }

}
