package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionBox;
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
    IEnvironment environment = mock(IEnvironment.class);
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

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();
    assertNotNull(entity);
    assertEquals(entity.getMapId(), 111);
    assertEquals(entity.getName(), "testProp");
    assertEquals(entity.getLocation().getX(), 100, 0.0001);
    assertEquals(entity.getLocation().getY(), 100, 0.0001);

    Prop prop = (Prop) entity;
    assertEquals(prop.getMaterial(), Material.PLASTIC);
    assertTrue(prop.isIndestructible());
    assertTrue(prop.hasCollision());
    assertEquals(prop.getAttributes().getHealth().getMaxValue().intValue(), 100);
    assertEquals(prop.getAttributes().getHealth().getCurrentValue().intValue(), 100);

    assertEquals(prop.getCollisionBoxWidth(), 100.0, 0.0001);
    assertEquals(prop.getCollisionBoxHeight(), 100.0, 0.0001);
    assertEquals(prop.getCollisionBoxAlign(), Align.LEFT);
    assertEquals(prop.getCollisionBoxValign(), Valign.MIDDLE);
    assertEquals(prop.getTeam(), 1);
  }

  @Test
  public void testColliderMapObjectLoader() {
    CollisionBoxMapObjectLoader loader = new CollisionBoxMapObjectLoader();
    IEnvironment environment = mock(IEnvironment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.COLLISIONBOX.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testCollider");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));
    when(mapObject.getWidth()).thenReturn(200);
    when(mapObject.getHeight()).thenReturn(200);

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(entity.getMapId(), 111);
    assertEquals(entity.getName(), "testCollider");
    assertEquals(entity.getLocation().getX(), 100, 0.0001);
    assertEquals(entity.getLocation().getY(), 100, 0.0001);

    CollisionBox collider = (CollisionBox) entity;

    assertEquals(collider.getCollisionBoxWidth(), 200.0, 0.0001);
    assertEquals(collider.getCollisionBoxHeight(), 200.0, 0.0001);
  }

  @Test
  public void testTriggerMapObjectLoader() {
    TriggerMapObjectLoader loader = new TriggerMapObjectLoader();
    IEnvironment environment = mock(IEnvironment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.TRIGGER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testTrigger");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(200);
    when(mapObject.getHeight()).thenReturn(200);
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_MESSAGE)).thenReturn("message");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATION)).thenReturn(TriggerActivation.INTERACT.name());
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_TARGETS)).thenReturn("1,2,3");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATORS)).thenReturn("4,5,6");
    when(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ONETIME)).thenReturn("false");

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(entity.getMapId(), 111);
    assertEquals(entity.getName(), "testTrigger");
    assertEquals(entity.getLocation().getX(), 100, 0.0001);
    assertEquals(entity.getLocation().getY(), 100, 0.0001);

    Trigger trigger = (Trigger) entity;

    assertFalse(trigger.isOneTimeTrigger());
    assertEquals(TriggerActivation.INTERACT, trigger.getActivationType());
    assertArrayEquals(new Integer[] { 1, 2, 3 }, trigger.getTargets().toArray());
    assertArrayEquals(new Integer[] { 4, 5, 6 }, trigger.getActivators().toArray());
    assertEquals(200.0, trigger.getCollisionBoxWidth(), 0.0001);
    assertEquals(200.0, trigger.getCollisionBoxHeight(), 0.0001);
  }

  @Test
  public void testEmitterMapObjectLoader() {
    EmitterMapObjectLoader loader = new EmitterMapObjectLoader();
    IEnvironment environment = mock(IEnvironment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.EMITTER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testEmitter");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(entity.getMapId(), 111);
    assertEquals(entity.getName(), "testEmitter");
    assertEquals(entity.getLocation().getX(), 100, 0.0001);
    assertEquals(entity.getLocation().getY(), 100, 0.0001);
  }

  @Test
  public void testLightSourceMapObjectLoader() {
    LightSourceMapObjectLoader loader = new LightSourceMapObjectLoader();
    IEnvironment environment = mock(IEnvironment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.LIGHTSOURCE.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testLight");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getDimension()).thenReturn(new Dimension(200, 200));

    when(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_ALPHA)).thenReturn(100);
    when(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_INTENSITY, 100)).thenReturn(100);
    when(mapObject.getCustomPropertyColor(MapObjectProperty.LIGHT_COLOR)).thenReturn(Color.WHITE);
    when(mapObject.getCustomPropertyBool(MapObjectProperty.LIGHT_ACTIVE, true)).thenReturn(true);
    when(mapObject.getCustomProperty(MapObjectProperty.LIGHT_SHAPE)).thenReturn(LightSource.ELLIPSE);

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(entity.getMapId(), 111);
    assertEquals(entity.getName(), "testLight");
    assertEquals(entity.getLocation().getX(), 100, 0.0001);
    assertEquals(entity.getLocation().getY(), 100, 0.0001);

    LightSource light = (LightSource) entity;
    assertTrue(light.isActive());
    assertEquals(Color.WHITE.getRed(), light.getColor().getRed());
    assertEquals(Color.WHITE.getBlue(), light.getColor().getBlue());
    assertEquals(Color.WHITE.getGreen(), light.getColor().getGreen());
    assertEquals(100, light.getColor().getAlpha());
    assertEquals(100, light.getIntensity());
    assertEquals(LightSource.ELLIPSE, light.getLightShapeType());
  }
}
