package de.gurkenlabs.utiliti.view.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.utiliti.model.KeyBindings.Command;
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

  @Test
  void categorySearchMatchesIndividualSettings() {
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GENERAL, "fps"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GENERAL, "verbosity"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.MCP, "port"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.MCP, "mcp"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.APPEARANCE, "font"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.APPEARANCE, "scale"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GRID, "snap"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GRID, "stroke"));
    assertFalse(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GRID, "fps"));
    assertFalse(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.MCP, "nonexistentqueryxyz"));
  }

  @Test
  void keymapSearchMatchesCommandAndShortcuts() {
    assertTrue(SettingsDialog.matchesKeymap(Command.UNDO, Command.UNDO.defaultKeyStroke(), "undo"));
    assertTrue(SettingsDialog.matchesKeymap(Command.SAVE_PROJECT, Command.SAVE_PROJECT.defaultKeyStroke(), "save"));
    assertTrue(SettingsDialog.matchesKeymap(Command.SCRIPT_SAVE, Command.SCRIPT_SAVE.defaultKeyStroke(), "save"));
    assertTrue(SettingsDialog.matchesKeymap(Command.DEBUG_PROJECT, Command.DEBUG_PROJECT.defaultKeyStroke(), "f9"));
    assertTrue(SettingsDialog.matchesKeymap(Command.SAVE_PROJECT, Command.SAVE_PROJECT.defaultKeyStroke(), "ctrl+s"));
    assertFalse(SettingsDialog.matchesKeymap(Command.UNDO, Command.UNDO.defaultKeyStroke(), "nonexistentqueryxyz"));
  }

  @Test
  void searchHandlesNullEmptyAndWhitespaceQueries() {
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.APPEARANCE, null));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GENERAL, "   "));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GRID, ""));
    assertTrue(SettingsDialog.matchesKeymap(Command.SAVE_PROJECT, Command.SAVE_PROJECT.defaultKeyStroke(), null));
    assertTrue(SettingsDialog.matchesKeymap(Command.SAVE_PROJECT, Command.SAVE_PROJECT.defaultKeyStroke(), "   "));
  }

  @Test
  void searchIsCaseInsensitive() {
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GENERAL, "FPS"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.MCP, "PORT"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.APPEARANCE, "THEME"));
    assertTrue(SettingsDialog.matchesKeymap(Command.UNDO, Command.UNDO.defaultKeyStroke(), "UNDO"));
    assertTrue(SettingsDialog.matchesKeymap(Command.SAVE_PROJECT, Command.SAVE_PROJECT.defaultKeyStroke(), "CTRL+S"));
  }

  @Test
  void categoryMatchesItsOwnNameAndDescription() {
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.APPEARANCE, "appearance"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GENERAL, "general"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.GRID, "grid"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.KEYMAP, "keymap"));
    assertTrue(SettingsDialog.matchesCategoryOrSettings(SettingsDialog.Category.MCP, "mcp"));
  }

  @Test
  void highlightHtmlHighlightsMatchingTextPreservingCase() {
    String highlighted = SettingsDialog.highlightHtml("Editor FPS cap", "fps");
    assertTrue(highlighted.startsWith("<html>"));
    assertTrue(highlighted.contains(">FPS</span>"));
    assertTrue(highlighted.contains("Editor "));
    assertTrue(highlighted.contains(" cap"));
  }

  @Test
  void highlightHtmlReturnsOriginalWhenNoMatchOrEmptyQuery() {
    assertEquals("Editor FPS cap", SettingsDialog.highlightHtml("Editor FPS cap", "xyz"));
    assertEquals("Editor FPS cap", SettingsDialog.highlightHtml("Editor FPS cap", ""));
    assertEquals("Editor FPS cap", SettingsDialog.highlightHtml("Editor FPS cap", null));
    assertEquals("", SettingsDialog.highlightHtml(null, "fps"));
  }

  @Test
  void highlightHtmlEscapesHtmlAndHighlightsMultiple() {
    String text = "<Script> & Save & save";
    String highlighted = SettingsDialog.highlightHtml(text, "save");
    assertTrue(highlighted.contains("&lt;Script&gt; &amp; "));
    assertTrue(highlighted.contains(">Save</span>"));
    assertTrue(highlighted.contains(">save</span>"));
  }
}
