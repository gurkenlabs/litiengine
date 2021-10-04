package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.configuration.DisplayMode;
import java.awt.Dimension;
import javax.swing.JFrame;
import org.junit.jupiter.api.Test;

public class GameWindowTests {

  @Test
  public void windowedNeedsToBeResizable() {
    JFrame frame = new JFrame();

    GameWindow.prepareHostControl(frame, DisplayMode.WINDOWED, new Dimension(300, 150));

    assertTrue(frame.isResizable());

    frame.dispose();
  }

  @Test
  public void windowedNeedsToAccountForWindowInsets() {
    JFrame frame = new JFrame();

    GameWindow.prepareHostControl(frame, DisplayMode.WINDOWED, new Dimension(300, 150));

    assertEquals(300 + frame.getInsets().left + frame.getInsets().right, frame.getWidth());
    assertEquals(150 + frame.getInsets().top + frame.getInsets().bottom, frame.getHeight());

    frame.dispose();
  }

  @Test
  public void borderlessNeedstoBeUndecorated() {
    JFrame frame = new JFrame();

    GameWindow.prepareHostControl(frame, DisplayMode.BORDERLESS, new Dimension(300, 150));

    assertFalse(frame.isResizable());
    assertTrue(frame.isUndecorated());

    frame.dispose();
  }
}
