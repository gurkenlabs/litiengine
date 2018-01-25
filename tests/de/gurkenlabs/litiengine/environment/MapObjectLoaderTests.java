package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.Optional;

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

    when(mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL)).thenReturn(Material.PLASTIC.name());
    when(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE)).thenReturn(true);
    when(mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION)).thenReturn(true);
    when(mapObject.getCustomPropertyInt(MapObjectProperty.HEALTH)).thenReturn(100);

    when(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH)).thenReturn(100.0f);
    when(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT)).thenReturn(100.0f);

    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALGIN)).thenReturn("LEFT");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALGIN)).thenReturn("MIDDLE");
    when(mapObject.getCustomPropertyInt(MapObjectProperty.TEAM)).thenReturn(1);

    Collection<IEntity> entities = loader.load(mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    Assert.assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();
    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testProp");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);

    Prop prop = (Prop) entity;
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

    Collection<IEntity> entities = loader.load(mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    Assert.assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

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

    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_MESSAGE)).thenReturn("message");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATION)).thenReturn(TriggerActivation.INTERACT.name());
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_TARGETS)).thenReturn("1,2,3");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATORS)).thenReturn("4,5,6");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ONETIME)).thenReturn("false");

    Collection<IEntity> entities = loader.load(mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    Assert.assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

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
    when(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE)).thenReturn(true);
    when(mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION)).thenReturn(false);
    when(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH)).thenReturn(100.0f);
    when(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT)).thenReturn(100.0f);

    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALGIN)).thenReturn("LEFT");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALGIN)).thenReturn("MIDDLE");

    Collection<IEntity> entities = loader.load(mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    Assert.assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

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

    Collection<IEntity> entities = loader.load(mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    Assert.assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    Assert.assertNotNull(entity);
    Assert.assertEquals(entity.getMapId(), 111);
    Assert.assertEquals(entity.getName(), "testEmitter");
    Assert.assertEquals(entity.getLocation().getX(), 100, 0.0001);
    Assert.assertEquals(entity.getLocation().getY(), 100, 0.0001);
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

    when(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_ALPHA)).thenReturn(100);
    when(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_INTENSITY)).thenReturn(100);
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHT_COLOR)).thenReturn("#ffffff");
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHT_ACTIVE)).thenReturn("true");
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHT_SHAPE)).thenReturn(LightSource.ELLIPSE);

    Collection<IEntity> entities = loader.load(mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    Assert.assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

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
    Assert.assertEquals(100, light.getColor().getAlpha());
    Assert.assertEquals(100, light.getIntensity());
    Assert.assertEquals(LightSource.ELLIPSE, light.getLightShapeType());
  }
}
