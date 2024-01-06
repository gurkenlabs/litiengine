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
  private static final int FILE_HEADER_LENGTH = 12;

  public Mp3FileReader() {
    super(128000 * 32 + 1);
  }

  @Override
  protected AudioFileFormat getAudioFileFormat(InputStream stream, long fileLength) throws UnsupportedAudioFileException, IOException {
    var byteBuffer = ByteBuffer.wrap(stream.readAllBytes());
    if (byteBuffer.limit() < FILE_HEADER_LENGTH) {
      throw new UnsupportedAudioFileException("Invalid audio stream");
    }

    var fileHeader = new byte[FILE_HEADER_LENGTH];
    byteBuffer.get(fileHeader);
    byteBuffer.clear();

    // The AudioSystem calls this and expects an UnsupportedAudioFileException if a FileReader cannot handle a file
    if (!canHandleAudioFormat(fileHeader)) {
      throw new UnsupportedAudioFileException("No mpeg audio format found");
    }

    var offset = Mpeg.getDataOffset(byteBuffer);
    return getFormatFromMpegFrames(byteBuffer, offset);
  }

  private AudioFileFormat getFormatFromMpegFrames(ByteBuffer byteBuffer, int offset) throws UnsupportedAudioFileException {
    var frames = new ArrayList<MpegFrame>();
    while (offset < byteBuffer.limit() - MINIMUM_BUFFER_LENGTH) {
      if (!Mpeg.isStart(byteBuffer.get(offset), byteBuffer.get(offset + 1))) {
        offset++;
        continue;
      }

      var frame = new MpegFrame(byteBuffer, offset);
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

  private boolean canHandleAudioFormat(byte[] fileHeader) {
    var audioHeader = new String(fileHeader).toUpperCase();
    return audioHeader.startsWith(Mpeg.ID3V2_TAG);
  }
}
