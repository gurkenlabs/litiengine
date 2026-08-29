package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptSpawningTests {
  private Environment environment;

  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(500, 500));
    when(map.getSizeInTiles()).thenReturn(new Dimension(50, 50));
    this.environment = new Environment(map);
    this.environment.init();
    Game.world().loadEnvironment(this.environment);
    this.environment.load();
  }

  @AfterEach
  void cleanUp() {
    if (this.environment != null) {
      Game.scripts().detach(this.environment);
    }
    Game.scripts().clearDiagnostics();
  }

  @Test
  void testScriptContextSpawningConveniences() {
    ScriptDefinition definition = new ScriptDefinition("spawn-test", "java", null, SpawnerEnvironmentScript.class.getName(), ScriptHostType.ENVIRONMENT);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("spawn-test");

    SpawnerEnvironmentScript script = (SpawnerEnvironmentScript) Game.scripts().attach(this.environment, binding);
    assertNotNull(script);

    // Test spawnCreature
    Creature creature = script.spawnCreature("hero", 50, 60);
    assertNotNull(creature);
    assertEquals(50, creature.getX());
    assertEquals(60, creature.getY());
    assertTrue(this.environment.getEntities().contains(creature));

    // Test spawnProp
    Prop prop = script.spawnProp("chest", new Point2D.Double(100, 110));
    assertNotNull(prop);
    assertEquals(100, prop.getX());
    assertEquals(110, prop.getY());
    assertTrue(this.environment.getEntities().contains(prop));

    // Test generic spawn
    CustomTestEntity custom = script.spawn(CustomTestEntity.class, 200, 220);
    assertNotNull(custom);
    assertEquals(200, custom.getX());
    assertEquals(220, custom.getY());
    assertTrue(this.environment.getEntities().contains(custom));

    // Test fluent spawner
    Creature boss = script.spawner()
      .creature("dragon")
      .at(300, 350)
      .withName("Smaug")
      .withTags("boss", "fire")
      .withHealth(500)
      .spawn();

    assertNotNull(boss);
    assertEquals("Smaug", boss.getName());
    assertTrue(boss.hasTag("boss"));
    assertTrue(boss.hasTag("fire"));
    assertEquals(500, boss.getHitPoints().getMax());
    assertEquals(500, boss.getHitPoints().getModifiedValue());
    assertEquals(300, boss.getX());
    assertEquals(350, boss.getY());
    assertTrue(this.environment.getEntities().contains(boss));
  }

  public static class CustomTestEntity extends Entity {
    public CustomTestEntity() {
      super();
    }
  }

  public static class SpawnerEnvironmentScript extends EnvironmentScript {}
}
