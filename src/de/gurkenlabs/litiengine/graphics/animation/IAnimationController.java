package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.List;

import de.gurkenlabs.litiengine.graphics.IImageEffect;

public interface IAnimationController {
  public void add(Animation animation);

  public void add(IImageEffect effect);

  public List<Animation> getAnimations();

  public Animation getCurrentAnimation();

  public BufferedImage getCurrentSprite();

  public List<IImageEffect> getImageEffects();

  public void playAnimation(final String animationName);

  public void updateAnimation();
}
