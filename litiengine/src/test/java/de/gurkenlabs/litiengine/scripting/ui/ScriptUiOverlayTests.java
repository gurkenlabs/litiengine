package de.gurkenlabs.litiengine.scripting.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.scripting.AbstractScript;
import de.gurkenlabs.litiengine.scripting.CreatureScript;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptContext;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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

  @Test
  void testRenderWithAndWithoutCamera() {
    ScriptUiOverlay overlay = new ScriptUiOverlay();
    overlay.floatText("Damage 50", new Point2D.Double(200, 150), Color.RED);
    overlay.drawScreenText("HUD Text", 20, 30, Color.WHITE);
    overlay.showBanner("BOSS APPEARED", "Prepare to fight!", 1000);

    BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();

    // Render without camera
    overlay.render(g);

    // Set camera and render again
    Camera camera = new Camera();
    camera.setFocus(200, 150);
    camera.setZoom(2.0f, 0);
    Game.world().setCamera(camera);

    overlay.render(g);
    g.dispose();

    overlay.clear();
    assertEquals(0, overlay.getFloatingTexts().size());
    assertEquals(0, overlay.getScreenTexts().size());
    overlay.close();
  }

  public static class TestCustomScript extends CreatureScript {
    boolean uiTested = false;
    boolean cameraTested = false;

    @Override
    public void onLoaded() {
      assertNotNull(this.ui());
      assertNotNull(this.context().ui());
      assertSame(this.ui(), this.context().ui());
      this.uiTested = true;

      assertSame(Game.world().camera(), this.camera());
      assertSame(Game.world().camera(), this.context().camera());
      this.cameraTested = true;
    }
  }

  @Test
  void testScriptContextAndAbstractScriptUiAndCameraApis() throws Exception {
    Camera camera = new Camera();
    Game.world().setCamera(camera);

    Creature creature = new Creature();
    ScriptDefinition def = new ScriptDefinition("test-ui-cam", "java", null, TestCustomScript.class.getName(), ScriptHostType.ENTITY);
    ScriptBinding binding = new ScriptBinding("test-ui-cam");
    ScriptContext<Creature> context = new ScriptContext<>(def, binding, creature);

    // Test context().ui() and context().camera()
    assertNotNull(context.ui());
    assertSame(camera, context.camera());

    // Test AbstractScript delegating ui() and camera()
    TestCustomScript script = new TestCustomScript();
    script.attach(context);

    assertTrue(script.uiTested);
    assertTrue(script.cameraTested);

    script.detach();
    context.close();
  }
}
