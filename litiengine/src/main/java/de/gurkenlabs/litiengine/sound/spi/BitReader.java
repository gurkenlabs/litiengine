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
      int bitIndex = 7 - (i % BITS_PER_BYTE); // Little-endian (LSB first)

      if(byteIndex > this.data.limit() -1){
        return END_OF_DATA;
      }
      // Extract the bit from the byte and shift it to the correct position
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

  public void reset() {
    this.current = this.startByteIndex * BITS_PER_BYTE;
  }
}
