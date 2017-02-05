package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.entities.IEntity;

public interface ISoundEngine extends ILaunchable {

  public void init(final float soundVolume);

  public void playMusic(Sound s);

  public void stopMusic(Sound s);

  public void playSound(IEntity entity, Sound s);

  public void playSound(Point2D location, Sound s);

  public void playSound(Sound s);

  public void setMaxDistance(float distance);

  public float getMaxDistance();
}
