package de.gurkenlabs.litiengine.video;

import static java.time.Duration.ZERO;
import static javafx.scene.media.MediaPlayer.Status.*;

import java.net.URL;

import javax.swing.JComponent;

import de.gurkenlabs.litiengine.resources.VideoResource;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

final class VideoManagerImpl implements VideoPlayer{
  
  private volatile JFXPanel panel = new JFXPanel();
  private volatile MediaView mediaView;

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
    if(playerValid()) {
      panel.setVisible(true);
      getPlayer().play();
    }
  }
  
  private synchronized void setMedia(Media media) {
    Platform.runLater(() -> {
      final MediaPlayer player = getPlayer();
      if(player != null) {
        player.dispose();
      }
      
      this.mediaView = new MediaView(new MediaPlayer(media));
      Group root = new Group(mediaView);
      Scene scene = new Scene(root, media.getWidth(), media.getHeight());
      mediaView.autosize();
      panel.setScene(scene);
      mediaView.getMediaPlayer().setOnReady(() -> panel.setSize(media.getWidth(), media.getHeight()));
    });
  }
  
  private synchronized void play(Media media) {
    setMedia(media);
    Platform.runLater(() -> {
      play();
    });
  }
  
  @Override
  public boolean isStatusUnknown() {
    if(playerValid()) {
      return getPlayer().getStatus() == UNKNOWN;
    }
    return true;
  }

  @Override
  public boolean isReady() {
    if(playerValid()) {
      return getPlayer().getStatus() == READY;
    }
    return false;
  }

  @Override
  public boolean isPlaying() {
    if(playerValid()) {
      return getPlayer().getStatus() == PLAYING;
    }
    return false;
  }
  
  @Override
  public boolean isErrored() {
    if(playerValid()) {
      return getPlayer().getStatus() == HALTED;
    }
    return false;
  }

  @Override
  public boolean isPaused() {
    if(playerValid()) {
      return getPlayer().getStatus() == PAUSED;
    }
    return false;
  }

  @Override
  public boolean isBuffering() {
    if(playerValid()) {
      return getPlayer().getStatus() == STALLED;
    }
    return false;
  }

  @Override
  public boolean isStopped() {
    if(playerValid()) {
      return getPlayer().getStatus() == STOPPED;
    }
    return false;
  }
  
  public VideoPlayer.Status getStatus() {
    switch (getPlayer().getStatus()) {
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
    getPlayer().dispose();
  }

  @Override
  public MediaException getError() {
    if(playerValid()) {
      return getPlayer().getError();
    }
    return null;
  }

  @Override
  public double getBalance() {
    if(playerValid()) {
      return getPlayer().getBalance();
    }
    return 0;
  }

  @Override
  public java.time.Duration getBufferProgressTime() {
    if(playerValid()) {
      return convertDuration(getPlayer().getBufferProgressTime());
    }
    return ZERO;
  }

  @Override
  public int getCurrentCount() {
    if(playerValid()) {
      return getPlayer().getCurrentCount();
    }
    return 0;
  }

  @Override
  public double getCurrentRate() {
    if(playerValid()) {
      return getPlayer().getCurrentRate();
    }
    return 0;
  }

  @Override
  public java.time.Duration getCurrentTime() {
    if(playerValid()) {
      return convertDuration(getPlayer().getCurrentTime());
    }
    return ZERO;
  }

  @Override
  public double getRate() {
    if(playerValid()) {
      return getPlayer().getRate();
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
      return convertDuration(getPlayer().getStopTime());
    }
    return ZERO;
  }

  @Override
  public java.time.Duration getTotalDuration() {
    if(playerValid()) {
      return convertDuration(getPlayer().getTotalDuration());
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
    if(playerValid()) {
      getPlayer().setBalance(value);
    }
  }

  @Override
  public void setCycleCount(int value) {
    if(playerValid()) {
      getPlayer().setCycleCount(value);
    }
  }

  @Override
  public void setRate(double value) {
    if(playerValid()) {
      getPlayer().setRate(value);
    }
  }

  @Override
  public void setStartTime(java.time.Duration value) {
    if(playerValid()) {
      getPlayer().setStartTime(convertDuration(value));
    }
  }

  @Override
  public void setStopTime(java.time.Duration value) {
    if(playerValid()) {
      getPlayer().setStopTime(convertDuration(value));
    }
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
  
  public JComponent getPanel() {
    return panel;
  }
  
  private java.time.Duration convertDuration(javafx.util.Duration duration) {
    return java.time.Duration.ofSeconds((long) duration.toSeconds());
  }
  
  private javafx.util.Duration convertDuration(java.time.Duration duration) {
    return javafx.util.Duration.seconds((double)duration.getSeconds());
  }
  
  private MediaPlayer getPlayer() {
    if(mediaView != null) {
      return mediaView.getMediaPlayer();
    }
    return null;
  }
  
  private boolean playerValid() {
    return getPlayer() != null;
  }
  
}
