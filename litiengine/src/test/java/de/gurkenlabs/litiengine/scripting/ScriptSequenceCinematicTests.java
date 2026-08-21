package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.graphics.Camera;
import java.awt.geom.Point2D;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptSequenceCinematicTests {
  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void testCinematicSequenceSteps() {
    Camera camera = new Camera();
    Game.world().setCamera(camera);

    ScriptDefinition definition = new ScriptDefinition("seq-test", "java", null, "Dummy", ScriptHostType.GAME);
    ScriptBinding binding = new ScriptBinding("seq-test");
    ScriptContext<Object> context = new ScriptContext<>(definition, binding, new Object());

    AtomicInteger stepsCompleted = new AtomicInteger(0);
    Creature target = new Creature();
    target.setLocation(100, 200);

    ScriptSequence sequence = context.sequence()
        .cameraPanTo(new Point2D.Double(50, 50), 10)
        .then(stepsCompleted::incrementAndGet)
        .cameraPanTo(target, 10)
        .then(stepsCompleted::incrementAndGet)
        .cameraZoom(2.0f, 100)
        .then(stepsCompleted::incrementAndGet)
        .screenShake(5.0, 10, 20)
        .then(stepsCompleted::incrementAndGet);

    Subscription sub = sequence.start();
    assertNotNull(sub);
    assertTrue(sequence.isRunning());
    context.close();
    assertFalse(sequence.isRunning());
  }
}
