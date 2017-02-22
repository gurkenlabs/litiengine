package de.gurkenlabs.litiengine.graphics.animation;

import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.Direction;
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

  private static Animation[] createWalkAnimations(final DecorMob mob) {
    final List<Animation> anims = new ArrayList<>();
    final Spritesheet walkUp = Spritesheet.find("decormob-" + mob.getMobType().toLowerCase() + "-walk-up.png");
    if (walkUp != null) {
      anims.add(new Animation(walkUp, true));
    }

    final Spritesheet walkDown = Spritesheet.find("decormob-" + mob.getMobType().toLowerCase() + "-walk-down.png");
    if (walkDown != null) {
      anims.add(new Animation(walkDown, true));
    }

    final Spritesheet walkLeft = Spritesheet.find("decormob-" + mob.getMobType().toLowerCase() + "-walk-left.png");
    if (walkLeft != null) {
      anims.add(new Animation(walkLeft, true));
    }

    final Spritesheet walkRight = Spritesheet.find("decormob-" + mob.getMobType().toLowerCase() + "-walk-right.png");
    if (walkRight != null) {
      anims.add(new Animation(walkRight, true));
    }

    final Spritesheet dead = Spritesheet.find("decormob-" + mob.getMobType().toLowerCase() + "-dead.png");
    if (dead != null) {
      anims.add(new Animation(dead, true));
    }

    final Animation[] animArr = new Animation[anims.size()];
    anims.toArray(animArr);
    return animArr;
  }

  private static Spritesheet findSpriteSheet(final DecorMob mob) {
    if (mob.getMobType() == null) {
      return null;
    }

    final String path = "decormob-" + mob.getMobType().toLowerCase() + ".png";
    final Spritesheet sheet = Spritesheet.find(path);
    return sheet;
  }

  private final DecorMob mob;

  public DecorMobAnimationController(final IEntity mob) {
    super(createAnimation((DecorMob) mob), createWalkAnimations((DecorMob) mob));
    this.mob = (DecorMob) mob;
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    if (this.mob.isDead()) {
      final String deadAnim = "decormob-" + this.mob.getMobType().toLowerCase() + "-dead";
      if (this.getAnimations().stream().anyMatch(x -> x != null && x.getName().equals(deadAnim))) {
        this.playAnimation(deadAnim);
        return;
      }
    }

    final String animName = "decormob-" + this.mob.getMobType().toLowerCase() + "-walk-" + Direction.fromAngle(this.mob.getAngle()).toString().toLowerCase();
    if (this.getAnimations().stream().anyMatch(x -> x != null && x.getName().equals(animName))) {
      this.playAnimation(animName);
      return;
    }

    this.playAnimation(this.mob.getMobType());
  }

}