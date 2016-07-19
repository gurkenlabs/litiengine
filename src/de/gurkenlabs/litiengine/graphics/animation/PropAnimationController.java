package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;

public class PropAnimationController extends AnimationController {

  public PropAnimationController(final IEntity prop) {
    super(createAnimation(prop, "DEFAULT"));
  }

  private static Animation createAnimation(final IEntity prop, final String name) {
    final Spritesheet spritesheet = findSpriteSheet(prop);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(name, spritesheet, true, true);
  }

  private static Spritesheet findSpriteSheet(final IEntity prop) {
    final String path = "sprites/prop-" + prop.getClass().getSimpleName().toLowerCase() + ".png";
    final Spritesheet sheet = Spritesheet.find(path);
    return sheet;
  }
}
