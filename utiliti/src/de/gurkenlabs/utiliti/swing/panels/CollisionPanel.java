package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;

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
    super("panel_collisionEntity");

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
    JLabel lblWidth = new JLabel(Resources.strings().get("panel_width"));
    JLabel lblHeight = new JLabel(Resources.strings().get("panel_height"));
    JLabel lblHorizontalAlignment = new JLabel("align");
    JLabel lblVerticalAlignment = new JLabel("valign");
    
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                .addComponent(lblWidth, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE)
                .addComponent(lblHeight, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE)
                .addComponent(lblHorizontalAlignment, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE)
                .addComponent(lblVerticalAlignment, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE))
            .addPreferredGap(ComponentPlacement.RELATED, LABEL_GAP, Short.MAX_VALUE)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(spinnerHeight, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
                .addComponent(spinnerWidth, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
                .addComponent(comboBoxAlign, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
                .addComponent(comboBoxValign, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
                .addComponent(chckbxHasCollision, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE))
            .addGap(CONTROL_MARGIN)));

    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(comboBoxAlign, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblHorizontalAlignment, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(comboBoxValign, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblVerticalAlignment, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addComponent(chckbxHasCollision, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)));
    
    return groupLayout;
  }
}