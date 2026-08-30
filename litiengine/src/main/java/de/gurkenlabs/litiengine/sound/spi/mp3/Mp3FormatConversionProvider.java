package de.gurkenlabs.litiengine.sound.spi.mp3;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

/** Converts LITIENGINE MPEG-1 Layer III streams to signed 16-bit PCM. */
public final class Mp3FormatConversionProvider extends FormatConversionProvider {

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
      int channels = sourceFormat.getChannels();
      float sampleRate = sourceFormat.getSampleRate();
      return new AudioFormat[]{
        new AudioFormat(PCM_SIGNED, sampleRate, 16, channels, channels * 2, sampleRate, false),
        new AudioFormat(PCM_SIGNED, sampleRate, 16, channels, channels * 2, sampleRate, true)
      };
    }
    return new AudioFormat[0];
  }

  @Override
  public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
    if (!targetEncoding.equals(PCM_SIGNED) || !isMpegFormat(sourceStream.getFormat())) {
      throw new IllegalArgumentException("Unsupported MP3 conversion");
    }

    var sourceFormat = sourceStream.getFormat();
    var targetFormat = new AudioFormat(PCM_SIGNED, sourceFormat.getSampleRate(), 16,
      sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);
    return decodedStream(sourceStream, targetFormat);
  }

  @Override
  public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
    var sourceFormat = sourceStream.getFormat();
    if (!isSupportedTarget(targetFormat, sourceFormat)) {
      throw new IllegalArgumentException("Unsupported MP3 conversion");
    }
    return decodedStream(sourceStream, targetFormat);
  }

  private static AudioInputStream decodedStream(AudioInputStream sourceStream, AudioFormat targetFormat) {
    var decoder = new Mp3DecoderInputStream(sourceStream, targetFormat);
    return new AudioInputStream(decoder, targetFormat, AudioSystem.NOT_SPECIFIED);
  }

  private boolean isSupportedTarget(AudioFormat targetFormat, AudioFormat sourceFormat) {
    return isMpegFormat(sourceFormat)
      && targetFormat.getEncoding().equals(PCM_SIGNED)
      && targetFormat.getSampleSizeInBits() == 16
      && targetFormat.getChannels() == sourceFormat.getChannels()
      && targetFormat.getFrameSize() == sourceFormat.getChannels() * 2
      && targetFormat.getSampleRate() == sourceFormat.getSampleRate();
  }

  private boolean isMpegFormat(AudioFormat format) {
    return format.getEncoding().equals(Mpeg.getEncoding(Mpeg.VERSION_1_0, Mpeg.LAYER_3));
  }
}
