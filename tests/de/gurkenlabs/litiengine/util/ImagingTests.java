package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ImagingTests {

  @Test
  public void testSubImage() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage[][] subImages = Imaging.getSubImages(image, 1, 2);

    assertEquals(2, subImages[0].length);
    assertEquals(15, subImages[0][0].getWidth());
    assertEquals(16, subImages[0][0].getHeight());
    assertEquals(15, subImages[0][1].getWidth());
    assertEquals(16, subImages[0][1].getHeight());
  }

  @Test
  public void testHorizontalFlip() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage flippedReferenceImage =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-flip-hor.png");
    int[] expectedPixels =
        ((DataBufferInt) flippedReferenceImage.getData().getDataBuffer()).getData();

    BufferedImage flipped = Imaging.horizontalFlip(image);
    int[] actualPixels = ((DataBufferInt) flipped.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixels, actualPixels);
  }

  @Test
  public void testVerticalFlip() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage flippedReferenceImage =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-flip-vert.png");
    int[] expectedPixels =
        ((DataBufferInt) flippedReferenceImage.getData().getDataBuffer()).getData();

    BufferedImage flipped = Imaging.verticalFlip(image);
    int[] actualPixels = ((DataBufferInt) flipped.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixels, actualPixels);
  }

  @ParameterizedTest(name = "testRotate rotatedImagePath={0}, rotation={1}")
  @MethodSource("getRotateArguments")
  public void testRotate(String rotatedImagePath, Rotation rotation) {
    // precondition check to prevent resource optimism
    String imagePath = "tests/de/gurkenlabs/litiengine/util/prop-flag.png";

    // image must be available on the file system
    if (!new File(imagePath).exists()) {
      fail();
    }

    // rotated image must be available on the file system
    if (!new File(rotatedImagePath).exists()) {
      fail();
    }

    // arrange
    BufferedImage image = Resources.images().get(imagePath);
    BufferedImage expectedRotatedImage = Resources.images().get(rotatedImagePath);
    int[] expectedPixels =
        ((DataBufferInt) expectedRotatedImage.getData().getDataBuffer()).getData();

    // act
    BufferedImage actualRotated = Imaging.rotate(image, rotation);
    int[] actualPixels = ((DataBufferInt) actualRotated.getData().getDataBuffer()).getData();

    // assert
    assertArrayEquals(expectedPixels, actualPixels);
  }

  /**
   * This method is used for defining the input arguments of the parameterized test {@link
   * #testRotate(String, Rotation)}
   *
   * @return Test arguments
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getRotateArguments() {
    return Stream.of(
        Arguments.of("tests/de/gurkenlabs/litiengine/util/prop-flag.png", Rotation.NONE),
        Arguments.of("tests/de/gurkenlabs/litiengine/util/prop-flag-90.png", Rotation.ROTATE_90),
        Arguments.of("tests/de/gurkenlabs/litiengine/util/prop-flag-180.png", Rotation.ROTATE_180),
        Arguments.of("tests/de/gurkenlabs/litiengine/util/prop-flag-270.png", Rotation.ROTATE_270));
  }

  @Test
  public void testScaling() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage expectedx2 =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-scale-x2.png");
    BufferedImage expectedStretched =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-stretch.png");
    BufferedImage expectedStretchedRatio =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-stretch-ratio.png");
    int[] expectedPixelsx2 = ((DataBufferInt) expectedx2.getData().getDataBuffer()).getData();
    int[] expectedPixelsStretched =
        ((DataBufferInt) expectedStretched.getData().getDataBuffer()).getData();
    int[] expectedPixelsStretchedRatio =
        ((DataBufferInt) expectedStretchedRatio.getData().getDataBuffer()).getData();

    BufferedImage scaledx2 = Imaging.scale(image, 2.0);
    BufferedImage scaledMaxDouble = Imaging.scale(image, 60);
    BufferedImage stretched = Imaging.scale(image, 30, 32, false);
    BufferedImage stretchedRatio = Imaging.scale(image, 30, 32, true);

    int[] actualPixelsx2 = ((DataBufferInt) scaledx2.getData().getDataBuffer()).getData();
    int[] actualPixelsMaxDouble =
        ((DataBufferInt) scaledMaxDouble.getData().getDataBuffer()).getData();
    int[] actualPixelsStretched = ((DataBufferInt) stretched.getData().getDataBuffer()).getData();
    int[] actualPixelsStretchedRatio =
        ((DataBufferInt) stretchedRatio.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixelsx2, actualPixelsx2);
    assertArrayEquals(expectedPixelsx2, actualPixelsMaxDouble);
    assertArrayEquals(expectedPixelsStretched, actualPixelsStretched);
    assertArrayEquals(expectedPixelsStretchedRatio, actualPixelsStretchedRatio);
  }

  @Test
  public void testOpacity() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage expected25 =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-opacity-25.png");
    BufferedImage expected50 =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-opacity-50.png");
    int[] expectedPixels25 = ((DataBufferInt) expected25.getData().getDataBuffer()).getData();
    int[] expectedPixels50 = ((DataBufferInt) expected50.getData().getDataBuffer()).getData();

    BufferedImage opacity25 = Imaging.setOpacity(image, .25f);
    BufferedImage opacity50 = Imaging.setOpacity(image, .5f);

    int[] actualPixels25 = ((DataBufferInt) opacity25.getData().getDataBuffer()).getData();
    int[] actualPixels50 = ((DataBufferInt) opacity50.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixels25, actualPixels25);
    assertArrayEquals(expectedPixels50, actualPixels50);
  }

  @Test
  public void testFlash() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage expectedFlash =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-flash.png");
    int[] expectedPixelsFlash = ((DataBufferInt) expectedFlash.getData().getDataBuffer()).getData();

    BufferedImage flash = Imaging.flashVisiblePixels(image, Color.RED);

    int[] actualPixelsFlash = ((DataBufferInt) flash.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixelsFlash, actualPixelsFlash);
  }

  @Test
  public void testAlpha() {
    BufferedImage expected =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage alpha =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-alpha.png");

    int[] expectedPixels = ((DataBufferInt) expected.getData().getDataBuffer()).getData();

    BufferedImage alphaResolved = Imaging.applyAlphaChannel(alpha, Color.RED);

    int[] actualAlphaResolved = ((DataBufferInt) alphaResolved.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixels, actualAlphaResolved);
  }

  @Test
  public void testBorder() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage expectedBorder =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-border.png");
    BufferedImage expectedBorderOnly =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-border-only.png");

    int[] expectedPixels = ((DataBufferInt) expectedBorder.getData().getDataBuffer()).getData();
    int[] expectedPixelsBorderOnly =
        ((DataBufferInt) expectedBorderOnly.getData().getDataBuffer()).getData();

    BufferedImage border = Imaging.borderAlpha(image, Color.RED, false);
    BufferedImage borderOnly = Imaging.borderAlpha(image, Color.RED, true);

    int[] actualPixelsBorder = ((DataBufferInt) border.getData().getDataBuffer()).getData();
    int[] actualPixelsBorderOnly = ((DataBufferInt) borderOnly.getData().getDataBuffer()).getData();

    assertArrayEquals(expectedPixels, actualPixelsBorder);
    assertArrayEquals(expectedPixelsBorderOnly, actualPixelsBorderOnly);
  }

  @Test
  public void testEmpty() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    BufferedImage imageEmpty =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-empty.png");

    assertFalse(Imaging.isEmpty(image));
    assertTrue(Imaging.isEmpty(imageEmpty));
  }

  @Test
  public void testCopy() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    int[] expectedPixels = ((DataBufferInt) image.getData().getDataBuffer()).getData();

    BufferedImage copy = Imaging.copy(image);

    int[] actualPixels = ((DataBufferInt) copy.getData().getDataBuffer()).getData();
    assertArrayEquals(expectedPixels, actualPixels);
  }

  @Test
  public void testSpriteFlipHorizontally() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");
    Spritesheet sprite =
        new Spritesheet(image, "tests/de/gurkenlabs/litiengine/util/prop-flag.png", 15, 16);
    BufferedImage imageFlippedHor =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-spriteflip-hor.png");

    int[] expectedPixels = ((DataBufferInt) imageFlippedHor.getData().getDataBuffer()).getData();

    BufferedImage flippedHorizontally = Imaging.flipSpritesHorizontally(sprite);
    int[] actualPixels = ((DataBufferInt) flippedHorizontally.getData().getDataBuffer()).getData();
    assertArrayEquals(expectedPixels, actualPixels);
  }

  @Test
  public void testSpriteFlipVertically() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-2rows.png");
    Spritesheet sprite =
        new Spritesheet(image, "tests/de/gurkenlabs/litiengine/util/prop-flag-2rows.png", 15, 16);
    BufferedImage imageFlippedVer =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag-spriteflip-ver.png");

    int[] expectedPixels = ((DataBufferInt) imageFlippedVer.getData().getDataBuffer()).getData();

    BufferedImage flippedVertically = Imaging.flipSpritesVertically(sprite);

    int[] actualPixels = ((DataBufferInt) flippedVertically.getData().getDataBuffer()).getData();
    assertArrayEquals(expectedPixels, actualPixels);
  }
}
