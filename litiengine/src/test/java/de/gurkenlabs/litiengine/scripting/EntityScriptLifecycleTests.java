package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.entities.EntityHitEvent;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.physics.CollisionEvent;
import java.awt.Dimension;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntityScriptLifecycleTests {
  private TestCombatEntity host;

  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterEach
  void cleanUp() {
    if (this.host != null) {
      Game.scripts().detach(this.host);
    }
    Game.scripts().clearDiagnostics();
    LifecycleTestScript.reset();
  }

  @Test
  void testHitDeathAndCollisionLifecycleHooks() {
    ScriptDefinition definition = new ScriptDefinition("lifecycle-test", "java", null, LifecycleTestScript.class.getName(), ScriptHostType.ENTITY);
    Game.scripts().setDefinitions(List.of(definition));
    ScriptBinding binding = new ScriptBinding("lifecycle-test");

    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    Environment environment = new Environment(map);
    environment.init();

    this.host = new TestCombatEntity();
    this.host.getHitPoints().setMax(100);
    this.host.getHitPoints().setValue(100);
    environment.add(this.host);
    Game.world().loadEnvironment(environment);
    environment.load();

    ScriptInstance instance = Game.scripts().attach(this.host, binding);
    assertNotNull(instance);

    // Trigger hit
    this.host.hit(30);
    assertEquals(1, LifecycleTestScript.hits);
    assertEquals(30, LifecycleTestScript.lastDamage);

    // Trigger collision event
    this.host.fireCollisionEvent(new CollisionEvent(this.host, new de.gurkenlabs.litiengine.entities.ICollisionEntity[0]));
    assertEquals(1, LifecycleTestScript.collisions);

    // Trigger death
    this.host.die();
    assertEquals(1, LifecycleTestScript.deaths);

    // Trigger message
    this.host.sendMessage(this, "alert");
    assertEquals(1, LifecycleTestScript.messages);
    assertEquals("alert", LifecycleTestScript.lastMessage);

    // Test removal
    ((LifecycleTestScript) instance).remove();
    assertTrue(environment.getEntities().stream().noneMatch(e -> e == this.host));

    // Detach and ensure unhooked
    Game.scripts().detach(this.host);
    this.host.hit(10);
    assertEquals(1, LifecycleTestScript.hits);
  }

  public static class TestCombatEntity extends CombatEntity {}

  public static class LifecycleTestScript extends EntityScript<TestCombatEntity> {
    static int hits = 0;
    static int deaths = 0;
    static int collisions = 0;
    static int messages = 0;
    static int lastDamage = 0;
    static String lastMessage = null;

    static void reset() {
      hits = 0;
      deaths = 0;
      collisions = 0;
      messages = 0;
      lastDamage = 0;
      lastMessage = null;
    }

    @Override
    protected void onHit(EntityHitEvent event) {
      hits++;
      lastDamage = event.getDamage();
    }

    @Override
    protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {
      deaths++;
    }

    @Override
    protected void onCollision(CollisionEvent event) {
      collisions++;
    }

    @Override
    protected void onMessage(String message, Object sender) {
      messages++;
      lastMessage = message;
    }
  }
}
