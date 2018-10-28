package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

/**
 * This class is responsible for the playback of all sounds in the engine. If
 * specified, it calculates the sound volume and pan depending to the assigned
 * entity or location.
 */
final class SoundPlayback implements Runnable, ISoundPlayback {
  private static final Logger log = Logger.getLogger(SoundPlayback.class.getName());
  private static final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
    private int id = 0;
    
    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "Sound Playback Thread " + ++id);
    }
  });
  private static final ExecutorService closeQueue = Executors.newSingleThreadExecutor(r -> new Thread(r, "Data Line Close Thread"));

  private final List<SoundPlaybackListener> playbackListeners;

  private final Point2D initialListenerLocation;
  private SourceDataLine dataLine;

  private IEntity entity;
  private float gain;
  private float actualGain;
  private FloatControl gainControl;
  private FloatControl panControl;

  private Point2D location;

  private final Sound sound;

  private boolean playing;
  private boolean loop;
  private boolean cancelled;
  private boolean paused;
  
  private volatile Thread playingIn;

  SoundPlayback(final Sound sound) {
    this(sound, null);
  }

  SoundPlayback(final Sound sound, final Point2D listenerLocation) {
    this.playbackListeners = new CopyOnWriteArrayList<>();
    this.sound = sound;
    this.initialListenerLocation = listenerLocation;
    this.gain = 1;
  }

  SoundPlayback(final Sound sound, final Point2D listenerLocation, final IEntity sourceEntity) {
    this(sound, listenerLocation);
    this.entity = sourceEntity;
  }

  SoundPlayback(final Sound sound, final Point2D listenerLocation, final Point2D location) {
    this(sound, listenerLocation);
    this.location = location;
  }

  @Override
  public void run() {
    this.playingIn = Thread.currentThread();
    this.playing = true;
    this.loadDataLine();

    if (this.dataLine == null) {
      return;
    }

    this.startDataLine();

    final byte[] buffer = new byte[64];
    ByteArrayInputStream str = new ByteArrayInputStream(this.sound.getStreamData());
    while (!this.cancelled) {
      if (this.isPaused()) {
        try {
          synchronized (this) {
            this.wait();
          }
        } catch (InterruptedException e) {
          continue;
        }
      }

      int readCount;
      readCount = str.read(buffer, 0, buffer.length);

      if (readCount < 0) {
        if (!this.loop || this.dataLine == null) {
          final SoundEvent event = new SoundEvent(this, this.sound);
          for (SoundPlaybackListener listener : this.playbackListeners) {
            listener.finished(event);
          }
          break;
        }

        this.restartDataLine();
        str = new ByteArrayInputStream(this.sound.getStreamData());

      } else if (this.dataLine != null) {
        this.dataLine.write(buffer, 0, readCount);
      }
      Thread.interrupted(); // clears the interrupted flag for waiting; the interrupted flag is inconsistent when used with the sound system
      // it can, however, establish a happens-before relationship
      // see https://community.oracle.com/thread/2381571
    }

    if (this.dataLine != null) {
      this.dataLine.drain();
    }

    this.playing = false;
  }

  @Override
  public void cancel() {
    this.cancelled = true;
    this.playingIn.interrupt();

    final SoundEvent event = new SoundEvent(this, this.sound);
    for (SoundPlaybackListener listener : this.playbackListeners) {
      listener.cancelled(event);
    }
  }

  @Override
  public void addSoundPlaybackListener(SoundPlaybackListener soundPlaybackListener) {
    this.playbackListeners.add(soundPlaybackListener);
  }

  @Override
  public void removeSoundPlaybackListener(SoundPlaybackListener soundPlaybackListener) {
    this.playbackListeners.remove(soundPlaybackListener);
  }

  @Override
  public float getGain() {
    return this.gain;
  }

  @Override
  public void pausePlayback() {
    this.paused = true;
    this.playingIn.interrupt();
  }

  @Override
  public void resumePlayback() {
    this.paused = false;
    synchronized (this) {
      this.notify();
    }
  }

  @Override
  public boolean isPaused() {
    return this.paused;
  }

  @Override
  public boolean isPlaying() {
    return this.playing;
  }

  @Override
  public void setGain(float gain) {
    this.gain = MathUtilities.clamp(gain, 0, 1);
  }

  protected static void terminate() {
    executorService.shutdownNow();
    try {
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    } catch (InterruptedException e) {}
    closeQueue.shutdown();
  }

  void dispose() {
    if (this.isPlaying()) {
      this.cancel();
    }

    if (this.dataLine != null) {
      closeQueue.execute(() -> {
        this.dataLine.stop();
        this.dataLine.flush();
        this.dataLine.close();
      });
      this.dataLine = null;
      this.gainControl = null;
      this.panControl = null;
    }
  }

  Sound getSound() {
    return this.sound;
  }

  /**
   * Plays the sound without any volume or pan adjustments.
   */
  void play() {
    this.play(false);
  }

  void play(final boolean loop) {
    this.play(loop, Game.getConfiguration().sound().getSoundVolume());
  }

  void play(final boolean loop, final float volume) {
    this.play(loop, null, volume);
  }

  void play(final float volume) {
    this.play(false, null, volume);
  }

  void play(final Point2D location) {
    this.play(false, location, Game.getConfiguration().sound().getSoundVolume());
  }

  void play(final boolean loop, final Point2D location, final float gain) {
    // clip must be disposed
    if (this.dataLine != null) {
      return;
    }

    this.actualGain = gain;
    this.location = location;
    this.loop = loop;
    executorService.execute(this);
  }

  void setMasterGain(final float g) {
    // Make sure there is a gain control
    if (this.gainControl == null) {
      return;
    }

    // make sure the value is valid (between 0 and 1)
    final float newGain = MathUtilities.clamp(g * this.gain, 0, 1);

    final double minimumDB = this.gainControl.getMinimum();
    final double maximumDB = 1;

    // convert the supplied linear gain into a "decible change" value
    // minimumDB is no volume
    // maximumDB is maximum volume
    // (Number of decibles is a logrithmic function of linear gain)
    final double ampGainDB = 10.0f / 20.0f * maximumDB - minimumDB;
    final double cste = Math.log(10.0) / 20;
    final float valueDB = (float) (minimumDB + 1 / cste * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * newGain));
    // Update the gain:
    this.gainControl.setValue(valueDB);
  }

  void updateControls(final Point2D listenerLocation) {
    if (listenerLocation == null) {
      return;
    }

    final Point2D loc = this.entity != null ? this.entity.getCenter() : this.location;
    if (loc == null) {
      return;
    }

    this.setMasterGain(calculateGain(loc, listenerLocation));
    this.setPan(calculatePan(loc, listenerLocation));
  }

  private static float calculateGain(final Point2D currentLocation, final Point2D listenerLocation) {
    if (currentLocation == null || listenerLocation == null) {
      return 0;
    }
    float gain;
    final float distanceFromListener = (float) currentLocation.distance(listenerLocation);
    if (distanceFromListener <= 0) {
      gain = 1.0f;
    } else if (distanceFromListener >= Game.getSoundEngine().getMaxDistance()) {
      gain = 0.0f;
    } else {
      gain = 1.0f - distanceFromListener / Game.getSoundEngine().getMaxDistance();
    }

    gain = MathUtilities.clamp(gain, 0, 1);
    gain *= Game.getConfiguration().sound().getSoundVolume();
    return gain;
  }

  private static float calculatePan(final Point2D currentLocation, final Point2D listenerLocation) {
    final double angle = GeometricUtilities.calcRotationAngleInDegrees(listenerLocation, currentLocation);
    return (float) -Math.sin(angle);
  }

  private void loadDataLine() {
    final DataLine.Info dataInfo = new DataLine.Info(SourceDataLine.class, this.sound.getFormat());
    try {
      this.dataLine = (SourceDataLine) AudioSystem.getLine(dataInfo);
      if (!dataLine.isOpen()) {
        this.dataLine.open();
      }
    } catch (final LineUnavailableException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void startDataLine() {
    this.initControls();
    this.updateControls(this.initialListenerLocation);
    this.dataLine.start();
  }

  private void restartDataLine() {
    this.dataLine.drain();
    this.loadDataLine();
    this.initControls();
    this.dataLine.start();
  }

  private void initControls() {
    if (this.dataLine == null) {
      return;
    }

    // Check if panning is supported:
    // Create a new pan Control:
    this.panControl = this.dataLine.isControlSupported(FloatControl.Type.PAN) ? (FloatControl) this.dataLine.getControl(FloatControl.Type.PAN) : null;

    // Check if changing the volume is supported:
    // Create a new gain control:
    this.gainControl = this.dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN) ? (FloatControl) this.dataLine.getControl(FloatControl.Type.MASTER_GAIN) : null;
    // Store it's initial gain to use as "maximum volume" later:

    this.setMasterGain(this.actualGain);
  }

  private void setPan(final float p) {
    // Make sure there is a pan control
    if (this.panControl == null) {
      return;
    }
    final float pan = MathUtilities.clamp(p, -1, 1);
    // Update the pan:
    this.panControl.setValue(pan);
  }
}