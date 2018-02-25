package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * This class is responsible for the playback of all sounds in the engine. If
 * specified, it calculates the sound volume and pan depending to the assigned
 * entity or location.
 */
public final class SoundPlayback implements Runnable, ISoundPlayback {
  private static final Logger log = Logger.getLogger(SoundPlayback.class.getName());
  private static final ExecutorService executorServie;
  protected static final SourceDataLineCloseQueue closeQueue;

  private final List<Consumer<Sound>> finishedConsumer;
  private final List<Consumer<Sound>> cancelledConsumer;

  private final Point2D initialListenerLocation;
  private SourceDataLine dataLine;

  private IEntity entity;
  private float gain;
  private FloatControl gainControl;
  private FloatControl panControl;

  private Point2D location;

  private final Sound sound;

  private boolean playing;
  private boolean loop;
  private boolean cancelled;
  private boolean paused;

  static {
    closeQueue = new SourceDataLineCloseQueue();
    executorServie = Executors.newCachedThreadPool();
    executorServie.execute(closeQueue);
  }

  protected SoundPlayback(final Sound sound) {
    this(sound, null);
  }

  protected SoundPlayback(final Sound sound, final Point2D listenerLocation) {
    this.finishedConsumer = new CopyOnWriteArrayList<>();
    this.cancelledConsumer = new CopyOnWriteArrayList<>();
    this.sound = sound;
    this.initialListenerLocation = listenerLocation;
  }

  protected SoundPlayback(final Sound sound, final Point2D listenerLocation, final IEntity sourceEntity) {
    this(sound, listenerLocation);
    this.entity = sourceEntity;
  }

  protected SoundPlayback(final Sound sound, final Point2D listenerLocation, final Point2D location) {
    this(sound, listenerLocation);
    this.location = location;
  }

  @Override
  public void run() {
    this.playing = true;
    this.loadDataLine();

    if (this.dataLine == null) {
      return;
    }

    this.initControls();
    this.initGain();

    this.updateControls(this.initialListenerLocation);

    this.dataLine.start();
    final byte[] buffer = new byte[1024];
    ByteArrayInputStream str = new ByteArrayInputStream(this.sound.getStreamData());
    while (!this.cancelled) {
      while (this.isPaused() && this.isPlaying() && !this.cancelled) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      int readCount;
      try {
        readCount = str.read(buffer);

        if (readCount < 0) {
          if (!this.loop || this.dataLine == null) {
            break;
          }

          restartDataLine();
          str = new ByteArrayInputStream(this.sound.getStreamData());

        } else if (this.dataLine != null) {
          this.dataLine.write(buffer, 0, readCount);
        }
      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    if (this.dataLine != null) {
      this.dataLine.drain();
    }

    this.playing = false;
    for (Consumer<Sound> cons : this.finishedConsumer) {
      cons.accept(this.getSound());
    }
  }

  @Override
  public void onFinished(Consumer<Sound> cons) {
    this.finishedConsumer.add(cons);
  }

  @Override
  public void onCancelled(Consumer<Sound> cons) {
    this.cancelledConsumer.add(cons);
  }

  @Override
  public void cancel() {
    this.cancelled = true;
    for (Consumer<Sound> cons : this.cancelledConsumer) {
      cons.accept(this.getSound());
    }
  }

  @Override
  public void pausePlayback() {
    this.paused = true;
  }

  @Override
  public void resumePlayback() {
    this.paused = false;
  }

  @Override
  public boolean isPaused() {
    return this.paused;
  }

  protected static void terminate() {
    closeQueue.terminate();
  }

  protected void dispose() {
    if (this.isPlaying()) {
      this.cancel();
    }

    if (this.dataLine != null) {
      closeQueue.enqueue(this.dataLine);
      this.dataLine = null;
      this.gainControl = null;
      this.panControl = null;
    }
  }

  protected Sound getSound() {
    return this.sound;
  }

  protected boolean isPlaying() {
    return this.playing;
  }

  /**
   * Plays the sound without any volume or pan adjustments.
   */
  protected void play() {
    this.play(false, null, -1);
  }

  protected void play(final boolean loop, final float volume) {
    this.play(loop, null, volume);
  }

  protected void play(final boolean loop, final Point2D location, final float gain) {
    // clip must be disposed
    if (this.dataLine != null) {
      return;
    }

    this.gain = gain;
    this.location = location;
    this.loop = loop;
    executorServie.execute(this);
  }

  protected void setGain(final float g) {
    // Make sure there is a gain control
    if (this.gainControl == null) {
      return;
    }

    // make sure the value is valid (between 0 and 1)
    final float newGain = MathUtilities.clamp(g, 0, 1);

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

  protected void updateControls(final Point2D listenerLocation) {
    if (listenerLocation == null) {
      return;
    }

    final Point2D loc = this.entity != null ? this.entity.getLocation() : this.location;
    if (loc == null) {
      return;
    }

    this.setGain(calculateGain(loc, listenerLocation));
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

  private void initGain() {
    final float initialGain = this.gain > 0 ? this.gain : Game.getConfiguration().sound().getSoundVolume();
    SoundPlayback.this.setGain(initialGain);
  }

  private void loadDataLine() {
    final DataLine.Info dataInfo = new DataLine.Info(SourceDataLine.class, this.sound.getFormat());
    try {
      this.dataLine = (SourceDataLine) AudioSystem.getLine(dataInfo);
      this.dataLine.open();
    } catch (final LineUnavailableException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void restartDataLine() {
    this.dataLine.drain();
    this.loadDataLine();
    this.initControls();
    this.initGain();
    this.dataLine.start();
  }

  private void initControls() {
    if (this.dataLine == null) {
      return;
    }

    // Check if panning is supported:
    try {
      if (!this.dataLine.isControlSupported(FloatControl.Type.PAN)) {
        this.panControl = null;
      } else {
        // Create a new pan Control:
        this.panControl = (FloatControl) this.dataLine.getControl(FloatControl.Type.PAN);
      }
    } catch (final IllegalArgumentException iae) {
      this.panControl = null;
    }

    // Check if changing the volume is supported:
    try {
      if (!this.dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        this.gainControl = null;
      } else {
        // Create a new gain control:
        this.gainControl = (FloatControl) this.dataLine.getControl(FloatControl.Type.MASTER_GAIN);
        // Store it's initial gain to use as "maximum volume" later:
      }
    } catch (final IllegalArgumentException iae) {
      this.gainControl = null;
    }
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

  private static class SourceDataLineCloseQueue implements Runnable {
    private boolean isRunning = true;
    private final Queue<SourceDataLine> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(final SourceDataLine clip) {
      this.queue.add(clip);
    }

    @Override
    public void run() {
      while (this.isRunning) {
        this.closeAllSoundSources();
        try {
          Thread.sleep(50);
        } catch (final InterruptedException e) {
          this.terminate();
          log.log(Level.SEVERE, e.getMessage(), e);
          Thread.currentThread().interrupt();
        }
      }

      this.closeAllSoundSources();
    }

    public void terminate() {
      this.isRunning = false;
    }

    private void closeAllSoundSources() {
      if (this.queue.isEmpty()) {
        return;
      }

      while (this.queue.peek() != null) {
        final SourceDataLine clip = this.queue.poll();
        clip.stop();
        clip.flush();
        clip.close();
      }
    }
  }
}