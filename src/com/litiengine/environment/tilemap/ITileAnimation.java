package com.litiengine.environment.tilemap;

import java.util.List;

public interface ITileAnimation {
  public List<ITileAnimationFrame> getFrames();
  public int getTotalDuration();
  public ITileAnimationFrame getCurrentFrame();
}
