package de.gurkenlabs.litiengine.sound.spi.mp3;

import java.nio.ByteBuffer;

public class BitReader {

  private final ByteBuffer data;

  private final int start;

  private int current;

  BitReader(byte... data) {
    this(ByteBuffer.wrap(data), 0);
  }

  BitReader(ByteBuffer data, int start) {
    this.data = data;
    this.start = start;
    this.current = start;
  }

  public int get(int bits) {
    int result = 0;
    for (int i = current; i < current + bits; i++) {
      int byteIndex = i / 8;
      int bitIndex = 7 - (i % 8); // Little-endian (LSB first)

      // Extract the bit from the byte and shift it to the correct position
      int bitValue = (this.data.get(byteIndex) >> bitIndex) & 1;
      result = (result << 1) | bitValue;
    }

    current += bits;
    return result;
  }

  public boolean getBoolean() {
    return this.get(1) == 1;
  }

  public void clear() {
    this.current = this.start;
  }
}
