package de.gurkenlabs.litiengine.sound.spi;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/// Common stream and resource handling for LITIENGINE Java Sound file readers.
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
    var inputStream = new FileInputStream(file);
    try {
      return getAudioInputStream(inputStream, file.length());
    } catch (UnsupportedAudioFileException | IOException | RuntimeException exception) {
      inputStream.close();
      throw exception;
    }
  }

  @Override
  public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
    return getAudioInputStream(stream, AudioSystem.NOT_SPECIFIED);
  }

  @Override
  public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
    try (var inputStream = url.openStream()) {
      return getAudioFileFormat(inputStream, AudioSystem.NOT_SPECIFIED);
    }
  }

  @Override
  public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
    var inputStream = url.openStream();
    try {
      return getAudioInputStream(inputStream, AudioSystem.NOT_SPECIFIED);
    } catch (UnsupportedAudioFileException | IOException | RuntimeException exception) {
      inputStream.close();
      throw exception;
    }
  }

  protected abstract AudioFileFormat getAudioFileFormat(InputStream stream, long fileLength)
    throws UnsupportedAudioFileException, IOException;

  protected AudioInputStream getAudioInputStream(InputStream stream, long fileLength) throws UnsupportedAudioFileException, IOException {

    var inputStream = stream.markSupported() ? stream : new BufferedInputStream(stream, this.markLimit);
    inputStream.mark(this.markLimit);
    AudioFileFormat audioFileFormat;
    try {
      audioFileFormat = getAudioFileFormat(inputStream, fileLength);
    } finally {
      inputStream.reset();
    }

    return new AudioInputStream(inputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
  }
}
