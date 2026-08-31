package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.util.io.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/// This class implements all required functionality to load sounds from the file system and provide a stream that can
/// later on be used for the sound playback.
public final class Sound {

  private final AudioFormat format;

  private final String name;

  private final byte[] streamData;

  private final byte[] data;

  /// Creates a new Sound instance by the specified file path. Loads the sound data into a byte array and also retrieves
  /// information about the format of the sound file.
  ///
  /// Note that the constructor is private. In order to load files use the static `Resources.sounds().get(String)` method.
  ///
  /// @param is
  /// The input stream to load the sound from.
  /// @param name
  /// The name of this sound file.
  /// @throws IOException
  /// If something went wrong loading the file
  /// @throws UnsupportedAudioFileException
  /// If the audio format is not supported
  public Sound(InputStream is, String name) throws IOException, UnsupportedAudioFileException {
    this.name = name;

    this.data = StreamUtilities.getBytes(is);

    try (var dataStream = new ByteArrayInputStream(this.data);
      var encodedStream = AudioSystem.getAudioInputStream(dataStream);
      var decodedStream = AudioSystem.getAudioInputStream(getOutFormat(encodedStream.getFormat()), encodedStream)) {
      this.streamData = StreamUtilities.getBytes(decodedStream);
      this.format = decodedStream.getFormat();
    }
  }

  /// Gets the audio format of this sound instance.
  ///
  /// @return The audio format of this instance.
  public AudioFormat getFormat() {
    return this.format;
  }

  /// Gets the name of this instance that is used to uniquely identify the resource of this sound.
  ///
  /// @return The name of this sound.
  public String getName() {
    return this.name;
  }

  /// Gets the raw data of this sound as byte array.
  ///
  /// This is used during resource serialization.
  ///
  /// @return The raw data of this sound as byte array.
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
    final int channels = inFormat.getChannels();
    final float rate = inFormat.getSampleRate();
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, channels, channels * 2, rate, false);
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
