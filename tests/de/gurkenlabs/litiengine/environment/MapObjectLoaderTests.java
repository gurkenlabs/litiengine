package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.annotation.CustomMapObjectProperty;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;
import de.gurkenlabs.litiengine.graphics.LightSource;

public class MapObjectLoaderTests {

  @Test
  public void testPropMapObjectLoader() {
    PropMapObjectLoader loader = new PropMapObjectLoader();
    Environment environment = mock(Environment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.PROP.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testProp");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));

    when(mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL)).thenReturn(Material.PLASTIC.name());
    when(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE)).thenReturn(true);
    when(mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION)).thenReturn(true);
    when(mapObject.getCustomPropertyBool(eq(MapObjectProperty.COLLISION), any(boolean.class))).thenReturn(true);
    when(mapObject.getCustomPropertyInt(MapObjectProperty.HEALTH)).thenReturn(100);

    when(mapObject.getCustomPropertyFloat(eq(MapObjectProperty.COLLISIONBOX_WIDTH), any(float.class))).thenReturn(100.0f);
    when(mapObject.getCustomPropertyFloat(eq(MapObjectProperty.COLLISIONBOX_HEIGHT), any(float.class))).thenReturn(100.0f);

    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALIGN)).thenReturn("LEFT");
    when(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALIGN)).thenReturn("MIDDLE");
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
    Environment environment = mock(Environment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.COLLISIONBOX.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testCollider");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(200f);
    when(mapObject.getHeight()).thenReturn(200f);

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
    Environment environment = mock(Environment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.TRIGGER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testTrigger");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(200f);
    when(mapObject.getHeight()).thenReturn(200f);

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
    Environment environment = mock(Environment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.EMITTER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testEmitter");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));

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
    Environment environment = mock(Environment.class);
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.LIGHTSOURCE.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testLight");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));

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

  @Test
  public void testCustomMapObjectLoader() {
    Environment.registerCustomEntityType(CustomEntity.class);
    
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn("customEntity");
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("somethin");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(50f);
    when(mapObject.getHeight()).thenReturn(150f);

    List<Property> customProps = Arrays.asList(new Property("foo", "foovalue"), new Property("bar", "111"));
    when(mapObject.getCustomProperties()).thenReturn(customProps);
    when(mapObject.getCustomProperty("foo")).thenReturn("foovalue");
    when(mapObject.getCustomProperty("bar")).thenReturn("111");

    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    Environment env = new Environment(map);

    Collection<IEntity> loaded = env.load(mapObject);

    assertEquals(1, loaded.size());

    IEntity ent = loaded.iterator().next();
    assertTrue(ent instanceof CustomEntity);

    CustomEntity customEntity = (CustomEntity) ent;
    assertEquals("foovalue", customEntity.getFoo());
    assertEquals(111, customEntity.getBar());
  }

  @EntityInfo(customMapObjectType = "customEntity")
  @CustomMapObjectProperty(keys = { "foo", "bar" })
  public static class CustomEntity extends Entity {
    private String foo;
    private int bar;

    public CustomEntity(IMapObject mo) {
    }

    public String getFoo() {
      return this.foo;
    }

    public int getBar() {
      return this.bar;
    }
  }
}
