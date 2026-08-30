package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class MouseDrawComponentTests {
  @BeforeAll
  static void initialize() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();
  }

  @Test
  void obscuredComponentDoesNotDraw() {
    TestComponent parent = new TestComponent(0, 0, 200, 200);
    MouseDrawComponent lower = new MouseDrawComponent(0, 0, 100, 100, null, null, null);
    TestComponent upper = new TestComponent(25, 25, 100, 100);
    parent.getComponents().add(lower);
    parent.getComponents().add(upper);
    parent.setVisible(true);
    int previousColor = lower.getDrawingSpace().getRGB(49, 49);

    MouseEvent dragEvent = createLeftDragEvent(50, 50);
    lower.mouseDragged(dragEvent);

    assertEquals(previousColor, lower.getDrawingSpace().getRGB(49, 49));

    parent.getComponents().remove(upper);
    lower.mouseDragged(dragEvent);

    assertNotEquals(previousColor, lower.getDrawingSpace().getRGB(49, 49));
  }

  private static MouseEvent createLeftDragEvent(int x, int y) {
    return new MouseEvent(
      new JLabel(), MouseEvent.MOUSE_DRAGGED, 0, InputEvent.BUTTON1_DOWN_MASK,
      x, y, 0, false);
  }

  private static class TestComponent extends GuiComponent {
    private TestComponent(double x, double y, double width, double height) {
      super(x, y, width, height);
    }
  }
}
