package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.video.VideoManager;

public final class VideoRenderer {

  private VideoRenderer() {
    throw new UnsupportedOperationException();
  }
  
  public static void render(final Graphics2D g, final VideoManager video) {
    if(video == null || video.isStatusUnknown()) {
      return;
    }
    g.translate(video.getX(), video.getY());
    video.getPanel().paint(g);
    g.translate(-video.getX(), -video.getY());
  }
  
}