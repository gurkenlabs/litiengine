package de.gurkenlabs.litiengine.video;

import java.awt.Container;
import java.net.URL;
import java.time.Duration;

import de.gurkenlabs.litiengine.resources.VideoResource;

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * Do not import anything from javafx into this interface!
 * 
 * Not all JREs contain javafx. Java will throw a
 * java.lang.Error if we accidentally try to load a 
 * javafx class if the library doesn't exist!
 *
 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

interface VideoPlayer {
  void dispose();
  
  boolean isStatusUnknown();
  boolean isReady();
  boolean isErrored();
  boolean isPlaying();
  boolean isPaused();
  boolean isBuffering();
  boolean isStopped();
  Status getStatus();
  
  Throwable getError();
  double getBalance();
  Duration getBufferProgressTime();
  int getCurrentCount();
  double getCurrentRate();
  Duration getCurrentTime();
  double getRate();
  Duration getStartTime();
  Duration getStopTime();
  Duration getTotalDuration();
  double getVolume();
  void pause();
  void setVideo(VideoResource video);
  void setVideo(URL url);
  void play(VideoResource video);
  void play(URL url);
  void play();
  void seek(Duration seekTime);
  void setBalance(double value);
  void setRate(double value);
  void setStartTime(Duration value);
  void setStopTime(Duration value);
  void setVolume(double value);
  void stop();
  Container getPanel();
  
  public static enum Status {
    DISPOSED,
    ERRORED,
    PAUSED,
    PLAYING,
    READY,
    STALLED,
    STOPPED,
    UNKNOWN
  }
  
}
