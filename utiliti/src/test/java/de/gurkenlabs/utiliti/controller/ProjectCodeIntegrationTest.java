package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityController;
import de.gurkenlabs.litiengine.scripting.CreatureScript;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptInfo;
import de.gurkenlabs.litiengine.scripting.ScriptProperty;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectCodeIntegrationTest {

  @Test
  void reloadWithNullOrInvalidPathReturnsEmptyDefinitions() {
    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.reload(null);
      assertTrue(integration.getDefinitions().isEmpty());
      assertTrue(integration.getScriptDefinitions().isEmpty());
      assertTrue(integration.getControllerDefinitions().isEmpty());

      integration.reload(Path.of("non_existent_game_file.litidata"));
      assertTrue(integration.getDefinitions().isEmpty());
      assertTrue(integration.getScriptDefinitions().isEmpty());
      assertTrue(integration.getControllerDefinitions().isEmpty());
    }
  }

  @Test
  void reloadWithEmptyDirectoryReturnsEmptyDefinitions(@TempDir Path tempDir) {
    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      Path gameFile = tempDir.resolve("game.litidata");
      integration.reload(gameFile);
      assertNotNull(integration.getDefinitions());
      assertTrue(integration.getDefinitions().isEmpty());
      assertTrue(integration.getScriptDefinitions().isEmpty());
      assertTrue(integration.getControllerDefinitions().isEmpty());
    }
  }

  @Test
  void closeResetsDefinitionsAndClassLoader() {
    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.close();
      assertTrue(integration.getDefinitions().isEmpty());
    }
  }

  @Test
  void discoversCompiledEntityControllers(@TempDir Path tempDir) throws Exception {
    Path gameFile = tempDir.resolve("game.litidata");
    copyClass(tempDir, DiscoverableController.class);

    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.reload(gameFile);

      assertTrue(integration.getControllerDefinitions().stream()
        .anyMatch(definition -> definition.className().equals(DiscoverableController.class.getName())
          && !definition.contract()));
    }
  }

  @Test
  void reloadUsesOutputDirectoriesFromProjectModel(@TempDir Path tempDir) throws Exception {
    Path customOutput = tempDir.resolve("custom-output");
    copyClass(customOutput, DiscoverableController.class, true);
    ProjectModel project = new ProjectModel(tempDir, null, ":run", null, 25,
        java.util.List.of(), java.util.List.of(customOutput), java.util.List.of(), java.util.List.of());

    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.reloadProject(project);

      assertTrue(integration.getControllerDefinitions().stream()
        .anyMatch(definition -> definition.className().equals(DiscoverableController.class.getName())));
    }
  }

  @Test
  void duplicateCompiledOutputsAreDiscoveredOnlyOnce(@TempDir Path tempDir) throws Exception {
    Path firstOutput = tempDir.resolve("first-output");
    Path secondOutput = tempDir.resolve("second-output");
    copyClass(firstOutput, DiscoverableController.class, true);
    copyClass(secondOutput, DiscoverableController.class, true);
    ProjectModel project = new ProjectModel(tempDir, null, ":run", null, 25,
        java.util.List.of(), java.util.List.of(firstOutput, secondOutput),
        java.util.List.of(), java.util.List.of());

    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.reloadProject(project);

      assertEquals(1, integration.getControllerDefinitions().stream()
        .filter(definition -> definition.className().equals(DiscoverableController.class.getName()))
        .count());
    }
  }

  @Test
  void discoveredScriptsRetainTheirProjectSourcePath(@TempDir Path tempDir) throws Exception {
    Path customOutput = tempDir.resolve("custom-output");
    copyClass(customOutput, DiscoverableScript.class, true);
    Path sourceRoot = tempDir.resolve("src/main/java");
    Path source = sourceRoot.resolve(ProjectCodeIntegrationTest.class.getName().replace('.', '/') + ".java");
    Files.createDirectories(source.getParent());
    Files.writeString(source, "// source location used by the editor");
    ProjectModel project = new ProjectModel(tempDir, null, ":run", null, 25,
        java.util.List.of(sourceRoot), java.util.List.of(customOutput), java.util.List.of(), java.util.List.of());

    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.reloadProject(project);

      var definition = integration.getScriptDefinitions().stream()
          .filter(candidate -> candidate.className().equals(DiscoverableScript.class.getName()))
          .findFirst().orElseThrow();
      assertEquals(source.toAbsolutePath().normalize(), definition.sourcePath());
      assertEquals(source.toAbsolutePath().normalize(), integration.findSource(DiscoverableScript.class.getName()));
    }
  }

  @Test
  void sourceIndexUsesTheDeclaredPackageWhenFoldersDoNotMatch(@TempDir Path tempDir) throws Exception {
    Path sourceRoot = tempDir.resolve("sources");
    Path source = sourceRoot.resolve("legacy/layout/Janitor.java");
    Files.createDirectories(source.getParent());
    Files.writeString(source, "package de.gurkenlabs.game;\npublic class Janitor {}\n");

    assertEquals("de.gurkenlabs.game.Janitor",
      ProjectCodeIntegration.sourceClassName(sourceRoot, source));
  }

  @ScriptInfo(id = "discoverable", host = ScriptHostType.ENTITY, target = Creature.class)
  public static final class DiscoverableScript extends CreatureScript {}

  public static final class DiscoverableController implements IEntityController {
    private final Creature entity;

    public DiscoverableController(Creature entity) {
      this.entity = entity;
    }

    @Override public void attach() {}

    @Override public void detach() {}

    @Override public IEntity getEntity() {
      return this.entity;
    }

    @Override public void update() {}
  }

  private static void copyClass(Path root, Class<?> type) throws Exception {
    copyClass(root, type, false);
  }

  private static void copyClass(Path root, Class<?> type, boolean directOutput) throws Exception {
    String className = type.getName();
    Path classes = directOutput ? root : root.resolve("build/classes/java/main");
    Path classFile = classes.resolve(className.replace('.', '/') + ".class");
    Files.createDirectories(classFile.getParent());
    try (var source = ProjectCodeIntegrationTest.class.getClassLoader()
      .getResourceAsStream(className.replace('.', '/') + ".class")) {
      Files.copy(source, classFile);
    }
  }
}
