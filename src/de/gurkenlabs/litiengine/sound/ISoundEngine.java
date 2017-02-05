package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.function.Predicate;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.entities.IEntity;

public interface ISoundEngine extends ILaunchable {

  public void init(final float soundVolume);

  public void load(Sound sound);

  public void playMusic(Sound s);

  public void playSound(IEntity entity, Sound s);

  public void playSound(Point2D location, Sound s);

  public void playSound(Sound s);

  public void rewind(Sound s);

  public void setMaxDistance(float distance);

  public float getMaxDistance();

  public void stopMusic(Sound s);
}
