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
   * @throws NoClassDefFoundError if JavaFX is not installed (you can catch this if you 
   * want to handle JavaFX not being installed)
   * 
   * @throws IOException if the VideoResource's URL protocol is web based and the 
   * connection is refused
   * 
   * @throws javafx.scene.media.MediaException see {@link javafx.scene.media.Media#Media(String)}
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if JavaFX exists but otherwise fails to load (It is highly 
   * discouraged to catch this)
   * 
   * @see {@link javafx.scene.media.Media#Media(String)} for more details and thrown exceptions
   */
  public VideoManager(VideoResource video) throws NoClassDefFoundError {
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
   * @throws IOException if the VideoResource's URL protocol is web based and the 
   * connection is refused
   * 
   * @throws javafx.scene.media.MediaException see {@link javafx.scene.media.Media#Media(String)}
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if JavaFX exists but otherwise fails to load (It is highly 
   * discouraged to catch this)
   * 
   * @see {@link javafx.scene.media.Media#Media(String)} for more details and thrown exceptions
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
   * 
   * @throws javafx.scene.media.MediaException see {@link javafx.scene.media.Media#Media(String)}
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if JavaFX exists but otherwise fails to load (It is highly 
   * discouraged to catch this)
   * 
   * @see {@link javafx.scene.media.Media#Media(String)} for more details and thrown exceptions
   */
  public VideoManager(URL url) throws NoClassDefFoundError, IOException {
    this(url, false);
  }
  
  /**
   * Creates a new video manager which will load the specified video, and play it if
   * desired
   * 
   * @param url the URL of the video
   * 
   * @throws NoClassDefFoundError if JavaFX is not installed (you can catch this if you 
   * want to handle JavaFX not being installed)
   * 
   * @throws IOException if the URL protocol is web based and the connection is refused
   * 
   * @throws javafx.scene.media.MediaException see {@link javafx.scene.media.Media#Media(String)}
   * 
   * @throws SecurityException if a security manager exists and it denies access to
   * the classloader which loaded this class
   * 
   * @throws LinkageError if JavaFX exists but otherwise fails to load (It is highly 
   * discouraged to catch this)
   * 
   * @see {@link javafx.scene.media.Media#Media(String)} for more details and thrown exceptions
   */
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
  
  /**
   * @return true if the status of the video player is unknown.
   * 
   * This is generally the State of the player immediately after 
   * creation. 
   *
   * @see javafx.scene.media.MediaPlayer.Status.UNKNOWN
   */
  @Override
  public boolean isStatusUnknown() {
    return impl.isStatusUnknown();
  }

  /**
   * @return true if the video is ready to play.
   * 
   * @see javafx.scene.media.MediaPlayer.Status.READY
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
   * 
   * @see javafx.scene.media.MediaPlayer.Status.HALTED
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
   * 
   * @see javafx.scene.media.MediaPlayer.Status.STALLED
   */
  @Override
  public boolean isBuffering() {
    return impl.isBuffering();
  }

  /**
   * @return true if playback has been stopped under normal conditions
   * 
   * @see javafx.scene.media.MediaPlayer.Status.STOPPED
   */
  @Override
  public boolean isStopped() {
    return impl.isStopped();
  }

  /**
   * @return the status of the video player
   * 
   * @see javafx.scene.media.MediaPlayer.Status
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
   * @see javafx.scene.media.MediaPlayer#bufferProgressTimeProperty()
   */
  @Override
  public Duration getBufferProgressTime() {
    return impl.getBufferProgressTime();
  }

  /**
   * @return the number of completed playback cycles. Begins at 0.
   * 
   * @see {@link javafx.scene.media.MediaPlayer#currentCountProperty()}
   */
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

  /**
   * @return the total amount of time the player is allowed to play until finished.
   * 
   * @see javafx.scene.media.MediaPlayer#totalDuration
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
