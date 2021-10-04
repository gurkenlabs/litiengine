package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.physics.Collision;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MapObjectSerializerTests {

  @Test
  public void testSerialization() {
    Prop p = new Prop("something");

    // general entity fields
    p.setName("testname");
    p.setX(10);
    p.setY(10.5);
    p.setMapId(123);
    p.addTag("test");
    p.addTag("test2");

    // combat fields
    p.setIndestructible(true);
    p.setTeam(2);

    // collision fields
    p.setCollisionBoxAlign(Align.CENTER_LEFT);
    p.setCollisionBoxValign(Valign.MIDDLE_DOWN);
    p.setCollision(true);
    p.setCollisionBoxHeight(5);
    p.setCollisionBoxWidth(5);

    // prop fields
    p.setMaterial(Material.CERAMIC);
    p.setCollisionType(Collision.STATIC);
    p.setAddShadow(true);
    p.setSpriteRotation(Rotation.ROTATE_270);
    p.setFlipHorizontally(true);
    p.setFlipVertically(true);
    p.setScaling(true);

    IMapObject mapObject = MapObjectSerializer.serialize(p);

    assertEquals("testname", mapObject.getName());
    assertEquals(10, mapObject.getX());
    assertEquals(10.5, mapObject.getY());
    assertEquals(123, mapObject.getId());
    assertEquals("test,test2", mapObject.getStringValue(MapObjectProperty.TAGS));

    assertEquals(true, mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE));
    assertEquals(2, mapObject.getIntValue(MapObjectProperty.COMBAT_TEAM));

    assertEquals(
        Align.CENTER_LEFT, mapObject.getEnumValue(MapObjectProperty.COLLISION_ALIGN, Align.class));
    assertEquals(
        Valign.MIDDLE_DOWN,
        mapObject.getEnumValue(MapObjectProperty.COLLISION_VALIGN, Valign.class));
    assertEquals(true, mapObject.getBoolValue(MapObjectProperty.COLLISION));
    assertEquals(5, mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_WIDTH));
    assertEquals(5, mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_HEIGHT));

    assertEquals(
        Material.CERAMIC, Material.get(mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL)));
    assertEquals(
        Collision.STATIC,
        mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class));
    assertEquals(true, mapObject.getBoolValue(MapObjectProperty.PROP_ADDSHADOW));
    assertEquals(
        Rotation.ROTATE_270,
        mapObject.getEnumValue(MapObjectProperty.PROP_ROTATION, Rotation.class));
    assertEquals(true, mapObject.getBoolValue(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    assertEquals(true, mapObject.getBoolValue(MapObjectProperty.PROP_FLIPVERTICALLY));
    assertEquals(true, mapObject.getBoolValue(MapObjectProperty.SCALE_SPRITE));
  }

  @Test
  public void testTmxPropertyAnnotationBoolean() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertTrue(mapObject.getBoolValue("testBool"));
  }

  @Test
  public void testTmxPropertyAnnotationInt() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(1, mapObject.getIntValue("testInt"));
  }

  @Test
  public void testTmxPropertyAnnotationShort() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(2, mapObject.getShortValue("testShort"));
  }

  @Test
  public void testTmxPropertyAnnotationLong() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(3, mapObject.getLongValue("testLong"));
  }

  @Test
  public void testTmxPropertyAnnotationByte() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(4, mapObject.getByteValue("testByte"));
  }

  @Test
  public void testTmxPropertyAnnotationDouble() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(5.5, mapObject.getDoubleValue("testDouble"));
  }

  @Test
  public void testTmxPropertyAnnotationFloat() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(6.6f, mapObject.getFloatValue("testFloat"));
  }

  @ParameterizedTest
  @MethodSource("getTmxPropertyAnnotationString")
  public void testTmxPropertyAnnotationString(String propertyName, String expectedValue) {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());
    assertEquals(expectedValue, mapObject.getStringValue(propertyName));
  }

  private static Stream<Arguments> getTmxPropertyAnnotationString() {
    return Stream.of(
        Arguments.of("testString", "test"),
        Arguments.of("testBoolArr", "false,false"),
        Arguments.of("testIntArr", "0"),
        Arguments.of("testShortArr", "0,0"),
        Arguments.of("testLongArr", "0,0,0"),
        Arguments.of("testByteArr", "0,0,0,0"),
        Arguments.of("testDoubleArr", "0.0,0.0"),
        Arguments.of("testByteArr", "0,0,0,0"),
        Arguments.of("testFloatArr", "0.0,0.0"),
        Arguments.of("testStringArr", "null,null"));
  }

  private class TestProp extends Prop {
    @TmxProperty(name = "testBool")
    private boolean testBool = true;

    @TmxProperty(name = "testInt")
    private int testInt = 1;

    @TmxProperty(name = "testShort")
    private short testShort = 2;

    @TmxProperty(name = "testLong")
    private long testLong = 3;

    @TmxProperty(name = "testByte")
    private byte testByte = 4;

    @TmxProperty(name = "testDouble")
    private double testDouble = 5.5;

    @TmxProperty(name = "testFloat")
    private float testFloat = 6.6f;

    @TmxProperty(name = "testString")
    private String testString = "test";

    @TmxProperty(name = "testBoolArr")
    private boolean[] testBoolArr = new boolean[2];

    @TmxProperty(name = "testIntArr")
    private int[] testIntArr = new int[1];

    @TmxProperty(name = "testShortArr")
    private short[] testShortArr = new short[2];

    @TmxProperty(name = "testLongArr")
    private long[] testLongArr = new long[3];

    @TmxProperty(name = "testByteArr")
    private byte[] testByteArr = new byte[4];

    @TmxProperty(name = "testDoubleArr")
    private double[] testDoubleArr = new double[2];

    @TmxProperty(name = "testFloatArr")
    private float[] testFloatArr = new float[2];

    @TmxProperty(name = "testStringArr")
    private String[] testStringArr = new String[2];

    public TestProp() {
      super("test-prop");
    }
  }
}
