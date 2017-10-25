package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.DecorMob.MovementBehavior;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.FireEmitter;

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

    when(mapObject.getCustomProperty(MapObjectProperty.MATERIAL)).thenReturn(Material.PLASTIC.name());
    when(mapObject.getCustomProperty(MapObjectProperty.INDESTRUCTIBLE)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperty.HEALTH)).thenReturn("100");

    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXWIDTH)).thenReturn("100.0");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXHEIGHT)).thenReturn("100.0");

    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONALGIN)).thenReturn("LEFT");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONVALGIN)).thenReturn("MIDDLE");
    when(mapObject.getCustomProperty(MapObjectProperty.TEAM)).thenReturn("1");

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
    CollisionBoxMapObjectLoader loader = new CollisionBoxMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.COLLISIONBOX.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testCollider");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));
    when(mapObject.getWidth()).thenReturn(200);
    when(mapObject.getHeight()).thenReturn(200);

    IEntity entity = loader.load(mapObject);

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testCollider");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    CollisionBox collider = (CollisionBox) entity;

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

    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGERMESSAGE)).thenReturn("message");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGERACTIVATION)).thenReturn(TriggerActivation.INTERACT.name());
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGERTARGETS)).thenReturn("1,2,3");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGERACTIVATORS)).thenReturn("4,5,6");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGERONETIME)).thenReturn("false");

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

  @Test
  public void testDecorMobMapObjectLoader() {
    DecorMobMapObjectLoader loader = new DecorMobMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.DECORMOB.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testDecorMob");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME)).thenReturn("decorSprite");
    when(mapObject.getCustomProperty(MapObjectProperty.DECORMOB_VELOCITY)).thenReturn("200");
    when(mapObject.getCustomProperty(MapObjectProperty.DECORMOB_BEHAVIOUR)).thenReturn(MovementBehavior.SHY.name());
    when(mapObject.getCustomProperty(MapObjectProperty.INDESTRUCTIBLE)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION)).thenReturn("false");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXWIDTH)).thenReturn("100.0");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXHEIGHT)).thenReturn("100.0");

    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONALGIN)).thenReturn("LEFT");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISIONVALGIN)).thenReturn("MIDDLE");

    IEntity entity = loader.load(mapObject);

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testDecorMob");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    DecorMob decorMob = (DecorMob) entity;

    Assert.assertTrue(decorMob.isIndestructible());
    Assert.assertFalse(decorMob.hasCollision());

    Assert.assertEquals(decorMob.getCollisionBoxWidth(), 100.0, 0.0001);
    Assert.assertEquals(decorMob.getCollisionBoxHeight(), 100.0, 0.0001);
    Assert.assertEquals(decorMob.getCollisionBoxAlign(), Align.LEFT);
    Assert.assertEquals(decorMob.getCollisionBoxValign(), Valign.MIDDLE);

    Assert.assertEquals(decorMob.getMovementBehavior(), MovementBehavior.SHY);
    Assert.assertEquals(decorMob.getVelocity(), 200, 0.0001);
    Assert.assertEquals(decorMob.getMobType(), "decorSprite");
  }

  @Test
  public void testEmitterMapObjectLoader() {
    EmitterMapObjectLoader loader = new EmitterMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.EMITTER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testEmitter");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomProperty(MapObjectProperty.EMITTERTYPE)).thenReturn("fire");

    IEntity entity = loader.load(mapObject);

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testEmitter");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    Emitter emitter = (Emitter) entity;
    Assert.assertTrue(emitter instanceof FireEmitter);
  }

  @Test
  public void testLightSourceMapObjectLoader() {
    LightSourceMapObjectLoader loader = new LightSourceMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.LIGHTSOURCE.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testLight");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomProperty(MapObjectProperty.LIGHTBRIGHTNESS)).thenReturn("100");
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHTINTENSITY)).thenReturn("100");
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHTCOLOR)).thenReturn("#ffffff");
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHTACTIVE)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHTSHAPE)).thenReturn(LightSource.ELLIPSE);

    IEntity entity = loader.load(mapObject);

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testLight");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    LightSource light = (LightSource) entity;
    Assert.assertTrue(light.isActive());
    Assert.assertEquals(Color.WHITE.getRed(), light.getColor().getRed());
    Assert.assertEquals(Color.WHITE.getBlue(), light.getColor().getBlue());
    Assert.assertEquals(Color.WHITE.getGreen(), light.getColor().getGreen());
    Assert.assertEquals(100, light.getBrightness());
    Assert.assertEquals(100, light.getIntensity());
    Assert.assertEquals(LightSource.ELLIPSE, light.getLightShapeType());
  }
}
