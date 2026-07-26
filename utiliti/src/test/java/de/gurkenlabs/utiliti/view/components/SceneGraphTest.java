package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.lang.reflect.Method;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SceneGraphTest {

  @AfterEach
  void cleanup() throws Exception {
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

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

  @Test
  void resolvesRowsByVerticalBounds() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    root.add(new DefaultMutableTreeNode("short"));
    JTree tree = new JTree(root);
    tree.setSize(400, 100);

    int y = tree.getRowBounds(1).y + tree.getRowBounds(1).height / 2;

    assertEquals(1, SceneGraph.rowAtY(tree, y));
    assertEquals(-1, SceneGraph.rowAtY(tree, 99));
  }

  @Test
  void isolationChecksLayersAfterSelectedLayer() {
    SceneGraph sceneGraph = new SceneGraph();
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObjectLayer selected = new MapObjectLayer();
    MapObjectLayer other = new MapObjectLayer();
    map.addLayer(selected);
    map.addLayer(other);

    assertFalse(sceneGraph.isLayerIsolatedForTest(map, selected));

    other.setVisible(false);

    assertTrue(sceneGraph.isLayerIsolatedForTest(map, selected));
  }

  @Test
  void singleLayerIsNotConsideredIsolated() {
    SceneGraph sceneGraph = new SceneGraph();
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObjectLayer selected = new MapObjectLayer();
    map.addLayer(selected);

    assertFalse(sceneGraph.isLayerIsolatedForTest(map, selected));
  }

  @Test
  void layerVisibilityChangesPreserveCollapsedState() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    GroupLayer group = new GroupLayer();
    group.setVisible(true);
    MapObjectLayer child = new MapObjectLayer();
    group.addLayer(child);
    map.addLayer(group);
    Environment environment = new Environment(map);
    environment.init();
    Game.world().loadEnvironment(environment);
    GroupLayer loadedGroup = (GroupLayer) environment.getMap().getRenderLayers().getFirst();
    SceneGraph sceneGraph = new SceneGraph();

    SwingUtilities.invokeAndWait(sceneGraph::refresh);
    SwingUtilities.invokeAndWait(() -> sceneGraph.setLayerExpandedForTest(loadedGroup, false));
    SwingUtilities.invokeAndWait(() -> sceneGraph.toggleLayerVisibilityForTest(loadedGroup));

    assertFalse(loadedGroup.isVisible());
    assertFalse(sceneGraph.isLayerExpandedForTest(loadedGroup));

    SwingUtilities.invokeAndWait(sceneGraph::refresh);

    assertFalse(sceneGraph.isLayerExpandedForTest(loadedGroup));
  }

  @Test
  void rowActionDoubleClicksDoNotStartRename() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName("layer");
    map.addLayer(layer);
    Environment environment = new Environment(map);
    environment.init();
    Game.world().loadEnvironment(environment);
    var loadedLayer = environment.getMap().getMapObjectLayers().getFirst();
    SceneGraph sceneGraph = new SceneGraph();

    SwingUtilities.invokeAndWait(sceneGraph::refresh);
    SwingUtilities.invokeAndWait(() -> sceneGraph.doubleClickRowActionForTest(loadedLayer, true));

    assertFalse(sceneGraph.isRenamingForTest());

    SwingUtilities.invokeAndWait(() -> sceneGraph.doubleClickRowActionForTest(loadedLayer, false));

    assertFalse(sceneGraph.isRenamingForTest());
  }

}
