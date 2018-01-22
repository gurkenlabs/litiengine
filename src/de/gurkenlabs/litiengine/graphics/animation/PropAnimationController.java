package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

public class PropAnimationController extends AnimationController {
  private static final String DAMAGED = "damaged";
  private static final String DESTROYED = "destroyed";
  private static final String INTACT = "intact";

  public static Animation createAnimation(final Prop prop, final PropState state) {
    final Spritesheet spritesheet = findSpriteSheet(prop, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true, Spritesheet.getCustomKeyFrameDurations(spritesheet.getName()));
  }

  private static Spritesheet findSpriteSheet(final Prop prop, final PropState state) {
    if (prop == null || prop.getSpritesheetName() == null || prop.getSpritesheetName().isEmpty()) {
      return null;
    }

    final String propState = state.name().toLowerCase();
    final String name = Prop.SPRITESHEET_PREFIX + prop.getSpritesheetName().toLowerCase() + "-" + propState;
    Spritesheet sprite = Spritesheet.find(name);

    if (sprite != null) {
      return sprite;
    }

    final String fallbackName = Prop.SPRITESHEET_PREFIX + prop.getSpritesheetName().toLowerCase();
    return Spritesheet.find(fallbackName);
  }

  private final Prop prop;

  public PropAnimationController(final IEntity prop) {
    super(createAnimation((Prop) prop, PropState.INTACT));
    this.prop = (Prop) prop;
    this.add(createAnimation(this.prop, PropState.DAMAGED));
    this.add(createAnimation(this.prop, PropState.DESTROYED));
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
    cacheKey += "_" + this.prop.isAddShadow();
    cacheKey += "_" + this.prop.getState();
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    final BufferedImage currentImage = super.getCurrentSprite();
    if (currentImage == null || !prop.isAddShadow()) {
      return currentImage;
    }

    // add a shadow at the lower end of the current sprite.
    final int ShadowYOffset = currentImage.getHeight();
    final BufferedImage shadow = ImageProcessing.addShadow(currentImage, 0, ShadowYOffset);
    ImageCache.SPRITES.put(cacheKey, shadow);
    return shadow;
  }

  @Override
  public void update() {
    super.update();
    switch (this.prop.getState()) {
    case DAMAGED:
      this.playAnimation(DAMAGED);
      break;
    case DESTROYED:
      this.playAnimation(DESTROYED);
      break;
    case INTACT:
    default:
      this.playAnimation(INTACT);
      break;
    }
  }
}