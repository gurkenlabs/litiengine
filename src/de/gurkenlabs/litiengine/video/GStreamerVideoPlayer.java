package de.gurkenlabs.litiengine.video;

import static org.freedesktop.gstreamer.lowlevel.GstClockAPI.GSTCLOCK_API;
import static java.time.Duration.ZERO;

import java.awt.Container;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.JFrame;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.VideoResource;

import org.freedesktop.gstreamer.Clock;
import org.freedesktop.gstreamer.Gst;
import static org.freedesktop.gstreamer.State.*;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.swing.GstVideoComponent;

final class GStreamerVideoPlayer implements VideoPlayer, Closeable{
  
  static {
    Gst.init("VideoPlayer");
  }
  
  private volatile PlayBin media;
  private volatile GstVideoComponent component;

  @Override
  public synchronized void setVideo(VideoResource video) {
    try {
      setVideo(new URL(video.getURI()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public synchronized void play(VideoResource video) {
    setVideo(video);
    play();
  }

  @Override
  public synchronized void setVideo(URL url) throws IOException {
    if(url.getProtocol().startsWith("http")) {
      if(!GStreamerVideoManager.allowNetworkConnections) {
        throw new IOException("Network access disallowed");
      }
      if(url.getProtocol().equals("http")) {
        throw new SSLHandshakeException("Insecure protocol: http. Use https");
      }
    }
    PlayBin media;
    try {
      media = new PlayBin("VideoPlayer", url.toURI());
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    setMedia(media);
  }

  @Override
  public synchronized void play(URL url) throws IOException {
    setVideo(url);
    play();
  }
  
  public synchronized void play() {
    if(playerValid()) {
      component.setVisible(true);
      media.setState(PLAYING);
    }
    else {
      throw new IllegalStateException();
    }
  }
  
  private synchronized void setMedia(PlayBin media) {
    try {
      close();
    }
    catch(IOException e) {
      throw new UncheckedIOException(e);
    }
    JFrame frame = (JFrame) Game.window().getHostControl();
    GstVideoComponent vid = new GstVideoComponent();
    vid.setSize(300, 300);
    frame.setVisible(true);
    Container container = new Container();
    container.add(vid);
    frame.add(container);
    media.setVideoSink(vid.getElement());
    component = vid;
    this.media = media;
  }
  
  @Override
  public boolean isStatusUnknown() {
    if(playerValid()) {
      return getPlayer().getState() == NULL;
    }
    return true;
  }

  @Override
  public boolean isReady() {
    if(playerValid()) {
      return getPlayer().getState() == READY;
    }
    return false;
  }

  @Override
  public boolean isPlaying() {
    if(playerValid()) {
      return getPlayer().getState() == PLAYING;
    }
    return false;
  }
  
  @Override
  public boolean isErrored() {
    if(playerValid()) {
      return false;
    }
    return false;
  }

  @Override
  public boolean isPaused() {
    if(playerValid()) {
      return getPlayer().getState() == PAUSED;
    }
    return false;
  }

  @Override
  public boolean isBuffering() {
    if(playerValid()) {
      return false;
    }
    return false;
  }

  @Override
  public boolean isStopped() {
    if(playerValid()) {
      return getPlayer().getState() == VOID_PENDING;
    }
    return false;
  }
  
  public VideoPlayer.Status getStatus() {
    switch (getPlayer().getState()) {
    case PAUSED:
      return Status.PAUSED;
    case PLAYING:
      return Status.PLAYING;
    case READY:
      return Status.READY;
    case VOID_PENDING:
      return Status.STOPPED;
    default:
      return Status.UNKNOWN;
    }
  }

  @Override
  public void dispose() {
    getPlayer().dispose();
  }

  @Override
  public Throwable getError() {
    return null;
  }

  @Override
  public double getBalance() {
    return 0;
  }

  @Override
  public java.time.Duration getBufferProgressTime() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getCurrentCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getCurrentRate() {
    if(playerValid()) {
      if(isPaused()) {
        return 0;
      }
      return getRate();
    }
    return 0;
  }

  @Override
  public java.time.Duration getCurrentTime() {
    if(playerValid()) {
      Clock clock = getPlayer().getClock();
      return convertDuration(clock.getTime());
    }
    return ZERO;
  }

  @Override
  public double getRate() {
    if(playerValid()) {
      long[] numerator = new long[]{0};
      long[] denominator = new long[] {0};
      GSTCLOCK_API.gst_clock_get_calibration(media.getClock(), null, null, numerator, denominator); //must use GSTCLOCK_API due to https://github.com/gstreamer-java/gst1-java-core/issues/212
      return (double)((double)numerator[0]/(double)denominator[0]);
    }
    return 0;
  }

  @Override
  public java.time.Duration getStartTime() {
    if(playerValid()) {
      return convertDuration(getPlayer().getStartTime());
    }
    return ZERO;
  }

  @Override
  public java.time.Duration getStopTime() {
    if(playerValid()) {
      return convertDuration(getPlayer().getBaseTime()); //TODO: update when setStopTime() is implemented
    }
    return ZERO;
  }

  @Override
  public java.time.Duration getTotalDuration() {
    if(playerValid()) {
      return convertDuration(getPlayer().getBaseTime());
    }
    return ZERO;
  }

  @Override
  public double getVolume() {
    if(playerValid()) {
      return getPlayer().getVolume();
    }
    return 0;
  }

  @Override
  public void pause() {
    if(playerValid()) {
      getPlayer().pause();
    }
  }

  @Override
  public void seek(java.time.Duration seekTime) {
    if(playerValid()) {
      getPlayer().seek(convertDuration(seekTime));
    }
  }

  @Override
  public void setBalance(double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRate(double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStartTime(java.time.Duration value) {
    if(playerValid()) {
      getPlayer().setStartTime(convertDuration(value));
    }
  }

  @Override
  public void setStopTime(java.time.Duration value) {
    throw new UnsupportedOperationException(); //TODO: Figure out how to implement
  }

  @Override
  public void setVolume(double value) {
    if(playerValid()) {
      getPlayer().setVolume(value);
    }
  }

  @Override
  public void stop() {
    if(playerValid()) {
      getPlayer().stop();
    }
  }
  
  @Override
  public GstVideoComponent getPanel() {
    return component;
  }
  
  private Duration convertDuration(long duration) {
    return Duration.ofSeconds(duration);
  }
  
  private long convertDuration(Duration duration) {
    return duration.toNanos();
  }
  
  private PlayBin getPlayer() {
    if(media != null) {
      return media;
    }
    return null;
  }
  
  private boolean playerValid() {
    return getPlayer() != null;
  }

  @Override
  public void close() throws IOException {
    if(media != null) {
      Game.window().getHostControl().remove(component);
      media.close();
    }
  }
  
}
