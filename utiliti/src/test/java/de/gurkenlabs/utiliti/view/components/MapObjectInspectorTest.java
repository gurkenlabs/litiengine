package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class MapObjectInspectorTest {

  @Test
  void coordinateSpinnerModelAllowsNegativeValues() {
    SpinnerNumberModel model = MapObjectInspector.createCoordinateSpinnerModel();

    model.setValue(-42.0);

    assertEquals(-42.0, model.getNumber().doubleValue());
  }

  @Test
  void propRenderWithLayerPropertyIsBoundToInspector() {
    MapObjectInspector inspector = new MapObjectInspector();
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setValue(MapObjectProperty.RENDERWITHLAYER, true);

    inspector.setControlValues(mapObject);

    JCheckBox renderWithLayer = findCheckBox(inspector, "Render with layer");
    assertNotNull(renderWithLayer);
    assertTrue(renderWithLayer.isSelected());
  }

  @Test
  void creatureRenderWithLayerPropertyIsBoundToInspector() {
    MapObjectInspector inspector = new MapObjectInspector();
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.CREATURE.name());
    mapObject.setValue(MapObjectProperty.RENDERWITHLAYER, true);

    inspector.setControlValues(mapObject);

    JCheckBox renderWithLayer = findCheckBox(inspector, "Render with layer");
    assertNotNull(renderWithLayer);
    assertTrue(renderWithLayer.isSelected());
  }

  @Test
  void renderWithLayerDisablesRenderTypeForProp() {
    MapObjectInspector inspector = new MapObjectInspector();
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setValue(MapObjectProperty.RENDERWITHLAYER, true);

    inspector.setControlValues(mapObject);

    assertTrue(findCheckBox(inspector, "Render with layer").isSelected());
    assertFalse(inspector.isRenderTypeEnabledForTest());
  }

  @Test
  void renderTypeIsEnabledForPropWhenNotRenderingWithLayer() {
    MapObjectInspector inspector = new MapObjectInspector();
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setValue(MapObjectProperty.RENDERWITHLAYER, false);

    inspector.setControlValues(mapObject);

    assertTrue(inspector.isRenderTypeEnabledForTest());
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
