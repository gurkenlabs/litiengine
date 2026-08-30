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

/// Reads MPEG-1 Layer III stream metadata for the Java Sound service provider.
public final class Mp3FileReader extends AudioFileReader {
  /// The Java Sound file type for MP3 resources.
  public static final AudioFileFormat.Type MP3 = new AudioFileFormat.Type("MP3", "mp3");

  private static final int HEADER_LENGTH = 10;
  private static final int MAX_FRAME_SEARCH = 64 * 1024;

  /// Creates an MP3 file reader.
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
    int id3TagLength = Mpeg.getId3TagLength(header);
    if (id3TagLength > 0) {
      stream.skipNBytes(id3TagLength - HEADER_LENGTH);
    } else {
      frames.write(header);
    }
    frames.write(stream.readNBytes(MAX_FRAME_SEARCH + Integer.BYTES));

    var frame = readFirstFrame(frames.toByteArray());
    var format = new AudioFormat(frame.getEncoding(), frame.getSampleRate(), AudioSystem.NOT_SPECIFIED,
      frame.getChannels(), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false);
    return new AudioFileFormat(MP3, format, AudioSystem.NOT_SPECIFIED);
  }

  private static MpegFrame readFirstFrame(byte[] data) throws UnsupportedAudioFileException {
    if (data.length < Integer.BYTES || !Mpeg.isStart(data[0], data[1])) {
      throw new UnsupportedAudioFileException("No MPEG-1 Layer III frame found");
    }
    return new MpegFrame(ByteBuffer.wrap(data), 0);
  }
}
