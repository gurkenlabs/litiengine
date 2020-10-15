package de.gurkenlabs.litiengine.video;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Duration;
import java.util.logging.Logger;

import org.freedesktop.gstreamer.swing.GstVideoComponent;

import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.resources.VideoResource;

public final class GStreamerVideoManager extends GuiComponent implements VideoPlayer {

  private static final Logger log = Logger.getLogger(GStreamerVideoManager.class.getName());
  private static boolean checked = false;
  
  public static boolean allowNetworkConnections = false;
  
  private VideoPlayer impl;
  
  {
    initialize();
  }
  
  /**
   * Creates a new VideoManager
   */
  public GStreamerVideoManager() {
    super(0,0);
  };
  
  /**
   * Creates a new VideoManager which load the
   * specified video without playing it.
   * 
   * @param video the video to load
   * 
   * @throws NoClassDefFoundError if JavaFX is not installed (you can catch this if you 
   * want to handle JavaFX not being installed)
   * 
   * @throws UncheckedIOException if the VideoResource's URI protocol is web based and the 
   * connection is refused
   */
  public GStreamerVideoManager(VideoResource video) {
    super(0,0);
    setVideo(video);
  }
  
  /**
   * Creates a new VideoManager which will load the
   * specified video, and play it if specified.
   * 
   * @param video the video to load
   * @param play whether to immediately begin playing the video
   * 
   * @throws NoClassDefFoundError if JavaFX is not installed (you can catch this if you 
   * want to handle JavaFX not being installed)
   * 
   * @throws UncheckedIOException if the VideoResource's URL protocol is web based and the 
   * connection is refused
   */
  public GStreamerVideoManager(VideoResource video, boolean play) {
    super(0,0);
    if(play) {
      play(video);
    }
    else {
      setVideo(video);
    }
  }
  
  /**
   * Creates a new VideoManager which will load the
   * specified video, and play it if specified.
   * 
   * @param url the URL of the video
   * 
   * @throws NoClassDefFoundError if JavaFX is not installed (you can catch this if you 
   * want to handle JavaFX not being installed)
   * 
   * @throws IOException if the URL protocol is web based and the connection is refused
   */
  public GStreamerVideoManager(URL url) throws NoClassDefFoundError, IOException {
    this(url, false);
  }
  
  /**
   * Creates a new video manager which will load the specified video, and play it if
   * desired
   * 
   * @param url the URL of the video
   * @param play whether to immediately begin playing the video
   * 
   * @throws IOException if the URL protocol is web based and the connection is refused
   * 
   */
  public GStreamerVideoManager(URL url, boolean play) throws NoClassDefFoundError, IOException {
    super(0,0);
    if(play) {
      play(url);
    }
    else {
      setVideo(url);
    }
  }
  
  /**
   * Initializes the media player
   * 
   * @throws IllegalStateException if the media player has already been initialized
   */
  private void initialize() {
    
    if(impl != null) {
      throw new IllegalStateException("Video player already initialized!");
    }
    
    impl = new GStreamerVideoPlayer();
    
  }
  
  /**
   * @return true if the status of the video player is unknown.
   * 
   * This is generally the State of the player immediately after 
   * creation. 
   */
  @Override
  public boolean isStatusUnknown() {
    return impl.isStatusUnknown();
  }

  /**
   * @return true if the video is ready to play.
   */
  @Override
  public boolean isReady() {
    return impl.isReady();
  }

  /**
   * @return true if a critical error has occured during video playback.
   * 
   * This means that playback can never continue again with this VideoManager
   * and a new VideoManager should be created.
   */
  @Override
  public boolean isErrored() {
    return impl.isErrored();
  }

  @Override
  public boolean isPlaying() {
    return impl.isPlaying();
  }

  @Override
  public boolean isPaused() {
    return impl.isPaused();
  }

  /**
   * @return true if the video has stopped playing because the
   * buffer has slowed or stopped.
   */
  @Override
  public boolean isBuffering() {
    return impl.isBuffering();
  }

  /**
   * @return true if playback has been stopped under normal conditions
   */
  @Override
  public boolean isStopped() {
    return impl.isStopped();
  }

  /**
   * @return the status of the video player
   */
  @Override
  public Status getStatus() {
    return impl.getStatus();
  }

  /**
   * Free all resources associated with this player.
   */
  @Override
  public void dispose() {
    impl.dispose();
  }

  @Override
  public Throwable getError() {
    return impl.getError();
  }

  /**
   * @return the audio balance (the leftness or rightness of the audio).
   */
  @Override
  public double getBalance() {
    return impl.getBalance();
  }

  /**
   * The current buffer position indicating how much media can be played without stalling the player
   */
  @Override
  public Duration getBufferProgressTime() {
    return impl.getBufferProgressTime();
  }

  /**
   * @return the number of completed playback cycles. Begins at 0.
   */
  @Override
  public int getCurrentCount() {
    return impl.getCurrentCount();
  }

  /**
   * @return the current playback speed of the video,
   * regardless of settings.
   * 
   * For example if {@link #setRate(double)} is called
   * with a value of 1.0, and then the player is paused,
   * this will return 0.0.
   */
  @Override
  public double getCurrentRate() {
    return impl.getCurrentRate();
  }

  /**
   * @return the current time elapsed in the video
   */
  @Override
  public Duration getCurrentTime() {
    return impl.getCurrentTime();
  }

  /**
   * @return the speed the video is set to play at
   * 
   * Not to be confused with {@link #getCurrentRate()}
   */
  @Override
  public double getRate() {
    return impl.getRate();
  }

  @Override
  public Duration getStartTime() {
    return impl.getStartTime();
  }

  @Override
  public Duration getStopTime() {
    return impl.getStopTime();
  }

  /**
   * @return the total amount of time the player is allowed to play until finished.
   */
  @Override
  public Duration getTotalDuration() {
    return impl.getTotalDuration();
  }

  @Override
  public double getVolume() {
    return impl.getVolume();
  }

  @Override
  public void pause() {
    impl.pause();
  }

  public void setVideo(VideoResource video) {
    impl.setVideo(video);
  }
  
  public void play(VideoResource video) {
    impl.play(video);
  }
  
  @Override
  public void play() {
    impl.play();
  }

  @Override
  public void seek(Duration seekTime) {
    impl.seek(seekTime);
  }

  @Override
  public void setBalance(double value) {
    impl.setBalance(value);
  }

  @Override
  public void setRate(double value) {
    impl.setRate(value);
  }

  @Override
  public void setStartTime(Duration value) {
    impl.setStartTime(value);
  }

  @Override
  public void setStopTime(Duration value) {
    impl.setStopTime(value);
  }

  @Override
  public void setVolume(double value) {
    impl.setVolume(value);
  }

  @Override
  public void stop() {
    impl.stop();
  }
  
  @Override
  public void render(Graphics2D g) {
    getPanel().paint(g);
  }

  @Override
  public GstVideoComponent getPanel() {
    return (GstVideoComponent) impl.getPanel();
  }

  @Override
  public void setVideo(URL url) throws IOException{
    impl.setVideo(url);
  }

  @Override
  public void play(URL url) throws IOException {
    impl.play(url);
  }
  
}
