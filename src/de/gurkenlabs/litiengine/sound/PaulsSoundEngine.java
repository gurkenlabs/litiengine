package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;

public class PaulsSoundEngine extends SoundEngine {
  private SoundSystem soundSystem;

  @Override
  public void init(final float soundVolume) {
    super.init(soundVolume);

    try {
      SoundSystemConfig.addLibrary(LibraryJavaSound.class);
      SoundSystemConfig.setCodec("wav", CodecWav.class);
      SoundSystemConfig.setCodec("ogg", CodecJOgg.class);

    } catch (final SoundSystemException e) {
      System.err.println("error linking with the pluggins");
    }

    this.soundSystem = new SoundSystem();
    SoundSystemConfig.setDefaultRolloff(0.15f);
    SoundSystemConfig.setMasterGain(soundVolume);
  }

  @Override
  public void playMusic(final Sound sound) {
    if (sound == null) {
      return;
    }
    
    if (this.isPlaying(sound.getPath())) {
      return;
    }

    this.soundSystem.backgroundMusic(sound.getPath(), sound.getUrl(), sound.getPath(), true);
  }
  
  @Override
  public void rewind(Sound sound) {
    this.soundSystem.rewind(sound.getPath());
  }

  @Override
  public void playSound(final Sound sound) {
    if (sound == null) {
      return;
    }

    if (this.isPlaying(sound.getName())) {
      return;
    }

    this.soundSystem.quickPlay(false, sound.getUrl(), sound.getName(), false, (float) this.getListenerPosition().getX(), (float) this.getListenerPosition().getY(), 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
  }

  @Override
  public void playSound(final IEntity entity, final Sound sound) {
    if (entity == null || sound == null || !this.canPlay(entity)) {
      return;
    }

    if (this.soundSystem.getMasterVolume() == 0) {
      return;
    }

    final String uniqueIdentifier = getIdentifier(entity, sound);
    if (this.isPlaying(uniqueIdentifier)) {
      return;
    }

    // center sounds that are roughly played at the same location

    float locationX = (float) this.getListenerPosition().getX();
    float locationY = (float) this.getListenerPosition().getY();
    if (this.getMaxListenerRadius() != 0 && entity.getDimensionCenter().distance(this.getListenerPosition()) > this.getMaxListenerRadius()) {
      locationX = 10000;
      locationY = 10000;
    } else if (entity.getLocation().distance(this.getListenerPosition()) > 5) {
      locationX = (float) entity.getDimensionCenter().getX();
      locationY = (float) entity.getDimensionCenter().getY();
    }

    final String name = this.soundSystem.quickPlay(false, sound.getUrl(), uniqueIdentifier, false, locationX, locationY, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
    this.add(new Playback(entity, name));
  }

  @Override
  public void playSound(final Point2D location, final Sound sound) {
    if (location == null || sound == null || !this.canPlay(location)) {
      return;
    }

    if (this.soundSystem.getMasterVolume() == 0) {
      return;
    }

    final String uniqueIdentifier = getIdentifier(location, sound);
    if (this.isPlaying(uniqueIdentifier)) {
      return;
    }

    float locationX = (int) this.getListenerPosition().getX();
    float locationY = (int) this.getListenerPosition().getY();
    if (location.distance(this.getListenerPosition()) > 50) {
      locationX = (float) location.getX();
      locationY = (float) location.getY();
    }

    this.soundSystem.quickPlay(false, sound.getUrl(), uniqueIdentifier, false, locationX, locationY, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
  }

  @Override
  public void terminate() {
    super.terminate();
    this.soundSystem.cleanup();
  }

  @Override
  public boolean isPlaying(final String identifier) {
    return this.soundSystem.playing(identifier);
  }

  @Override
  public void updatePosition(final String identifier, final Point2D location) {
    this.soundSystem.setPosition(identifier, (float) location.getX(), (float) location.getY(), 0);
  }

  @Override
  public void updateListenerPosition(final Point2D location) {
    this.soundSystem.setListenerPosition((float) location.getX(), (float) location.getY(), 0);
  }
}
