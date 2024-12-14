package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.io.Codec;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Represents a sound resource that extends the NamedResource class.
 * <p>
 * This class is used to manage sound resources, including their data and format.
 * </p>
 */
@XmlRootElement(name = "sound")
public class SoundResource extends NamedResource {
  @XmlElement(name = "data")
  private String data;

  @XmlElement(name = "format")
  private SoundFormat format = SoundFormat.UNSUPPORTED;

  /**
   * Default constructor for SoundResource.
   * <p>
   * This constructor doesn't do anything and is intended for XML serialization purposes.
   * </p>
   */
  public SoundResource() {
    // keep for xml serialization
  }

  /**
   * Constructs a new SoundResource with the specified sound and format.
   * <p>
   * This constructor initializes the SoundResource with the provided Sound object and SoundFormat. It sets the name of the resource to the name of
   * the sound and encodes the raw data of the sound.
   * </p>
   *
   * @param sound  The Sound object to be used for this resource.
   * @param format The format of the sound.
   */
  public SoundResource(Sound sound, SoundFormat format) {
    this.setName(sound.getName());
    this.data = Codec.encode(sound.getRawData());
    this.format = format;
  }

  /**
   * Constructs a new SoundResource with the specified input stream, name, and format.
   * <p>
   * This constructor initializes the SoundResource by creating a new Sound object from the provided input stream and name, and sets the format of the
   * sound.
   * </p>
   *
   * @param data   The input stream containing the sound data.
   * @param name   The name of the sound.
   * @param format The format of the sound.
   * @throws IOException                   If an I/O error occurs while reading the sound data.
   * @throws UnsupportedAudioFileException If the audio file format is not supported.
   */
  public SoundResource(InputStream data, String name, SoundFormat format) throws IOException, UnsupportedAudioFileException {
    this(new Sound(data, name), format);
  }

  /**
   * Gets the encoded sound data.
   * <p>
   * This method returns the encoded sound data as a string.
   * </p>
   *
   * @return The encoded sound data.
   */
  @XmlTransient
  public String getData() {
    return this.data;
  }

  /**
   * Gets the format of the sound.
   * <p>
   * This method returns the format of the sound.
   * </p>
   *
   * @return The format of the sound.
   */
  @XmlTransient
  public SoundFormat getFormat() {
    return this.format;
  }

  /**
   * Sets the encoded sound data.
   * <p>
   * This method sets the encoded sound data.
   * </p>
   *
   * @param data The encoded sound data to set.
   */
  public void setData(String data) {
    this.data = data;
  }

  /**
   * Sets the format of the sound.
   * <p>
   * This method sets the format of the sound.
   * </p>
   *
   * @param format The format of the sound to set.
   */
  public void setFormat(SoundFormat format) {
    this.format = format;
  }
}
