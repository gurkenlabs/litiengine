package de.gurkenlabs.litiengine.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.sound.SoundFormat;
import de.gurkenlabs.litiengine.util.io.Codec;

@XmlRootElement(name = "sound")
public class SoundResource extends NamedResource {
  @XmlElement(name = "data")
  private String data;

  @XmlElement(name = "format")
  private SoundFormat format = SoundFormat.UNDEFINED;

  public SoundResource() {
    // keep for xml serialization
  }

  public SoundResource(Sound sound, SoundFormat format) {
    this.setName(sound.getName());
    this.data = Codec.encode(sound.getRawData());
    this.format = format;
  }

  public SoundResource(InputStream data, String name, SoundFormat format) throws IOException, UnsupportedAudioFileException {
    this(new Sound(data, name), format);
  }

  @XmlTransient
  public String getData() {
    return this.data;
  }

  @XmlTransient
  public SoundFormat getFormat() {
    return this.format;
  }

  public void setData(String data) {
    this.data = data;
  }

  public void setFormat(SoundFormat format) {
    this.format = format;
  }
}
