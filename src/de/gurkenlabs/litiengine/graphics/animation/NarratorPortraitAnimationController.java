package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.environment.Narrator;
import de.gurkenlabs.litiengine.environment.Narrator.Emotion;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.image.ImageProcessing;

public class NarratorPortraitAnimationController extends AnimationController {
  private final Narrator narrator;

  public NarratorPortraitAnimationController(final Narrator narrator) {
    super(createAnimation(narrator, Emotion.NORMAL));
    this.narrator = narrator;
    for (Emotion e : Emotion.values()) {
      switch (e) {
      case ANGRY:
        this.add(createAnimation(this.narrator, e));
        break;
      case BORED:
        break;
      case HAPPY:
        break;
      case SAD:
        break;
      case SILENT:
        this.add(createAnimation(this.narrator, e, 500, 120));
        break;
      case SURPRISED:
        break;
      default:
        break;

      }
    }
  }

  public static Animation createAnimation(final Narrator narrator, final Emotion emotion, int... keyFrameDurations) {
    final Spritesheet spritesheet = findSpriteSheet(narrator, emotion);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(emotion.name(), spritesheet, true, false, keyFrameDurations);
  }

  public static Animation createAnimation(final Narrator narrator, final Emotion emotion) {
    final Spritesheet spritesheet = findSpriteSheet(narrator, emotion);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(emotion.name(), spritesheet, true, false);
  }

  private static Spritesheet findSpriteSheet(final Narrator narrator, final Emotion emotion) {
    if (narrator == null || narrator.getName().isEmpty() || narrator.getName() == null || emotion == null) {
      return null;
    }

    String emotionString = emotion.name().toLowerCase();
    final String name = "sprites/narrator-" + narrator.getName().toLowerCase() + "-" + emotionString + ".png";
    final Spritesheet sheet = Spritesheet.find(name);
    return sheet;
  }

  @Override
  public BufferedImage getCurrentSprite() {
    // get shadow from the cache or draw it dynamically and add it to the
    // cache
    // get complete image from the cache
    final Animation animation = this.getCurrentAnimation();
    if (animation == null || animation.getSpritesheet() == null) {
      return null;
    }

    String cacheKey = this.buildCurrentCacheKey();
    cacheKey += "_";
    cacheKey += "_" + this.narrator.getEmotion();
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    BufferedImage currentImage = super.getCurrentSprite();
    if (currentImage == null) {
      return null;
    }

    // add a shadow at the lower end of the current sprite.
    final int ShadowYOffset = (int) (currentImage.getHeight());
    final BufferedImage shadow = ImageProcessing.addShadow(currentImage, 0, ShadowYOffset);
    return ImageCache.SPRITES.putPersistent(cacheKey, shadow);
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    this.playAnimation(this.narrator.getEmotion().name());
  }

}
