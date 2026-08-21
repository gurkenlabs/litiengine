package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
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
  void testPackageRenameCollisionRejectionAndRollback(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path oldPkg = srcMainJava.resolve("com/example/game/entity");
    Path destPkg = srcMainJava.resolve("com/example/game/mobs");
    Files.createDirectories(oldPkg);
    Files.createDirectories(destPkg);

    Path scriptFile = oldPkg.resolve("EnemyBehavior.java");
    Files.writeString(scriptFile, "package com.example.game.entity;\n\npublic class EnemyBehavior {}\n");

    Path collidingFile = destPkg.resolve("EnemyBehavior.java");
    Files.writeString(collidingFile, "package com.example.game.mobs;\n\npublic class EnemyBehavior { // existing }\n");

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

    assertTrue(Files.exists(scriptFile));
    assertTrue(Files.exists(collidingFile));
    assertTrue(Files.readString(collidingFile).contains("// existing"));
    assertEquals("com.example.game.entity.EnemyBehavior", definition.getImplementation());
  }

  @Test
  void testMoveScriptPreservesClassNameWhenDisplayNameDiffers(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path entityPkg = srcMainJava.resolve("com/example/game/entity");
    Files.createDirectories(entityPkg);

    Path scriptFile = entityPkg.resolve("EnemyBehavior.java");
    Files.writeString(scriptFile, "package com.example.game.entity;\n\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "@ScriptInfo(id = \"enemy-ai\", name = \"Enemy AI\")\n"
        + "public class EnemyBehavior extends EntityScript<de.gurkenlabs.litiengine.entities.Creature> {}\n");

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    Path projectFile = tempDir.resolve("game.litidata");
    Editor.instance().setProjectPath(projectFile);
    Editor.instance().setProjectModel(model);

    ScriptDefinition definition = new ScriptDefinition(
        "enemy-ai", "java", "src/main/java/com/example/game/entity/EnemyBehavior.java",
        "com.example.game.entity.EnemyBehavior", ScriptHostType.ENTITY);
    definition.setName("Enemy AI");
    Editor.instance().getGameFile().getScripts().add(definition);

    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    boolean moved = panel.executeMoveScriptToPackage(definition, "com.example.game.mobs");
    assertTrue(moved);

    assertFalse(Files.exists(scriptFile));
    Path movedFile = srcMainJava.resolve("com/example/game/mobs/EnemyBehavior.java");
    assertTrue(Files.exists(movedFile), "Target file must be named EnemyBehavior.java, not Enemy AI.java");

    assertEquals("com.example.game.mobs.EnemyBehavior", definition.getImplementation());
    assertEquals("src/main/java/com/example/game/mobs/EnemyBehavior.java", definition.getSource());
    assertEquals("enemy-ai", definition.getId());
    assertEquals("Enemy AI", definition.getName());
  }

  @Test
  void testMoveScriptCollisionRejection(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path oldPkg = srcMainJava.resolve("com/example/game/entity");
    Path newPkg = srcMainJava.resolve("com/example/game/mobs");
    Files.createDirectories(oldPkg);
    Files.createDirectories(newPkg);

    Path scriptFile = oldPkg.resolve("EnemyBehavior.java");
    Files.writeString(scriptFile, "package com.example.game.entity;\npublic class EnemyBehavior {}\n");

    Path collidingFile = newPkg.resolve("EnemyBehavior.java");
    Files.writeString(collidingFile, "package com.example.game.mobs;\npublic class EnemyBehavior { // existing }\n");

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
    boolean moved = panel.executeMoveScriptToPackage(definition, "com.example.game.mobs");
    assertFalse(moved, "Move must be rejected on file collision");

    assertTrue(Files.exists(scriptFile));
    assertTrue(Files.exists(collidingFile));
    assertTrue(Files.readString(collidingFile).contains("// existing"));
  }

  @Test
  void testDeletePackageProtectsNonScriptFiles(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path pkgDir = srcMainJava.resolve("com/example/game");
    Files.createDirectories(pkgDir);

    Path scriptFile = pkgDir.resolve("PlayerScript.java");
    Files.writeString(scriptFile, "package com.example.game;\npublic class PlayerScript {}\n");

    Path nonScriptClass = pkgDir.resolve("Player.java");
    Files.writeString(nonScriptClass, "package com.example.game;\npublic class Player {}\n");

    Path nonScriptResource = pkgDir.resolve("items.json");
    Files.writeString(nonScriptResource, "{}\n");

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    Path projectFile = tempDir.resolve("game.litidata");
    Editor.instance().setProjectPath(projectFile);
    Editor.instance().setProjectModel(model);

    ScriptDefinition definition = new ScriptDefinition(
        "PlayerScript", "java", "src/main/java/com/example/game/PlayerScript.java",
        "com.example.game.PlayerScript", ScriptHostType.ENTITY);
    Editor.instance().getGameFile().getScripts().add(definition);

    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    boolean deleted = panel.deletePackage("com.example.game", true);
    assertFalse(deleted, "Delete package must be refused when non-script files exist");

    assertTrue(Files.exists(nonScriptClass), "Non-script class must not be deleted");
    assertTrue(Files.exists(nonScriptResource), "Non-script resource must not be deleted");
    assertTrue(Files.exists(scriptFile), "Script file must remain intact when package deletion is denied");
    assertTrue(Editor.instance().getGameFile().getScripts().contains(definition));
  }

  @Test
  void testRenameClassPreservesCustomScriptIdAndDisplayName(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path pkgDir = srcMainJava.resolve("com/example/game");
    Files.createDirectories(pkgDir);

    Path scriptFile = pkgDir.resolve("EnemyBehavior.java");
    String originalSource = "package com.example.game;\n\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "@ScriptInfo(id = \"enemy-ai\", name = \"Enemy AI\")\n"
        + "public class EnemyBehavior extends EntityScript<de.gurkenlabs.litiengine.entities.Creature> {\n"
        + "}\n";
    Files.writeString(scriptFile, originalSource);

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    Path projectFile = tempDir.resolve("game.litidata");
    Editor.instance().setProjectPath(projectFile);
    Editor.instance().setProjectModel(model);

    ScriptDefinition definition = new ScriptDefinition(
        "enemy-ai", "java", "src/main/java/com/example/game/EnemyBehavior.java",
        "com.example.game.EnemyBehavior", ScriptHostType.ENTITY);
    definition.setName("Enemy AI");
    Editor.instance().getGameFile().getScripts().add(definition);

    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    boolean renamed = panel.renameScript(definition, "HostileBehavior");
    assertTrue(renamed);

    assertFalse(Files.exists(scriptFile));
    Path newFile = pkgDir.resolve("HostileBehavior.java");
    assertTrue(Files.exists(newFile));

    String updatedSource = Files.readString(newFile);
    assertTrue(updatedSource.contains("class HostileBehavior"));
    assertTrue(updatedSource.contains("id = \"enemy-ai\""), "Custom script id must be preserved");
    assertTrue(updatedSource.contains("name = \"Enemy AI\""), "Custom display name must be preserved");

    assertEquals("enemy-ai", definition.getId(), "ScriptDefinition ID must remain stable");
    assertEquals("Enemy AI", definition.getName(), "ScriptDefinition Name must remain stable");
    assertEquals("com.example.game.HostileBehavior", definition.getImplementation());
    assertEquals("src/main/java/com/example/game/HostileBehavior.java", definition.getSource());
  }

  @Test
  void testRenameClassWhenNewClassNameMatchesExistingCustomId(@TempDir Path tempDir) throws IOException {
    Path srcMainJava = tempDir.resolve("src/main/java");
    Path pkgDir = srcMainJava.resolve("com/example/game");
    Files.createDirectories(pkgDir);

    Path scriptFile = pkgDir.resolve("EnemyBehavior.java");
    String originalSource = "package com.example.game;\n\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "@ScriptInfo(id = \"EnemyAI\", name = \"Enemy AI\")\n"
        + "public class EnemyBehavior extends EntityScript<de.gurkenlabs.litiengine.entities.Creature> {\n"
        + "}\n";
    Files.writeString(scriptFile, originalSource);

    ProjectModel model = new ProjectModel(
        tempDir, null, ":run", "com.example.game.MyGame", 21,
        List.of(srcMainJava), List.of(), List.of(), List.of());

    Path projectFile = tempDir.resolve("game.litidata");
    Editor.instance().setProjectPath(projectFile);
    Editor.instance().setProjectModel(model);

    ScriptDefinition definition = new ScriptDefinition(
        "EnemyAI", "java", "src/main/java/com/example/game/EnemyBehavior.java",
        "com.example.game.EnemyBehavior", ScriptHostType.ENTITY);
    definition.setName("Enemy AI");
    Editor.instance().getGameFile().getScripts().add(definition);

    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    // Renaming class from EnemyBehavior to EnemyAI (which matches definition.getId())
    boolean renamed = panel.renameScript(definition, "EnemyAI");
    assertTrue(renamed, "Rename should succeed even when newClassName matches custom script id");

    assertFalse(Files.exists(scriptFile));
    Path newFile = pkgDir.resolve("EnemyAI.java");
    assertTrue(Files.exists(newFile), "Class file must be renamed to EnemyAI.java");

    String updatedSource = Files.readString(newFile);
    assertTrue(updatedSource.contains("class EnemyAI"), "Class declaration must be EnemyAI");
    assertEquals("com.example.game.EnemyAI", definition.getImplementation());
    assertEquals("src/main/java/com/example/game/EnemyAI.java", definition.getSource());
    assertEquals("EnemyAI", definition.getId());
  }
}
