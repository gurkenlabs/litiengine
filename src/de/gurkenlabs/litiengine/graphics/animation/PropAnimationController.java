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
    super(createAnimation((Prop) prop, PropState.INTACT));
    this.prop = (Prop) prop;
  }
  
  public static Animation createAnimation(final Prop prop, final PropState state) {
    final Spritesheet spritesheet = findSpriteSheet(prop, state);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(state.name(), spritesheet, true, true);
  }

  private static Spritesheet findSpriteSheet(final Prop prop, final PropState state) {
    String propState = "";
    if (!prop.isIndestructible()) {
      propState = state.name().toLowerCase();
    }
    final String name = "prop-" + prop.getSpritePath().toLowerCase() + "-" + propState + ".png";
    final Spritesheet sheet = Spritesheet.findByName(name);
    return sheet;
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    switch (this.prop.getState()) {
    case INTACT:
      this.playAnimation(INTACT);
      break;
    case DAMAGED:
      this.playAnimation(DAMAGED);
      break;
    case DESTROYED:
      this.playAnimation(DESTROYED);
      break;
    default:
      this.playAnimation(INTACT);
      break;
    }

  }

}