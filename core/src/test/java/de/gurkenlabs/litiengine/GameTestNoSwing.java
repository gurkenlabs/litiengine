package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

public class GameTestNoSwing {

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
