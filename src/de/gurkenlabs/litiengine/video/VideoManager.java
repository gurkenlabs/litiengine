package de.gurkenlabs.litiengine.video;

import java.awt.Container;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.resources.VideoResource;

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * Do not import anything from javafx into this class!
 * 
 * Not all JREs contain javafx. Java will throw a
 * java.lang.Error if we accidentally try to load a 
 * javafx class if the library doesn't exist!
 *
 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

public final class VideoManager extends GuiComponent implements VideoPlayer {

  private static final Logger log = Logger.getLogger(VideoManager.class.getName());
  private static boolean checked = false;
  
  public static boolean allowNetworkConnections = false;
  
  private VideoPlayer impl;
  
  {
    if(!checked) {
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
    }
    checked = true;
    initialize();
  }
  
  /**
   * Creates a new VideoManager
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
  public VideoManager() throws NoClassDefFoundError {
    super(0,0);
  };
  
  /**
   * Creates a new VideoManager which load the
   * specified video without playing it.
   * 
   * @param video the video to load
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
  public VideoManager(VideoResource video) throws NoClassDefFoundError {
    super(0,0);
    setVideo(video);
  }
  
  /**
   * Creates a new VideoManager which will load the
   * specified video.
   * 
   * @param video the video to load
   * @param play whether to immediately begin playing the video
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
  public VideoManager(VideoResource video, boolean play) throws NoClassDefFoundError {
    super(0,0);
    if(play) {
      play(video);
    }
    else {
      setVideo(video);
    }
  }
  
  public VideoManager(URL url) throws NoClassDefFoundError, IOException {
    this(url, false);
  }
  
  public VideoManager(URL url, boolean play) throws NoClassDefFoundError, IOException {
    super(0,0);
    if(url.getProtocol().startsWith("http")) {
      if(!allowNetworkConnections) {
        throw new IOException("Network access disallowed");
      }
      if(url.getProtocol().equals("http")) {
        throw new SSLHandshakeException("Insecure protocol: http. Use https");
      }
    }
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
    getPanel().update(g);
  }

  @Override
  public Container getPanel() {
    return impl.getPanel();
  }

  @Override
  public void setVideo(URL url) {
    impl.setVideo(url);
  }

  @Override
  public void play(URL url) {
    impl.play(url);
  }
  
}
