package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
  private class PlayThread extends Thread {
    @Override
    public void run() {
      final DataLine.Info dataInfo = new DataLine.Info(SourceDataLine.class, SoundSource.this.sound.getFormat());

      try {
        SoundSource.this.dataLine = (SourceDataLine) AudioSystem.getLine(dataInfo);
        SoundSource.this.dataLine.open();
      } catch (final LineUnavailableException e) {
        e.printStackTrace();
      }

      if (SoundSource.this.dataLine == null) {
        return;
      }

      SoundSource.this.initControls();
      final float initialGain = SoundSource.this.gain > 0 ? SoundSource.this.gain : Game.getConfiguration().SOUND.getSoundVolume();
      SoundSource.this.setGain(initialGain);

      SoundSource.this.location = SoundSource.this.location;
      SoundSource.this.updateControls(SoundSource.this.initialListenerLocation);

      SoundSource.this.dataLine.start();
      SoundSource.this.played = false;
      SoundSource.this.playing = true;
      final byte[] buffer = new byte[1024];
      final ByteArrayInputStream str = new ByteArrayInputStream(SoundSource.this.sound.getStreamData());
      while (true) {
        int readCount;
        try {
          readCount = str.read(buffer);

          if (readCount < 0) {
            break;
          }

          if (SoundSource.this.dataLine == null) {
            break;
          }

          SoundSource.this.dataLine.write(buffer, 0, readCount);
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
      if (SoundSource.this.dataLine != null) {
        SoundSource.this.dataLine.drain();
      }
      SoundSource.this.playing = false;
    }
  }

  private static class SourceDataLineCloseQueue extends Thread {
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
          e.printStackTrace();
        }
      }

      if (this.queue.size() > 0) {
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

  protected static final SourceDataLineCloseQueue closeQueue;
  static {
    closeQueue = new SourceDataLineCloseQueue();
    closeQueue.start();
  }

  protected static void terminate() {
    closeQueue.terminate();
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
    gain *= Game.getConfiguration().SOUND.getSoundVolume();
    return gain;
  }

  private static float calculatePan(final Point2D currentLocation, final Point2D listenerLocation) {
    final double angle = GeometricUtilities.calcRotationAngleInDegrees(listenerLocation, currentLocation);
    final float pan = (float) -Math.sin(angle);
    return pan;
  }

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
    final PlayThread thread = new PlayThread();
    thread.start();
    this.played = true;
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

  private void setGain(final float g) {
    // Make sure there is a gain control
    if (this.gainControl == null) {
      return;
    }

    // make sure the value is valid (between 0 and 1)
    final float gain = MathUtilities.clamp(g, 0, 1);

    final double minimumDB = this.gainControl.getMinimum();
    final double maximumDB = 1;

    // convert the supplied linear gain into a "decible change" value
    // minimumDB is no volume
    // maximumDB is maximum volume
    // (Number of decibles is a logrithmic function of linear gain)
    final double ampGainDB = 10.0f / 20.0f * maximumDB - minimumDB;
    final double cste = Math.log(10.0) / 20;
    final float valueDB = (float) (minimumDB + 1 / cste * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * gain));
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
}