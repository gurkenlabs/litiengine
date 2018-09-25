package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;

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
    p.setObstacle(true);
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
    assertEquals("test,test2", mapObject.getString(MapObjectProperty.TAGS));

    assertEquals(true, mapObject.getBool(MapObjectProperty.COMBAT_INDESTRUCTIBLE));
    assertEquals(2, mapObject.getInt(MapObjectProperty.COMBAT_TEAM));

    assertEquals(Align.CENTER_LEFT, mapObject.getEnum(MapObjectProperty.COLLISION_ALIGN, Align.class));
    assertEquals(Valign.MIDDLE_DOWN, mapObject.getEnum(MapObjectProperty.COLLISION_VALIGN, Valign.class));
    assertEquals(true, mapObject.getBool(MapObjectProperty.COLLISION));
    assertEquals(5, mapObject.getFloat(MapObjectProperty.COLLISIONBOX_WIDTH));
    assertEquals(5, mapObject.getFloat(MapObjectProperty.COLLISIONBOX_HEIGHT));

    assertEquals(Material.CERAMIC, mapObject.getEnum(MapObjectProperty.PROP_MATERIAL, Material.class));
    assertEquals(true, mapObject.getBool(MapObjectProperty.PROP_OBSTACLE));
    assertEquals(true, mapObject.getBool(MapObjectProperty.PROP_ADDSHADOW));
    assertEquals(Rotation.ROTATE_270, mapObject.getEnum(MapObjectProperty.PROP_ROTATION, Rotation.class));
    assertEquals(true, mapObject.getBool(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    assertEquals(true, mapObject.getBool(MapObjectProperty.PROP_FLIPVERTICALLY));
    assertEquals(true, mapObject.getBool(MapObjectProperty.PROP_SCALE));
  }

  @Test
  public void testTmxPropertyAnnotation() {
    IMapObject mapObject = MapObjectSerializer.serialize(new TestProp());

    assertEquals(true, mapObject.getBool("testBool"));
    assertEquals(1, mapObject.getInt("testInt"));
    assertEquals(2, mapObject.getShort("testShort"));
    assertEquals(3, mapObject.getLong("testLong"));
    assertEquals(4, mapObject.getByte("testByte"));
    assertEquals(5.5, mapObject.getDouble("testDouble"));
    assertEquals(6.6f, mapObject.getFloat("testFloat"));
    assertEquals("test", mapObject.getString("testString"));
    
    assertEquals("false,false", mapObject.getString("testBoolArr"));
    assertEquals("0", mapObject.getString("testIntArr"));
    assertEquals("0,0", mapObject.getString("testShortArr"));
    assertEquals("0,0,0", mapObject.getString("testLongArr"));
    assertEquals("0,0,0,0", mapObject.getString("testByteArr"));
    assertEquals("0.0,0.0", mapObject.getString("testDoubleArr"));
    assertEquals("0,0,0,0", mapObject.getString("testByteArr"));
    assertEquals("0.0,0.0", mapObject.getString("testFloatArr"));
    assertEquals("null,null", mapObject.getString("testStringArr"));
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
