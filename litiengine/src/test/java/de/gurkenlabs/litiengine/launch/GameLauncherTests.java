package de.gurkenlabs.litiengine.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameLauncherTests {
  @AfterEach
  void cleanup() {
    Game.scripts().clearDiagnostics();
    Game.scripts().setDefinitions(java.util.List.of());
  }

  @Test
  void testGameLauncherScriptDiscoveryAndSetup(@TempDir Path tempDir) throws IOException {
    Path scriptsDir = tempDir.resolve("scripts");
    Files.createDirectories(scriptsDir);

    Path gameScriptFile = scriptsDir.resolve("TestGameScript.java");
    Files.writeString(gameScriptFile, "package scripts; import de.gurkenlabs.litiengine.scripting.GameScript; public class TestGameScript extends GameScript {}");

    Path entityScriptFile = scriptsDir.resolve("TestPlayerScript.java");
    Files.writeString(entityScriptFile, "package scripts; import de.gurkenlabs.litiengine.scripting.CreatureScript; public class TestPlayerScript extends CreatureScript {}");

    GameLauncher.prepare(tempDir, Game.COMMANDLINE_ARG_NOGUI, "--title", "LauncherTest", "--gravity", "90", "--scale", "3");

    assertEquals("LauncherTest", Game.info().getName());
    assertEquals(90, Game.world().gravity());
    assertEquals(3.0f, Game.graphics().getBaseRenderScale());

    assertNotNull(Game.scripts().getDefinition("TestGameScript"));
    assertNotNull(Game.scripts().getDefinition("TestPlayerScript"));
    assertTrue(Game.scripts().getGameBindings().stream().anyMatch(b -> b.getScript().equals("TestGameScript")));
  }
}
