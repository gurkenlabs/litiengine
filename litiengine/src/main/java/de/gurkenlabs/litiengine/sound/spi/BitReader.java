package de.gurkenlabs.litiengine.sound.spi;

import java.nio.ByteBuffer;

public class BitReader {
  public static final int END_OF_DATA = -1;
  private final int BITS_PER_BYTE = 8;

  private final ByteBuffer data;

  private final int startByteIndex;

  private int current;

  public BitReader(byte... data) {
    this(data, 0);
  }

  public BitReader(byte[] data, int startByte) {
    this(ByteBuffer.wrap(data), startByte, 0);
  }

  public BitReader(ByteBuffer data, int startByte, int skipBits) {
    this.data = data;
    this.startByteIndex = startByte;
    this.current = this.startByteIndex * BITS_PER_BYTE + skipBits;
  }

  public int get(int bits) {
    if (bits < 0 || bits > Integer.SIZE) {
      throw new IllegalArgumentException("bits must be between 0 and 32");
    }

    if (this.current + bits > this.data.limit() * BITS_PER_BYTE) {
      return END_OF_DATA;
    }

    int result = 0;
    for (int i = this.current; i < this.current + bits; i++) {
      int byteIndex = i / BITS_PER_BYTE;
      int bitIndex = (BITS_PER_BYTE - 1) - (i % BITS_PER_BYTE);

      int bitValue = (this.data.get(byteIndex) >> bitIndex) & 1;
      result = (result << 1) | bitValue;
    }

    this.current += bits;
    return result;
  }

  public int getNextBit() {
    return this.get(1);
  }

  public boolean getBoolean() {
    return getNextBit() == 1;
  }

  /**
   * Returns the number of bits read since the start position.
   *
   * @return bits consumed since construction (or last reset)
   */
  public int getPosition() {
    return this.current - this.startByteIndex * BITS_PER_BYTE;
  }

  /**
   * Skips the specified number of bits without returning them.
   *
   * @param bits number of bits to skip
   */
  public void skip(int bits) {
    setPosition(getPosition() + bits);
  }

  public void setPosition(int position) {
    if (position < 0 || this.startByteIndex * BITS_PER_BYTE + position > this.data.limit() * BITS_PER_BYTE) {
      throw new IllegalArgumentException("Bit position is outside the input");
    }
    this.current = this.startByteIndex * BITS_PER_BYTE + position;
  }

  public int remaining() {
    return this.data.limit() * BITS_PER_BYTE - this.current;
  }

  public void reset() {
    this.current = this.startByteIndex * BITS_PER_BYTE;
  }
}
