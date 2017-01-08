package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class DecorMobAnimationController extends AnimationController {
  public static Animation createAnimation(final DecorMob mob) {
    final Spritesheet spritesheet = findSpriteSheet(mob);
    if (spritesheet == null) {
      return null;
    }

    return new Animation(mob.getMobType(), spritesheet, true, true);
  }

  private static Spritesheet findSpriteSheet(final DecorMob mob) {
    if(mob.getMobType() == null){
      return null;
    }
    
    final String path = Game.getInfo().getSpritesDirectory() + "decormob-" + mob.getMobType().toLowerCase() + ".png";
    final Spritesheet sheet = Spritesheet.find(path);
    return sheet;
  }

  private final DecorMob mob;

  public DecorMobAnimationController(final IEntity prop) {
    super(createAnimation((DecorMob) prop));
    this.mob = (DecorMob) prop;
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    this.playAnimation(this.mob.getMobType());
  }

}