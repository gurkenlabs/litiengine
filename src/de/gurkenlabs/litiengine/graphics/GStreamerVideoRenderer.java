package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.video.GStreamerVideoManager;

public final class GStreamerVideoRenderer {

  private GStreamerVideoRenderer() {
    throw new UnsupportedOperationException();
  }
  
  public static void render(final Graphics2D g, final GStreamerVideoManager video) {
    if(video == null || video.isStatusUnknown()) {
      return;
    }
    g.translate(video.getX(), video.getY());
    video.getPanel().paint(g);
    g.translate(-video.getX(), -video.getY());
  }
  
}
