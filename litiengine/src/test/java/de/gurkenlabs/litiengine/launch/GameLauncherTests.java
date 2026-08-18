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

    GameLauncher.prepare(tempDir, Game.COMMANDLINE_ARG_NOGUI, "--title", "LauncherTest", "--gravity", "90", "--scale", "3");

    assertEquals("LauncherTest", Game.info().getName());
    assertTrue(Game.isInNoGUIMode());
    assertEquals(90, Game.world().gravity());
    assertEquals(3.0f, Game.graphics().getBaseRenderScale());

    assertNotNull(Game.scripts().getDefinition("scripts.TestGameScript"));
    assertNotNull(Game.scripts().getDefinition("scripts.TestPlayerScript"));
    assertNotNull(Game.scripts().getDefinition("com.example.PackagedGameScript"));
    assertNotNull(Game.scripts().getDefinition("org.example.PackagedGameScript"));
    assertEquals(null, Game.scripts().getDefinition("com.example.GameConfig"));
    assertTrue(Game.scripts().getGameBindings().stream().anyMatch(b -> b.getScript().equals("scripts.TestGameScript")));
  }
}
