package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;

public class EnvironmentTests {
  private IEnvironment testEnvironment;

  @Test
  public void testInitialization() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));

    Environment env = new Environment(map);

    assertNotNull(env);
  }

  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init();
  }

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    this.testEnvironment = new Environment(map);
  }

  @Test
  public void testCombatEntity() {
    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getName()).thenReturn("test");
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    this.testEnvironment.add(combatEntity);

    assertNotNull(this.testEnvironment.get(123));
    assertNotNull(this.testEnvironment.getCombatEntity(123));
    assertNotNull(this.testEnvironment.getCombatEntity("test"));
    assertEquals(1, this.testEnvironment.getCombatEntities().size());
    assertEquals(1, this.testEnvironment.getEntitiesByType(ICombatEntity.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(combatEntity);

    assertNull(this.testEnvironment.get(123));
    assertNull(this.testEnvironment.getCombatEntity(123));
    assertNull(this.testEnvironment.getCombatEntity("test"));
    assertEquals(0, this.testEnvironment.getCombatEntities().size());
    assertEquals(0, this.testEnvironment.getEntitiesByType(ICombatEntity.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testMovableEntity() {
    IMovableEntity movableEntity = mock(IMovableEntity.class);
    when(movableEntity.getMapId()).thenReturn(456);
    when(movableEntity.getName()).thenReturn("test");
    when(movableEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    this.testEnvironment.add(movableEntity);

    assertNotNull(this.testEnvironment.get(456));
    assertNotNull(this.testEnvironment.getMovableEntity(456));
    assertNotNull(this.testEnvironment.getMovableEntity("test"));
    assertEquals(1, this.testEnvironment.getMovableEntities().size());
    assertEquals(1, this.testEnvironment.getEntitiesByType(IMovableEntity.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(movableEntity);

    assertNull(this.testEnvironment.get(456));
    assertNull(this.testEnvironment.getMovableEntity(456));
    assertNull(this.testEnvironment.getMovableEntity("test"));
    assertEquals(0, this.testEnvironment.getMovableEntities().size());
    assertEquals(0, this.testEnvironment.getEntitiesByType(IMovableEntity.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testTrigger() {
    Trigger testTrigger = new Trigger(TriggerActivation.COLLISION, "test", "testmessage");
    testTrigger.setMapId(1);

    this.testEnvironment.add(testTrigger);

    assertNotNull(this.testEnvironment.getTrigger("test"));
    assertNotNull(this.testEnvironment.get(1));
    assertNotNull(this.testEnvironment.get("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Trigger.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testTrigger);

    assertNull(this.testEnvironment.getTrigger("test"));
    assertNull(this.testEnvironment.get(1));
    assertNull(this.testEnvironment.get("test"));

    assertEquals(0, this.testEnvironment.getEntitiesByType(Trigger.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testLightSource() {
    LightSource testLight = new LightSource(100, new Color(255, 255, 255, 100), LightSource.ELLIPSE, true);
    testLight.setMapId(999);
    testLight.setName("test");

    this.testEnvironment.add(testLight);

    assertNotNull(this.testEnvironment.getLightSource(999));
    assertNotNull(this.testEnvironment.getLightSource("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(LightSource.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testLight);

    assertNull(this.testEnvironment.getLightSource(999));
    assertNull(this.testEnvironment.getLightSource("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(LightSource.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testCollisionBox() {
    CollisionBox testCollider = new CollisionBox(true);
    testCollider.setMapId(1);
    testCollider.setName("test");

    this.testEnvironment.add(testCollider);

    assertNotNull(this.testEnvironment.getCollisionBox(1));
    assertNotNull(this.testEnvironment.getCollisionBox("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(CollisionBox.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testCollider);

    assertNull(this.testEnvironment.getCollisionBox(1));
    assertNull(this.testEnvironment.getCollisionBox("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(CollisionBox.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testEmitter() {
    Emitter testEmitter = new Emitter(1, 1) {
      @Override
      protected Particle createNewParticle() {
        return null;
      }
    };

    testEmitter.setMapId(1);
    testEmitter.setName("test");

    this.testEnvironment.add(testEmitter);

    assertNotNull(this.testEnvironment.getEmitter(1));
    assertNotNull(this.testEnvironment.getEmitter("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Emitter.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testEmitter);

    assertNull(this.testEnvironment.getEmitter(1));
    assertNull(this.testEnvironment.getEmitter("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(Emitter.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testProp() {
    Prop testProp = new Prop(0, 0, null);
    testProp.setMapId(1);
    testProp.setName("test");

    this.testEnvironment.add(testProp);

    assertNotNull(this.testEnvironment.getProp(1));
    assertNotNull(this.testEnvironment.getProp("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Prop.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testProp);

    assertNull(this.testEnvironment.getProp(1));
    assertNull(this.testEnvironment.getProp("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(Prop.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testStaticShadow() {
    StaticShadow testShadow = new StaticShadow(0, 0, 1, 1, StaticShadowType.NONE);
    testShadow.setMapId(1);
    testShadow.setName("test");

    this.testEnvironment.add(testShadow);

    assertNotNull(this.testEnvironment.getStaticShadow(1));
    assertNotNull(this.testEnvironment.getStaticShadow("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(StaticShadow.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testShadow);

    assertNull(this.testEnvironment.getStaticShadow(1));
    assertNull(this.testEnvironment.getStaticShadow("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(StaticShadow.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testMapArea() {
    MapArea testArea = new MapArea(0, 0, 1, 1);
    testArea.setMapId(1);
    testArea.setName("test");

    this.testEnvironment.add(testArea);

    assertNotNull(this.testEnvironment.getArea(1));
    assertNotNull(this.testEnvironment.getArea("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(MapArea.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testArea);

    assertNull(this.testEnvironment.getArea(1));
    assertNull(this.testEnvironment.getArea("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(MapArea.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testSpawnPoint() {
    Spawnpoint testSpawn = new Spawnpoint(1, 0, 0);
    testSpawn.setName("test");

    this.testEnvironment.add(testSpawn);

    assertNotNull(this.testEnvironment.getSpawnpoint(1));
    assertNotNull(this.testEnvironment.getSpawnpoint("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Spawnpoint.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());

    this.testEnvironment.remove(testSpawn);

    assertNull(this.testEnvironment.getSpawnpoint(1));
    assertNull(this.testEnvironment.getSpawnpoint("test"));
    assertEquals(0, this.testEnvironment.getEntitiesByType(MapArea.class).size());
    assertEquals(0, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testGetNonExistingEntities() {
    this.testEnvironment.add(null);
    this.testEnvironment.clear();

    assertNull(this.testEnvironment.get(123456789));
    assertNull(this.testEnvironment.getCombatEntity(123456789));
    assertNull(this.testEnvironment.getMovableEntity(123456789));
    assertNull(this.testEnvironment.get(""));
    assertNull(this.testEnvironment.get(null));
  }

  @ParameterizedTest
  @EnumSource(value = RenderType.class)
  public void testEntityByRenderType(RenderType renderType) {
    ICombatEntity entity = mock(ICombatEntity.class);
    when(entity.getMapId()).thenReturn(123);
    when(entity.getName()).thenReturn("test");
    when(entity.getRenderType()).thenReturn(renderType);

    this.testEnvironment.add(entity);
    
    assertNotNull(this.testEnvironment.get(123));
    assertNotNull(this.testEnvironment.get("test"));
    assertEquals(1, this.testEnvironment.getEntities(renderType).size());

    this.testEnvironment.remove(entity);

    assertNull(this.testEnvironment.get(123));
    assertNull(this.testEnvironment.get("test"));
    assertEquals(0, this.testEnvironment.getEntities(renderType).size());
  }

  @Test
  public void testEntitiesByTag() {
    IMovableEntity entityWithTags = mock(IMovableEntity.class);
    when(entityWithTags.getMapId()).thenReturn(456);
    when(entityWithTags.getRenderType()).thenReturn(RenderType.NORMAL);

    IMovableEntity anotherEntityWithTags = mock(IMovableEntity.class);
    when(anotherEntityWithTags.getMapId()).thenReturn(123);
    when(anotherEntityWithTags.getRenderType()).thenReturn(RenderType.NORMAL);

    ArrayList<String> tags = new ArrayList<>();
    tags.add("tag1");
    tags.add("tag2");
    when(entityWithTags.getTags()).thenReturn(tags);
    when(anotherEntityWithTags.getTags()).thenReturn(tags);

    this.testEnvironment.add(entityWithTags);
    this.testEnvironment.add(anotherEntityWithTags);

    assertEquals(2, this.testEnvironment.getByTag("tag1").size());
    assertEquals(2, this.testEnvironment.getByTag("tag2").size());
    assertEquals(0, this.testEnvironment.getByTag("invalidTag").size());

    this.testEnvironment.remove(entityWithTags);
    this.testEnvironment.remove(anotherEntityWithTags);

    assertEquals(0, this.testEnvironment.getByTag("tag1").size());
    assertEquals(0, this.testEnvironment.getByTag("tag2").size());
  }
}
