package de.gurkenlabs.utiliti.view.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SettingsDialogTest {
  @Test
  void resolvesSystemAndConfiguredLocales() {
    assertEquals("SYSTEM", SettingsDialog.resolveLocaleOption(null, null));
    assertEquals("SYSTEM", SettingsDialog.resolveLocaleOption("en", null));
    assertEquals("SYSTEM", SettingsDialog.resolveLocaleOption("", ""));
    assertEquals("ENGLISH", SettingsDialog.resolveLocaleOption("en", "US"));
    assertEquals("GERMAN", SettingsDialog.resolveLocaleOption("de", "DE"));
  }

  @Test
  void fallsBackToEnglishForUnsupportedLocale() {
    assertEquals("ENGLISH", SettingsDialog.resolveLocaleOption("it", "IT"));
  }

  @Test
  void unsavedDialogLocationIsNotVisible() {
    org.junit.jupiter.api.Assertions.assertFalse(
        SettingsDialog.isVisibleOnScreen(Integer.MIN_VALUE, Integer.MIN_VALUE, 1280, 800));
  }

  @Test
  void clampToScreenKeepsDialogWithinBounds() {
    if (java.awt.GraphicsEnvironment.isHeadless()) {
      return;
    }
    javax.swing.JDialog dialog = new javax.swing.JDialog();
    dialog.setSize(300, 200);
    dialog.setLocation(-500, -500);
    SettingsDialog.clampToScreen(dialog);
    org.junit.jupiter.api.Assertions.assertTrue(dialog.getX() >= 0);
    org.junit.jupiter.api.Assertions.assertTrue(dialog.getY() >= 0);
    dialog.dispose();
  }
}
