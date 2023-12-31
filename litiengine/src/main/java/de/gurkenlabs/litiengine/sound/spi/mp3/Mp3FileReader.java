package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.AudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Mp3FileReader extends AudioFileReader {
  public static final AudioFileFormat.Type MP3 = new AudioFileFormat.Type("MP3", "mp3");

  private static final int MINIMUM_BUFFER_LENGTH = 40;

  public Mp3FileReader() {
    super(128000 * 32 + 1);
  }

  @Override
  protected AudioFileFormat getAudioFileFormat(InputStream stream, long fileLength) throws UnsupportedAudioFileException, IOException {
    var byteBuffer = ByteBuffer.wrap(stream.readAllBytes());

    // The AudioSystem calls this and expects an UnsupportedAudioFileException if a FileReader cannot handle a file
    if (!canHandleAudioFormat(byteBuffer)) {
      throw new UnsupportedAudioFileException("No mpeg audio format found");
    }

    return getFormatFromMpegFrames(byteBuffer);
  }

  private AudioFileFormat getFormatFromMpegFrames(ByteBuffer byteBuffer) throws UnsupportedAudioFileException {
    var offset = Mpeg.getDataOffset(byteBuffer);

    var frames = new ArrayList<MpegFrame>();
    while (offset < byteBuffer.limit() - MINIMUM_BUFFER_LENGTH) {
      if (!Mpeg.isStart(byteBuffer.get(offset), byteBuffer.get(offset + 1))) {
        offset++;
        continue;
      }

      var frame = new MpegFrame(byteBuffer.get(offset), byteBuffer.get(offset + 1), byteBuffer.get(offset + 2), byteBuffer.get(offset + 3));
      if (offset + frame.getLengthInBytes() > byteBuffer.limit()) {
        throw new UnsupportedAudioFileException("Frame length exceeds end of file");
      }

      // ensure frame consistency
      if (!frames.isEmpty()) {
        var firstFrame = frames.getFirst();
        if (firstFrame.getSampleRate() != frame.getSampleRate()
          || !firstFrame.getLayer().equals(frame.getLayer())
          || !firstFrame.getVersion().equals(frame.getVersion())) {
          throw new UnsupportedAudioFileException("Inconsistent frame header");
        }
      }

      frames.add(frame);
      offset += frame.getLengthInBytes();
    }

    // This could be fetched by the XING header if available or the VBRI header (only used by the Fraunhofer Encoder)
    // https://www.codeproject.com/Articles/8295/MPEG-Audio-Frame-Header#XINGHeader
    var bitRate = (int) frames.stream().mapToDouble(MpegFrame::getBitRate).average().orElse(0);

    final var frame = frames.getFirst();
    var audioFormat = new AudioFormat(frame.getEncoding(), frame.getSampleRate(), bitRate, frame.getChannels(), frame.getLengthInBytes(), frame.getFrameRate(), true);

    return new AudioFileFormat(MP3, audioFormat, frames.size());
  }

  private boolean canHandleAudioFormat(ByteBuffer buffer) {
    // Check for WAV, AU, and AIFF, Ogg Vorbis, Flac, MAC, Shoutcast and OGG formats.
    if ((buffer.get(0) == 'R') && (buffer.get(1) == 'I') && (buffer.get(2) == 'F') && (buffer.get(3) == 'F') && (buffer.get(8) == 'W') && (buffer.get(9) == 'A') && (buffer.get(10) == 'V') && (buffer.get(11) == 'E')) {
      int isPCM = ((buffer.get(21) << 8) & 0x0000FF00) | ((buffer.get(20)) & 0x00000FF);
      return isPCM != 1;
    } else if ((buffer.get(0) == '.') && (buffer.get(1) == 's') && (buffer.get(2) == 'n') && (buffer.get(3) == 'd')) {
      // AU stream found
      return false;
    } else if ((buffer.get(0) == 'F') && (buffer.get(1) == 'O') && (buffer.get(2) == 'R') && (buffer.get(3) == 'M') && (buffer.get(8) == 'A') && (buffer.get(9) == 'I') && (buffer.get(10) == 'F') && (buffer.get(11) == 'F')) {
      // AIFF stream found
      return false;
    } else if (((buffer.get(0) == 'M') | (buffer.get(0) == 'm')) && ((buffer.get(1) == 'A') | (buffer.get(1) == 'a')) && ((buffer.get(2) == 'C') | (buffer.get(2) == 'c'))) {
      // APE stream found
      return false;
    } else if (((buffer.get(0) == 'F') | (buffer.get(0) == 'f')) && ((buffer.get(1) == 'L') | (buffer.get(1) == 'l')) && ((buffer.get(2) == 'A') | (buffer.get(2) == 'a')) && ((buffer.get(3) == 'C') | (buffer.get(3) == 'c'))) {
      // FLAC stream found
      return false;
    } else if (((buffer.get(0) == 'I') | (buffer.get(0) == 'i')) && ((buffer.get(1) == 'C') | (buffer.get(1) == 'c')) && ((buffer.get(2) == 'Y') | (buffer.get(2) == 'y'))) {
      // Shoutcast stream found
      return false;
    } else if (((buffer.get(0) == 'O') | (buffer.get(0) == 'o')) && ((buffer.get(1) == 'G') | (buffer.get(1) == 'g')) && ((buffer.get(2) == 'G') | (buffer.get(2) == 'g'))) {
      // Ogg stream found
      return false;
    }

    return true;
  }
}
