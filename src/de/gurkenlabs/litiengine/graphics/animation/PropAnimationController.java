package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class PropAnimationController<T extends Prop> extends EntityAnimationController<T> {
  public static final String PROP_IDENTIFIER = "prop-";

  /**
   * Initializes a new instance of the {@code PropAnimationController} class.
   *
   * @param prop The prop related to this controller.
   */
  public PropAnimationController(final T prop) {
    super(prop);

    this.setDefault(createAnimation(this.getEntity(), PropState.INTACT));
    this.add(createAnimation(this.getEntity(), PropState.DAMAGED));
    this.add(createAnimation(this.getEntity(), PropState.DESTROYED));
  }

  /**
   * Gets the sprite name for the specified prop and state.
   *
   * @param prop The prop to retrieve the sprite name for.
   * @param appendState A flag indicating whether the state should be appended to the name.
   * @return A string representing the sprite name for the specified prop in its state.
   * @see Prop#getSpritesheetName()
   * @see Prop#getState()
   */
  public static String getSpriteName(final Prop prop, boolean appendState) {
    return getSpriteName(prop, prop.getState(), appendState);
  }

  /**
   * Gets the sprite name for the specified prop and state.
   *
   * @param prop The prop to retrieve the sprite name for.
   * @param state The state of the prop.
   * @param appendState A flag indicating whether the state should be appended to the name.
   * @return A string representing the sprite name for the specified prop in its state.
   * @see Prop#getSpritesheetName()
   * @see Prop#getState()
   */
  public static String getSpriteName(final Prop prop, PropState state, boolean appendState) {
    StringBuilder sb = new StringBuilder(PROP_IDENTIFIER);
    sb.append(prop.getSpritesheetName());
    if (appendState) {
      sb.append("-");
      sb.append(state.spriteString());
    }

    return sb.toString();
  }

  @Override
  public BufferedImage getCurrentImage() {
    final Animation animation = this.getCurrent();
    if (animation == null || animation.getSpritesheet() == null) {
      return null;
    }

    String cacheKey = this.buildCurrentCacheKey();
    cacheKey += "_" + this.getEntity().isAddShadow();
    cacheKey += "_" + this.getEntity().getState();
    cacheKey += "_" + this.getEntity().getSpriteRotation();
    cacheKey += "_" + this.getEntity().flipHorizontally();
    cacheKey += "_" + this.getEntity().flipVertically();
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    BufferedImage currentImage = super.getCurrentImage();
    if (currentImage == null) {
      return null;
    }

    if (this.getEntity().getSpriteRotation() != Rotation.NONE) {
      currentImage = Imaging.rotate(currentImage, this.getEntity().getSpriteRotation());
    }

    if (this.getEntity().flipHorizontally()) {
      currentImage = Imaging.horizontalFlip(currentImage);
    }

    if (this.getEntity().flipVertically()) {
      currentImage = Imaging.verticalFlip(currentImage);
    }

    if (!this.getEntity().isAddShadow()) {
      return currentImage;
    }

    // add a shadow at the lower end of the current sprite.
    final int ShadowYOffset = currentImage.getHeight();
    final BufferedImage shadow = Imaging.addShadow(currentImage, 0, ShadowYOffset);
    Resources.images().add(cacheKey, shadow);

    return shadow;
  }

  @Override
  public void update() {
    super.update();
    this.play(this.getEntity().getState().spriteString());
  }

  @Override
  public boolean isAutoScaling() {
    return this.getEntity().isScaling();
  }

  private static Animation createAnimation(final Prop prop, final PropState state) {
    final Spritesheet spritesheet = findSpriteSheet(prop, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(
        state.spriteString(),
        spritesheet,
        true,
        true,
        Resources.spritesheets().getCustomKeyFrameDurations(spritesheet.getName()));
  }

  private static Spritesheet findSpriteSheet(final Prop prop, final PropState state) {
    if (prop == null || prop.getSpritesheetName() == null || prop.getSpritesheetName().isEmpty()) {
      return null;
    }

    Spritesheet opt = Resources.spritesheets().get(getSpriteName(prop, state, true));

    if (opt != null) {
      return opt;
    }

    return Resources.spritesheets().get(getSpriteName(prop, state, false));
  }
}
