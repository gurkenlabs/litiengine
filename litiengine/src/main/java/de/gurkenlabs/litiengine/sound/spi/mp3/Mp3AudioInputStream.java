package de.gurkenlabs.litiengine.sound.spi.mp3;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Mp3AudioInputStream extends AudioInputStream {

  private final ByteBuffer pcmBuffer;
  private final AudioFormat targetFormat;
  private final byte[] mp3Data;
  private int mp3Position;
  private boolean closed = false;
  private final SynthesisFilter[] synthesisFilters;
  private final OverlapAdd[][] overlapAdd;

  private static final int RESERVOIR_SIZE = 4096;
  private int framesProcessed = 0;
  private int framesSkipped = 0;
  private final byte[] reservoir;
  private int reservoirWritePos;
  private int reservoirTotalWritten;

  public Mp3AudioInputStream(AudioInputStream sourceStream, AudioFormat targetFormat) {
    super(sourceStream, targetFormat, -1);
    this.targetFormat = targetFormat;
    framesProcessed = 0;
    framesSkipped = 0;

    try {
      this.mp3Data = sourceStream.readAllBytes();
      this.mp3Position = 0;

      if (mp3Data.length >= 10 && mp3Data[0] == 'I' && mp3Data[1] == 'D' && mp3Data[2] == '3') {
        int id3Size = ((mp3Data[6] & 0x7f) << 21) | ((mp3Data[7] & 0x7f) << 14) | ((mp3Data[8] & 0x7f) << 7) | (mp3Data[9] & 0x7f);
        mp3Position = 10 + id3Size;
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read MP3 data", e);
    }

    int bufferSize = Math.max(65536, 4608 * 100);
    this.pcmBuffer = ByteBuffer.allocate(bufferSize);
    this.pcmBuffer.order(targetFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

    int channels = targetFormat.getChannels();

    this.synthesisFilters = new SynthesisFilter[channels];
    for (int ch = 0; ch < channels; ch++) {
      this.synthesisFilters[ch] = new SynthesisFilter(ch, 32767.0f);
      this.synthesisFilters[ch].initialize();  // Initialize with dummy pass to fill buffers
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

  private int detectMp3Channels() {
    for (int i = mp3Position; i < mp3Data.length - 4; i++) {
      if ((mp3Data[i] & 0xFF) == 0xFF && (mp3Data[i+1] & 0xE0) == 0xE0) {
        int header = ((mp3Data[i] & 0xFF) << 24) | ((mp3Data[i+1] & 0xFF) << 16) | ((mp3Data[i+2] & 0xFF) << 8) | (mp3Data[i+3] & 0xFF);
        int mode = (header >> 6) & 0x3;
        return (mode == 3) ? 1 : 2;
      }
    }
    return targetFormat.getChannels();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (closed) throw new IOException("Stream closed");
    if (len == 0) return 0;

    while (pcmBuffer.position() < len) {
      if (!decodeNextFrame()) break;
    }

    pcmBuffer.flip();
    int bytesToRead = Math.min(len, pcmBuffer.remaining());
    if (bytesToRead > 0) pcmBuffer.get(b, off, bytesToRead);
    pcmBuffer.compact();

    if (bytesToRead == 0) {
      if (mp3Position >= mp3Data.length || !decodeNextFrame()) return -1;
      return read(b, off, len);
    }
    return bytesToRead;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    super.close();
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

  private boolean decodeNextFrame() {
    if (mp3Position >= mp3Data.length) return false;

    try {
      int searchStart = mp3Position;
      while (searchStart < mp3Data.length - 4) {
        if ((mp3Data[searchStart] & 0xFF) == 0xFF && (mp3Data[searchStart + 1] & 0xE0) == 0xE0) {
          // Check for XING/Info header
          int xingHeader = ((mp3Data[searchStart] & 0xFF) << 24) | ((mp3Data[searchStart + 1] & 0xFF) << 16)
                         | ((mp3Data[searchStart + 2] & 0xFF) << 8) | (mp3Data[searchStart + 3] & 0xFF);
          int xingBitrateIndex = (xingHeader >> 12) & 0xF;
          int xingSampleRateIndex = (xingHeader >> 10) & 0x3;
          int xingPadding = (xingHeader >> 9) & 0x1;
          int xingVersion = (xingHeader >> 19) & 0x3;

          int[] xingBitrates = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 0};
          int xingBitrate = xingBitrates[xingBitrateIndex] * 1000;
          int[][] xingSampleRates = {{11025, 12000, 8000, 0}, {0, 0, 0, 0}, {22050, 24000, 16000, 0}, {44100, 48000, 32000, 0}};
          int xingSampleRate = xingSampleRates[xingVersion][xingSampleRateIndex];

          if (xingSampleRate > 0 && xingBitrate > 0) {
            int xingFrameSize = (144 * xingBitrate / xingSampleRate) + xingPadding;

            // Check for XING/Info marker at various offsets
            boolean foundXing = false;
            int[] xingOffsets = {36, 32, 27, 23};
            for (int offset : xingOffsets) {
              if (searchStart + offset + 4 <= mp3Data.length) {
                String marker = new String(mp3Data, searchStart + offset, 4);
                if ("Xing".equals(marker) || "Info".equals(marker)) {
                  foundXing = true;
                  break;
                }
              }
            }
            if (foundXing) {
              searchStart += xingFrameSize; // Skip entire XING frame
              continue;
            }
          }
          break;
        }
        searchStart++;
      }

      if (searchStart >= mp3Data.length - 4) return false;
      mp3Position = searchStart;

      int header = ((mp3Data[mp3Position] & 0xFF) << 24) | ((mp3Data[mp3Position + 1] & 0xFF) << 16)
                 | ((mp3Data[mp3Position + 2] & 0xFF) << 8) | (mp3Data[mp3Position + 3] & 0xFF);

      int bitrateIndex = (header >> 12) & 0xF;
      int sampleRateIndex = (header >> 10) & 0x3;
      int padding = (header >> 9) & 0x1;
      int version = (header >> 19) & 0x3;
      int protection = (header >> 16) & 0x1;
      int channels = detectChannels(mp3Data, mp3Position);

      int[] bitrates = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 0};
      int bitrate = bitrates[bitrateIndex] * 1000;

      int[][] sampleRates = {{11025, 12000, 8000, 0}, {0, 0, 0, 0}, {22050, 24000, 16000, 0}, {44100, 48000, 32000, 0}};
      int sampleRate = sampleRates[version][sampleRateIndex];

    int frameSize = (144 * bitrate / sampleRate) + padding;
    int sideInfoSize = (channels == 1) ? 17 : 32;
    int headerAndSideInfoSize = 4 + (protection == 1 ? 2 : 0) + sideInfoSize;

      int sideInfoOffset = mp3Position + 4 + (protection == 1 ? 2 : 0);
      int mainDataBegin = ((mp3Data[sideInfoOffset] & 0xFF) << 1) | ((mp3Data[sideInfoOffset + 1] >> 7) & 1);
      int mainDataSize = frameSize - headerAndSideInfoSize;


      byte[] frameMainData;
      if (mainDataBegin == 0) {
        // No bit reservoir needed
        frameMainData = new byte[mainDataSize];
        if (mainDataSize > 0 && mp3Position + headerAndSideInfoSize + mainDataSize <= mp3Data.length) {
          System.arraycopy(mp3Data, mp3Position + headerAndSideInfoSize, frameMainData, 0, mainDataSize);
        }
        writeReservoir(mp3Data, mp3Position + headerAndSideInfoSize, mainDataSize);
      } else {
        // Use bit reservoir
        // If mainDataBegin > reservoirTotalWritten, readReservoir will pad with zeros
        byte[] fromReservoir = readReservoir(mainDataBegin);
        int bytesFromFrame = mainDataSize - mainDataBegin;

        if (bytesFromFrame <= 0) {
          // All data comes from reservoir (may be padded with zeros)
          frameMainData = new byte[mainDataSize];
          int offset = mainDataBegin - mainDataSize;
          if (offset >= 0 && offset < fromReservoir.length) {
            System.arraycopy(fromReservoir, offset, frameMainData, 0, Math.min(mainDataSize, fromReservoir.length - offset));
          }
          // If offset < 0, frameMainData remains all zeros (correct behavior)
        } else {
          // Mix of reservoir and frame data
          byte[] fromFrame = new byte[bytesFromFrame];
          if (mp3Position + headerAndSideInfoSize + bytesFromFrame <= mp3Data.length) {
            System.arraycopy(mp3Data, mp3Position + headerAndSideInfoSize, fromFrame, 0, bytesFromFrame);
          }
          frameMainData = new byte[mainDataSize];
          System.arraycopy(fromReservoir, 0, frameMainData, 0, mainDataBegin);
          System.arraycopy(fromFrame, 0, frameMainData, mainDataBegin, bytesFromFrame);
        }
        if (mainDataSize > 0 && mp3Position + headerAndSideInfoSize + mainDataSize <= mp3Data.length) {
          writeReservoir(mp3Data, mp3Position + headerAndSideInfoSize, mainDataSize);
        }
      }

      boolean allZeros = true;
      for (byte b : frameMainData) {
        if (b != 0) { allZeros = false; break; }
      }

      MpegFrame frame;
      if (allZeros) {
        ByteBuffer buffer = ByteBuffer.wrap(mp3Data);
        try { frame = new MpegFrame(buffer, mp3Position); } catch (Exception e) { framesSkipped++; mp3Position += frameSize; return true; }
      } else {
        ByteBuffer headerBuffer = ByteBuffer.wrap(mp3Data);
        try { frame = new MpegFrame(headerBuffer, mp3Position, frameMainData); } catch (Exception e) { framesSkipped++; mp3Position += frameSize; return true; }
      }

      float[][][] samples = frame.getSamples();
      if (samples == null) { framesSkipped++; mp3Position += frameSize; return true; }

      framesProcessed++;

      int frameChannels = frame.getChannels();
      int outputChannels = synthesisFilters.length;

      for (int ch = 0; ch < outputChannels; ch++) synthesisFilters[ch].resetPcmBufferIndex();

      int[] pcmTempBuffer = new int[1152 * 2];
      var sideInfo = frame.getSideInfo();

      // Process each output channel
      for (int ch = 0; ch < outputChannels; ch++) {
        int sourceCh = (frameChannels == 1) ? 0 : ch;

        for (int gr = 0; gr < 2; gr++) {
          float[] freqData = samples[sourceCh][gr];
          if (freqData == null) continue;

          int blockType = sideInfo.channels[sourceCh].granules[gr].block_type;
          boolean mixedBlock = sideInfo.channels[sourceCh].granules[gr].mixed_block_flag;

          float[] reordered = Reordering.reorder(freqData, blockType, mixedBlock);
          float[] aliasReduced = AliasReduction.process(reordered, blockType);

          float[][] subbandTimeData = new float[32][18];
          for (int sb = 0; sb < 32; sb++) {
            float[] subbandFreq = new float[18];
            for (int k = 0; k < 18; k++) {
              int idx = sb * 18 + k;
              if (idx < aliasReduced.length) subbandFreq[k] = aliasReduced[idx];
            }
            float[] imdctOut = Imdct.process(subbandFreq, blockType, mixedBlock);
            float[] windowed = Imdct.applyWindow(imdctOut, blockType);
            float[] overlapResult = overlapAdd[sourceCh][sb].process(windowed);
            System.arraycopy(overlapResult, 0, subbandTimeData[sb], 0, 18);
          }

          for (int time = 0; time < 18; time++) {
            float[] subbandSamples = new float[32];
            for (int sb = 0; sb < 32; sb++) subbandSamples[sb] = subbandTimeData[sb][time];
            synthesisFilters[ch].inputSamples(subbandSamples);
            synthesisFilters[ch].calculatePcmSamples(ch, pcmTempBuffer);
          }
        }
      }

      int totalPcmSamples = 0;
      for (int ch = 0; ch < outputChannels; ch++) totalPcmSamples += synthesisFilters[ch].getPcmBufferIndex();

      int bytesNeeded = totalPcmSamples * 2;
      if (pcmBuffer.remaining() < bytesNeeded) return false;

      // Write samples with proper interleaving for stereo
      if (outputChannels == 2) {
        int samplesPerChannel = totalPcmSamples / 2;
        for (int i = 0; i < samplesPerChannel; i++) {
          for (int ch = 0; ch < outputChannels; ch++) {
            int pcmIdx = ch * samplesPerChannel + i;
            short pcmSample = (short) pcmTempBuffer[pcmIdx];
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
        for (int i = 0; i < totalPcmSamples; i++) {
          short pcmSample = (short) pcmTempBuffer[i];
          if (targetFormat.isBigEndian()) {
            pcmBuffer.put((byte) (pcmSample >> 8));
            pcmBuffer.put((byte) pcmSample);
          } else {
            pcmBuffer.put((byte) pcmSample);
            pcmBuffer.put((byte) (pcmSample >> 8));
          }
        }
      }

      mp3Position += frameSize;
      return true;
    } catch (Exception e) {
      mp3Position += 1;
      return mp3Position < mp3Data.length;
    }
  }

  private int detectChannels(byte[] data, int offset) {
    if (offset + 4 > data.length) return 2;
    int header = ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16) | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
    return ((header >> 6) & 0x3) == 3 ? 1 : 2;
  }
}
