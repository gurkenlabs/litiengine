package de.gurkenlabs.litiengine.video;

import java.awt.Graphics2D;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.gui.GuiComponent;

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * Do not import anything from javafx into this class!
 * 
 * Not all JREs contain javafx. Some JREs may throw
 * java.lang.Error upon loading of this class if
 * a javafx import exists but the library is missing!
 *
 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

public final class VideoManager extends GuiComponent implements VideoPlayer {

  private static final Logger log = Logger.getLogger(VideoManager.class.getName());
  
  private VideoPlayer impl;
  
  {
    initialize();
  }
  
  /**
   * Creates a new VideoManager
   */
  VideoManager() {
    super(0,0);
  };
  
  /**
   * Creates a new VideoManager which load the
   * specified video without playing it.
   * 
   * @param video the video to load
   */
  VideoManager(VideoResource video) {
    super(0,0);
    setVideo(video);
  }
  
  /**
   * Creates a new VideoManager which will load the
   * specified video.
   * 
   * @param video the video to load
   * @param play whether to immediately begin playing the video
   */
  VideoManager(VideoResource video, boolean play) {
    super(0,0);
    if(play) {
      playVideo(video);
    }
    else {
      setVideo(video);
    }
  }
  
  /**
   * Initializes the media player
   * 
   * @throws IllegalStateException if the media player has already been initialized
   * 
   * @throws NoClassDefFoundError if JavaFX is not installed (you can catch this
   * if you want to handle JavaFX not being installed)
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if the linkage otherwise fails (It is highly discouraged to 
   * catch this)
   */
  public void initialize() throws NoClassDefFoundError {
    
    if(impl != null) {
      throw new IllegalStateException("Video player already initialized!");
    }
    
    try {
        ClassLoader classLoader = VideoManager.class.getClassLoader();
        Class.forName("javafx.scene.media.MediaPlayer", false, classLoader);
    } catch (ClassNotFoundException e) {
        NoClassDefFoundError err = new NoClassDefFoundError("JavaFX is not installed!");
        err.initCause(e);
        log.log(Level.SEVERE, err, () -> err.getMessage());
        throw err;
    } catch (LinkageError e) {
        log.log(Level.SEVERE, e, () -> e.getMessage());
        throw e;
    } catch (SecurityException e) {
      log.log(Level.SEVERE, e, () -> e.getMessage());
      throw e;
    }
    
    impl = new VideoManagerImpl();
    
  }
  
  @Override
  public boolean isStatusUnknown() {
    return impl.isStatusUnknown();
  }

  @Override
  public boolean isReady() {
    return impl.isReady();
  }
  

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

  @Override
  public boolean isBuffering() {
    return impl.isBuffering();
  }

  @Override
  public boolean isStopped() {
    return impl.isStopped();
  }

  @Override
  public Status getStatus() {
    return impl.getStatus();
  }

  @Override
  public void dispose() {
    impl.dispose();
  }

  @Override
  public Throwable getError() {
    return impl.getError();
  }

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

  @Override
  public double getCurrentRate() {
    return impl.getCurrentRate();
  }

  @Override
  public Duration getCurrentTime() {
    return impl.getCurrentTime();
  }

  @Override
  public Object getMedia() {
    //TODO return VideoResource
  }

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
    //TODO
  }
  
  public void play(VideoResource video) {
    //TODO
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
  public void setCycleCount(int value) {
    impl.setCycleCount(value);
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
    impl.getPanel().update(g);
  }
  
}
