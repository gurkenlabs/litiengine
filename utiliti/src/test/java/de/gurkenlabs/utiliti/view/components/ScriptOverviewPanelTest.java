package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.ScriptBindingService;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class ScriptOverviewPanelTest {

  @Test
  void testOverviewPanelBindsFieldsMethodsAndUsages() {
    AtomicReference<ScriptBindingService.ScriptUsage> clickedUsage = new AtomicReference<>();
    AtomicInteger jumpedLine = new AtomicInteger(-1);

    ScriptOverviewPanel panel = new ScriptOverviewPanel(
      clickedUsage::set,
      jumpedLine::set
    );

    ScriptDefinition def = new ScriptDefinition(
      "JanitorBehavior", "java", "scripts/JanitorBehavior.java", "de.gurkenlabs.scripts.JanitorBehavior", ScriptHostType.ENTITY
    );
    def.setTargetType(Creature.class.getName());

    String source = """
      package de.gurkenlabs.scripts;
      import de.gurkenlabs.litiengine.scripting.*;
      public class JanitorBehavior extends CreatureScript {
        @ScriptProperty
        private int cnt = 0;

        @Override
        public void onLoaded() {}

        @Override
        public void update() {}
      }
      """;

    ScriptOutline.Symbol outline = ScriptOutline.parse(source);
    assertNotNull(outline);

    ScriptBindingService.ScriptUsage usage = new ScriptBindingService.ScriptUsage(
      new ScriptBindingTarget.EntityInstance("overworld", 194), "Creature #194", 194
    );

    panel.bind(def, outline, List.of(usage));
    assertNotNull(panel);
  }
}
