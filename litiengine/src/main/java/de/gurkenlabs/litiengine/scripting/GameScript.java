package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import java.util.Objects;

/** Base class for scripts attached to the game lifecycle. */
public abstract class GameScript extends AbstractScript<Object> {
  @Override protected final void attached() throws Exception { this.onStarted(); }
  @Override protected final void detached() throws Exception { this.onStopped(); }

  /** Called after the binding enters the running game lifecycle. */
  protected void onStarted() throws Exception { this.started(); }

  /** Called before the binding leaves the game lifecycle. */
  protected void onStopped() throws Exception { this.stopped(); }

  /** Loads an environment map by map name. */
  public void loadMap(String mapName) {
    Objects.requireNonNull(mapName, "Map name must not be null.");
    Game.world().loadEnvironment(mapName);
  }

  /** Loads an environment map. */
  public void loadMap(IMap map) {
    Objects.requireNonNull(map, "Map must not be null.");
    Game.world().loadEnvironment(map);
  }

  /** Plays a sound effect by resource name. */
  public void playSound(String soundName) {
    if (soundName == null || soundName.isBlank()) return;
    Sound sound = Resources.sounds().get(soundName);
    if (sound != null) {
      Game.audio().playSound(sound);
    }
  }

  /** Plays a background music track by resource name. */
  public void playMusic(String musicName) {
    if (musicName == null || musicName.isBlank()) return;
    Sound music = Resources.sounds().get(musicName);
    if (music != null) {
      Game.audio().playMusic(music);
    }
  }

  /** Stops currently playing music. */
  public void stopMusic() {
    Game.audio().stopMusic();
  }

  /** Exits and terminates the game. */
  public void exit() {
    Game.terminate();
  }

  /** @deprecated Override {@link #onStarted()} in new scripts. */
  @Deprecated
  protected void started() throws Exception {}

  /** @deprecated Override {@link #onStopped()} in new scripts. */
  @Deprecated
  protected void stopped() throws Exception {}
}
