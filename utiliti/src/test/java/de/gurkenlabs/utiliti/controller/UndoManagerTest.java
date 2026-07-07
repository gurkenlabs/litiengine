package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UndoManagerTest {

  @Test
  void restoreStateMovesMapObjectBackToSnapshotLayer() throws Exception {
    MapObjectLayer sourceLayer = new MapObjectLayer();
    MapObjectLayer targetLayer = new MapObjectLayer();
    MapObject target = new MapObject();
    target.setId(1);
    target.setName("target");
    targetLayer.addMapObject(target);

    MapObject snapshot = new MapObject();
    snapshot.setId(1);
    snapshot.setName("snapshot");
    setSnapshotLayer(snapshot, sourceLayer);

    restoreState(target, snapshot);

    assertSame(sourceLayer, target.getLayer());
    assertTrue(sourceLayer.getMapObjects().contains(target));
    assertFalse(targetLayer.getMapObjects().contains(target));
    assertEquals("snapshot", target.getName());
  }

  @Test
  void restoreStateRemovesMapObjectFromLayerWhenSnapshotHasNoLayer() throws Exception {
    MapObjectLayer targetLayer = new MapObjectLayer();
    MapObject target = new MapObject();
    target.setId(1);
    targetLayer.addMapObject(target);

    MapObject snapshot = new MapObject();
    snapshot.setId(1);

    restoreState(target, snapshot);

    assertNull(target.getLayer());
    assertFalse(targetLayer.getMapObjects().contains(target));
  }

  private static void restoreState(IMapObject target, IMapObject snapshot) throws Exception {
    Method restoreState = UndoManager.class.getDeclaredMethod("restoreState", IMapObject.class, IMapObject.class);
    restoreState.setAccessible(true);
    restoreState.invoke(null, target, snapshot);
  }

  private static void setSnapshotLayer(MapObject object, MapObjectLayer layer) throws Exception {
    Field layerField = MapObject.class.getDeclaredField("layer");
    layerField.setAccessible(true);
    layerField.set(object, layer);
  }
}
