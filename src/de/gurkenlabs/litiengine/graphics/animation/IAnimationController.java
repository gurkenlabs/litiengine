package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.IImageEffect;

public interface IAnimationController extends IUpdateable {
  public void addListener(AnimationListener listener);

  public void removeListener(AnimationListener listener);

  public void add(Animation animation);

  public void add(IImageEffect effect);

  public Collection<Animation> getAnimations();

  public Animation getAnimation(String animationName);

  public Animation getCurrentAnimation();

  public BufferedImage getCurrentSprite();

  public BufferedImage getCurrentSprite(int width, int height);

  public AffineTransform getAffineTransform();

  public Animation getDefaultAnimation();

  public List<IImageEffect> getImageEffects();

  public boolean hasAnimation(String animationName);

  public boolean isPlaying(String animationName);

  public void playAnimation(final String animationName);

  public void setDefaultAnimation(Animation defaultAnimation);

  public void setAffineTransform(AffineTransform affineTransform);

  public void remove(Animation animation);

  public void remove(IImageEffect effect);
}
