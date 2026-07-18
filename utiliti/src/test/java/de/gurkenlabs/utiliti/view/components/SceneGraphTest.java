package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import javax.swing.tree.TreeSelectionModel;
import org.junit.jupiter.api.Test;

class SceneGraphTest {

  @Test
  void supportsDiscontiguousSelection() {
    SceneGraph sceneGraph = new SceneGraph();

    assertEquals(
        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION,
        sceneGraph.getSelectionModeForTest());
  }

  @Test
  void displaysLastRenderedLayerOnTop() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObjectLayer bottom = new MapObjectLayer();
    MapObjectLayer middle = new MapObjectLayer();
    GroupLayer top = new GroupLayer();
    MapObjectLayer bottomChild = new MapObjectLayer();
    MapObjectLayer topChild = new MapObjectLayer();
    top.addLayer(bottomChild);
    top.addLayer(topChild);
    map.addLayer(bottom);
    map.addLayer(middle);
    map.addLayer(top);

    assertSame(top, SceneGraph.layersInDisplayOrder(map).get(0));
    assertSame(middle, SceneGraph.layersInDisplayOrder(map).get(1));
    assertSame(bottom, SceneGraph.layersInDisplayOrder(map).get(2));
    assertSame(topChild, SceneGraph.layersInDisplayOrder(top).get(0));
    assertSame(bottomChild, SceneGraph.layersInDisplayOrder(top).get(1));
  }

  @Test
  void selectNullDoesNotClearSelectionDuringTreeFocusChange() {
    SceneGraph sceneGraph = new SceneGraph();
    sceneGraph.selectLayerNodeForTest(new MapObjectLayer());

    sceneGraph.setFocussingForTest(true);
    sceneGraph.select(null);

    assertTrue(sceneGraph.hasTreeSelectionForTest());
  }

  @Test
  void mapSynchronizationDoesNotReplaceLayerDuringTreeFocusChange() {
    SceneGraph sceneGraph = new SceneGraph();
    MapObjectLayer layer = new MapObjectLayer();
    sceneGraph.addMapNodeForTest(new TmxMap(MapOrientations.ORTHOGONAL));
    sceneGraph.selectLayerNodeForTest(layer);

    sceneGraph.setFocussingForTest(true);
    sceneGraph.selectMap();

    assertSame(layer, sceneGraph.getSelectedLayerForTest());
  }

  @Test
  void numericSearchOutsideIntegerRangeIsIgnoredSafely() {
    assertNull(SceneGraph.parseSearchIdForTest("999999999999999999999"));
    assertEquals(42, SceneGraph.parseSearchIdForTest("42"));
  }

  @Test
  void clearRemovesSelectedAndExpandedLayerState() {
    SceneGraph sceneGraph = new SceneGraph();
    TmxMap deletedMap = new TmxMap(MapOrientations.ORTHOGONAL);
    TmxMap retainedMap = new TmxMap(MapOrientations.ORTHOGONAL);
    sceneGraph.cacheLayerStateForTest(deletedMap);
    sceneGraph.cacheLayerStateForTest(retainedMap);

    sceneGraph.clearMapState(deletedMap);

    assertTrue(!sceneGraph.hasCachedLayerStateForTest(deletedMap));
    assertTrue(sceneGraph.hasCachedLayerStateForTest(retainedMap));

    sceneGraph.clear();

    assertTrue(!sceneGraph.hasCachedLayerStateForTest(retainedMap));
  }

}
