package de.gurkenlabs.litiengine.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.io.Codec;

@XmlRootElement(name = "sound")
public class SoundResource extends NamedResource{
  @XmlElement(name = "data")
  private String data;
  
  public SoundResource() {
    // keep for xml serialization
  }
  
  public SoundResource(Sound sound) {
    this.setName(sound.getName());
    this.data = Codec.encode(sound.getRawData());
  }
  
  public SoundResource(InputStream data, String name) throws IOException, UnsupportedAudioFileException {
    this(new Sound(data, name));
  }
 
  @XmlTransient
  public String getData() {
    return this.data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
