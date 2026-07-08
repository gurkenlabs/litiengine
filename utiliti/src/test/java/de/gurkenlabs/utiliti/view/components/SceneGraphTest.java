package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
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
}
