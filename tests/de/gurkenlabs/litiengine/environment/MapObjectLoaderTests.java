package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.awt.Point;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.entities.Collider;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class MapObjectLoaderTests {

  @Test
  public void testPropMapObjectLoader() {
    PropMapObjectLoader loader = new PropMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.PROP.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testProp");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomProperty(MapObjectProperties.MATERIAL)).thenReturn(Material.PLASTIC.name());
    when(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperties.COLLISION)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperties.HEALTH)).thenReturn("100");

    when(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH)).thenReturn("100.0");
    when(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT)).thenReturn("100.0");

    when(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN)).thenReturn("LEFT");
    when(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN)).thenReturn("MIDDLE");
    when(mapObject.getCustomProperty(MapObjectProperties.TEAM)).thenReturn("1");

    IEntity ent = loader.load(mapObject);

    Assert.assertNotNull(ent);
    Assert.assertEquals(ent.getMapId(), 111);
    Assert.assertEquals(ent.getName(), "testProp");
    Assert.assertEquals(ent.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(ent.getLocation().getY(), 100, 0.0001);

    Prop prop = (Prop) ent;
    Assert.assertEquals(prop.getMaterial(), Material.PLASTIC);
    Assert.assertTrue(prop.isIndestructible());
    Assert.assertTrue(prop.hasCollision());
    Assert.assertEquals(prop.getAttributes().getHealth().getMaxValue().intValue(), 100);
    Assert.assertEquals(prop.getAttributes().getHealth().getCurrentValue().intValue(), 100);

    Assert.assertEquals(prop.getCollisionBoxWidth(), 100.0, 0.0001);
    Assert.assertEquals(prop.getCollisionBoxHeight(), 100.0, 0.0001);
    Assert.assertEquals(prop.getCollisionBoxAlign(), Align.LEFT);
    Assert.assertEquals(prop.getCollisionBoxValign(), Valign.MIDDLE);
    Assert.assertEquals(prop.getTeam(), 1);
  }

  @Test
  public void testColliderMapObjectLoader() {
    ColliderMapObjectLoader loader = new ColliderMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.COLLISIONBOX.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testCollider");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    IEntity entity = loader.load(mapObject);

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testCollider");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    Collider collider = (Collider) entity;

    Assert.assertEquals(collider.getCollisionBoxWidth(), 200.0, 0.0001);
    Assert.assertEquals(collider.getCollisionBoxHeight(), 200.0, 0.0001);
  }

  @Test
  public void testTriggerMapObjectLoader() {
    TriggerMapObjectLoader loader = new TriggerMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.TRIGGER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testTrigger");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomProperty(MapObjectProperties.TRIGGERMESSAGE)).thenReturn("message");
    when(mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATION)).thenReturn(TriggerActivation.INTERACT.name());
    when(mapObject.getCustomProperty(MapObjectProperties.TRIGGERTARGETS)).thenReturn("1,2,3");
    when(mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATORS)).thenReturn("4,5,6");
    when(mapObject.getCustomProperty(MapObjectProperties.TRIGGERONETIME)).thenReturn("false");

    IEntity entity = loader.load(mapObject);

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testTrigger");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    Trigger trigger = (Trigger) entity;

    Assert.assertFalse(trigger.isOneTimeTrigger());
    Assert.assertEquals(TriggerActivation.INTERACT, trigger.getActivationType());
    Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, trigger.getTargets().toArray());
    Assert.assertArrayEquals(new Integer[] { 4, 5, 6 }, trigger.getActivators().toArray());
    Assert.assertEquals(200.0, trigger.getCollisionBoxWidth(), 0.0001);
    Assert.assertEquals(200.0, trigger.getCollisionBoxHeight(), 0.0001);
  }
}
