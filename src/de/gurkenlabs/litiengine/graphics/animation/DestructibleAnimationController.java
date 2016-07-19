package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Destructible;
import de.gurkenlabs.litiengine.entities.DestructibleState;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;

public class DestructibleAnimationController extends AnimationController {
  private static final String INTACT = "intact";
  private static final String DAMAGED = "damaged";
  private static final String DESTROYED = "destroyed";
  private final Destructible dest;

  public DestructibleAnimationController(final IEntity dest) {
    super(createAnimation((Destructible) dest, DestructibleState.Intact), createAnimation((Destructible) dest, DestructibleState.Damaged), createAnimation((Destructible) dest, DestructibleState.Destroyed));
    this.dest = (Destructible) dest;
  }

  private static Animation createAnimation(final Destructible dest, DestructibleState state) {
    final Spritesheet spritesheet = findSpriteSheet(dest, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true);
  }

  @Override
  public void update(IGameLoop loop) {
    super.update(loop);
    switch (this.dest.getState()) {
    case Intact:
      this.playAnimation(INTACT);
      break;
    case Damaged:
      this.playAnimation(DAMAGED);
      break;
    case Destroyed:
      this.playAnimation(DESTROYED);
      break;
    default:
      this.playAnimation(INTACT);
      break;
    }

  }

  private static Spritesheet findSpriteSheet(final Destructible dest, DestructibleState state) {
    final String path = Game.getInfo().spritesDirectory() + "dest-" + dest.getSpritePath().toLowerCase() + "-" + state.name().toLowerCase() + ".png";
    final Spritesheet sheet = Spritesheet.find(path);
    return sheet;
  }

}