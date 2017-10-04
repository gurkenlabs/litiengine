package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.awt.Point;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
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
    when(mapObject.getLocation()).thenReturn(new Point(0, 0));
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

    Prop prop = (Prop) ent;

    Assert.assertEquals(prop.getMaterial(), Material.PLASTIC);
    Assert.assertEquals(prop.isIndestructible(), true);
    Assert.assertEquals(prop.hasCollision(), true);
    Assert.assertEquals(prop.getAttributes().getHealth().getMaxValue().intValue(), 100);
    Assert.assertEquals(prop.getAttributes().getHealth().getCurrentValue().intValue(), 100);

    Assert.assertEquals(prop.getCollisionBoxWidth(), 100.0, 0.0001);
    Assert.assertEquals(prop.getCollisionBoxHeight(), 100.0, 0.0001);
    Assert.assertEquals(prop.getCollisionBoxAlign(), Align.LEFT);
    Assert.assertEquals(prop.getCollisionBoxValign(), Valign.MIDDLE);
    Assert.assertEquals(prop.getTeam(), 1);
  }
}
