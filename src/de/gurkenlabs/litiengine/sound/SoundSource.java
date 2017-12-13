package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class SoundSource {
  private static final Logger log = Logger.getLogger(SoundSource.class.getName());
  private static final ExecutorService executorServie;
  protected static final SourceDataLineCloseQueue closeQueue;

  private SourceDataLine dataLine;

  private IEntity entity;
  private float gain;
  private FloatControl gainControl;

  private final Point2D initialListenerLocation;

  private Point2D location;

  private FloatControl panControl;

  private boolean played;

  private boolean playing;

  private final Sound sound;

  private PlayRunnable runnable;

  static {
    closeQueue = new SourceDataLineCloseQueue();
    executorServie = Executors.newCachedThreadPool();
    executorServie.execute(closeQueue);
  }

  protected SoundSource(final Sound sound) {
    this(sound, null);
  }

  protected SoundSource(final Sound sound, final Point2D listenerLocation) {
    this.sound = sound;
    this.initialListenerLocation = listenerLocation;
  }

  protected SoundSource(final Sound sound, final Point2D listenerLocation, final IEntity sourceEntity) {
    this(sound, listenerLocation);
    this.entity = sourceEntity;
  }

  protected SoundSource(final Sound sound, final Point2D listenerLocation, final Point2D location) {
    this(sound, listenerLocation);
    this.location = location;
  }

  public void dispose() {
    if (this.dataLine != null) {
      closeQueue.enqueue(this.dataLine);
      this.dataLine = null;
      this.gainControl = null;
      this.panControl = null;
    }
  }

  public Sound getSound() {
    return this.sound;
  }

  public boolean isPlaying() {
    return this.played || this.playing;
  }

  /**
   * Plays the sound without any volume or pan adjustments.
   */
  public void play() {
    this.play(false, null, -1);
  }

  public void interrupt() {
    runnable.setCancelled(true);
  }

  /**
   * Loops the sound with the specified volume.
   *
   * @param loop
   * @param volume
   */
  public void play(final boolean loop, final float volume) {
    this.play(loop, null, volume);
  }

  public void play(final boolean loop, final Point2D location, final float gain) {
    // clip must be disposed
    if (this.dataLine != null) {
      return;
    }

    this.gain = gain;
    this.location = location;
    this.runnable = new PlayRunnable(loop);
    executorServie.execute(this.runnable);
    this.played = true;
  }

  protected static void terminate() {
    closeQueue.terminate();
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

  private void setPan(final float p) {
    // Make sure there is a pan control
    if (this.panControl == null) {
      return;
    }
    final float pan = MathUtilities.clamp(p, -1, 1);
    // Update the pan:
    this.panControl.setValue(pan);
  }

  private class PlayRunnable implements Runnable {
    private boolean loop;

    private boolean cancelled;

    public PlayRunnable(final boolean loop) {
      this.loop = loop;
    }

    @Override
    public void run() {

      this.loadDataLine();

      if (SoundSource.this.dataLine == null) {
        return;
      }

      this.initControls();
      this.initGain();

      SoundSource.this.updateControls(SoundSource.this.initialListenerLocation);

      SoundSource.this.dataLine.start();
      SoundSource.this.played = false;
      SoundSource.this.playing = true;
      final byte[] buffer = new byte[1024];
      ByteArrayInputStream str = new ByteArrayInputStream(SoundSource.this.sound.getStreamData());
      while (!this.cancelled) {
        int readCount;
        try {
          readCount = str.read(buffer);

          if (readCount < 0) {
            if (!this.loop || SoundSource.this.dataLine == null) {
              break;
            }

            restartDataLine();
            str = new ByteArrayInputStream(SoundSource.this.sound.getStreamData());

          } else if (SoundSource.this.dataLine != null) {
            SoundSource.this.dataLine.write(buffer, 0, readCount);
          }
        } catch (final IOException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }
      if (SoundSource.this.dataLine != null) {
        SoundSource.this.dataLine.drain();
      }
      SoundSource.this.playing = false;
    }

    private void initGain() {
      final float initialGain = SoundSource.this.gain > 0 ? SoundSource.this.gain : Game.getConfiguration().sound().getSoundVolume();
      SoundSource.this.setGain(initialGain);
    }

    public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
    }

    private void loadDataLine() {
      final DataLine.Info dataInfo = new DataLine.Info(SourceDataLine.class, SoundSource.this.sound.getFormat());
      try {
        SoundSource.this.dataLine = (SourceDataLine) AudioSystem.getLine(dataInfo);
        SoundSource.this.dataLine.open();
      } catch (final LineUnavailableException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    private void restartDataLine() {
      SoundSource.this.dataLine.drain();
      this.loadDataLine();
      this.initControls();
      this.initGain();
      SoundSource.this.dataLine.start();
    }

    private void initControls() {
      if (SoundSource.this.dataLine == null) {
        return;
      }

      // Check if panning is supported:
      try {
        if (!SoundSource.this.dataLine.isControlSupported(FloatControl.Type.PAN)) {
          SoundSource.this.panControl = null;
        } else {
          // Create a new pan Control:
          SoundSource.this.panControl = (FloatControl) SoundSource.this.dataLine.getControl(FloatControl.Type.PAN);
        }
      } catch (final IllegalArgumentException iae) {
        SoundSource.this.panControl = null;
      }

      // Check if changing the volume is supported:
      try {
        if (!SoundSource.this.dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
          SoundSource.this.gainControl = null;
        } else {
          // Create a new gain control:
          SoundSource.this.gainControl = (FloatControl) SoundSource.this.dataLine.getControl(FloatControl.Type.MASTER_GAIN);
          // Store it's initial gain to use as "maximum volume" later:
        }
      } catch (final IllegalArgumentException iae) {
        SoundSource.this.gainControl = null;
      }
    }
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
        while (this.queue.peek() != null) {
          final SourceDataLine clip = this.queue.poll();
          clip.stop();
          clip.flush();
          clip.close();
        }

        try {
          Thread.sleep(50);
        } catch (final InterruptedException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }

      if (!this.queue.isEmpty()) {
        for (final SourceDataLine line : this.queue) {
          line.stop();
          line.flush();
          line.close();
        }
      }
    }

    public void terminate() {
      this.isRunning = false;
    }
  }
}