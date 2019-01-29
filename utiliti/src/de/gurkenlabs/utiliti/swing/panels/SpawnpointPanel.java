package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.Icons;

@SuppressWarnings("serial")
public class SpawnpointPanel extends PropertyPanel {
  private final JTextField textFieldType;
  private final JComboBox<Direction> comboBoxDirection;

  public SpawnpointPanel() {
    super("panel_spawnPoint", Icons.SPAWNPOINT);
    
    this.textFieldType = new JTextField();
    this.textFieldType.setColumns(10);

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<Direction>(Direction.values()));

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldType.setText("");
    this.comboBoxDirection.setSelectedItem(Direction.DOWN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldType.setText(mapObject.getStringValue(MapObjectProperty.SPAWN_TYPE));

    String direction = mapObject.getStringValue(MapObjectProperty.SPAWN_DIRECTION);
    if (direction != null && !direction.isEmpty()) {
      this.comboBoxDirection.setSelectedItem(Direction.valueOf(direction));
    }
  }

  private void setupChangedListeners() {
    this.setup(this.textFieldType, MapObjectProperty.SPAWN_TYPE);
    this.setup(this.comboBoxDirection, MapObjectProperty.SPAWN_DIRECTION);
  }
  
  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
        new LayoutItem("panel_direction", this.comboBoxDirection),
        new LayoutItem("panel_entity", this.textFieldType),
    };
    
    return this.createLayout(layoutItems);
  }
}
