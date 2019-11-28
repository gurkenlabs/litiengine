package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.ImageEffect;

public interface IAnimationController extends IUpdateable {
  public void addListener(AnimationListener listener);

  public void removeListener(AnimationListener listener);

  public void add(Animation animation);

  public void add(ImageEffect effect);
  
  /**
   * Remove all <code>Animation</code>s from the <code>AnimationController</code>.
   */
  public void clear();

  public Collection<Animation> getAll();

  public Animation get(String animationName);

  public Animation getCurrent();
  
  public Animation getDefault();

  public BufferedImage getCurrentSprite();

  public BufferedImage getCurrentSprite(int width, int height);

  public AffineTransform getAffineTransform();

  public List<ImageEffect> getImageEffects();

  public boolean hasAnimation(String animationName);

  public boolean isPlaying(String animationName);

  public void play(final String animationName);

  public void setDefault(Animation defaultAnimation);

  public void setAffineTransform(AffineTransform affineTransform);

  public void remove(Animation animation);

  public void remove(ImageEffect effect);
  
  public boolean isEnabled();

  public void setEnabled(boolean enabled);
}
