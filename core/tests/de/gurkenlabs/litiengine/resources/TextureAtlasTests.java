package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TextureAtlasTests {

  @Test
  public void testReadTextureAtlasFromFile() {
    TextureAtlas atlas =
        TextureAtlas.read("de/gurkenlabs/litiengine/resources/gurk-nukem-atlas.xml");

    assertNotNull(atlas);
    assertEquals(123, atlas.getWidth());
    assertEquals(122, atlas.getHeight());
    assertEquals(12, atlas.getSprites().size());

    TextureAtlas.Sprite deanIdle = atlas.getSprite("Dean-idle-left.png");

    assertNotNull(deanIdle);
    assertEquals(105, deanIdle.getX());
    assertEquals(0, deanIdle.getY());
    assertEquals(18, deanIdle.getWidth());
    assertEquals(32, deanIdle.getHeight());
    assertTrue(deanIdle.isRotated());

    // now load the image
    BufferedImage image = Resources.images().get(atlas.getAbsoluteImagePath());

    assertNotNull(image);
    assertEquals(123, image.getWidth());
    assertEquals(122, image.getHeight());
  }

  @ParameterizedTest
  @MethodSource("getTextureAtlasLoadResourceName")
  public void testTextureAtlasLoad(String resourceName) {
    TextureAtlas atlas =
        TextureAtlas.read("de/gurkenlabs/litiengine/resources/gurk-nukem-atlas.xml");

    // file must be available on the file system
    if (atlas == null) {
      fail();
    }

    Resources.images().load(atlas);

    BufferedImage img = Resources.images().get(resourceName);
    assertNotNull(img);
  }

  private static Stream<Arguments> getTextureAtlasLoadResourceName() {
    return Stream.of(
        Arguments.of("Dean-idle-left.png"),
        Arguments.of("Dean-walk-left.png"),
        Arguments.of("gurknukem-idle-left.png"),
        Arguments.of("gurknukem-walk-left.png"),
        Arguments.of("icon.png"),
        Arguments.of("Jorge-idle-left.png"),
        Arguments.of("Jorge-walk-left.png"),
        Arguments.of("prop-barrel-damaged.png"),
        Arguments.of("prop-barrel-destroyed.png"),
        Arguments.of("prop-barrel-intact.png"),
        Arguments.of("prop-bunker.png"),
        Arguments.of("prop-flag.png"));
  }

  @ParameterizedTest
  @MethodSource("getTextureAtlasLoadWidthHeight")
  public void testTextureAtlasLoadWidth(
      String resourceName, int expectedWidth, int expectedHeight) {
    TextureAtlas atlas =
        TextureAtlas.read("de/gurkenlabs/litiengine/resources/gurk-nukem-atlas.xml");

    // file must be available on the file system
    if (atlas == null) {
      fail();
    }

    Resources.images().load(atlas);

    BufferedImage img = Resources.images().get(resourceName);
    assertEquals(expectedWidth, img.getWidth());
  }

  @ParameterizedTest
  @MethodSource("getTextureAtlasLoadWidthHeight")
  public void testTextureAtlasLoadHeight(
      String resourceName, int expectedWidth, int expectedHeight) {
    TextureAtlas atlas =
        TextureAtlas.read("de/gurkenlabs/litiengine/resources/gurk-nukem-atlas.xml");

    // file must be available on the file system
    if (atlas == null) {
      fail();
    }

    Resources.images().load(atlas);

    BufferedImage img = Resources.images().get(resourceName);
    assertEquals(expectedHeight, img.getHeight());
  }

  private static Stream<Arguments> getTextureAtlasLoadWidthHeight() {
    return Stream.of(
        Arguments.of("Dean-idle-left.png", 32, 18),
        Arguments.of("Dean-walk-left.png", 64, 18),
        Arguments.of("gurknukem-idle-left.png", 36, 18),
        Arguments.of("gurknukem-walk-left.png", 72, 18),
        Arguments.of("icon.png", 64, 64),
        Arguments.of("prop-barrel-damaged.png", 10, 12),
        Arguments.of("prop-bunker.png", 104, 41));
  }
}
