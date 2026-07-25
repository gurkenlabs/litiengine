package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PropertyPanelMultiEditTest {
  private static final String PROPERTY = "multi-edit-test";

  @Test
  void propertyChangeIsAppliedToAllBoundMapObjects() {
    initializeGame();
    MapObject first = new MapObject();
    first.setId(1);
    MapObject second = new MapObject();
    second.setId(2);
    TestPanel panel = new TestPanel();
    panel.bindAll(List.of(first, second));

    panel.selectProperty();

    assertTrue(first.getBoolValue(PROPERTY, false));
    assertTrue(second.getBoolValue(PROPERTY, false));
  }

  @Test
  void unchangedMixedTextDoesNotOverwriteSecondaryValue() {
    initializeGame();
    MapObject first = new MapObject();
    first.setValue(PROPERTY, "first");
    MapObject second = new MapObject();
    second.setValue(PROPERTY, "second");
    TestPanel panel = new TestPanel();
    panel.bindAll(List.of(first, second));

    panel.loseTextFocusWithoutEditing();

    assertEquals("first", first.getStringValue(PROPERTY, null));
    assertEquals("second", second.getStringValue(PROPERTY, null));
  }

  @Test
  void rebindingFocusedTextDoesNotCountAsUserEditing() {
    initializeGame();
    MapObject initial = new MapObject();
    initial.setValue(PROPERTY, "initial");
    MapObject first = new MapObject();
    first.setValue(PROPERTY, "first");
    MapObject second = new MapObject();
    second.setValue(PROPERTY, "second");
    TestPanel panel = new TestPanel();
    panel.bind(initial);

    panel.rebindWhileTextFocused(List.of(first, second));

    assertEquals("first", first.getStringValue(PROPERTY, null));
    assertEquals("second", second.getStringValue(PROPERTY, null));
  }

  @Test
  void multiEditDoesNotEndAnExistingUndoOperation() {
    initializeGame();
    MapObject first = new MapObject();
    first.setId(1);
    MapObject second = new MapObject();
    second.setId(2);
    TestPanel panel = new TestPanel();
    panel.bindAll(List.of(first, second));
    UndoManager undoManager = UndoManager.instance();
    assertTrue(undoManager.tryBeginOperation());

    panel.selectProperty();

    assertFalse(undoManager.tryBeginOperation());
    undoManager.endOperation();
  }

  private static void initializeGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("property-panel-multi-edit-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
  }

  @AfterEach
  void cleanup() throws Exception {
    UndoManager.clearAll();
    var terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  private static final class TestPanel extends PropertyPanel {
    private final JCheckBox control = new JCheckBox();
    private final JTextField text = new JTextField();

    private TestPanel() {
      setup(this.control, PROPERTY);
      setup(this.text, PROPERTY);
    }

    private void selectProperty() {
      this.control.doClick();
    }

    private void loseTextFocusWithoutEditing() {
      notifyTextFocusListeners(java.awt.event.FocusEvent.FOCUS_GAINED);
      notifyTextFocusListeners(java.awt.event.FocusEvent.FOCUS_LOST);
    }

    private void rebindWhileTextFocused(List<IMapObject> mapObjects) {
      notifyTextFocusListeners(java.awt.event.FocusEvent.FOCUS_GAINED);
      this.text.setText("edited before rebind");
      bindAll(mapObjects);
      notifyTextFocusListeners(java.awt.event.FocusEvent.FOCUS_LOST);
    }

    private void notifyTextFocusListeners(int eventId) {
      var event = new java.awt.event.FocusEvent(this.text, eventId);
      for (java.awt.event.FocusListener listener : this.text.getFocusListeners()) {
        if (eventId == java.awt.event.FocusEvent.FOCUS_GAINED) {
          listener.focusGained(event);
        } else {
          listener.focusLost(event);
        }
      }
    }

    @Override
    protected void clearControls() {
      this.control.setSelected(false);
      this.text.setText("");
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.control.setSelected(mapObject.getBoolValue(PROPERTY, false));
      this.text.setText(mapObject.getStringValue(PROPERTY, ""));
    }

    @Override
    protected void updateEnvironment() {
      // The batch mutation is isolated from entity reloading in this component test.
    }
  }
}
