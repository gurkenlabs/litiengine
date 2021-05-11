package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.EntityPivotType;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.swing.Icons;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class SpawnpointPanel extends PropertyPanel {
  private final JTextField textFieldInfo;
  private final JSpinner spinnerOffsetX;
  private final JSpinner spinnerOffsetY;
  private final JComboBox<Direction> comboBoxDirection;
  private final JComboBox<EntityPivotType> comboBoxPivot;

  public SpawnpointPanel() {
    super("panel_spawnPoint", Icons.SPAWNPOINT);

    this.textFieldInfo = new JTextField();
    this.textFieldInfo.setColumns(10);
    this.spinnerOffsetX = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.spinnerOffsetY = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<Direction>(Direction.values()));

    this.comboBoxPivot = new JComboBox<>();
    this.comboBoxPivot.setModel(
        new DefaultComboBoxModel<EntityPivotType>(EntityPivotType.values()));

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldInfo.setText("");
    this.spinnerOffsetX.setValue(0.0);
    this.spinnerOffsetY.setValue(0.0);
    this.comboBoxDirection.setSelectedItem(Direction.DOWN);
    this.comboBoxPivot.setSelectedItem(EntityPivotType.DIMENSION_CENTER);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldInfo.setText(mapObject.getStringValue(MapObjectProperty.SPAWN_INFO));
    this.spinnerOffsetX.setValue(mapObject.getDoubleValue(MapObjectProperty.SPAWN_PIVOT_OFFSETX));
    this.spinnerOffsetY.setValue(mapObject.getDoubleValue(MapObjectProperty.SPAWN_PIVOT_OFFSETY));
    this.comboBoxDirection.setSelectedItem(
        mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.DOWN));
    this.comboBoxPivot.setSelectedItem(
        mapObject.getEnumValue(
            MapObjectProperty.SPAWN_PIVOT,
            EntityPivotType.class,
            EntityPivotType.DIMENSION_CENTER));
  }

  private void setupChangedListeners() {
    this.setup(this.textFieldInfo, MapObjectProperty.SPAWN_INFO);
    this.setup(this.spinnerOffsetX, MapObjectProperty.SPAWN_PIVOT_OFFSETX);
    this.setup(this.spinnerOffsetY, MapObjectProperty.SPAWN_PIVOT_OFFSETY);
    this.setup(this.comboBoxDirection, MapObjectProperty.SPAWN_DIRECTION);
    this.setup(this.comboBoxPivot, MapObjectProperty.SPAWN_PIVOT);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_direction", this.comboBoxDirection),
            new LayoutItem("panel_pivot", this.comboBoxPivot),
            new LayoutItem("panel_pivotOffsetX", this.spinnerOffsetX),
            new LayoutItem("panel_pivotOffsetY", this.spinnerOffsetY),
            new LayoutItem("panel_entity", this.textFieldInfo),
        };

    return this.createLayout(layoutItems);
  }
}
