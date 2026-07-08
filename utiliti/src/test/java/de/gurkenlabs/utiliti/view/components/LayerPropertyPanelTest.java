package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import java.util.List;
import org.junit.jupiter.api.Test;

class LayerPropertyPanelTest {

  @Test
  void removeUnsetPropertiesDeletesMissingLayerProperties() {
    MapObjectLayer layer = new MapObjectLayer();
    layer.setValue("keep", "1");
    layer.setValue("remove", "2");

    LayerPropertyPanel.removeUnsetProperties(layer, List.of("keep"));

    assertTrue(layer.hasCustomProperty("keep"));
    assertFalse(layer.hasCustomProperty("remove"));
  }
}
