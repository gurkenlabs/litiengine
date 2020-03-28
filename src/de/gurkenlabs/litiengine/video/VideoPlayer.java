package de.gurkenlabs.litiengine.video;

import java.time.Duration;

import javax.swing.JComponent;

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
  Object getMedia();
  double getRate();
  Duration getStartTime();
  Duration getStopTime();
  Duration getTotalDuration();
  double getVolume();
  void pause();
  void play();
  void seek(Duration seekTime);
  void setBalance(double value);
  void setCycleCount(int value);
  void setRate(double value);
  void setStartTime(Duration value);
  void setStopTime(Duration value);
  void setVolume(double value);
  void stop();
  JComponent getPanel();
  
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
