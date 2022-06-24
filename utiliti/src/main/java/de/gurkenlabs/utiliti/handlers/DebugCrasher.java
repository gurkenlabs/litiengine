package de.gurkenlabs.utiliti.handlers;

import de.gurkenlabs.litiengine.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DebugCrasher extends KeyAdapter {

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == 67
        && e.isControlDown()
        && e.isShiftDown()
        && e.isAltDown()) { // Press CTRL + SHIFT + ALT + C to generate debug crash report
      debugCrash();
    }
  }

  private void debugCrash() {
    if (((GameLoop) Game.loop()).getUncaughtExceptionHandler() instanceof DefaultUncaughtExceptionHandler defaultUncaughtExceptionHandler) {
      defaultUncaughtExceptionHandler.dumpThreads(true);
    }
    throw new ManualDebugError("Pressed CTRL + SHIFT + ALT + C for manual debug crash.");
  }

  private static class ManualDebugError extends Error {
    ManualDebugError(String message) {
      super(message);
    }
  }
}
