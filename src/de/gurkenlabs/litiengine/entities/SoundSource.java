package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.SFXPlayback;
import de.gurkenlabs.litiengine.sound.Sound;

@EntityInfo(renderType = RenderType.OVERLAY)
@CollisionInfo(collision = false, collisionType = Collision.NONE)
@TmxType(MapObjectType.SOUNDSOURCE)
public class SoundSource extends Entity {

  private int volume;
  private int range;

  private boolean loop;
  private Sound sound;
  private SFXPlayback playback;

  public SoundSource() {
  }

  public SoundSource(Sound sound) {
    this.setSound(sound);
  }

  public SoundSource(String name) {
    this.setSound(name);
  }

  public SoundSource(double x, double y) {
    this.setX(x);
    this.setY(y);
  }

  public SoundSource(double x, double y, double width, double height) {
    this(x, y);
    this.setWidth(width);
    this.setHeight(height);
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public boolean isLoop() {
    return loop;
  }

  public void setLoop(boolean loop) {
    this.loop = loop;
  }

  public Sound getSound() {
    return sound;
  }

  public SFXPlayback getPlayback() {
    return this.playback;
  }

  public String getSoundName() {
    return this.sound.getName();
  }

  public int getRange() {
    return range;
  }

  public void setRange(int range) {
    this.range = range;
  }

  public void setSound(String name) {
    this.sound = Resources.sounds().get(name);
  }

  public void setSound(Sound sound) {
    this.sound = sound;
  }

  public void play() {
    this.playback = Game.audio().playSound(this.getSound(), this, this.isLoop(), this.getRange());
  }

  public void pause() {
    this.getPlayback().pausePlayback();
  }

  public void resume() {
    this.getPlayback().resumePlayback();
  }

  public void stop() {
    this.getPlayback().cancel();
  }

}
