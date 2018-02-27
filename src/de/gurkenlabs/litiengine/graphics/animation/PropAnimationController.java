package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class PropAnimationController extends EntityAnimationController<Prop> {
  private static final String DAMAGED = "damaged";
  private static final String DESTROYED = "destroyed";
  private static final String INTACT = "intact";

  public PropAnimationController(final Prop prop) {
    super(prop);
    this.setDefaultAnimation(this.createAnimation(this.getEntity(), PropState.INTACT));
    this.add(createAnimation(this.getEntity(), PropState.DAMAGED));
    this.add(createAnimation(this.getEntity(), PropState.DESTROYED));
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
    cacheKey += "_" + this.getEntity().isAddShadow();
    cacheKey += "_" + this.getEntity().getState();
    cacheKey += "_" + this.getEntity().getSpriteRotation();
    cacheKey += "_" + this.getEntity().flipHorizontally();
    cacheKey += "_" + this.getEntity().flipVertically();
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    BufferedImage currentImage = super.getCurrentSprite();
    if (currentImage == null) {
      return null;
    }

    if (this.getEntity().getSpriteRotation() != Rotation.NONE) {
      currentImage = ImageProcessing.rotate(currentImage, this.getEntity().getSpriteRotation());
    }

    if (this.getEntity().flipHorizontally()) {
      currentImage = ImageProcessing.horizontalFlip(currentImage);
    }

    if (this.getEntity().flipVertically()) {
      currentImage = ImageProcessing.verticalFlip(currentImage);
    }

    if (!this.getEntity().isAddShadow()) {
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
    switch (this.getEntity().getState()) {
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

  private Animation createAnimation(final Prop prop, final PropState state) {
    final Spritesheet spritesheet = this.findSpriteSheet(prop, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true, Spritesheet.getCustomKeyFrameDurations(spritesheet.getName()));
  }

  private Spritesheet findSpriteSheet(final Prop prop, final PropState state) {
    if (prop == null || prop.getSpritesheetName() == null || prop.getSpritesheetName().isEmpty()) {
      return null;
    }

    final String propState = state.name().toLowerCase();
    final String name = this.getSpritePrefix() + prop.getSpritesheetName().toLowerCase() + "-" + propState;
    Spritesheet sprite = Spritesheet.find(name);

    if (sprite != null) {
      return sprite;
    }

    final String fallbackName = this.getSpritePrefix() + prop.getSpritesheetName().toLowerCase();
    return Spritesheet.find(fallbackName);
  }
}