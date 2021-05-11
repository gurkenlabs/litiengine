package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

public class CodecTests {
  @Test
  public void testSmallFloatEncodingMax() {
    final float small = 6553.41254F;

    final short encoded = Codec.encodeSmallFloatingPointNumber(small, 1);
    final float decoded = Codec.decodeSmallFloatingPointNumber(encoded, 1);

    assertEquals(small, decoded, 0.1F);
  }

  @Test
  public void testSmallFloatEncodingMin() {
    final float small = 0F;

    final short encoded = Codec.encodeSmallFloatingPointNumber(small, 1);
    final float decoded = Codec.decodeSmallFloatingPointNumber(encoded, 1);

    assertEquals(small, decoded, 0.1F);
  }

  @Test
  public void testSmallFloatEncodingNegative() {
    final float small = -1;

    assertThrows(
        IllegalArgumentException.class, () -> Codec.encodeSmallFloatingPointNumber(small, 1));
  }

  @Test
  public void testSmallFloatEncodingTooLarge() {
    final float small = 6553.51254F;
    assertThrows(
        IllegalArgumentException.class, () -> Codec.encodeSmallFloatingPointNumber(small, 2));
  }

  @Test
  public void testAngleEncoding() {
    final float angle = 99.99999F;
    final float angle2 = -1F;

    final byte encoded = Codec.encodeAngle(angle);
    final float decoded = Codec.decodeAngle(encoded);

    final short encodedShort = Codec.encodeAnglePrecise(angle);
    final float decodedShort = Codec.decodeAngle(encodedShort);

    final short encodedShort2 = Codec.encodeAnglePrecise(angle2);
    final float decodedShort2 = Codec.decodeAngle(encodedShort2);

    assertEquals(99.99999, decoded, 1.43);
    assertEquals(99.99999, decodedShort, 0.1F);
    assertEquals(359.0, decodedShort2, 0.1F);
  }

  @Test
  public void testImageCodec() {
    BufferedImage image =
        Resources.images().get("tests/de/gurkenlabs/litiengine/util/prop-flag.png");

    String encodedImage = Codec.encode(image);
    BufferedImage decodedImage = Codec.decodeImage(encodedImage);

    assertEquals(image.getWidth(), decodedImage.getWidth());
    assertEquals(image.getHeight(), decodedImage.getHeight());
    assertTrue(Imaging.areEqual(image, decodedImage));

    assertNull(Codec.encode(null, ImageFormat.PNG));
  }
}
