package de.gurkenlabs.litiengine.util.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.gurkenlabs.util.io.SerializationHelper;

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

  @Test(expected = IllegalArgumentException.class)
  public void testSmallFloatEncodingNegative() {
    final float small = -1;

    SerializationHelper.encodeSmallFloatingPointNumber(small, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallFloatEncodingTooLarge() {
    final float small = 6553.51254F;
    SerializationHelper.encodeSmallFloatingPointNumber(small, 2);
  }
}
