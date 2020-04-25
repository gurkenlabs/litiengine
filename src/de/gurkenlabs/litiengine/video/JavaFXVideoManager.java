package de.gurkenlabs.litiengine.video;

import static java.time.Duration.ZERO;
import static javafx.scene.media.MediaPlayer.Status.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.logging.Level;

import javax.net.ssl.SSLHandshakeException;
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

public final class JavaFXVideoManager extends VideoManager{

  private volatile JFXPanel panel = new JFXPanel();
  private volatile MediaView mediaView;

  public static boolean allowNetworkConnections = false;
  public static final String NAME = "javaFX";
  
  public static void register() {
    VideoManagerFactory.registerPlayerType(NAME, JavaFXVideoManager.class);
  }
  
  protected JavaFXVideoManager(VideoResource video) {
    super(video);
  }
  
  @Override
  public synchronized void setVideo(VideoResource video) {
    if(video.getURI().startsWith("http")) {
      try {
        setVideo(new URL(video.getURI()));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    setMedia(new Media(video.getURI()));
  }

  @Override
  public synchronized void play(VideoResource video) {
    setVideo(video);
    while(isStatusUnknown());
    play();
  }

  @Override
  public synchronized void setVideo(URL url) throws IOException {
    if(url.getProtocol().startsWith("http")) {
      if(!allowNetworkConnections) {
        throw new IOException("Network access disallowed");
      }
      if(url.getProtocol().equals("http")) {
        throw new SSLHandshakeException("Insecure protocol: http. Use https");
      }
    }
    setMedia(new Media(url.toString()));
  }

  @Override
  public synchronized void play(URL url) throws IOException {
    setVideo(url);
    while(isStatusUnknown());
    play();
  }
  
  public synchronized void play() {
    if(playerValid()) {
      panel.setVisible(true);
      Platform.runLater(() -> {
        getPlayer().play();
      });
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
    if(getPlayer() == null) {
      return Status.UNKNOWN;
    }
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
  
  @Override
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

  @Override
  protected void initialize() {
    //no special initialization code needed for javafx
  }

  @Override
  protected void loadNatives() {
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
    loadedNatives.add(NAME);
  }

  @Override
  protected boolean nativesLoaded() {
    return loadedNatives.contains(NAME);
  }
  
}
