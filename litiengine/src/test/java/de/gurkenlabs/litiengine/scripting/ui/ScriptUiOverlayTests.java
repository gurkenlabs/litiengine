package de.gurkenlabs.litiengine.scripting.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import java.awt.Color;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptUiOverlayTests {
  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void testFloatingTextLifecycle() {
    ScriptUiOverlay overlay = new ScriptUiOverlay();
    FloatingText text = overlay.floatText("Critical Hit!", new Point2D.Double(100, 100), Color.RED, null, 100, -50.0);

    assertNotNull(text);
    assertEquals("Critical Hit!", text.getText());
    assertEquals(Color.RED, text.getColor());
    assertEquals(1, overlay.getFloatingTexts().size());

    // Advance
    text.update(50);
    assertEquals(0.5f, text.getProgress(), 0.01f);
    assertFalse(text.isFinished());
    assertTrue(text.getLocation().getY() < 100.0);

    text.update(50);
    assertTrue(text.isFinished());

    overlay.update();
    assertEquals(0, overlay.getFloatingTexts().size());

    // Test screen text and banner
    var screenText = overlay.drawScreenText("Score: 100", 10, 10, Color.YELLOW, null, 100);
    assertNotNull(screenText);
    assertEquals(1, overlay.getScreenTexts().size());

    overlay.showBanner("Wave 1", "Fight!", 200);

    overlay.update();
    overlay.close();
  }
}
