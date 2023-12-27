package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.EntityInfo;
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
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.test.GameTestSuite;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class MapObjectLoaderTests {
  private Environment testEnvironment;

  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.terminateGame();
  }

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    when(map.getOrientation()).thenReturn(MapOrientations.ORTHOGONAL);
    when(map.getRenderLayers()).thenReturn(new ArrayList<>());

    this.testEnvironment = new Environment(map);
  }

  @Test
  void testCreatureMapObjectLoader() {
    CreatureMapObjectLoader loader = new CreatureMapObjectLoader();
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.CREATURE.name());
    mapObject.setId(111);
    mapObject.setName("testCreature");
    mapObject.setLocation(100, 100);

    // collision
    mapObject.setValue(MapObjectProperty.COLLISION, true);
    mapObject.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, 100.0f);
    mapObject.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, 100.0f);
    mapObject.setValue(MapObjectProperty.COLLISION_ALIGN, Align.LEFT);
    mapObject.setValue(MapObjectProperty.COLLISION_VALIGN, Valign.MIDDLE);

    // combat
    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, 100);
    mapObject.setValue(MapObjectProperty.COMBAT_TEAM, 1);

    // movement
    mapObject.setValue(MapObjectProperty.MOVEMENT_VELOCITY, 200f);
    mapObject.setValue(MapObjectProperty.MOVEMENT_ACCELERATION, 150);
    mapObject.setValue(MapObjectProperty.MOVEMENT_DECELERATION, 160);
    mapObject.setValue(MapObjectProperty.MOVEMENT_TURNONMOVE, true);

    Collection<IEntity> entities = loader.load(this.testEnvironment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = opt.get();
    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testCreature", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

    Creature creature = (Creature) entity;
    assertTrue(creature.hasCollision());
    assertEquals(100.0, creature.getCollisionBoxWidth(), 0.0001);
    assertEquals(100.0, creature.getCollisionBoxHeight(), 0.0001);
    assertEquals(Align.LEFT, creature.getCollisionBoxAlign());
    assertEquals(Valign.MIDDLE, creature.getCollisionBoxValign());

    assertTrue(creature.isIndestructible());
    assertEquals(100, creature.getHitPoints().getMax().intValue());
    assertEquals(100, creature.getHitPoints().get().intValue());
    assertEquals(1, creature.getTeam());

    assertEquals(200f, creature.getVelocity().get().floatValue());
    assertEquals(150f, creature.getAcceleration());
    assertEquals(160f, creature.getDeceleration());
    assertTrue(creature.turnOnMove());
  }

  @Test
  void testPropMapObjectLoader() {
    PropMapObjectLoader loader = new PropMapObjectLoader();
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setId(111);
    mapObject.setName("testProp");
    mapObject.setLocation(100, 100);
    mapObject.setValue(MapObjectProperty.PROP_MATERIAL, Material.PLASTIC.getName());

    mapObject.setValue(MapObjectProperty.COLLISION, true);
    mapObject.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, 100.0f);
    mapObject.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, 100.0f);
    mapObject.setValue(MapObjectProperty.COLLISION_ALIGN, Align.LEFT);
    mapObject.setValue(MapObjectProperty.COLLISION_VALIGN, Valign.MIDDLE);

    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, 100);
    mapObject.setValue(MapObjectProperty.COMBAT_TEAM, 1);

    Collection<IEntity> entities = loader.load(this.testEnvironment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = opt.get();
    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testProp", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

    Prop prop = (Prop) entity;
    assertEquals(Material.PLASTIC, prop.getMaterial());

    assertTrue(prop.hasCollision());
    assertEquals(100.0, prop.getCollisionBoxWidth(), 0.0001);
    assertEquals(100.0, prop.getCollisionBoxHeight(), 0.0001);
    assertEquals(Align.LEFT, prop.getCollisionBoxAlign());
    assertEquals(Valign.MIDDLE, prop.getCollisionBoxValign());

    assertTrue(prop.isIndestructible());
    assertEquals(100, prop.getHitPoints().getMax().intValue());
    assertEquals(100, prop.getHitPoints().get().intValue());
    assertEquals(1, prop.getTeam());
  }

  @Test
  void testColliderMapObjectLoader() {
    CollisionBoxMapObjectLoader loader = new CollisionBoxMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.COLLISIONBOX.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testCollider");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(200f);
    when(mapObject.getHeight()).thenReturn(200f);

    Collection<IEntity> entities = loader.load(this.testEnvironment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = opt.get();

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
  void testTriggerMapObjectLoader() {
    TriggerMapObjectLoader loader = new TriggerMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.TRIGGER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testTrigger");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(200f);
    when(mapObject.getHeight()).thenReturn(200f);

    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_MESSAGE)).thenReturn("message");
    when(mapObject.getEnumValue(eq(MapObjectProperty.TRIGGER_ACTIVATION), any(Class.class), any(TriggerActivation.class))).thenReturn(TriggerActivation.INTERACT);
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_TARGETS, null)).thenReturn("1,2,3");
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATORS, null)).thenReturn("4,5,6");
    when(mapObject.getStringValue(MapObjectProperty.TRIGGER_ONETIME)).thenReturn("false");

    Collection<IEntity> entities = loader.load(this.testEnvironment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = opt.get();

    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testTrigger", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);

    Trigger trigger = (Trigger) entity;

    assertFalse(trigger.isOneTimeTrigger());
    assertEquals(TriggerActivation.INTERACT, trigger.getActivationType());
    assertArrayEquals(new Integer[] {1, 2, 3}, trigger.getTargets().toArray());
    assertArrayEquals(new Integer[] {4, 5, 6}, trigger.getActivators().toArray());
    assertEquals(200.0, trigger.getCollisionBoxWidth(), 0.0001);
    assertEquals(200.0, trigger.getCollisionBoxHeight(), 0.0001);
  }

  @Test
  void testEmitterMapObjectLoader() {
    EmitterMapObjectLoader loader = new EmitterMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.EMITTER.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testEmitter");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));

    Collection<IEntity> entities = loader.load(this.testEnvironment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = opt.get();

    assertNotNull(entity);
    assertEquals(111, entity.getMapId());
    assertEquals("testEmitter", entity.getName());
    assertEquals(100, entity.getX(), 0.0001);
    assertEquals(100, entity.getY(), 0.0001);
  }

  @Test
  void testLightSourceMapObjectLoader() {
    LightSourceMapObjectLoader loader = new LightSourceMapObjectLoader();
    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn(MapObjectType.LIGHTSOURCE.name());
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("testLight");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));

    when(mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, 100)).thenReturn(100);
    when(mapObject.getColorValue(MapObjectProperty.LIGHT_COLOR)).thenReturn(new Color(255, 255, 255, 100));
    when(mapObject.getBoolValue(MapObjectProperty.LIGHT_ACTIVE, true)).thenReturn(true);
    when(mapObject.getEnumValue(eq(MapObjectProperty.LIGHT_SHAPE), eq(LightSource.Type.class), any(LightSource.Type.class))).thenReturn(LightSource.Type.ELLIPSE);

    Collection<IEntity> entities = loader.load(this.testEnvironment, mapObject);
    Optional<IEntity> opt = entities.stream().findFirst();
    assertTrue(opt.isPresent());

    IEntity entity = opt.get();

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
    assertEquals(LightSource.Type.ELLIPSE, light.getLightShapeType());
  }

  @Test
  void testCustomMapObjectLoader() {
    Environment.registerCustomEntityType(CustomEntity.class);

    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn("customEntity");
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("somethin");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(50f);
    when(mapObject.getHeight()).thenReturn(150f);

    Map<String, ICustomProperty> customProps = new HashMap<>(2);
    customProps.put("foo", new CustomProperty("foovalue"));
    customProps.put("bar", new CustomProperty("111"));
    when(mapObject.getProperties()).thenReturn(customProps);
    when(mapObject.getStringValue("foo")).thenReturn("foovalue");
    when(mapObject.getStringValue("foo", null)).thenReturn("foovalue");
    when(mapObject.getStringValue("bar")).thenReturn("111");
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

  @Test
  void customPropertiesShoudBePassedToTheEntity() {
    Environment.registerCustomEntityType(CustomEntity.class);

    IMapObject mapObject = mock(IMapObject.class);
    when(mapObject.getType()).thenReturn("customEntity");
    when(mapObject.getId()).thenReturn(111);
    when(mapObject.getName()).thenReturn("somethin");
    when(mapObject.getLocation()).thenReturn(new Point(100, 100));
    when(mapObject.getWidth()).thenReturn(50f);
    when(mapObject.getHeight()).thenReturn(150f);

    Map<String, ICustomProperty> customProps = new HashMap<>(2);
    customProps.put("wasdd", new CustomProperty("11111"));
    customProps.put("zapp", new CustomProperty("33333"));
    when(mapObject.getProperties()).thenReturn(customProps);

    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    Environment env = new Environment(map);

    Collection<IEntity> loaded = env.load(mapObject);

    assertEquals(1, loaded.size());

    IEntity ent = loaded.iterator().next();
    assertTrue(ent instanceof CustomEntity);

    CustomEntity customEntity = (CustomEntity) ent;
    assertEquals("11111", customEntity.getProperties().getStringValue("wasdd"));
    assertEquals("33333", customEntity.getProperties().getStringValue("zapp"));
  }

  @Test
  void testMapObjectType() {
    String mapObject1 = null;
    String mapObject2 = "";

    assertNull(MapObjectType.get(mapObject1));
    assertNull(MapObjectType.get(mapObject2));
  }

  @EntityInfo(customMapObjectType = "customEntity")
  static class CustomEntity extends Entity {
    @TmxProperty(name = "foo")
    private String foo;

    @TmxProperty(name = "bar")
    private int bar;

    public CustomEntity(IMapObject mo) {}

    public String getFoo() {
      return this.foo;
    }

    public int getBar() {
      return this.bar;
    }
  }
}
