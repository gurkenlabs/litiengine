package de.gurkenlabs.litiengine.sound.spi.mp3;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

final class Mp3DecoderInputStream extends InputStream {

  private static final int ID3_HEADER_LENGTH = 10;
  private static final int ID3V1_TAG_LENGTH = 128;
  private static final int APE_DESCRIPTOR_LENGTH = 32;
  private static final int RESERVOIR_SIZE = 4096;
  private static final int SAMPLES_PER_FRAME = 1152;

  private final PushbackInputStream encodedStream;
  private final ByteBuffer pcmBuffer;
  private final AudioFormat targetFormat;
  private long encodedPosition;
  private boolean initialized;
  private boolean endOfStream;
  private boolean decodedAudioFrame;
  private boolean closed;
  private final SynthesisFilter[] synthesisFilters;
  private final OverlapAdd[][] overlapAdd;

  private final byte[] reservoir;
  private int reservoirWritePos;
  private int reservoirTotalWritten;

  Mp3DecoderInputStream(AudioInputStream sourceStream, AudioFormat targetFormat) {
    this.encodedStream = new PushbackInputStream(sourceStream, ID3_HEADER_LENGTH);
    this.targetFormat = targetFormat;
    this.encodedPosition = 0;

    int bufferSize = Math.max(65536, 4608 * 100);
    this.pcmBuffer = ByteBuffer.allocate(bufferSize);
    this.pcmBuffer.order(targetFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

    int channels = targetFormat.getChannels();

    this.synthesisFilters = new SynthesisFilter[channels];
    for (int ch = 0; ch < channels; ch++) {
      this.synthesisFilters[ch] = new SynthesisFilter(ch, 32700.0f);
    }

    this.overlapAdd = new OverlapAdd[channels][32];
    for (int ch = 0; ch < channels; ch++) {
      for (int sb = 0; sb < 32; sb++) {
        this.overlapAdd[ch][sb] = new OverlapAdd();
      }
    }

    this.reservoir = new byte[RESERVOIR_SIZE];
    this.reservoirWritePos = 0;
    this.reservoirTotalWritten = 0;
  }

  @Override
  public int read() throws IOException {
    byte[] sample = new byte[1];
    int read = read(sample, 0, 1);
    return read == -1 ? -1 : Byte.toUnsignedInt(sample[0]);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    if (closed) throw new IOException("Stream closed");
    if (len == 0) return 0;
    ensureInitialized();

    int pcmFrameSize = SAMPLES_PER_FRAME * targetFormat.getFrameSize();
    while (pcmBuffer.position() < len && pcmBuffer.remaining() >= pcmFrameSize) {
      if (!decodeNextFrame()) break;
    }

    pcmBuffer.flip();
    int bytesToRead = Math.min(len, pcmBuffer.remaining());
    if (bytesToRead > 0) pcmBuffer.get(b, off, bytesToRead);
    pcmBuffer.compact();

    if (bytesToRead == 0) {
      if (!decodeNextFrame()) return -1;
      return read(b, off, len);
    }
    return bytesToRead;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    encodedStream.close();
  }

  private void ensureInitialized() throws IOException {
    if (this.initialized) return;

    byte[] header = encodedStream.readNBytes(ID3_HEADER_LENGTH);
    try {
      int id3TagLength = Mpeg.getId3TagLength(header);
      if (id3TagLength == 0) {
        encodedStream.unread(header);
      } else {
        encodedStream.skipNBytes(id3TagLength - header.length);
        encodedPosition = id3TagLength;
      }
    } catch (UnsupportedAudioFileException exception) {
      throw new IOException("Invalid MP3 metadata", exception);
    }
    this.initialized = true;
  }

  private void writeReservoir(byte[] data, int offset, int length) {
    for (int i = 0; i < length; i++) {
      reservoir[reservoirWritePos] = data[offset + i];
      reservoirWritePos = (reservoirWritePos + 1) % RESERVOIR_SIZE;
    }
    reservoirTotalWritten += length;
  }

  private byte[] readReservoir(int bytesToRead) {
    byte[] result = new byte[bytesToRead];
    int available = Math.min(reservoirTotalWritten, RESERVOIR_SIZE);
    if (bytesToRead > available) {
      int zeros = bytesToRead - available;
      int toRead = available;
      int startPos = (reservoirWritePos - available + RESERVOIR_SIZE) % RESERVOIR_SIZE;
      for (int i = 0; i < toRead; i++) result[zeros + i] = reservoir[(startPos + i) % RESERVOIR_SIZE];
    } else {
      int startPos = (reservoirWritePos - bytesToRead + RESERVOIR_SIZE) % RESERVOIR_SIZE;
      for (int i = 0; i < bytesToRead; i++) result[i] = reservoir[(startPos + i) % RESERVOIR_SIZE];
    }
    return result;
  }

  private boolean decodeNextFrame() throws IOException {
    long frameOffset = encodedPosition;
    try {
      byte[] frameData;
      while (true) {
        frameOffset = encodedPosition;
        frameData = readFrame();
        if (frameData == null) return false;
        if (!isMetadataFrame(frameData)) break;
      }

      int header = readHeader(frameData);
      int protection = (header >> 16) & 0x1;
      int channels = detectChannels(frameData);
      int frameSize = frameData.length;
      int sideInfoSize = channels == 1 ? 17 : 32;
      int headerAndSideInfoSize = 4 + (protection == 0 ? 2 : 0) + sideInfoSize;

      int sideInfoOffset = 4 + (protection == 0 ? 2 : 0);
      int mainDataBegin = ((frameData[sideInfoOffset] & 0xFF) << 1) | ((frameData[sideInfoOffset + 1] >> 7) & 1);
      int mainDataSize = frameSize - headerAndSideInfoSize;
      if (mainDataSize < 0) throw new IOException("Invalid MPEG frame size at byte " + frameOffset);
      byte[] currentMainData = new byte[mainDataSize];
      System.arraycopy(frameData, headerAndSideInfoSize, currentMainData, 0, mainDataSize);

      byte[] frameMainData = new byte[mainDataBegin + mainDataSize];
      byte[] previous = readReservoir(mainDataBegin);
      System.arraycopy(previous, 0, frameMainData, 0, previous.length);
      System.arraycopy(currentMainData, 0, frameMainData, previous.length, currentMainData.length);
      writeReservoir(currentMainData, 0, currentMainData.length);

      MpegFrame frame;
      try {
        frame = new MpegFrame(ByteBuffer.wrap(frameData), 0, frameMainData);
      } catch (UnsupportedAudioFileException exception) {
        throw new IOException("Invalid MPEG frame at byte " + frameOffset, exception);
      }

      float[][][] samples = frame.getSamples();
      if (samples == null) throw new IOException("MPEG frame contains no decoded samples at byte " + frameOffset);

      int frameChannels = frame.getChannels();
      int outputChannels = synthesisFilters.length;

      for (int ch = 0; ch < outputChannels; ch++) synthesisFilters[ch].resetPcmBufferIndex();

      int[][] pcmTempBuffers = new int[outputChannels][SAMPLES_PER_FRAME];
      var sideInfo = frame.getSideInfo();

      // Process each output channel
      for (int ch = 0; ch < outputChannels; ch++) {
        int sourceCh = (frameChannels == 1) ? 0 : ch;

        for (int gr = 0; gr < 2; gr++) {
          float[] freqData = samples[sourceCh][gr];
          if (freqData == null) continue;

          int blockType = sideInfo.channels[sourceCh].granules[gr].block_type;
          boolean mixedBlock = sideInfo.channels[sourceCh].granules[gr].mixed_block_flag;

          float[] reordered = Reordering.reorder(freqData, blockType, mixedBlock, frame.getSampleRate());
          float[] aliasReduced = AliasReduction.process(reordered, blockType, mixedBlock);

          float[][] subbandTimeData = new float[32][18];
          for (int sb = 0; sb < 32; sb++) {
            float[] subbandFreq = new float[18];
            for (int k = 0; k < 18; k++) {
              int idx = sb * 18 + k;
              if (idx < aliasReduced.length) subbandFreq[k] = aliasReduced[idx];
            }
            boolean useLongWindow = mixedBlock && sb < 2;
            float[] imdctOut = Imdct.process(subbandFreq, blockType, useLongWindow);
            float[] overlapResult = overlapAdd[sourceCh][sb].process(imdctOut);
            System.arraycopy(overlapResult, 0, subbandTimeData[sb], 0, 18);
          }

          for (int time = 0; time < 18; time++) {
            float[] subbandSamples = new float[32];
            for (int sb = 0; sb < 32; sb++) {
              float sample = subbandTimeData[sb][time];
              subbandSamples[sb] = (sb & 1) != 0 && (time & 1) != 0 ? -sample : sample;
            }
            synthesisFilters[ch].inputSamples(subbandSamples);
            synthesisFilters[ch].calculatePcmSamples(ch, pcmTempBuffers[ch]);
          }
        }
      }

      int samplesPerChannel = synthesisFilters[0].getPcmBufferIndex();
      int bytesNeeded = samplesPerChannel * outputChannels * 2;
      if (pcmBuffer.remaining() < bytesNeeded) {
        throw new IllegalStateException("Insufficient PCM buffer capacity");
      }

      // Write samples with proper interleaving for stereo
      if (outputChannels == 2) {
        for (int i = 0; i < samplesPerChannel; i++) {
          for (int ch = 0; ch < outputChannels; ch++) {
            short pcmSample = (short) pcmTempBuffers[ch][i];
            if (targetFormat.isBigEndian()) {
              pcmBuffer.put((byte) (pcmSample >> 8));
              pcmBuffer.put((byte) pcmSample);
            } else {
              pcmBuffer.put((byte) pcmSample);
              pcmBuffer.put((byte) (pcmSample >> 8));
            }
          }
        }
      } else {
        // Mono - write directly
        for (int i = 0; i < samplesPerChannel; i++) {
          short pcmSample = (short) pcmTempBuffers[0][i];
          if (targetFormat.isBigEndian()) {
            pcmBuffer.put((byte) (pcmSample >> 8));
            pcmBuffer.put((byte) pcmSample);
          } else {
            pcmBuffer.put((byte) pcmSample);
            pcmBuffer.put((byte) (pcmSample >> 8));
          }
        }
      }

      decodedAudioFrame = true;
      return true;
    } catch (IndexOutOfBoundsException | ArithmeticException exception) {
      throw new IOException("Malformed MPEG frame at byte " + frameOffset, exception);
    }
  }

  private byte[] readFrame() throws IOException {
    if (endOfStream) return null;

    long frameOffset = encodedPosition;
    byte[] headerBytes = encodedStream.readNBytes(Integer.BYTES);
    encodedPosition += headerBytes.length;
    if (headerBytes.length == 0) {
      endOfStream = true;
      return null;
    }
    if (headerBytes.length < Integer.BYTES) {
      throw new IOException("Truncated MPEG header at byte " + frameOffset);
    }
    if (matches(headerBytes, 0, "TAG") || matches(headerBytes, 0, "ID3")) {
      endOfStream = true;
      return null;
    }
    if (!Mpeg.isStart(headerBytes[0], headerBytes[1])) {
      if (decodedAudioFrame && hasTrailingApev2Metadata(headerBytes)) {
        endOfStream = true;
        return null;
      }
      throw new IOException("Missing MPEG frame sync at byte " + frameOffset);
    }

    int header = readHeader(headerBytes);
    int bitrateIndex = (header >>> 12) & 0xf;
    int sampleRateIndex = (header >>> 10) & 0x3;
    int bitrate;
    int sampleRate;
    try {
      String version = Mpeg.getVersion((header >>> 19) & 0x3);
      String layer = Mpeg.getLayer((header >>> 17) & 0x3);
      if (!Mpeg.VERSION_1_0.equals(version) || !Mpeg.LAYER_3.equals(layer)) {
        throw new UnsupportedAudioFileException("Only MPEG-1 Layer III is supported");
      }
      bitrate = Mpeg.getBitRate(bitrateIndex);
      sampleRate = Mpeg.getSampleRate(sampleRateIndex);
    } catch (UnsupportedAudioFileException exception) {
      throw new IOException("Invalid MPEG header at byte " + frameOffset, exception);
    }

    int channels = detectChannels(headerBytes);
    if (sampleRate != (int) targetFormat.getSampleRate() || channels != targetFormat.getChannels()) {
      throw new IOException("MPEG stream format changed at byte " + frameOffset);
    }

    int frameSize = 144000 * bitrate / sampleRate + ((header >>> 9) & 1);
    byte[] frameData = new byte[frameSize];
    System.arraycopy(headerBytes, 0, frameData, 0, headerBytes.length);
    int remaining = frameSize - headerBytes.length;
    int read = encodedStream.readNBytes(frameData, headerBytes.length, remaining);
    encodedPosition += read;
    if (read != remaining) {
      throw new IOException("Truncated MPEG frame at byte " + frameOffset);
    }
    return frameData;
  }

  static boolean isMetadataFrame(byte[] frameData) {
    return hasXingHeader(frameData) || hasVbriHeader(frameData);
  }

  static boolean hasXingHeader(byte[] frameData) {
    int header = readHeader(frameData);
    int crcLength = ((header >>> 16) & 1) == 0 ? 2 : 0;
    int markerOffset = Integer.BYTES + crcLength + Mpeg.getSideInfoLength(detectChannels(frameData));
    if (markerOffset + Integer.BYTES > frameData.length) return false;

    return matches(frameData, markerOffset, "Xing") || matches(frameData, markerOffset, "Info");
  }

  private static boolean hasVbriHeader(byte[] frameData) {
    return matches(frameData, 36, "VBRI");
  }

  private static boolean matches(byte[] data, int offset, String marker) {
    if (offset < 0 || offset + marker.length() > data.length) return false;
    for (int i = 0; i < marker.length(); i++) {
      if (data[offset + i] != (byte) marker.charAt(i)) return false;
    }
    return true;
  }

  private boolean hasTrailingApev2Metadata(byte[] prefix) throws IOException {
    var tail = new TrailingMetadata();
    tail.append(prefix, 0, prefix.length);

    byte[] buffer = new byte[8192];
    int read;
    while ((read = encodedStream.readNBytes(buffer, 0, buffer.length)) > 0) {
      encodedPosition += read;
      tail.append(buffer, 0, read);
    }

    long apeEnd = tail.length();
    if (apeEnd >= ID3V1_TAG_LENGTH && tail.matches(apeEnd - ID3V1_TAG_LENGTH, "TAG")) {
      apeEnd -= ID3V1_TAG_LENGTH;
    }

    long footerOffset = apeEnd - APE_DESCRIPTOR_LENGTH;
    if (!isApeDescriptor(tail, footerOffset, false)) return false;

    long tagSize = tail.readLittleEndianInt(footerOffset + 12);
    long itemCount = tail.readLittleEndianInt(footerOffset + 16);
    long footerFlags = tail.readLittleEndianInt(footerOffset + 20);
    if (tagSize < APE_DESCRIPTOR_LENGTH || tagSize > apeEnd) return false;

    long tagStart = apeEnd - tagSize;
    boolean hasHeader = (footerFlags & 0x80000000L) != 0;
    if (!hasHeader) return tagStart == 0;
    if (tagStart != APE_DESCRIPTOR_LENGTH || !isApeDescriptor(tail, 0, true)) return false;

    return tail.readLittleEndianInt(8) == tail.readLittleEndianInt(footerOffset + 8)
      && tail.readLittleEndianInt(12) == tagSize
      && tail.readLittleEndianInt(16) == itemCount
      && tail.readLittleEndianInt(20) == (footerFlags | 0x20000000L);
  }

  private static boolean isApeDescriptor(TrailingMetadata tail, long offset, boolean header) {
    if (offset < 0 || !tail.matches(offset, "APETAGEX")) return false;
    long version = tail.readLittleEndianInt(offset + 8);
    long flags = tail.readLittleEndianInt(offset + 20);
    long expectedTypeFlag = header ? 0x20000000L : 0;
    return version == 2000
      && (flags & 0x60000000L) == expectedTypeFlag
      && (flags & 0x1fffffffL) == 0
      && tail.isZero(offset + 24, 8);
  }

  private static int readHeader(byte[] data) {
    return ((data[0] & 0xff) << 24) | ((data[1] & 0xff) << 16)
      | ((data[2] & 0xff) << 8) | (data[3] & 0xff);
  }

  private static int detectChannels(byte[] data) {
    int header = readHeader(data);
    return ((header >> 6) & 0x3) == 3 ? 1 : 2;
  }

  private static final class TrailingMetadata {
    private static final int RETAINED_TAIL_LENGTH = APE_DESCRIPTOR_LENGTH + ID3V1_TAG_LENGTH;

    private final byte[] first = new byte[APE_DESCRIPTOR_LENGTH];
    private final byte[] last = new byte[RETAINED_TAIL_LENGTH];
    private long length;
    private int firstLength;
    private int lastWritePosition;

    void append(byte[] data, int offset, int count) {
      int firstBytes = Math.min(count, first.length - firstLength);
      if (firstBytes > 0) {
        System.arraycopy(data, offset, first, firstLength, firstBytes);
        firstLength += firstBytes;
      }
      for (int i = 0; i < count; i++) {
        last[lastWritePosition] = data[offset + i];
        lastWritePosition = (lastWritePosition + 1) % last.length;
      }
      length += count;
    }

    long length() {
      return length;
    }

    boolean matches(long offset, String marker) {
      if (offset < 0 || offset + marker.length() > length) return false;
      for (int i = 0; i < marker.length(); i++) {
        if (get(offset + i) != (byte) marker.charAt(i)) return false;
      }
      return true;
    }

    long readLittleEndianInt(long offset) {
      if (offset < 0 || offset + Integer.BYTES > length) return -1;
      return Byte.toUnsignedLong(get(offset))
        | (Byte.toUnsignedLong(get(offset + 1)) << 8)
        | (Byte.toUnsignedLong(get(offset + 2)) << 16)
        | (Byte.toUnsignedLong(get(offset + 3)) << 24);
    }

    boolean isZero(long offset, int count) {
      if (offset < 0 || offset + count > length) return false;
      for (int i = 0; i < count; i++) {
        if (get(offset + i) != 0) return false;
      }
      return true;
    }

    private byte get(long offset) {
      if (offset < firstLength) return first[(int) offset];

      long retainedLength = Math.min(length, last.length);
      long retainedOffset = length - retainedLength;
      if (offset < retainedOffset || offset >= length) throw new IndexOutOfBoundsException();
      int oldest = length <= last.length ? 0 : lastWritePosition;
      return last[(oldest + (int) (offset - retainedOffset)) % last.length];
    }
  }
}
