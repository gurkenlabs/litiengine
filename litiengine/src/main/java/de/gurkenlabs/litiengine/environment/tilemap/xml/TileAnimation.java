package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.List;
import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlAccessorType(XmlAccessType.FIELD)
public class TileAnimation implements ITileAnimation {
  @XmlElement(name = "frame", type = Frame.class)
  private List<ITileAnimationFrame> frames;

  public TileAnimation() {
    this.frames = new ArrayList<>();
  }

  public TileAnimation(List<ITileAnimationFrame> frames) {
    this.setFrames(frames);
  }

  @Override
  public List<ITileAnimationFrame> getFrames() {
    return this.frames;
  }

  public void setFrames(List<ITileAnimationFrame> frames) {
    this.frames = new ArrayList<>();
    if (frames != null) {
      for (ITileAnimationFrame frame : frames) {
        if (frame != null) {
          this.frames.add(new Frame(frame));
        }
      }
    }
  }

  @Override
  public int getTotalDuration() {
    int totalDuration = 0;
    for (ITileAnimationFrame frame : this.getFrames()) {
      if (frame != null) {
        totalDuration += frame.getDuration();
      }
    }
    return totalDuration;
  }

  @Override
  public ITileAnimationFrame getCurrentFrame() {
    if (this.getFrames().isEmpty()) {
      return null;
    }
    if (this.getTotalDuration() <= 0) {
      return this.getFrames().getFirst();
    }
    return this.getFrameAt(Game.time().sinceEnvironmentLoad());
  }

  ITileAnimationFrame getFrameAt(long elapsed) {
    int duration = this.getTotalDuration();
    if (this.getFrames().isEmpty()) {
      return null;
    }
    if (duration <= 0) {
      return this.getFrames().getFirst();
    }

    long time = elapsed % duration;
    for (ITileAnimationFrame frame : this.getFrames()) {
      time -= frame.getDuration();
      if (time < 0) {
        return frame;
      }
    }
    throw new AssertionError(); // we should never reach this line
  }
}
