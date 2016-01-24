package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.List;

import de.gurkenlabs.litiengine.graphics.IImageEffect;

public interface IAnimationController {
  public List<IImageEffect> getImageEffects();
  
  public void add(IImageEffect effect);
  
  public void add(Animation animation);

  public List<Animation> getAnimations();

  public Animation getCurrentAnimation();

  public BufferedImage getCurrentSprite();

  public void playAnimation(final String animationName);

  public void updateAnimation();
}
