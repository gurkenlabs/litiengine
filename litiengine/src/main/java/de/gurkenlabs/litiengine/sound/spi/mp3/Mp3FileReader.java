package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.AudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/** Reads MPEG-1 Layer III stream metadata for the Java Sound service provider. */
public final class Mp3FileReader extends AudioFileReader {
  /** The Java Sound file type for MP3 resources. */
  public static final AudioFileFormat.Type MP3 = new AudioFileFormat.Type("MP3", "mp3");

  private static final int HEADER_LENGTH = 10;
  private static final int MAX_FRAME_SEARCH = 64 * 1024;

  /** Creates an MP3 file reader. */
  public Mp3FileReader() {
    super(16 * 1024 * 1024);
  }

  @Override
  protected AudioFileFormat getAudioFileFormat(InputStream stream, long fileLength)
    throws UnsupportedAudioFileException, IOException {
    byte[] header = stream.readNBytes(HEADER_LENGTH);
    if (header.length < Integer.BYTES) {
      throw new UnsupportedAudioFileException("Invalid MP3 stream");
    }

    var frames = new ByteArrayOutputStream();
    if (hasId3Tag(header)) {
      stream.skipNBytes(id3DataSize(header));
    } else {
      frames.write(header);
    }
    frames.write(stream.readNBytes(MAX_FRAME_SEARCH + Integer.BYTES));

    var frame = readFirstFrame(frames.toByteArray());
    var format = new AudioFormat(frame.getEncoding(), frame.getSampleRate(), AudioSystem.NOT_SPECIFIED,
      frame.getChannels(), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false);
    return new AudioFileFormat(MP3, format, AudioSystem.NOT_SPECIFIED);
  }

  private static boolean hasId3Tag(byte[] header) {
    return Mpeg.ID3V2_TAG.equals(new String(header, 0, 3, StandardCharsets.ISO_8859_1));
  }

  private static int id3DataSize(byte[] header) throws UnsupportedAudioFileException {
    for (int i = 6; i < HEADER_LENGTH; i++) {
      if ((header[i] & 0x80) != 0) {
        throw new UnsupportedAudioFileException("Invalid ID3 tag size");
      }
    }
    return ((header[6] & 0x7f) << 21) | ((header[7] & 0x7f) << 14)
      | ((header[8] & 0x7f) << 7) | (header[9] & 0x7f);
  }

  private static MpegFrame readFirstFrame(byte[] data) throws UnsupportedAudioFileException {
    if (data.length < Integer.BYTES || !Mpeg.isStart(data[0], data[1])) {
      throw new UnsupportedAudioFileException("No MPEG-1 Layer III frame found");
    }
    return new MpegFrame(ByteBuffer.wrap(data), 0);
  }
}
