package de.gurkenlabs.litiengine.sound.spi;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.URL;

public abstract class AudioFileReader extends javax.sound.sampled.spi.AudioFileReader {
  private final int markLimit;

  protected AudioFileReader(int markLimit) {
    this.markLimit = markLimit;
  }

  @Override
  public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
    try (var inputStream = new FileInputStream(file)) {
      return getAudioFileFormat(inputStream, file.length());
    }
  }

  @Override
  public AudioFileFormat getAudioFileFormat(final InputStream stream) throws UnsupportedAudioFileException, IOException {
    var inputStream = stream.markSupported() ? stream : new BufferedInputStream(stream, this.markLimit);
    try {
      inputStream.mark(this.markLimit);
      return getAudioFileFormat(inputStream, AudioSystem.NOT_SPECIFIED);
    } finally {
      inputStream.reset();
    }
  }

  @Override
  public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
    try (var inputStream = new FileInputStream(file)) {
      return getAudioInputStream(inputStream, file.length());
    }
  }

  @Override
  public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
    return getAudioInputStream(stream, AudioSystem.NOT_SPECIFIED);
  }

  @Override
  @Deprecated
  public AudioFileFormat getAudioFileFormat(URL url) {
    throw new UnsupportedOperationException("URL is deprecated");
  }

  @Override
  @Deprecated
  public AudioInputStream getAudioInputStream(URL url) {
    throw new UnsupportedOperationException("URL is deprecated");
  }

  protected abstract AudioFileFormat getAudioFileFormat(InputStream stream, long fileLength)
    throws UnsupportedAudioFileException, IOException;

  protected AudioInputStream getAudioInputStream(InputStream stream, long fileLength)  throws UnsupportedAudioFileException, IOException {

    var inputStream = stream.markSupported() ? stream : new BufferedInputStream(stream, this.markLimit);
    inputStream.mark(this.markLimit);
    var audioFileFormat = getAudioFileFormat(inputStream, fileLength);
    inputStream.reset();

    return new AudioInputStream(inputStream,  audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
  }
}
