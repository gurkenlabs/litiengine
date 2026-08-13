package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptSourcePathsTest {

  @Test
  void createsRuntimeScriptsOutsideBuildSourceSetsWhenNoProjectModel() {
    assertEquals("scripts/java/CreatureScript.java", ScriptSourcePaths.create("java", "CreatureScript"));
    assertEquals("scripts/groovy/GameScript.groovy", ScriptSourcePaths.create("groovy", "GameScript"));
  }

  @Test
  void createsScriptInGamePackageForStandardLayout(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Files.createDirectories(srcMainJava);
    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    String path = ScriptSourcePaths.create(model, "java", "CreatureScript3");
    assertEquals("src/main/java/com/example/game/scripts/CreatureScript3.java", path);
    assertEquals("com.example.game.scripts", ScriptSourcePaths.derivePackageName(model, path));
  }

  @Test
  void createsScriptInGamePackageForFlatLayout(@TempDir Path tempDir) throws IOException {
    Path src = tempDir.resolve("src");
    Files.createDirectories(src);
    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "de.gurkenlabs.lepus.LepusGame", 21,
        List.of(src), List.of(), List.of(), List.of());

    String path = ScriptSourcePaths.create(model, "java", "CreatureScript3");
    assertEquals("src/de/gurkenlabs/lepus/scripts/CreatureScript3.java", path);
    assertEquals("de.gurkenlabs.lepus.scripts", ScriptSourcePaths.derivePackageName(model, path));
  }

  @Test
  void detectsPackageFromExistingSourceFilesWhenMainClassMissing(@TempDir Path tempDir) throws IOException {
    Path src = tempDir.resolve("src");
    Path pkgDir = src.resolve("de/gurkenlabs/lepus");
    Files.createDirectories(pkgDir);
    Files.writeString(pkgDir.resolve("Game.java"), "package de.gurkenlabs.lepus;\npublic class Game {}");

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", null, 21,
        List.of(src), List.of(), List.of(), List.of());

    String path = ScriptSourcePaths.create(model, "java", "CreatureScript3");
    assertEquals("src/de/gurkenlabs/lepus/scripts/CreatureScript3.java", path);
    assertEquals("de.gurkenlabs.lepus.scripts", ScriptSourcePaths.derivePackageName(model, path));
  }

  @Test
  void testFlatLayoutPreferredEvenIfSrcMainJavaExists(@TempDir Path tempDir) throws IOException {
    Path src = tempDir.resolve("src");
    Path pkgDir = src.resolve("de/gurkenlabs/lepus");
    Files.createDirectories(pkgDir);
    Path srcMainJava = tempDir.resolve("src/main/java");
    Files.createDirectories(srcMainJava);

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "de.gurkenlabs.lepus.LepusGame", 21,
        List.of(srcMainJava, src), List.of(), List.of(), List.of());

    String path = ScriptSourcePaths.create(model, "java", "NewScript");
    assertEquals("src/de/gurkenlabs/lepus/scripts/NewScript.java", path);
    assertEquals("de.gurkenlabs.lepus.scripts", ScriptSourcePaths.derivePackageName(model, path));
  }

  @Test
  void detectsCommonPackagePrefixFromMultipleFiles(@TempDir Path tempDir) throws IOException {
    Path src = tempDir.resolve("src");
    Path pkg1 = src.resolve("de/gurkenlabs/lepus/entities");
    Path pkg2 = src.resolve("de/gurkenlabs/lepus/gui");
    Files.createDirectories(pkg1);
    Files.createDirectories(pkg2);
    Files.writeString(pkg1.resolve("Creature.java"), "package de.gurkenlabs.lepus.entities;\npublic class Creature {}");
    Files.writeString(pkg2.resolve("Hud.java"), "package de.gurkenlabs.lepus.gui;\npublic class Hud {}");

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", null, 21,
        List.of(src), List.of(), List.of(), List.of());

    String path = ScriptSourcePaths.create(model, "java", "NewScript");
    assertEquals("src/de/gurkenlabs/lepus/scripts/NewScript.java", path);
    assertEquals("de.gurkenlabs.lepus.scripts", ScriptSourcePaths.derivePackageName(model, path));
  }

  @Test
  void derivesNullPackageOutsideSourceRoots(@TempDir Path tempDir) {
    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "de.gurkenlabs.lepus.LepusGame", 21,
        List.of(tempDir.resolve("src")), List.of(), List.of(), List.of());

    assertNull(ScriptSourcePaths.derivePackageName(model, "scripts/java/CreatureScript3.java"));
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

