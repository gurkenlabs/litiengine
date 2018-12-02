package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlRootElement(name = "animation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Animation implements ITileAnimation, Serializable {
  private static final long serialVersionUID = -6359129685451548791L;

  @XmlElement(name = "frame")
  private List<Frame> frames;

  private transient List<ITileAnimationFrame> tileAnimationFrames;

  private transient int totalDuration;

  @Override
  public List<ITileAnimationFrame> getFrames() {
    if (this.tileAnimationFrames != null) {
      return this.tileAnimationFrames;
    }

    if (this.frames == null) {
      return new ArrayList<>();
    }

    this.tileAnimationFrames = new ArrayList<>(this.frames);
    return this.tileAnimationFrames;
  }

  @Override
  public int getTotalDuration() {
    if (this.totalDuration > 0) {
      return this.totalDuration;
    }

    if (this.getFrames().isEmpty()) {
      return 0;
    }

    for (ITileAnimationFrame frame : this.getFrames()) {
      if (frame != null) {
        this.totalDuration += frame.getDuration();
      }
    }

    return this.totalDuration;
  }
}
