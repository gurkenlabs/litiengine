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
    return new AudioFormat.Encoding[]{Mpeg.getEncoding(Mpeg.VERSION_1_0, Mpeg.LAYER_3)};
  }

  @Override
  public AudioFormat.Encoding[] getTargetEncodings() {
    return new AudioFormat.Encoding[]{PCM_SIGNED};
  }

  @Override
  public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
    if (isMpegFormat(sourceFormat)) {
      return new AudioFormat.Encoding[]{PCM_SIGNED};
    }
    return new AudioFormat.Encoding[0];
  }

  @Override
  public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
    if (targetEncoding.equals(PCM_SIGNED) && isMpegFormat(sourceFormat)) {
      return OUTPUT_FORMATS;
    }
    return new AudioFormat[0];
  }

  @Override
  public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
    if (targetEncoding.equals(PCM_SIGNED)) {
      return new Mp3AudioInputStream(sourceStream, OUTPUT_FORMATS[0]); // Default to first format
    }
    return null;
  }

  @Override
  public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
    if (targetFormat.getEncoding().equals(PCM_SIGNED) && isMpegFormat(sourceStream.getFormat())) {
      return new Mp3AudioInputStream(sourceStream, targetFormat);
    }
    return null;
  }
  
  private boolean isMpegFormat(AudioFormat format) {
    return format.getEncoding().equals(Mpeg.getEncoding(Mpeg.VERSION_1_0, Mpeg.LAYER_3));
  }
}
