package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;

// TODO: extend the tests by all default entity types and provide tests for the delete operation
public class EnvironmentTests {
  private IEnvironment testEnvironment;

  @Test
  public void testInitialization() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));

    Environment env = new Environment(map);

    assertNotNull(env);
  }

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    this.testEnvironment = new Environment(map);
  }

  @Test
  public void testAddAndGetCombatEntity() {
    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    this.testEnvironment.add(combatEntity);

    assertNotNull(this.testEnvironment.get(123));
    assertNotNull(this.testEnvironment.getCombatEntity(123));
    assertEquals(1, this.testEnvironment.getCombatEntities().size());
    assertEquals(1, this.testEnvironment.getEntitiesByType(ICombatEntity.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testAddAndGetMovableEntity() {
    IMovableEntity movableEntity = mock(IMovableEntity.class);
    when(movableEntity.getMapId()).thenReturn(456);
    when(movableEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    this.testEnvironment.add(movableEntity);

    assertNotNull(this.testEnvironment.get(456));
    assertNotNull(this.testEnvironment.getMovableEntity(456));
    assertEquals(1, this.testEnvironment.getMovableEntities().size());
    assertEquals(1, this.testEnvironment.getEntitiesByType(IMovableEntity.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testAddAndGetTrigger() {
    Trigger testTrigger = new Trigger(TriggerActivation.COLLISION, "test", "testmessage");
    this.testEnvironment.add(testTrigger);

    assertNotNull(this.testEnvironment.getTrigger("test"));
    assertNotNull(this.testEnvironment.get("test"));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Trigger.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testAddAndGetLightSource() {
    LightSource testLight = new LightSource(100, new Color(255, 255, 255, 100), LightSource.ELLIPSE, true);
    testLight.setMapId(999);

    this.testEnvironment.add(testLight);

    assertNotNull(this.testEnvironment.getLightSource(999));
    assertEquals(1, this.testEnvironment.getEntitiesByType(LightSource.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testAddAndGetCollisionBox() {
    CollisionBox testCollider = new CollisionBox(true);
    testCollider.setMapId(1);

    this.testEnvironment.add(testCollider);

    assertNotNull(this.testEnvironment.getCollisionBox(1));
    assertEquals(1, this.testEnvironment.getEntitiesByType(CollisionBox.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testAddAndGetEmitter() {
    Emitter testEmitter = new Emitter(1, 1) {
      @Override
      protected Particle createNewParticle() {
        return null;
      }
    };

    testEmitter.setMapId(1);

    this.testEnvironment.add(testEmitter);

    assertNotNull(this.testEnvironment.getEmitter(1));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Emitter.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testAddAndGetProp() {
    Prop testProp = new Prop(0, 0, null);
    testProp.setMapId(1);

    this.testEnvironment.add(testProp);

    assertNotNull(this.testEnvironment.getProp(1));
    assertEquals(1, this.testEnvironment.getEntitiesByType(Prop.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }
  
  @Test
  public void testAddAndGetStaticShadow() {
    StaticShadow testShadow = new StaticShadow(0,0,1,1, StaticShadowType.NONE);
    
    testShadow.setMapId(1);

    this.testEnvironment.add(testShadow);

    assertNotNull(this.testEnvironment.getStaticShadow(1));
    assertEquals(1, this.testEnvironment.getEntitiesByType(StaticShadow.class).size());
    assertEquals(1, this.testEnvironment.getEntities().size());
  }

  @Test
  public void testGetNonExistingEntities() {
    this.testEnvironment.add(null);

    assertNull(this.testEnvironment.get(123456789));
    assertNull(this.testEnvironment.getCombatEntity(123456789));
    assertNull(this.testEnvironment.getMovableEntity(123456789));
    assertNull(this.testEnvironment.get(""));
    assertNull(this.testEnvironment.get(null));
  }

  @ParameterizedTest
  @EnumSource(value = RenderType.class)
  public void testGetEntityByRenderType(RenderType renderType) {
    ICombatEntity entity = mock(ICombatEntity.class);
    when(entity.getMapId()).thenReturn(123);
    when(entity.getRenderType()).thenReturn(renderType);

    this.testEnvironment.add(entity);

    assertEquals(1, this.testEnvironment.getEntities(renderType).size());
  }

  @Test
  public void testAddandGetEntitiesByTag() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));

    Environment env = new Environment(map);

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

    env.add(entityWithTags);
    env.add(anotherEntityWithTags);

    assertEquals(2, env.getByTag("tag1").size());
    assertEquals(2, env.getByTag("tag2").size());
    assertEquals(0, env.getByTag("invalidTag").size());
  }
}
