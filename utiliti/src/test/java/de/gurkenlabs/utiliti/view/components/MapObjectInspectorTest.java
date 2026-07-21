package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class MapObjectInspectorTest {

  @Test
  void inspectorContentTracksNarrowViewportWidth() {
    MapObjectInspector inspector = new MapObjectInspector();

    JScrollPane scrollPane = findComponent(inspector, JScrollPane.class);

    assertNotNull(scrollPane);
    assertTrue(scrollPane.getViewport().getView() instanceof Scrollable scrollable
        && scrollable.getScrollableTracksViewportWidth());
  }

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

  @Test
  void sameTypeMultiEditShowsTypePanelButHidesSingleObjectControls() {
    MapObjectInspector inspector = new MapObjectInspector();
    MapObject first = mapObject(MapObjectType.PROP);
    MapObject second = mapObject(MapObjectType.PROP);

    inspector.bindAll(List.of(first, second));

    assertTrue(inspector.isTypeCardVisibleForTest());
    assertFalse(inspector.isCustomCardVisibleForTest());
    assertFalse(inspector.areTransformControlsEnabledForTest());
  }

  @Test
  void mixedTypeMultiEditHidesTypeSpecificPanels() {
    MapObjectInspector inspector = new MapObjectInspector();

    inspector.bindAll(List.of(mapObject(MapObjectType.PROP), mapObject(MapObjectType.TRIGGER)));

    assertFalse(inspector.isTypeCardVisibleForTest());
    assertFalse(inspector.isCustomCardVisibleForTest());
    assertFalse(inspector.areTransformControlsEnabledForTest());
  }

  @Test
  void returningToSingleEditRestoresSingleObjectControls() {
    MapObjectInspector inspector = new MapObjectInspector();
    MapObject mapObject = mapObject(MapObjectType.PROP);
    inspector.bindAll(List.of(mapObject, mapObject(MapObjectType.PROP)));

    inspector.bind(mapObject);

    assertTrue(inspector.isCustomCardVisibleForTest());
    assertTrue(inspector.areTransformControlsEnabledForTest());
  }

  @Test
  void singlePropBindingPreservesSpecializedPanelSetup() {
    Resources.spritesheets().clear();
    new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "prop-crate.png", 1, 1);
    MapObjectInspector inspector = new MapObjectInspector();

    inspector.bind(mapObject(MapObjectType.PROP));

    PropPanel propPanel = (PropPanel) inspector.getCurrentPanelForTest();
    assertEquals(1, propPanel.getSpriteItemCountForTest());
    Resources.spritesheets().clear();
  }

  private static MapObject mapObject(MapObjectType type) {
    MapObject mapObject = new MapObject();
    mapObject.setType(type.name());
    return mapObject;
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

  private static <T extends Component> T findComponent(Container container, Class<T> type) {
    for (Component component : container.getComponents()) {
      if (type.isInstance(component)) {
        return type.cast(component);
      }
      if (component instanceof Container child) {
        T found = findComponent(child, type);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
