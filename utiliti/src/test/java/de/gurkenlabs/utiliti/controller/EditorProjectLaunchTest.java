package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EditorProjectLaunchTest {
  @Test
  void buildConfigurationStampChangesOnlyForLaunchModelInputs(@TempDir Path root) throws Exception {
    Files.writeString(root.resolve("settings.gradle"), "rootProject.name = 'game'");
    Path game = Files.createDirectories(root.resolve("game"));
    Files.writeString(game.resolve("build.gradle"), "plugins { id 'application' }");
    long initial = Editor.buildConfigurationStamp(root);

    Files.writeString(game.resolve("Creature.java"), "class Creature {}");
    assertEquals(initial, Editor.buildConfigurationStamp(root));

    Files.writeString(game.resolve("build.gradle"), "plugins { id 'application' }\napplication {}\n");
    assertNotEquals(initial, Editor.buildConfigurationStamp(root));
  }
}
