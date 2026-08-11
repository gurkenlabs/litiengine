package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScriptSourcePathsTest {

  @Test
  void createsRuntimeScriptsOutsideBuildSourceSets() {
    assertEquals("scripts/java/CreatureScript.java", ScriptSourcePaths.create("java", "CreatureScript"));
    assertEquals("scripts/groovy/GameScript.groovy", ScriptSourcePaths.create("groovy", "GameScript"));
  }

  @Test
  void renamePreservesLegacyDirectoryAndExtension() {
    assertEquals(
        "src/main/java/ZombieScript.java",
        ScriptSourcePaths.rename("src/main/java/CreatureScript.java", "java", "ZombieScript"));
    assertEquals(
        "scripts/groovy/Startup.groovy",
        ScriptSourcePaths.rename("scripts\\groovy\\GameScript.groovy", "groovy", "Startup"));
  }
}
