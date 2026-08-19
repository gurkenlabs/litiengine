package de.gurkenlabs.litiengine.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Prop;
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
  void testGameLauncherScriptDiscoveryAndSetup() throws IOException {
    Path tempDir = Files.createDirectories(Path.of("build/tmp/launcher-test-" + System.nanoTime()).toAbsolutePath().normalize());
    try {
      Path scriptsDir = tempDir.resolve("scripts");
      Path sourceDir = tempDir.resolve("src/main/java/com/example");
      Path duplicateSourceDir = tempDir.resolve("src/main/java/org/example");
      Files.createDirectories(scriptsDir);
      Files.createDirectories(sourceDir);
      Files.createDirectories(duplicateSourceDir);

      Path gameScriptFile = scriptsDir.resolve("TestGameScript.java");
      Files.writeString(gameScriptFile, "package scripts; import de.gurkenlabs.litiengine.scripting.GameScript; public class TestGameScript extends GameScript {}");

      Path entityScriptFile = scriptsDir.resolve("TestPlayerScript.java");
      Files.writeString(entityScriptFile, "package scripts; import de.gurkenlabs.litiengine.scripting.CreatureScript; public class TestPlayerScript extends CreatureScript {}");
      Path packagedGameScriptFile = sourceDir.resolve("PackagedGameScript.java");
      Files.writeString(packagedGameScriptFile, "package com.example; import de.gurkenlabs.litiengine.scripting.GameScript; public class PackagedGameScript extends GameScript {}");
      Path utilityFile = sourceDir.resolve("GameConfig.java");
      Files.writeString(utilityFile, "package com.example; public class GameConfig {}");
      Path duplicateGameScriptFile = duplicateSourceDir.resolve("PackagedGameScript.java");
      Files.writeString(duplicateGameScriptFile, "package org.example; import de.gurkenlabs.litiengine.scripting.GameScript; public class PackagedGameScript extends GameScript {}");
      Path annotatedEntityScriptFile = sourceDir.resolve("PlayerScript.java");
      Files.writeString(annotatedEntityScriptFile, "package com.example; import de.gurkenlabs.litiengine.entities.Prop; import de.gurkenlabs.litiengine.scripting.*; @ScriptInfo(id = \"player\", host = ScriptHostType.ENTITY, target = Prop.class) public class PlayerScript extends EntityScript<Prop> {}");
      Path baseNpcFile = sourceDir.resolve("BaseNpcScript.java");
      Files.writeString(baseNpcFile, "package com.example; import de.gurkenlabs.litiengine.entities.Creature; import de.gurkenlabs.litiengine.scripting.EntityScript; public abstract class BaseNpcScript extends EntityScript<Creature> {}");
      Path guardScriptFile = sourceDir.resolve("GuardScript.java");
      Files.writeString(guardScriptFile, "package com.example; import de.gurkenlabs.litiengine.scripting.*; @ScriptInfo(id = \"guard\", host = ScriptHostType.ENTITY) public class GuardScript extends BaseNpcScript {}");
      Path commentFile = sourceDir.resolve("CommentTest.java");
      Files.writeString(commentFile, "package com.example; // This class extends GameScript\n/* and this also extends GameScript */\npublic class CommentTest {}");
      Path customGameFile = sourceDir.resolve("CustomGameScript.java");
      Files.writeString(customGameFile, "package com.example; import de.gurkenlabs.litiengine.scripting.*; @ScriptInfo(id = \"custom-game\", host = ScriptHostType.GAME) public class CustomGameScript {}");

      Path relativeProjectRoot = Path.of(".").toAbsolutePath().normalize().relativize(tempDir);
      GameLauncher.prepare(relativeProjectRoot, Game.COMMANDLINE_ARG_NOGUI, "--title", "LauncherTest", "--gravity", "90", "--scale", "3");

    assertEquals("LauncherTest", Game.info().getName());
    assertTrue(Game.isInNoGUIMode());
    assertEquals(90, Game.world().gravity());
    assertEquals(3.0f, Game.graphics().getBaseRenderScale());

    assertNotNull(Game.scripts().getDefinition("scripts.TestGameScript"));
    assertNotNull(Game.scripts().getDefinition("scripts.TestPlayerScript"));
    assertNotNull(Game.scripts().getDefinition("com.example.PackagedGameScript"));
    assertNotNull(Game.scripts().getDefinition("org.example.PackagedGameScript"));
    assertEquals(Prop.class.getName(), Game.scripts().getDefinition("player").getTargetType());
    assertNotNull(Game.scripts().getDefinition("guard"));
    assertEquals(de.gurkenlabs.litiengine.scripting.ScriptHostType.ENTITY, Game.scripts().getDefinition("guard").getHost());
    assertNotNull(Game.scripts().getDefinition("custom-game"));
    assertEquals(de.gurkenlabs.litiengine.scripting.ScriptHostType.GAME, Game.scripts().getDefinition("custom-game").getHost());
    assertEquals(null, Game.scripts().getDefinition("com.example.CommentTest"));
    assertEquals(null, Game.scripts().getDefinition("com.example.GameConfig"));
    assertTrue(Game.scripts().getGameBindings().stream().anyMatch(b -> b.getScript().equals("scripts.TestGameScript")));
    } finally {
      try (var stream = Files.walk(tempDir)) {
        stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
          try { Files.deleteIfExists(p); } catch (IOException ignored) {}
        });
      } catch (IOException ignored) {}
    }
  }
}
