package com.litiengine.graphics.animation;

import java.util.EventListener;

public interface KeyFrameListener extends EventListener {
  public void currentFrameChanged(KeyFrame previousFrame, KeyFrame currentFrame);
}
