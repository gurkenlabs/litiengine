package de.gurkenlabs.utiliti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class LocalizationBundlesTest {
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)(?:,[^}]*)?}");
  private static final List<String> LOCALIZED_BUNDLES = List.of(
    "strings_de_DE.properties",
    "strings_es_ES.properties",
    "strings_fr_FR.properties");

  @Test
  void localizedBundlesMatchEnglishKeysAndPlaceholders() throws IOException {
    Properties english = load("strings.properties");

    for (String bundleName : LOCALIZED_BUNDLES) {
      Properties localized = load(bundleName);
      assertEquals(english.stringPropertyNames(), localized.stringPropertyNames(), bundleName);
      for (String key : english.stringPropertyNames()) {
        String value = localized.getProperty(key);
        assertFalse(value.isBlank(), bundleName + ": " + key);
        assertEquals(placeholders(english.getProperty(key)), placeholders(value), bundleName + ": " + key);
      }
    }
  }

  @Test
  void localizedBundlesAreReadAsUtf8() throws IOException {
    assertEquals("Öffnen...", load("strings_de_DE.properties").getProperty("menu_file_open"));
    assertEquals("Español", load("strings_es_ES.properties").getProperty("menu_view_language_es_ES"));
    assertEquals("Français", load("strings_fr_FR.properties").getProperty("menu_view_language_fr_FR"));
  }

  @Test
  void localizedPlaceholdersCanBeFormatted() throws IOException {
    for (String bundleName : LOCALIZED_BUNDLES) {
      Properties localized = load(bundleName);
      for (String key : localized.stringPropertyNames()) {
        String value = localized.getProperty(key);
        if (placeholders(value).isEmpty()) {
          continue;
        }
        Object[] arguments = new Object[10];
        java.util.Arrays.fill(arguments, 1);
        String formatted = MessageFormat.format(value, arguments);
        assertFalse(PLACEHOLDER.matcher(formatted).find(), bundleName + ": " + key);
      }
    }
  }

  private static Properties load(String resourceName) throws IOException {
    InputStream stream = LocalizationBundlesTest.class.getClassLoader().getResourceAsStream(resourceName);
    assertNotNull(stream, resourceName);
    try (stream; InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      Properties properties = new Properties();
      properties.load(reader);
      return properties;
    }
  }

  private static Set<String> placeholders(String value) {
    Set<String> placeholders = new LinkedHashSet<>();
    Matcher matcher = PLACEHOLDER.matcher(value);
    while (matcher.find()) {
      placeholders.add(matcher.group(1));
    }
    return placeholders;
  }
}
