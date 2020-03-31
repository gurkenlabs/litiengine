package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.video.VideoManager;

public final class VideoRenderer {

  private VideoRenderer() {
    throw new UnsupportedOperationException();
  }
  
  public static void render(final Graphics2D g, final VideoManager video, final double x, final double y) {
    if(video == null) {
      return;
    }
    //TODO implement me!
  }
  
}
