package de.gurkenlabs.litiengine.sound.spi;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * The Mpeg Audio Layer III implementation.
 */
public class MP3FileReader extends AudioFileReader {
  public static final AudioFileFormat.Type MPEG = new AudioFileFormat.Type("MPEG", "mpeg");
  public static final AudioFileFormat.Type MP3 = new AudioFileFormat.Type("MP3", "mp3");
  public static final AudioFormat.Encoding	MPEG1L1 = new AudioFormat.Encoding("MPEG1L1");
  public static final AudioFormat.Encoding	MPEG1L2 = new AudioFormat.Encoding("MPEG1L2");
  public static final AudioFormat.Encoding	MPEG1L3 = new AudioFormat.Encoding("MPEG1L3");
  public static final AudioFormat.Encoding	MPEG2L1 = new AudioFormat.Encoding("MPEG2L1");
  public static final AudioFormat.Encoding	MPEG2L2 = new AudioFormat.Encoding("MPEG2L2");
  public static final AudioFormat.Encoding	MPEG2L3 = new AudioFormat.Encoding("MPEG2L3");
  public static final AudioFormat.Encoding	MPEG2DOT5L1 = new AudioFormat.Encoding("MPEG2DOT5L1");
  public static final AudioFormat.Encoding	MPEG2DOT5L2 = new AudioFormat.Encoding("MPEG2DOT5L2");
  public static final AudioFormat.Encoding	MPEG2DOT5L3 = new AudioFormat.Encoding("MPEG2DOT5L3");

  private static int INITAL_READ_LENGTH = 128000 * 32;
  private static int MARK_LIMIT = INITAL_READ_LENGTH + 1;

  private final AudioFormat.Encoding[][] encodings = {
    { MPEG2L1, MPEG2L2, MPEG2L3 },
    { MPEG1L1, MPEG1L2, MPEG1L3 },
    { MPEG2DOT5L1, MPEG2DOT5L2, MPEG2DOT5L3 }, };

  public MP3FileReader() {
    super(MARK_LIMIT);
  }

  @Override
  protected AudioFileFormat getAudioFileFormat(InputStream stream, long fileLength) throws UnsupportedAudioFileException, IOException {
    int mLength = (int) fileLength;
    int size = stream.available();

    // TODO: USE https://github.com/mpatric/mp3agic to get the format?
    // TODO: CURRENTLY USED, see for reference => https://github.com/bowbahdoe/java-audio-stack
    PushbackInputStream pis = new PushbackInputStream(stream, MARK_LIMIT);
    byte head[] = new byte[22];
    pis.read(head);

    if (isInvalidHead(head)) {
      throw new UnsupportedAudioFileException();
    } else {
      pis.unread(head);
    }

    // MPEG header info.
    int nVersion = AudioSystem.NOT_SPECIFIED;
    int nLayer = AudioSystem.NOT_SPECIFIED;
    int nSFIndex = AudioSystem.NOT_SPECIFIED;
    int nMode = AudioSystem.NOT_SPECIFIED;
    int FrameSize = AudioSystem.NOT_SPECIFIED;
    int nFrameSize = AudioSystem.NOT_SPECIFIED;
    int nFrequency = AudioSystem.NOT_SPECIFIED;
    int nTotalFrames = AudioSystem.NOT_SPECIFIED;
    float FrameRate = AudioSystem.NOT_SPECIFIED;
    int BitRate = AudioSystem.NOT_SPECIFIED;
    int nChannels = AudioSystem.NOT_SPECIFIED;
    int nHeader = AudioSystem.NOT_SPECIFIED;
    int nTotalMS = AudioSystem.NOT_SPECIFIED;
    boolean nVBR = false;
    AudioFormat.Encoding encoding = null;
    try {

    } catch (Exception e) {
      throw new UnsupportedAudioFileException("not a MPEG stream:" + e.getMessage());
    }

    return null;
  }

  // TODO: not sure if this is really needed
  private boolean isInvalidHead(byte[] head) {
    // TODO: use var header = head.toString() to make these checks better readable with startsWith and subString

    // Check for WAV, AU, and AIFF, Ogg Vorbis, Flac, MAC file formats.
    // check for Shoutcast (unsupported) and OGG (unsupported) streams.
    if ((head[0] == 'R') && (head[1] == 'I') && (head[2] == 'F') && (head[3] == 'F') && (head[8] == 'W') && (head[9] == 'A') && (head[10] == 'V') && (head[11] == 'E')) {
      int isPCM = ((head[21] << 8) & 0x0000FF00) | ((head[20]) & 0x00000FF);
      return isPCM != 1;
    } else if ((head[0] == '.') && (head[1] == 's') && (head[2] == 'n') && (head[3] == 'd')) {
      // AU stream found
      return false;
    } else if ((head[0] == 'F') && (head[1] == 'O') && (head[2] == 'R') && (head[3] == 'M') && (head[8] == 'A') && (head[9] == 'I') && (head[10] == 'F') && (head[11] == 'F')) {
      // AIFF stream found
      return false;
    } else if (((head[0] == 'M') | (head[0] == 'm')) && ((head[1] == 'A') | (head[1] == 'a')) && ((head[2] == 'C') | (head[2] == 'c'))) {
      // APE stream found
      return false;
    } else if (((head[0] == 'F') | (head[0] == 'f')) && ((head[1] == 'L') | (head[1] == 'l')) && ((head[2] == 'A') | (head[2] == 'a')) && ((head[3] == 'C') | (head[3] == 'c'))) {
      // FLAC stream found
      return false;
    } else if (((head[0] == 'I') | (head[0] == 'i')) && ((head[1] == 'C') | (head[1] == 'c')) && ((head[2] == 'Y') | (head[2] == 'y'))) {
      // Shoutcast stream found
      return false;
    } else if (((head[0] == 'O') | (head[0] == 'o')) && ((head[1] == 'G') | (head[1] == 'g')) && ((head[2] == 'G') | (head[2] == 'g'))) {
      // Ogg stream found
      return false;
    }

    return true;
  }
}
