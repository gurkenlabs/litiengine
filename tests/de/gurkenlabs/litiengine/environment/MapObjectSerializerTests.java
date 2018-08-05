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
    assertEquals("test,test2", mapObject.getCustomProperty(MapObjectProperty.TAGS));

    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.COMBAT_INDESTRUCTIBLE));
    assertEquals(2, mapObject.getCustomPropertyInt(MapObjectProperty.COMBAT_TEAM));

    assertEquals(Align.CENTER_LEFT, mapObject.getCustomPropertyEnum(MapObjectProperty.COLLISION_ALIGN, Align.class));
    assertEquals(Valign.MIDDLE_DOWN, mapObject.getCustomPropertyEnum(MapObjectProperty.COLLISION_VALIGN, Valign.class));
    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION));
    assertEquals(5, mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH));
    assertEquals(5, mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT));

    assertEquals(Material.CERAMIC, mapObject.getCustomPropertyEnum(MapObjectProperty.PROP_MATERIAL, Material.class));
    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.PROP_OBSTACLE));
    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.PROP_ADDSHADOW));
    assertEquals(Rotation.ROTATE_270, mapObject.getCustomPropertyEnum(MapObjectProperty.PROP_ROTATION, Rotation.class));
    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.PROP_FLIPVERTICALLY));
    assertEquals(true, mapObject.getCustomPropertyBool(MapObjectProperty.PROP_SCALE));
  }
}
