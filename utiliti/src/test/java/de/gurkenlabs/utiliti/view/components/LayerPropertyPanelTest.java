package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LayerPropertyPanelTest {

  @AfterEach
  void cleanup() throws Exception {
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void removeUnsetPropertiesDeletesMissingLayerProperties() {
    MapObjectLayer layer = new MapObjectLayer();
    layer.setValue("keep", "1");
    layer.setValue("remove", "2");

    LayerPropertyPanel.removeUnsetProperties(layer, List.of("keep"));

    assertTrue(layer.hasCustomProperty("keep"));
    assertFalse(layer.hasCustomProperty("remove"));
  }

  @Test
  void bindMapObjectLayerDoesNotSaveWhenApplyingLayerColor() {
    LayerPropertyPanel panel = new LayerPropertyPanel();
    MapObjectLayer layer = new MapObjectLayer();
    layer.setColor(Color.RED);

    assertDoesNotThrow(() -> panel.bind(layer));
  }

  @Test
  void visibilityCheckboxIsRenderedAndUpdatesLayerVisibility() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("layer-visibility-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName("layer");
    map.addLayer(layer);
    Game.world().loadEnvironment(map);

    LayerPropertyPanel panel = new LayerPropertyPanel();
    panel.bind(layer);
    JCheckBox visible = findCheckBox(panel, "Visible");

    assertNotNull(visible);
    assertTrue(layer.isVisible());
    visible.doClick();
    assertFalse(layer.isVisible());
  }

  private static JCheckBox findCheckBox(Container container, String text) {
    for (Component component : container.getComponents()) {
      if (component instanceof JCheckBox checkBox && text.equals(checkBox.getText())) {
        return checkBox;
      }
      if (component instanceof Container child) {
        JCheckBox found = findCheckBox(child, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
