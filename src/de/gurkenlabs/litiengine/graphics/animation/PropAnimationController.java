package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class PropAnimationController extends AnimationController {
  private static final String INTACT = "intact";
  private static final String DAMAGED = "damaged";
  private static final String DESTROYED = "destroyed";
  private final Prop prop;

  public PropAnimationController(final IEntity prop) {
    super(createAnimation((Prop) prop, PropState.Intact));
    this.prop = (Prop) prop;
  }

  public static Animation createAnimation(final Prop prop, PropState state) {
    final Spritesheet spritesheet = findSpriteSheet(prop, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true);
  }

  @Override
  public void update(IGameLoop loop) {
    super.update(loop);
    switch (this.prop.getState()) {
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

  private static Spritesheet findSpriteSheet(final Prop prop, PropState state) {
    String propState = "";
    if(!prop.isIndestructible()){
      propState = state.name().toLowerCase();
    }
    final String path = Game.getInfo().spritesDirectory() + "prop-" + prop.getSpritePath().toLowerCase() + "-" + propState + ".png";
    final Spritesheet sheet = Spritesheet.find(path);
    return sheet;
  }

}