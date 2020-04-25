package de.gurkenlabs.litiengine.video;

import java.awt.Container;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
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

public abstract class VideoManager extends GuiComponent implements VideoPlayer {

  protected static final Logger log = Logger.getLogger(VideoManager.class.getName());
  protected static ArrayList<String> loadedNatives = new ArrayList<String>();
  
  private VideoPlayer impl;
  
  {
    if(!nativesLoaded()) {
      try {
        loadNatives();
      } catch (LinkageError e) {
          log.log(Level.SEVERE, e, () -> e.getMessage());
          throw e;
      } catch (SecurityException e) {
        log.log(Level.SEVERE, e, () -> e.getMessage());
        throw e;
      }
    }

    initialize();
  }
  
  /**
   * Creates a new VideoManager which can play the
   * specified video.
   * 
   * Subclasses MUST overwrite this constructor
   * 
   * @param video the video to load
   * 
   * @throws LinkageError if the native binaries were unable to load
   */
  protected VideoManager(VideoResource video) {
    super(0,0);
    setVideo(video);
  }
  
  /**
   * Initializes the media player
   * 
   * should set {@link #impl} to an instance of VideoPlayer
   * 
   * @throws IllegalStateException if the media player has already been initialized
   */
  protected abstract void initialize();
  
  /**
   * Load the native library required to play videos with this video manager
   * 
   * Should add a string representation of the library to {@link #loadedNatives}
   * if it successfully loads
   * 
   * @throws LinkageError if the library is unable to load
   * @throws IllegalStateException if the library is already loaded
   */
  protected abstract void loadNatives();
  
  /**
   * @return true if the native library required to play the video has been loaded
   */
  protected abstract boolean nativesLoaded();
  
  public static boolean nativeLoaded(String libName) {
    return loadedNatives.contains(libName);
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
