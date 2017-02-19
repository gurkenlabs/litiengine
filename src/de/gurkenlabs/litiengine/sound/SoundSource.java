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
  protected static final SourceDataLineCloseQueue closeQueue;

  private Point2D location;
  private IEntity entity;
  private Point2D initialListenerLocation;
  private final Sound sound;

  private SourceDataLine dataLine;
  private FloatControl panControl;
  private FloatControl gainControl;

  private boolean played;
  private boolean playing;
  private float gain;

  static {
    closeQueue = new SourceDataLineCloseQueue();
    closeQueue.start();
  }

  protected static void terminate() {
    closeQueue.terminate();
  }

  protected SoundSource(Sound sound, Point2D listenerLocation) {
    this.sound = sound;
    this.initialListenerLocation = listenerLocation;
  }

  protected SoundSource(Sound sound) {
    this(sound, null);
  }

  protected SoundSource(Sound sound, Point2D listenerLocation, Point2D location) {
    this(sound, listenerLocation);
    this.location = location;
  }

  protected SoundSource(Sound sound, Point2D listenerLocation, IEntity sourceEntity) {
    this(sound, listenerLocation);
    this.entity = sourceEntity;
  }

  public void dispose() {
    if (dataLine != null) {
      closeQueue.enqueue(dataLine);
      this.dataLine = null;
      this.gainControl = null;
      this.panControl = null;
    }
  }

  public void play(boolean loop, Point2D location, float gain) {
    // clip must be disposed
    if (dataLine != null) {
      return;
    }

    this.gain = gain;
    PlayThread thread = new PlayThread();
    thread.start();
    this.played = true;
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
  public void play(boolean loop, float volume) {
    this.play(loop, null, volume);
  }

  public boolean isPlaying() {
    return this.played || this.playing;
  }

  public Sound getSound() {
    return this.sound;
  }

  protected void updateControls(Point2D listenerLocation) {
    if (listenerLocation == null) {
      return;
    }

    Point2D loc = this.entity != null ? this.entity.getLocation() : this.location;
    if (loc == null) {
      return;
    }

    this.setGain(calculateGain(loc, listenerLocation));
    this.setPan(calculatePan(loc, listenerLocation));
  }

  private static float calculatePan(Point2D currentLocation, Point2D listenerLocation) {
    double angle = GeometricUtilities.calcRotationAngleInDegrees(listenerLocation, currentLocation);
    float pan = (float) -Math.sin(angle);
    return pan;
  }

  private static float calculateGain(Point2D currentLocation, Point2D listenerLocation) {
    if (currentLocation == null || listenerLocation == null) {
      return 0;
    }
    float gain;
    float distanceFromListener = (float) currentLocation.distance(listenerLocation);
    if (distanceFromListener <= 0) {
      gain = 1.0f;
    } else if (distanceFromListener >= Game.getSoundEngine().getMaxDistance()) {
      gain = 0.0f;
    } else {
      gain = 1.0f - (distanceFromListener / Game.getSoundEngine().getMaxDistance());
    }

    gain = MathUtilities.clamp(gain, 0, 1);
    gain *= Game.getConfiguration().SOUND.getSoundVolume();
    return gain;
  }

  private void initControls() {
    if (this.dataLine == null) {
      return;
    }

    // Check if panning is supported:
    try {
      if (!dataLine.isControlSupported(FloatControl.Type.PAN)) {
        panControl = null;
      } else {
        // Create a new pan Control:
        panControl = (FloatControl) dataLine.getControl(FloatControl.Type.PAN);
      }
    } catch (IllegalArgumentException iae) {
      panControl = null;
    }

    // Check if changing the volume is supported:
    try {
      if (!dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        gainControl = null;
      } else {
        // Create a new gain control:
        gainControl = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
        // Store it's initial gain to use as "maximum volume" later:
      }
    } catch (IllegalArgumentException iae) {
      gainControl = null;
    }
  }

  private void setGain(float g) {
    // Make sure there is a gain control
    if (gainControl == null)
      return;

    // make sure the value is valid (between 0 and 1)
    float gain = MathUtilities.clamp(g, 0, 1);

    double minimumDB = gainControl.getMinimum();
    double maximumDB = 1;

    // convert the supplied linear gain into a "decible change" value
    // minimumDB is no volume
    // maximumDB is maximum volume
    // (Number of decibles is a logrithmic function of linear gain)
    double ampGainDB = ((10.0f / 20.0f) * maximumDB) - minimumDB;
    double cste = Math.log(10.0) / 20;
    float valueDB = (float) (minimumDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * gain));
    // Update the gain:
    gainControl.setValue(valueDB);
  }

  private void setPan(float p) {
    // Make sure there is a pan control
    if (panControl == null)
      return;
    float pan = MathUtilities.clamp(p, -1, 1);
    // Update the pan:
    panControl.setValue(pan);
  }

  private class PlayThread extends Thread {
    @Override
    public void run() {
      DataLine.Info dataInfo = new DataLine.Info(SourceDataLine.class, sound.getFormat());

      try {
        dataLine = (SourceDataLine) AudioSystem.getLine(dataInfo);
        dataLine.open();
      } catch (LineUnavailableException e) {
        e.printStackTrace();
      }

      if (dataLine == null) {
        return;
      }

      initControls();
      float initialGain = gain > 0 ? gain : Game.getConfiguration().SOUND.getSoundVolume();
      setGain(initialGain);

      SoundSource.this.location = location;
      updateControls(initialListenerLocation);

      dataLine.start();
      played = false;
      playing = true;
      final byte[] buffer = new byte[1024];
      ByteArrayInputStream str = new ByteArrayInputStream(sound.getStreamData());
      while (true) {
        int readCount;
        try {
          readCount = str.read(buffer);

          if (readCount < 0) {
            break;
          }

          if (dataLine == null) {
            break;
          }

          dataLine.write(buffer, 0, readCount);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (dataLine != null) {
        dataLine.drain();
      }
      playing = false;
    }
  }

  private static class SourceDataLineCloseQueue extends Thread {
    private boolean isRunning = true;
    private final Queue<SourceDataLine> queue = new ConcurrentLinkedQueue<>();

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
        for (SourceDataLine line : queue) {
          line.stop();
          line.flush();
          line.close();
        }
      }
    }

    public void terminate() {
      this.isRunning = false;
    }

    public void enqueue(SourceDataLine clip) {
      this.queue.add(clip);
    }
  }
}