package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SerializationHelperTest {
  @Test
  public void testSmallFloatEncodingMax() {
    final float small = 6553.41254F;

    final short encoded = SerializationHelper.encodeSmallFloatingPointNumber(small, 1);
    final float decoded = SerializationHelper.decodeSmallFloatingPointNumber(encoded, 1);

    assertEquals(small, decoded, 0.1F);
  }

  @Test
  public void testSmallFloatEncodingMin() {
    final float small = 0F;

    final short encoded = SerializationHelper.encodeSmallFloatingPointNumber(small, 1);
    final float decoded = SerializationHelper.decodeSmallFloatingPointNumber(encoded, 1);

    assertEquals(small, decoded, 0.1F);
  }

  @Test()
  public void testSmallFloatEncodingNegative() {
    final float small = -1;

    assertThrows(IllegalArgumentException.class, () -> SerializationHelper.encodeSmallFloatingPointNumber(small, 1));
  }

  @Test()
  public void testSmallFloatEncodingTooLarge() {
    final float small = 6553.51254F;
    assertThrows(IllegalArgumentException.class, () -> SerializationHelper.encodeSmallFloatingPointNumber(small, 2));
  }
}
