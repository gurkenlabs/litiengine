package de.gurkenlabs.litiengine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.awt.Canvas;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class MouseTests {
  @BeforeAll
  static void initialize() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    new Input.InputGameAdapter().initialized();
  }

  @Test
  void wheelEventUsesLogicalMouseLocationAndPreservesWheelData() {
    Mouse mouse = (Mouse) Input.mouse();
    mouse.setLocation(800, 400);
    AtomicReference<MouseWheelEvent> receivedEvent = new AtomicReference<>();
    MouseWheelListener listener = receivedEvent::set;
    mouse.onWheelMoved(listener);
    MouseWheelEvent physicalEvent =
      new MouseWheelEvent(
        new Canvas(),
        MouseWheelEvent.MOUSE_WHEEL,
        123,
        0,
        640,
        360,
        1640,
        1360,
        2,
        true,
        MouseWheelEvent.WHEEL_BLOCK_SCROLL,
        3,
        -2,
        -1.5);

    try {
      mouse.mouseWheelMoved(physicalEvent);
    } finally {
      mouse.removeMouseWheelListener(listener);
    }

    MouseWheelEvent event = receivedEvent.get();
    assertNotNull(event);
    assertEquals(800, event.getX());
    assertEquals(400, event.getY());
    assertEquals(1640, event.getXOnScreen());
    assertEquals(1360, event.getYOnScreen());
    assertEquals(123, event.getWhen());
    assertEquals(2, event.getClickCount());
    assertEquals(MouseWheelEvent.WHEEL_BLOCK_SCROLL, event.getScrollType());
    assertEquals(3, event.getScrollAmount());
    assertEquals(-2, event.getWheelRotation());
    assertEquals(-1.5, event.getPreciseWheelRotation());
  }
}
