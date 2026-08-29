package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.EntityScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptResourceTests {
  @Test
  void savesAndLoadsDefinitionsGlobalBindingsAndParameters(@TempDir Path temporaryDirectory) {
    ResourceBundle bundle = new ResourceBundle();
    ScriptDefinition definition = new ScriptDefinition("game.startup", "groovy", "src/main/groovy/Startup.groovy", "game.Startup", ScriptHostType.GAME);
    definition.setName("Startup");
    bundle.getScripts().add(definition);
    ScriptBinding binding = new ScriptBinding("game.startup");
    binding.setParameter("difficulty", "HARD");
    bundle.getGameScripts().add(binding);
    EntityScriptBinding entityBinding = new EntityScriptBinding("example.Zombie");
    entityBinding.getScripts().add(new ScriptBinding("game.startup"));
    bundle.getEntityScripts().add(entityBinding);

    String fileName = bundle.save(temporaryDirectory.resolve("game").toString(), false);
    ResourceBundle loaded = ResourceBundle.load(fileName);

    assertNotNull(loaded);
    assertEquals(1, loaded.getScripts().size());
    assertEquals("game.Startup", loaded.getScripts().getFirst().getImplementation());
    assertEquals("HARD", loaded.getGameScripts().getFirst().getParameters().get("difficulty"));
    assertEquals("example.Zombie", loaded.getEntityScripts().getFirst().getTargetType());
    assertEquals("game.startup", loaded.getEntityScripts().getFirst().getScripts().getFirst().getScript());
  }
}
