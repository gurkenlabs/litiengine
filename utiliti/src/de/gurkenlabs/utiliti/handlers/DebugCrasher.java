package de.gurkenlabs.utiliti.handlers;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import de.gurkenlabs.utiliti.Program;

public class DebugCrasher extends KeyAdapter {

  public void keyPressed(KeyEvent e) {
    if(e.getKeyCode() == 67 && e.isControlDown() && e.isShiftDown() && e.isAltDown()) { //Press CTRL + SHIFT + ALT + C to generate debug crash report
      Program.debugCrash = true;
    }
  }
}
