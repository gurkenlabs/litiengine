package de.gurkenlabs.litiengine.sound.spi.mp3;

final class BitReserve {
  private static final int BUFSIZE = 4096 * 8;
  private static final int BUFSIZE_MASK = BUFSIZE - 1;

  private int offset;
  private int totbit;
  private int bufByteIdx;
  private final int[] buf;

  BitReserve() {
    this.buf = new int[BUFSIZE];
    this.offset = 0;
    this.totbit = 0;
    this.bufByteIdx = 0;
  }

  public int hsstell() {
    return totbit;
  }

  public int hgetbits(int n) {
    totbit += n;

    int val = 0;
    int pos = bufByteIdx;
    if (pos + n < BUFSIZE) {
      while (n-- > 0) {
        val <<= 1;
        val |= (buf[pos++] != 0) ? 1 : 0;
      }
    } else {
      while (n-- > 0) {
        val <<= 1;
        val |= (buf[pos] != 0) ? 1 : 0;
        pos = (pos + 1) & BUFSIZE_MASK;
      }
    }
    bufByteIdx = pos;
    return val;
  }

  public int hget1bit() {
    totbit++;
    int val = buf[bufByteIdx] != 0 ? 1 : 0;
    bufByteIdx = (bufByteIdx + 1) & BUFSIZE_MASK;
    return val;
  }

  public void hputbuf(int val) {
    int ofs = offset;
    buf[ofs++] = val & 0x80;
    buf[ofs++] = val & 0x40;
    buf[ofs++] = val & 0x20;
    buf[ofs++] = val & 0x10;
    buf[ofs++] = val & 0x08;
    buf[ofs++] = val & 0x04;
    buf[ofs++] = val & 0x02;
    buf[ofs++] = val & 0x01;
    
    totbit += 8;

    if (ofs == BUFSIZE) {
      offset = 0;
    } else {
      offset = ofs;
    }
  }

  public void rewindNbits(int n) {
    totbit -= n;
    bufByteIdx -= n;
    if (bufByteIdx < 0) {
      bufByteIdx += BUFSIZE;
    }
  }

  public void rewindNbytes(int n) {
    int bits = n << 3;
    totbit -= bits;
    bufByteIdx -= bits;
    if (bufByteIdx < 0) {
      bufByteIdx += BUFSIZE;
    }
  }

  public void reset() {
    offset = 0;
    totbit = 0;
    bufByteIdx = 0;
  }
}
