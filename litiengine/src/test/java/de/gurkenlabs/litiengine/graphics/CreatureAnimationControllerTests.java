package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreatureAnimationControllerTests {

  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterEach
  void terminateGame() {
    GameTest.terminateGame();
  }

  @Test
  void walkSpritesAreUsedAsMoveAnimations() {
    new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "zombie11-dead-right.png", 1, 1);
    new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "zombie11-walk-right.png", 1, 1);
    Creature creature = new TestCreature("zombie11");
    creature.setAngle(Direction.RIGHT.toAngle());
    CreatureAnimationController<Creature> controller = new CreatureAnimationController<>(creature, false);

    assertTrue(controller.hasAnimation("zombie11-walk-right"));
    controller.update();

    assertEquals("zombie11-walk-right", controller.getCurrent().getName());
  }

  private static class TestCreature extends Creature {
    TestCreature(String spritesheetName) {
      super(spritesheetName);
    }

    @Override
    protected IEntityAnimationController<? extends Creature> createAnimationController() {
      return new CreatureAnimationController<>(this, false);
    }
  }
}
