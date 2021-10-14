package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.Game;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;

/** A {@code SoundPlayback} implementation for the playback of sound effects. */
public class SFXPlayback extends SoundPlayback {
  private Sound sound;
  private FloatControl panControl;
  private Supplier<Point2D> source;
  private int range;
  private float volumeModifier;
  private VolumeControl volume;
  private boolean loop;

  SFXPlayback(Sound sound, Supplier<Point2D> source, boolean loop, int range, float volumeModifier)
      throws LineUnavailableException {
    super(sound.getFormat());
    this.loop = loop;
    this.sound = sound;
    this.panControl =
        this.line.isControlSupported(FloatControl.Type.PAN)
            ? (FloatControl) this.line.getControl(FloatControl.Type.PAN)
            : null;
    this.source = source;
    this.range = range;
    this.volumeModifier = volumeModifier;
    this.volume = this.createVolumeControl();
  }

  @Override
  public void run() {
    do {
      if (this.play(this.sound)) {
        return;
      }
    } while (this.loop);
    this.finish();
  }

  void updateLocation(Point2D listenerLocation) {
    Point2D location = source.get();
    if (location != null) {
      double dx = location.getX() - listenerLocation.getX();
      double dy = location.getY() - listenerLocation.getY();
      double dist = Math.sqrt(dx * dx + dy * dy);
      if (this.panControl != null) {
        this.panControl.setValue(dist > 0 ? (float) (dx / dist) : 0f);
      }
      this.volume.set(
          Game.config().sound().getSoundVolume()
              * this.volumeModifier
              * (float) Math.max(1.0 - dist / this.range, 0.0));
    } else {
      this.volume.set(Game.config().sound().getSoundVolume() * this.volumeModifier);
    }
  }

  @Override
  protected void play() {
    this.updateLocation(Game.audio().getListenerLocation());
    super.play();
    Game.audio().addSound(this);
  }
}
