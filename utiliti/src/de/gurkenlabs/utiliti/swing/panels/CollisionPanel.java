package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.Icons;

@SuppressWarnings("serial")
public class CollisionPanel extends PropertyPanel {
  private final JCheckBox chckbxHasCollision;
  private final JSpinner spinnerWidth;
  private final JSpinner spinnerHeight;
  private final JComboBox<Align> comboBoxAlign;
  private final JComboBox<Valign> comboBoxValign;

  /**
   * Create the panel.
   */
  public CollisionPanel() {
    super("panel_collisionEntity", Icons.COLLISIONBOX);

    this.chckbxHasCollision = new JCheckBox("collision");
    this.spinnerWidth = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.spinnerHeight = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.comboBoxAlign = new JComboBox<>();
    this.comboBoxAlign.setModel(new DefaultComboBoxModel<>(Align.values()));
    this.comboBoxValign = new JComboBox<>();
    this.comboBoxValign.setModel(new DefaultComboBoxModel<>(Valign.values()));
    
    this.setLayout(this.createLayout());
    this.setupChangedListeners();
  }
  
  @Override
  protected void clearControls() {
    this.chckbxHasCollision.setSelected(false);
    this.spinnerWidth.setValue(0.0);
    this.spinnerHeight.setValue(0.0);
    this.comboBoxAlign.setSelectedItem(Align.CENTER);
    this.comboBoxValign.setSelectedItem(Valign.DOWN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxHasCollision.setSelected(mapObject.getBoolValue(MapObjectProperty.COLLISION));
    this.spinnerWidth.setValue(mapObject.getDoubleValue(MapObjectProperty.COLLISIONBOX_WIDTH));
    this.spinnerHeight.setValue(mapObject.getDoubleValue(MapObjectProperty.COLLISIONBOX_HEIGHT));
    this.comboBoxAlign.setSelectedItem(Align.get(mapObject.getStringValue(MapObjectProperty.COLLISION_ALIGN)));
    this.comboBoxValign.setSelectedItem(Valign.get(mapObject.getStringValue(MapObjectProperty.COLLISION_VALIGN)));
  }
  
  private void setupChangedListeners() {
    this.setup(this.chckbxHasCollision, MapObjectProperty.COLLISION);
    this.setup(this.spinnerWidth, MapObjectProperty.COLLISIONBOX_WIDTH);
    this.setup(this.spinnerHeight, MapObjectProperty.COLLISIONBOX_HEIGHT);
    this.setup(this.comboBoxAlign, MapObjectProperty.COLLISION_ALIGN);
    this.setup(this.comboBoxValign, MapObjectProperty.COLLISION_VALIGN);
  }
  
  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
        new LayoutItem("panel_width", this.spinnerWidth),
        new LayoutItem("panel_height", this.spinnerHeight),
        new LayoutItem("panel_align", this.comboBoxAlign),
        new LayoutItem("panel_valign", this.comboBoxValign),
    };
    
    return this.createLayout(layoutItems, this.chckbxHasCollision);
  }
}