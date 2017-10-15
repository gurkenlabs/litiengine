package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.IImageEffect;

public interface IAnimationController extends IUpdateable {
  public void add(Animation animation);

  public void add(IImageEffect effect);

  public void dispose();

  public List<Animation> getAnimations();

  public Animation getAnimation(String animationName);

  public Animation getCurrentAnimation();

  public BufferedImage getCurrentSprite();

  public BufferedImage getCurrentSprite(int width, int height);

  public Animation getDefaultAnimation();

  public List<IImageEffect> getImageEffects();

  public void onPlayback(Consumer<Animation> cons);

  public void onPlaybackEnded(Consumer<Animation> cons);

  public void playAnimation(final String animationName);
}
