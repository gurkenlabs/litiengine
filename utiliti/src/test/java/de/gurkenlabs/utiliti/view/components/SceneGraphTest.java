package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import org.junit.jupiter.api.Test;

class SceneGraphTest {

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

}
