package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlAccessorType(XmlAccessType.FIELD)
public class TileAnimation implements ITileAnimation {
  @XmlElement(name = "frame", type = Frame.class)
  private List<ITileAnimationFrame> frames;

  private transient int totalDuration;

  @Override
  public List<ITileAnimationFrame> getFrames() {
    return this.frames;
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

  @Override
  public ITileAnimationFrame getCurrentFrame() {
    long time = Game.time().sinceEnvironmentLoad() % this.getTotalDuration();
    for (ITileAnimationFrame frame : this.getFrames()) {
      time -= frame.getDuration();
      if (time <= 0) {
        return frame;
      }
    }
    throw new AssertionError(); // we should never reach this line
  }
}
