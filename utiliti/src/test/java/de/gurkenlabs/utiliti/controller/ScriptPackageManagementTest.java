package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import de.gurkenlabs.utiliti.view.components.ScriptWorkspacePanel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(GameTestSuite.class)
class ScriptPackageManagementTest {

  @BeforeEach
  void setUp() {
    Game.world().unloadEnvironment();
    Game.scripts().detachAll();
    Game.scripts().setDefinitions(List.of());
    UndoManager.clearAll();
    Editor.instance().load(null, false);
    if (Editor.instance().getGameFile() != null) {
      Editor.instance().getGameFile().getScripts().clear();
    }
  }

  @Test
  void testCreateScriptInPackage(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Files.createDirectories(srcMainJava);
    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    String rel = ScriptSourcePaths.create(model, "java", "com.example.game.entity", "EnemyBehavior");
    assertEquals("src/main/java/com/example/game/entity/EnemyBehavior.java", rel);
    assertEquals("com.example.game.entity", ScriptSourcePaths.derivePackageName(model, rel));
  }

  @Test
  void testPackageRenameRefactoring(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path oldPkg = srcMainJava.resolve("com/example/game/entity");
    Files.createDirectories(oldPkg);

    Path scriptFile = oldPkg.resolve("EnemyBehavior.java");
    Files.writeString(scriptFile, "package com.example.game.entity;\n\nimport de.gurkenlabs.litiengine.scripting.CreatureScript;\n\npublic class EnemyBehavior extends CreatureScript {\n}\n");

    Path otherFile = srcMainJava.resolve("com/example/game/Main.java");
    Files.createDirectories(otherFile.getParent());
    Files.writeString(otherFile, "package com.example.game;\n\nimport com.example.game.entity.EnemyBehavior;\n\npublic class Main {\n}\n");

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    Path projectFile = tempDir.resolve("game.litidata");
    Editor.instance().setProjectPath(projectFile);
    Editor.instance().setProjectModel(model);

    ScriptDefinition definition = new ScriptDefinition(
        "EnemyBehavior", "java", "src/main/java/com/example/game/entity/EnemyBehavior.java",
        "com.example.game.entity.EnemyBehavior", ScriptHostType.ENTITY);
    Editor.instance().getGameFile().getScripts().add(definition);

    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    panel.executeRenamePackage("com.example.game.entity", "com.example.game.mobs");

    assertFalse(Files.exists(scriptFile));
    Path newScriptFile = srcMainJava.resolve("com/example/game/mobs/EnemyBehavior.java");
    assertTrue(Files.exists(newScriptFile));

    String updatedScript = Files.readString(newScriptFile);
    assertTrue(updatedScript.contains("package com.example.game.mobs;"));

    String updatedMain = Files.readString(otherFile);
    assertTrue(updatedMain.contains("import com.example.game.mobs.EnemyBehavior;"));

    assertEquals("com.example.game.mobs.EnemyBehavior", definition.getImplementation());
    assertEquals("src/main/java/com/example/game/mobs/EnemyBehavior.java", definition.getSource());
  }

  @Test
  void testMoveScriptToPackage(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path entityPkg = srcMainJava.resolve("com/example/game/entity");
    Files.createDirectories(entityPkg);

    Path scriptFile = entityPkg.resolve("DestructibleBehavior.java");
    Files.writeString(scriptFile, "package com.example.game.entity;\n\npublic class DestructibleBehavior {\n}\n");

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    Path projectFile = tempDir.resolve("game.litidata");
    Editor.instance().setProjectPath(projectFile);
    Editor.instance().setProjectModel(model);

    ScriptDefinition definition = new ScriptDefinition(
        "DestructibleBehavior", "java", "src/main/java/com/example/game/entity/DestructibleBehavior.java",
        "com.example.game.entity.DestructibleBehavior", ScriptHostType.ENTITY);
    Editor.instance().getGameFile().getScripts().add(definition);

    String targetPackage = "com.example.game.environment";
    String newSourceRel = ScriptSourcePaths.create(model, "java", targetPackage, "DestructibleBehavior");
    Path newPath = tempDir.resolve(newSourceRel);

    String content = Files.readString(scriptFile);
    String updated = content.replaceFirst("(?m)^\\s*package\\s+[a-zA-Z0-9_.]+\\s*;", "package " + targetPackage + ";");
    Files.createDirectories(newPath.getParent());
    Files.writeString(newPath, updated);
    Files.deleteIfExists(scriptFile);

    definition.setSource(newSourceRel);
    definition.setImplementation(targetPackage + ".DestructibleBehavior");

    assertFalse(Files.exists(scriptFile));
    assertTrue(Files.exists(newPath));
    assertEquals("com.example.game.environment.DestructibleBehavior", definition.getImplementation());
    assertTrue(Files.readString(newPath).contains("package com.example.game.environment;"));
  }
}
