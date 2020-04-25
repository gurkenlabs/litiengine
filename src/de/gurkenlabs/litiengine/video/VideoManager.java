package de.gurkenlabs.litiengine.video;

import java.awt.Container;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.resources.VideoResource;

/**
 * 
 * UNIMPLEMENTED
 * 
 * THIS CLASS IS SUBJECT TO CHANGE DRAMATICALLY
 *
 */

public final class VideoManager extends GuiComponent implements VideoPlayer {

  private static final Logger log = Logger.getLogger(VideoManager.class.getName());
  private static boolean checked = false;
  
  public static boolean allowNetworkConnections = false;
  
  protected VideoPlayer impl;
  
  {
    if(!checked) {
      try {
        
        //TODO Load natives
        
      } catch (LinkageError e) {
          log.log(Level.SEVERE, e, () -> e.getMessage());
          throw e;
      } catch (SecurityException e) {
        log.log(Level.SEVERE, e, () -> e.getMessage());
        throw e;
      }
    }
    checked = true;
    initialize();
  }
  
  /**
   * Creates a new VideoManager
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if the native binaries were unable to load
   */
  public VideoManager() {
    super(0,0);
  };
  
  /**
   * Creates a new VideoManager which load the
   * specified video without playing it.
   * 
   * @param video the video to load
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if the native binaries were unable to load
   */
  public VideoManager(VideoResource video) {
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
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if the native binaries were unable to load
   */
  public VideoManager(VideoResource video, boolean play) {
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
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if the native binaries were unable to load
   * 
   */
  public VideoManager(URL url) throws NoClassDefFoundError, IOException {
    this(url, false);
  }
  
  /**
   * Creates a new video manager which will load the specified video, and play it if
   * desired
   * 
   * @param url the URL of the video
   * @param play whether to immediately begin playing the video
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if the native binaries were unable to load
   */
  public VideoManager(URL url, boolean play) throws NoClassDefFoundError, IOException {
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
  protected void initialize() {
    
    if(impl != null) {
      throw new IllegalStateException("Video player already initialized!");
    }
    
    //impl = new VideoManagerImpl();
    
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

  @Override
  public Duration getBufferProgressTime() {
    return impl.getBufferProgressTime();
  }

  @Override
  public int getCurrentCount() {
    return impl.getCurrentCount();
  }

  /**
   * @return the speed the video is set to play at
   * 
   * Not to be confused with {@link #getRate()}
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
   * @return the current playback speed of the video,
   * regardless of settings.
   * 
   * For example if {@link #setRate(double)} is called
   * with a value of 1.0, and then the player is paused,
   * this will return 0.0.
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
    getPanel().update(g);
  }

  @Override
  public Container getPanel() {
    return impl.getPanel();
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
