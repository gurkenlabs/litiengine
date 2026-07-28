package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectCodeIntegrationTest {

  @Test
  void reloadWithNullOrInvalidPathReturnsEmptyDefinitions() {
    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.reload(null);
      assertTrue(integration.getDefinitions().isEmpty());

      integration.reload(Path.of("non_existent_game_file.litidata"));
      assertTrue(integration.getDefinitions().isEmpty());
    }
  }

  @Test
  void reloadWithEmptyDirectoryReturnsEmptyDefinitions(@TempDir Path tempDir) {
    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      Path gameFile = tempDir.resolve("game.litidata");
      integration.reload(gameFile);
      assertNotNull(integration.getDefinitions());
      assertTrue(integration.getDefinitions().isEmpty());
    }
  }

  @Test
  void closeResetsDefinitionsAndClassLoader() {
    try (ProjectCodeIntegration integration = new ProjectCodeIntegration()) {
      integration.close();
      assertTrue(integration.getDefinitions().isEmpty());
    }
  }
}
