package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.scripting.EntityScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import org.junit.jupiter.api.Test;

class ScriptInspectorPanelTest {
  @Test
  void defaultBindingMovesWithTheSelectedTargetWithoutTouchingOtherTargets() {
    ResourceBundle bundle = new ResourceBundle();
    EntityScriptBinding oldTarget = new EntityScriptBinding("game.Zombie");
    oldTarget.getScripts().add(new ScriptBinding("behavior"));
    EntityScriptBinding otherTarget = new EntityScriptBinding("game.Boss");
    otherTarget.getScripts().add(new ScriptBinding("behavior"));
    bundle.getEntityScripts().add(oldTarget);
    bundle.getEntityScripts().add(otherTarget);

    ScriptInspectorPanel.updateDefaultBinding(
      bundle, "behavior", "game.Zombie", "game.Nurse", true, false);

    assertEquals(2, bundle.getEntityScripts().size());
    assertTrue(bundle.getEntityScripts().stream().anyMatch(binding -> binding.getTargetType().equals("game.Boss")));
    EntityScriptBinding nurse = bundle.getEntityScripts().stream()
      .filter(binding -> binding.getTargetType().equals("game.Nurse")).findFirst().orElseThrow();
    assertFalse(nurse.isInherited());
    assertEquals("behavior", nurse.getScripts().getFirst().getScript());
  }
}
