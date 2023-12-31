package de.gurkenlabs.litiengine.sound.spi.mp3;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import java.util.ArrayList;
import java.util.Arrays;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class Mp3FormatConversionProvider extends FormatConversionProvider {

  private static final AudioFormat[] OUTPUT_FORMATS =
    {
      // mono, 16 bit signed
      new AudioFormat(PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, false),
      new AudioFormat(PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, true),
      // stereo, 16 bit signed
      new AudioFormat(PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, false),
      new AudioFormat(PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, true),
    };

  @Override
  public AudioFormat.Encoding[] getSourceEncodings() {
    var sourceEncodings = new ArrayList<AudioFormat.Encoding>();
    for (var encodings : Mpeg.ENCODINGS.values()) {
      sourceEncodings.addAll(Arrays.asList(encodings));
    }

    var result = new AudioFormat.Encoding[sourceEncodings.size()];
    return sourceEncodings.toArray(result);
  }

  @Override
  public AudioFormat.Encoding[] getTargetEncodings() {
    return new AudioFormat.Encoding[0];
  }

  @Override
  public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
    return new AudioFormat.Encoding[0];
  }

  @Override
  public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
    return new AudioFormat[0];
  }

  @Override
  public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
    return null;
  }

  @Override
  public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
    return null;
  }
}
