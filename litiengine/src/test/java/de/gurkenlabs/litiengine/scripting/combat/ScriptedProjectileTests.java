package de.gurkenlabs.litiengine.scripting.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptedProjectileTests {
  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void testScriptedProjectileSpawnsAndHitsTarget() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(500, 500));
    when(map.getSizeInTiles()).thenReturn(new Dimension(50, 50));
    Environment environment = new Environment(map);
    environment.init();

    Creature shooter = new Creature();
    shooter.setLocation(0, 0);
    environment.add(shooter);

    Creature target = new Creature();
    target.setLocation(50, 0);
    target.setSize(16, 16);
    target.getHitPoints().setMax(100);
    target.getHitPoints().setValue(100);
    environment.add(target);

    Creature splashVictim = new Creature();
    splashVictim.setLocation(50, 20);
    splashVictim.setSize(16, 16);
    splashVictim.getHitPoints().setMax(100);
    splashVictim.getHitPoints().setValue(100);
    environment.add(splashVictim);

    Game.world().loadEnvironment(environment);
    environment.load();

    AtomicBoolean hitOccurred = new AtomicBoolean(false);
    AtomicBoolean expired = new AtomicBoolean(false);

    ScriptedProjectile projectile = new ScriptedProjectileBuilder(environment)
        .from(0, 0)
        .towards(100, 0)
        .speed(500)
        .damage(25)
        .splash(40, 15)
        .size(16, 16)
        .collisionBox(16, 16)
        .source(shooter)
        .onHitEntity((hitEntity, proj) -> hitOccurred.set(true))
        .onExpire(p -> expired.set(true))
        .spawn();

    assertNotNull(projectile);
    assertEquals(500, projectile.getSpeed());
    assertEquals(25, projectile.getDamage());
    assertEquals(40, projectile.getSplashRadius());
    assertEquals(15, projectile.getSplashDamage());

    // Update projectile until it moves into target
    for (int i = 0; i < 10; i++) {
      projectile.update();
    }

    assertTrue(hitOccurred.get());
    assertEquals(75, target.getHitPoints().getModifiedValue());
    assertEquals(85, splashVictim.getHitPoints().getModifiedValue());
  }
}
