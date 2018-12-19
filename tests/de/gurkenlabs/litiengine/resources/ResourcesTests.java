package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;

public class ResourcesTests {

  @Test
  public void testInitialization() {
    assertNotNull(Resources.fonts());
    assertNotNull(Resources.images());
    assertNotNull(Resources.maps());
    assertNotNull(Resources.sounds());
    assertNotNull(Resources.spritesheets());
    assertNotNull(Resources.strings());
  }

  @Test
  public void testResourceContainer() {
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
  public void testMapResourcesAlias() {
    IMap map = Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");
    
    assertEquals(map, Resources.maps().get("test-map"));
  }
}
