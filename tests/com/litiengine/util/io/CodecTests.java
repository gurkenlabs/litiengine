package com.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.litiengine.resources.Resources;
import com.litiengine.util.Imaging;

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

    assertThrows(IllegalArgumentException.class, () -> Codec.encodeSmallFloatingPointNumber(small, 1));
  }

  @Test
  public void testSmallFloatEncodingTooLarge() {
    final float small = 6553.51254F;
    assertThrows(IllegalArgumentException.class, () -> Codec.encodeSmallFloatingPointNumber(small, 2));
  }

  @Test
  public void testAngleEncoding() {
    final float angle = 99.99999F;

    final byte encoded = Codec.encodeAngle(angle);
    final float decoded = Codec.decodeAngle(encoded);

    final short encodedShort = Codec.encodeAnglePrecise(angle);
    final float decodedShort = Codec.decodeAngle(encodedShort);

    assertEquals(99.99999, decoded, 1.43);
    assertEquals(99.99999, decodedShort, 0.1F);
  }

  @Test
  public void testImageCodec() {
    BufferedImage image = Resources.images().get("tests/com/litiengine/util/prop-flag.png");

    String encodedImage = Codec.encode(image);
    BufferedImage decodedImage = Codec.decodeImage(encodedImage);

    assertEquals(image.getWidth(), decodedImage.getWidth());
    assertEquals(image.getHeight(), decodedImage.getHeight());
    assertTrue(Imaging.areEqual(image, decodedImage));
  }
}
