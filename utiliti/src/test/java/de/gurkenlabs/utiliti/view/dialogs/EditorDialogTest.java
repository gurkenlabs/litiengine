package de.gurkenlabs.utiliti.view.dialogs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EditorDialogTest {
  @Test
  void separatesPromptAndDetailAtNewline() {
    assertArrayEquals(
      new String[] {"Delete this asset?", "It is still used by entities."},
      ConfirmDialog.splitMessage("Delete this asset?\n It is still used by entities."));
  }

  @Test
  void separatesPromptAndDetailAtQuestionMark() {
    assertArrayEquals(
      new String[] {"Revert all changes?", "This action cannot be undone."},
      ConfirmDialog.splitMessage("Revert all changes? This action cannot be undone."));
  }

  @Test
  void normalizesExportExtensions() {
    assertEquals(".png", ExportFormatDialog.normalizeExtension("png"));
    assertEquals(".gif", ExportFormatDialog.normalizeExtension(".gif"));
    assertEquals(".png", ExportFormatDialog.normalizeExtension(""));
  }
}
