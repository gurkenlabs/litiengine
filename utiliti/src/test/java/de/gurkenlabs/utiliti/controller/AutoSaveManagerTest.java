package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AutoSaveManagerTest {

  @Test
  void getBackupPathReturnsNullForNullGameFile() {
    assertNull(AutoSaveManager.getBackupPath(null));
  }

  @Test
  void getBackupPathAppendsBakExtension() {
    Path original = Path.of("game.litidata");
    Path backup = AutoSaveManager.getBackupPath(original);
    assertNotNull(backup);
    assertEquals("game.litidata.bak", backup.getFileName().toString());
  }

  @Test
  void singletonInstanceIsNotNull() {
    assertNotNull(AutoSaveManager.instance());
  }
}
