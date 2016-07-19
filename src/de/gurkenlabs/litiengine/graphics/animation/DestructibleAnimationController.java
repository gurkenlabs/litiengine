package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.Destructible;
import de.gurkenlabs.litiengine.entities.DestructibleState;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;

public class DestructibleAnimationController extends AnimationController {
  public DestructibleAnimationController(final IEntity dest) {
    super(createAnimation((Destructible) dest, DestructibleState.Intact), createAnimation((Destructible) dest, DestructibleState.Damaged), createAnimation((Destructible) dest, DestructibleState.Destroyed));
  }

  private static Animation createAnimation(final Destructible dest, DestructibleState state) {
    final Spritesheet spritesheet = findSpriteSheet(dest, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true);
  }

  private static Spritesheet findSpriteSheet(final Destructible dest, DestructibleState state) {
    final String path = "sprites/dest" + "-" + dest.getClass().getSimpleName().toLowerCase() + "-" + state.name().toLowerCase() + ".png";
    final Spritesheet sheet = Spritesheet.find(path);
    return sheet;
  }

}