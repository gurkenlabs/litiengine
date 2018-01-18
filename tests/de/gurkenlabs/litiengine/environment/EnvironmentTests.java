package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;

// TODO: extend the tests by all default entity types and provide tests for the delete operation
public class EnvironmentTests {

  @Test
  public void testInitialization() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));

    Environment env = new Environment(map);

    Assert.assertNotNull(env);
  }

  @Test
  public void testAddAndGetEntities() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));

    Environment env = new Environment(map);

    Trigger testTrigger = new Trigger(TriggerActivation.COLLISION, "test", "testmessage");

    LightSource testLight = new LightSource(100, 100, Color.WHITE, LightSource.ELLIPSE, true);
    testLight.setMapId(999);

    CollisionBox testCollider = new CollisionBox(true);
    testCollider.setMapId(1);

    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    IMovableEntity movableEntity = mock(IMovableEntity.class);
    when(movableEntity.getMapId()).thenReturn(456);
    when(movableEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    IMovableEntity notAddedToEnvironment = mock(IMovableEntity.class);
    when(notAddedToEnvironment.getMapId()).thenReturn(222);

    env.add(testTrigger);
    env.add(testLight);
    env.add(testCollider);
    env.add(combatEntity);
    env.add(movableEntity);

    Assert.assertNotNull(env.getTrigger("test"));
    Assert.assertNotNull(env.get("test"));

    Assert.assertNotNull(env.getLightSource(999));
    Assert.assertNotNull(env.getCollisionBox(1));

    Assert.assertNotNull(env.get(123));
    Assert.assertNotNull(env.getCombatEntity(123));
    Assert.assertEquals(1, env.getCombatEntities().size());
    Assert.assertNotNull(env.get(456));
    Assert.assertNotNull(env.getMovableEntity(456));
    Assert.assertEquals(1, env.getMovableEntities().size());

    Assert.assertNull(env.get(123456789));
    Assert.assertNull(env.getCombatEntity(123456789));
    Assert.assertNull(env.getMovableEntity(123456789));
    Assert.assertNull(env.get(""));
    Assert.assertNull(env.get(null));

    Assert.assertEquals(2, env.getEntities(RenderType.NORMAL).size());
    Assert.assertEquals(2, env.getEntities(RenderType.OVERLAY).size());
    Assert.assertEquals(1, env.getEntities(RenderType.GROUND).size());
    Assert.assertEquals(5, env.getEntities().size());
    Assert.assertEquals(1, env.getEntitiesByType(Trigger.class).size());
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

    Assert.assertEquals(2, env.getByTag("tag1").size());
    Assert.assertEquals(2, env.getByTag("tag2").size());
    Assert.assertEquals(0, env.getByTag("invalidTag").size());
  }
}
