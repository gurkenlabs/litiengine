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
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;

public class MapObjectLoaderTests {

  @Test
  public void testPropMapObjectLoader() {
    PropMapObjectLoader loader = new PropMapObjectLoader();
    IEnvironment environment = mock(IEnvironment.class);
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setId(111);
    mapObject.setName("testProp");
    mapObject.setLocation(100, 100);
    mapObject.setValue(MapObjectProperty.PROP_MATERIAL, Material.PLASTIC.getName());
    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    mapObject.setValue(MapObjectProperty.COLLISION, true);
    mapObject.setValue(MapObjectProperty.COMBAT_HEALTH, 100);
    
    mapObject.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, 100.0f);
    mapObject.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, 100.0f);
    mapObject.setValue(MapObjectProperty.COLLISION_ALIGN, Align.LEFT);
    mapObject.setValue(MapObjectProperty.COLLISION_VALIGN, Valign.MIDDLE);
    mapObject.setValue(MapObjectProperty.COMBAT_TEAM, 1);

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();
    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testProp", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

    Prop prop = (Prop) entity;
    assertEquals(Material.PLASTIC, prop.getMaterial());
    assertTrue(prop.isIndestructible());
    assertTrue(prop.hasCollision());
    assertEquals(100, prop.getHitPoints().getMaxValue().intValue());
    assertEquals(100, prop.getHitPoints().getCurrentValue().intValue());

    assertEquals(100.0, prop.getCollisionBoxWidth(), 0.0001);
    assertEquals(100.0, prop.getCollisionBoxHeight(), 0.0001);
    assertEquals(Align.LEFT, prop.getCollisionBoxAlign());
    assertEquals(Valign.MIDDLE, prop.getCollisionBoxValign());
    assertEquals(1, prop.getTeam());
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
    when(mapObject.getWidth()).thenReturn(200f);
    when(mapObject.getHeight()).thenReturn(200f);

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testCollider", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

    CollisionBox collider = (CollisionBox) entity;

    assertEquals(200.0, collider.getCollisionBoxWidth(), 0.0001);
    assertEquals(200.0, collider.getCollisionBoxHeight(), 0.0001);
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
    when(mapObject.getWidth()).thenReturn(200f);
    when(mapObject.getHeight()).thenReturn(200f);

    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_MESSAGE)).thenReturn("message");
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATION)).thenReturn(TriggerActivation.INTERACT.name());
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_TARGETS)).thenReturn("1,2,3");
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATORS)).thenReturn("4,5,6");
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_ONETIME)).thenReturn("false");

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testTrigger", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

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

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testEmitter", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);
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

    when(mapObject.getIntValue(MapObjectProperty.LIGHT_ALPHA)).thenReturn(100);
    when(mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, 100)).thenReturn(100);
    when(mapObject.getColorValue(MapObjectProperty.LIGHT_COLOR)).thenReturn(Color.WHITE);
    when(mapObject.getBoolValue(MapObjectProperty.LIGHT_ACTIVE, true)).thenReturn(true);
    when(mapObject.getStringValue(MapObjectProperty.LIGHT_SHAPE)).thenReturn(LightSource.ELLIPSE);

    Collection<IEntity> entities = loader.load(environment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = entities.stream().findFirst().get();

    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testLight", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

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

    Map<String, ICustomProperty> customProps = new Hashtable<>(2);
    customProps.put("foo", new CustomProperty("foovalue"));
    customProps.put("bar", new CustomProperty("111"));
    when(mapObject.getProperties()).thenReturn(customProps);
    when(mapObject.getStringValue("foo")).thenReturn("foovalue");
    when(mapObject.getStringValue("bar")).thenReturn("111");

    when(mapObject.getStringValue("foo", null)).thenReturn("foovalue");
    when(mapObject.getStringValue("bar", null)).thenReturn("111");

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
  public static class CustomEntity extends Entity {
    @TmxProperty(name = "foo")
    private String foo;

    @TmxProperty(name = "bar")
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
