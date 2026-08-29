package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import java.awt.image.BufferedImage;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

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

  @Test
  void directionalWalkSpritesParticipateInFlipFallback() {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xffffffff);
    new Spritesheet(image, "flip-walk-right.png", 1, 1);
    Creature creature = new TestCreature("flip");
    creature.setAngle(Direction.LEFT.toAngle());
    CreatureAnimationController<Creature> controller = new CreatureAnimationController<>(creature, true);

    assertTrue(controller.hasAnimation("flip-walk-left"));
  }

  @Test
  @ResourceLock("default-locale")
  void directionalWalkSpritesAreLocaleIndependent() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "locale-walk-right.png", 1, 1);
      Creature creature = new TestCreature("locale");
      creature.setAngle(Direction.RIGHT.toAngle());

      CreatureAnimationController<Creature> controller = new CreatureAnimationController<>(creature, false);

      assertTrue(controller.hasAnimation("locale-walk-right"));
      controller.update();
      assertEquals("locale-walk-right", controller.getCurrent().getName());
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  @ResourceLock("default-locale")
  void standardDirectionalSpriteNamesAreLocaleIndependent() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      Creature creature = new TestCreature("knight");

      assertEquals("knight-idle-right", CreatureAnimationController.getSpriteName(
          creature, CreatureAnimationState.IDLE, Direction.RIGHT));
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  void fallbackWalkAnimationMatchesFacingDirection() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("fallback-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    de.gurkenlabs.litiengine.environment.Environment env = new de.gurkenlabs.litiengine.environment.Environment(map);
    Game.world().loadEnvironment(env);

    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xffffffff);
    new Spritesheet(image, "zombiewalter-walk-left.png", 1, 1);
    Creature creature = new Creature("zombiewalter");
    creature.setFacingDirection(Direction.RIGHT);
    env.add(creature);
    creature.animations().update();

    assertEquals("zombiewalter-walk-right", creature.animations().getCurrent().getName());
  }

  private static class TestCreature extends Creature {
    TestCreature(String spritesheetName) {
      super(spritesheetName);
    }

    @Override
    protected IEntityAnimationController<? extends Creature> createAnimationController() {
      return new CreatureAnimationController<>(this, false);
    }

    @Override
    public boolean isIdle() {
      return false;
    }
  }
}
