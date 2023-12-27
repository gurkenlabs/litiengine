package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.sound.Sound;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class ResourcesTests {

  @Test
  void testInitialization() {
    assertNotNull(Resources.fonts());
    assertNotNull(Resources.images());
    assertNotNull(Resources.maps());
    assertNotNull(Resources.sounds());
    assertNotNull(Resources.spritesheets());
    assertNotNull(Resources.strings());
  }

  @Test
  void testResourceContainer() {
    Resources.images().clear();

    final String imageName = "my-test.jpg";

    BufferedImage testImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);

    Resources.images().add(imageName, testImage);

    assertEquals(testImage, Resources.images().get(imageName));

    assertTrue(Resources.images().get(e -> true).contains(testImage));
    assertFalse(Resources.images().get(e -> false).contains(testImage));

    assertEquals(1, Resources.images().count());
    assertEquals(testImage, Resources.images().remove(imageName));

    assertEquals(0, Resources.images().count());
  }

  @Test
  void testMapResourcesAlias() {
    IMap map =
      Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    assertEquals(map, Resources.maps().get("test-map"));
  }

  @Test
  void testResourceFromWeb() throws IOException {
    try (InputStream stream =
           Resources.get(
             "https://github.com/gurkenlabs/litiengine/blob/1ab49e67edf67242f1e1e6a67b54f36dd9b09c7e/resources/litiengine-banner.png?raw=true")) {
      assertNotNull(stream);
    }
  }

  @Test
  void testSoundResources() {
    Sound sound = Resources.sounds().get("de/gurkenlabs/litiengine/resources/bip.ogg");
    Sound nonExisting = Resources.sounds().get("randomname.mp3");

    assertNotNull(sound);
    assertEquals("bip", sound.getName());
    assertNull(nonExisting);
  }

  @Test
  void testReadStringResources() {
    String fileContent =
      Resources.read("de/gurkenlabs/litiengine/resources/stringfile-utf8.txt");
    assertEquals("my utf8 èncöded strîng!!1$", fileContent);
  }

  @Test
  void testReadStringCharsetResources() {
    String fileContent =
      Resources.read(
        "de/gurkenlabs/litiengine/resources/stringfile-iso8859-1.txt",
        StandardCharsets.ISO_8859_1);
    assertEquals("my iso8859 èncöded strîng!!1$", fileContent);
  }

  @Test
  void testStringList() {
    String[] strings =
      Resources.strings().getList("de/gurkenlabs/litiengine/resources/test.txt");

    assertEquals(4, strings.length);

    assertEquals("123", strings[0]);
    assertEquals("456", strings[1]);
    assertEquals("mystring", strings[2]);
    assertEquals("some other string", strings[3]);
  }

  @Test
  void testLocalizableString() {
    final String bundleName = "de/gurkenlabs/litiengine/resources/custom-strings";
    String oldLang = Game.config().client().getLanguage();
    String oldCountry = Game.config().client().getCountry();
    Game.config().client().setLanguage(Locale.ROOT.getLanguage());
    Game.config().client().setCountry(Locale.ROOT.getCountry());
    try {
      String myString = Resources.strings().getFrom(bundleName, "mystring");
      String myOtherString = Resources.strings().getFrom(bundleName, "myOtherString");
      String lowerCase = Resources.strings().getFrom(bundleName, "myotherstring");


      assertEquals("test me once", myString);
      assertEquals("test me twice", myOtherString);
      assertEquals("i'm lower case", lowerCase);
    } finally {
      Game.config().client().setLanguage(oldLang);
      Game.config().client().setCountry(oldCountry);
    }
  }

  @Test
  void testLocalizedString() {
    final String bundleName = "de/gurkenlabs/litiengine/resources/custom-strings";

    String oldLang = Game.config().client().getLanguage();
    String oldCountry = Game.config().client().getCountry();
    Game.config().client().setLanguage("de");
    Game.config().client().setCountry("DE");
    try {
      String myString = Resources.strings().getFrom(bundleName, "mystring");
      String myOtherString = Resources.strings().getFrom(bundleName, "myOtherString");

      assertEquals("teste mich mal", myString);
      assertEquals("teste mich ein zweites Mal", myOtherString);
    } finally {
      Game.config().client().setLanguage(oldLang);
      Game.config().client().setCountry(oldCountry);
    }
  }

  @Test()
  void testStringsWithNoArgs() {
    final String bundleName = "de/gurkenlabs/litiengine/resources/custom-strings";
    String oldLang = Game.config().client().getLanguage();
    String oldCountry = Game.config().client().getCountry();
    Game.config().client().setLanguage(Locale.ROOT.getLanguage());
    Game.config().client().setCountry(Locale.ROOT.getCountry());
    try {
      String result = Resources.strings().getFrom(bundleName, "mystring", new Object[1]);

      assertEquals("test me once", result);
    } finally {
      Game.config().client().setLanguage(oldLang);
      Game.config().client().setCountry(oldCountry);
    }
  }
}
