package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.image.ImageProcessing;

public class PropAnimationController extends AnimationController {
  private static final String INTACT = "intact";
  private static final String DAMAGED = "damaged";
  private static final String DESTROYED = "destroyed";

  private final Prop prop;

  public PropAnimationController(final IEntity prop) {
    super(createAnimation((Prop) prop, PropState.INTACT));
    this.prop = (Prop) prop;
    this.add(createAnimation(this.prop, PropState.DAMAGED));
    this.add(createAnimation(this.prop, PropState.DESTROYED));
  }

  public static Animation createAnimation(final Prop prop, final PropState state) {
    final Spritesheet spritesheet = findSpriteSheet(prop, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true);
  }

  private static Spritesheet findSpriteSheet(final Prop prop, final PropState state) {
    if (prop == null || prop.getSpritePath() == null || prop.getSpritePath().isEmpty()) {
      return null;
    }

    String propState = state.name().toLowerCase();
    final String name = "prop-" + prop.getSpritePath().toLowerCase() + "-" + propState + ".png";
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
    cacheKey += "_" + this.prop.getState();
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