package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.util.io.StreamUtilities;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class implements all required functionality to load sounds from the file system and provide
 * a stream that can later on be used for the sound playback.
 */
public final class Sound {

  private AudioFormat format;

  private final String name;

  private AudioInputStream stream;

  private byte[] streamData;

  private byte[] data;

  /**
   * Creates a new Sound instance by the specified file path. Loads the sound data into a byte array
   * and also retrieves information about the format of the sound file.
   *
   * <p>Note that the constructor is private. In order to load files use the static {@code
   * Resources.sounds().get(String)} method.
   *
   * @param is The input stream to load the sound from.
   * @param name The name of this sound file.
   * @throws IOException If something went wrong loading the file
   * @throws UnsupportedAudioFileException If the audio format is not supported
   */
  public Sound(InputStream is, String name) throws IOException, UnsupportedAudioFileException {
    this.name = name;

    this.data = StreamUtilities.getBytes(is);

    AudioInputStream in = AudioSystem.getAudioInputStream(is);
    if (in != null) {
      final AudioFormat baseFormat = in.getFormat();
      final AudioFormat decodedFormat = getOutFormat(baseFormat);
      // Get AudioInputStream that will be decoded by underlying VorbisSPI
      in = AudioSystem.getAudioInputStream(decodedFormat, in);
      this.stream = in;
      this.streamData = StreamUtilities.getBytes(this.stream);
      this.format = this.stream.getFormat();
    }
  }

  /**
   * Gets the audio format of this sound instance.
   *
   * @return The audio format of this instance.
   */
  public AudioFormat getFormat() {
    return this.format;
  }

  /**
   * Gets the name of this instance that is used to uniquely identify the resource of this sound.
   *
   * @return The name of this sound.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the raw data of this sound as byte array.
   *
   * <p>This is used during resource serialization.
   *
   * @return The raw data of this sound as byte array.
   */
  public byte[] getRawData() {
    return this.data;
  }

  byte[] getStreamData() {
    if (this.streamData == null) {
      return new byte[0];
    }

    return this.streamData.clone();
  }

  private static AudioFormat getOutFormat(final AudioFormat inFormat) {
    final int ch = inFormat.getChannels();
    final float rate = inFormat.getSampleRate();
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
