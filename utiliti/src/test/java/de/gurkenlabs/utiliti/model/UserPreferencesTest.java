package de.gurkenlabs.utiliti.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserPreferencesTest {
  @Test
  void reopensLastProjectByDefault() {
    UserPreferences preferences = new UserPreferences();

    assertTrue(preferences.reopenLastProject());
    preferences.setReopenLastProject(false);
    assertFalse(preferences.reopenLastProject());
  }

  @Test
  void storesAndBoundsEditorFontPreferences() {
    UserPreferences preferences = new UserPreferences();

    assertEquals("Roboto", preferences.getEditorFontFamily());
    assertEquals(12, preferences.getEditorFontSize());

    preferences.setEditorFontFamily("Consolas");
    preferences.setEditorFontSize(100);
    assertEquals("Consolas", preferences.getEditorFontFamily());
    assertEquals(UserPreferences.EDITOR_FONT_SIZE_MAX, preferences.getEditorFontSize());

    preferences.setEditorFontSize(1);
    assertEquals(UserPreferences.EDITOR_FONT_SIZE_MIN, preferences.getEditorFontSize());

    preferences.setEditorFontFamily(" ");
    assertEquals("Roboto", preferences.getEditorFontFamily());
  }

  @Test
  void settingsDialogHasNoSavedLocationByDefault() {
    UserPreferences preferences = new UserPreferences();

    assertEquals(Integer.MIN_VALUE, preferences.getSettingsDialogX());
    assertEquals(Integer.MIN_VALUE, preferences.getSettingsDialogY());
  }

  @Test
  void storesAndBoundsEditorFpsCap() {
    UserPreferences preferences = new UserPreferences();

    assertEquals(60, preferences.getEditorFpsCap());
    preferences.setEditorFpsCap(2000);
    assertEquals(UserPreferences.EDITOR_FPS_CAP_MAX, preferences.getEditorFpsCap());
    preferences.setEditorFpsCap(0);
    assertEquals(UserPreferences.EDITOR_FPS_CAP_MIN, preferences.getEditorFpsCap());
  }

  @Test
  void gradleLaunchArgumentsAreOptionalAndNormalized() {
    UserPreferences preferences = new UserPreferences();

    assertEquals("", preferences.getGradleLaunchArguments());
    preferences.setGradleLaunchArguments("  --stacktrace --info  ");
    assertEquals("--stacktrace --info", preferences.getGradleLaunchArguments());
    preferences.setGradleLaunchArguments(null);
    assertEquals("", preferences.getGradleLaunchArguments());
  }
}
