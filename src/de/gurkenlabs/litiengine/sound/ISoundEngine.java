package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.function.Predicate;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.entities.IEntity;

public interface ISoundEngine extends ILaunchable {
  public void init(float soundVolume);

  public void setGain(float volume);

  public float getGain();

  public void playMusic(Sound s);

  public void playSound(Sound s);

  public void rewind(Sound s);

  public void stopMusic(Sound s);

  public void playSound(IEntity entity, Sound s);

  public void playSound(Point2D location, Sound s);

  public boolean isPlaying(String identifier);

  public void updatePosition(String identifier, Point2D location);

  public void updateListenerPosition(Point2D location);

  public void addEntityPlayCondition(Predicate<IEntity> predicate);

  public void addPlayCondition(Predicate<Point2D> predicate);

  public void setMaxRadius(float radius);
}
