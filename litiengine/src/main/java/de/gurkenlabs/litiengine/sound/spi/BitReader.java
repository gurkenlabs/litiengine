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
    int result = 0;
    for (int i = this.current; i < this.current + bits; i++) {
      int byteIndex = i / BITS_PER_BYTE;
      int bitIndex = (BITS_PER_BYTE - 1) - (i % BITS_PER_BYTE);

      if(byteIndex > this.data.limit() -1){
        return END_OF_DATA;
      }
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
    this.current += bits;
  }

  public void reset() {
    this.current = this.startByteIndex * BITS_PER_BYTE;
  }
}
