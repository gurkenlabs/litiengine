package de.gurkenlabs.litiengine.video;

import static java.time.Duration.ZERO;
import static javafx.scene.media.MediaPlayer.Status.*;

import java.net.URL;

import javax.swing.JComponent;

import de.gurkenlabs.litiengine.resources.VideoResource;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

final class VideoManagerImpl implements VideoPlayer{
  
  private JFXPanel panel = new JFXPanel();
  private Media media;
  private MediaPlayer mediaPlayer;
  private MediaView mediaView;

  @Override
  public void setVideo(VideoResource video) {
    setMedia(new Media(video.getURI()));
  }

  @Override
  public void play(VideoResource video) {
    play(new Media(video.getURI()));
  }

  @Override
  public void setVideo(URL url) {
    setMedia(new Media(url.toString()));
  }

  @Override
  public void play(URL url) {
    play(new Media(url.toString()));
  }
  
  public void play() {
    if(panel != null && playerValid() && media != null && mediaView != null) {
      panel.setVisible(true);
      mediaPlayer.play();
    }
  }
  
  private void setMedia(Media media) {
    if(mediaPlayer != null) {
      mediaPlayer.dispose();
    }
    
    this.mediaPlayer = new MediaPlayer(media);
    this.media = media;
    this.mediaView = new MediaView(mediaPlayer);
    Group root = new Group(mediaView);
    Scene scene = new Scene(root, media.getWidth(), media.getHeight());
    panel.setScene(scene);
  }
  
  private void play(Media media) {
    setMedia(media);
    play();
  }
  
  @Override
  public boolean isStatusUnknown() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == UNKNOWN;
    }
    return true;
  }

  @Override
  public boolean isReady() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == READY;
    }
    return false;
  }

  @Override
  public boolean isPlaying() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == PLAYING;
    }
    return false;
  }
  
  @Override
  public boolean isErrored() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == HALTED;
    }
    return false;
  }

  @Override
  public boolean isPaused() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == PAUSED;
    }
    return false;
  }

  @Override
  public boolean isBuffering() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == STALLED;
    }
    return false;
  }

  @Override
  public boolean isStopped() {
    if(playerValid()) {
      return mediaPlayer.getStatus() == STOPPED;
    }
    return false;
  }
  
  public VideoPlayer.Status getStatus() {
    switch (mediaPlayer.getStatus()) {
    case DISPOSED:
      return Status.DISPOSED;
    case HALTED:
      return Status.ERRORED;
    case PAUSED:
      return Status.PAUSED;
    case PLAYING:
      return Status.PLAYING;
    case READY:
      return Status.READY;
    case STALLED:
      return Status.STALLED;
    case STOPPED:
      return Status.STOPPED;
    default:
      return Status.UNKNOWN;
    }
  }

  @Override
  public void dispose() {
    mediaPlayer.dispose();
  }

  @Override
  public MediaException getError() {
    if(playerValid()) {
      return mediaPlayer.getError();
    }
    return null;
  }

  @Override
  public double getBalance() {
    if(playerValid()) {
      return mediaPlayer.getBalance();
    }
    return 0;
  }

  @Override
  public java.time.Duration getBufferProgressTime() {
    if(playerValid()) {
      return convertDuration(mediaPlayer.getBufferProgressTime());
    }
    return ZERO;
  }

  @Override
  public int getCurrentCount() {
    if(playerValid()) {
      return mediaPlayer.getCurrentCount();
    }
    return 0;
  }

  @Override
  public double getCurrentRate() {
    if(playerValid()) {
      return mediaPlayer.getCurrentRate();
    }
    return 0;
  }

  @Override
  public java.time.Duration getCurrentTime() {
    if(playerValid()) {
      return convertDuration(mediaPlayer.getCurrentTime());
    }
    return ZERO;
  }

  @Override
  public double getRate() {
    if(playerValid()) {
      return mediaPlayer.getRate();
    }
    return 0;
  }

  @Override
  public java.time.Duration getStartTime() {
    if(playerValid()) {
      return convertDuration(mediaPlayer.getStartTime());
    }
    return ZERO;
  }

  @Override
  public java.time.Duration getStopTime() {
    if(playerValid()) {
      return convertDuration(mediaPlayer.getStopTime());
    }
    return ZERO;
  }

  @Override
  public java.time.Duration getTotalDuration() {
    if(playerValid()) {
      return convertDuration(mediaPlayer.getTotalDuration());
    }
    return ZERO;
  }

  @Override
  public double getVolume() {
    if(playerValid()) {
      return mediaPlayer.getVolume();
    }
    return 0;
  }

  @Override
  public void pause() {
    if(playerValid()) {
      mediaPlayer.pause();
    }
  }

  @Override
  public void seek(java.time.Duration seekTime) {
    if(playerValid()) {
      mediaPlayer.seek(convertDuration(seekTime));
    }
  }

  @Override
  public void setBalance(double value) {
    if(playerValid()) {
      mediaPlayer.setBalance(value);
    }
  }

  @Override
  public void setCycleCount(int value) {
    if(playerValid()) {
      mediaPlayer.setCycleCount(value);
    }
  }

  @Override
  public void setRate(double value) {
    if(playerValid()) {
      mediaPlayer.setRate(value);
    }
  }

  @Override
  public void setStartTime(java.time.Duration value) {
    if(playerValid()) {
      mediaPlayer.setStartTime(convertDuration(value));
    }
  }

  @Override
  public void setStopTime(java.time.Duration value) {
    if(playerValid()) {
      mediaPlayer.setStopTime(convertDuration(value));
    }
  }

  @Override
  public void setVolume(double value) {
    if(playerValid()) {
      mediaPlayer.setVolume(value);
    }
  }

  @Override
  public void stop() {
    if(playerValid()) {
      mediaPlayer.stop();
    }
  }
  
  public JComponent getPanel() {
    return panel;
  }
  
  private java.time.Duration convertDuration(javafx.util.Duration duration) {
    return java.time.Duration.ofSeconds((long) duration.toSeconds());
  }
  
  private javafx.util.Duration convertDuration(java.time.Duration duration) {
    return javafx.util.Duration.seconds((double)duration.getSeconds());
  }
  
  private boolean playerValid() {
    return mediaPlayer != null;
  }
  
}
