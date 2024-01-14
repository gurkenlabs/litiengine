package de.gurkenlabs.litiengine.sound.spi;

import java.nio.ByteBuffer;

public class BitReader {
  private final int BITS_PER_BYTE = 8;

  private final ByteBuffer data;

  private final int startByteIndex;

  private int current;

  public BitReader(byte... data) {
    this(data, 0);
  }

  public BitReader(byte[] data, int start) {
    this(ByteBuffer.wrap(data), start);
  }

  public BitReader(ByteBuffer data, int start) {
    this.data = data;
    this.startByteIndex = start;
    this.current = this.startByteIndex * BITS_PER_BYTE;
  }

  public int get(int bits) {
    int result = 0;
    for (int i = this.current; i < this.current + bits; i++) {
      int byteIndex = i / BITS_PER_BYTE;
      int bitIndex = 7 - (i % BITS_PER_BYTE); // Little-endian (LSB first)

      // Extract the bit from the byte and shift it to the correct position
      int bitValue = (this.data.get(byteIndex) >> bitIndex) & 1;
      result = (result << 1) | bitValue;
    }

    this.current += bits;
    return result;
  }

  public boolean getBoolean() {
    return this.get(1) == 1;
  }

  public void reset() {
    this.current = this.startByteIndex * BITS_PER_BYTE;
  }
}
