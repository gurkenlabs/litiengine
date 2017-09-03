package de.gurkenlabs.utiLITI;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.CollisionEntity.CollisionAlign;
import de.gurkenlabs.litiengine.entities.CollisionEntity.CollisionValign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;

public class CollisionPanel extends PropertyPanel<IMapObject> {
  private JCheckBox chckbxHasCollision;
  JSpinner spinnerWidth;
  JSpinner spinnerHeight;
  JComboBox comboBoxAlign;
  JComboBox comboBoxValign;

  /**
   * Create the panel.
   */
  public CollisionPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_collisionEntity"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblHealth = new JLabel(Resources.get("panel_collision"));

    JLabel lblMaterial = new JLabel(Resources.get("panel_width"));

    chckbxHasCollision = new JCheckBox(" ");

    JLabel lblHeightFactor = new JLabel(Resources.get("panel_height"));

    spinnerWidth = new JSpinner();
    spinnerWidth.setModel(new SpinnerNumberModel(new Float(0), new Float(0), null, new Float(0.5)));

    spinnerHeight = new JSpinner();
    spinnerHeight.setModel(new SpinnerNumberModel(new Float(0), new Float(0), null, new Float(0.5)));

    comboBoxAlign = new JComboBox();
    comboBoxAlign.setModel(new DefaultComboBoxModel(CollisionAlign.values()));

    comboBoxValign = new JComboBox();
    comboBoxValign.setModel(new DefaultComboBoxModel(CollisionValign.values()));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(chckbxHasCollision))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(spinnerWidth))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblHeightFactor)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(comboBoxAlign, 0, 53, Short.MAX_VALUE)
                    .addComponent(comboBoxValign, 0, 53, Short.MAX_VALUE))
                .addContainerGap()));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(chckbxHasCollision))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxAlign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblHeightFactor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxValign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.chckbxHasCollision.addActionListener(new MapObjectPropertyActionListener((m) -> {
      m.setCustomProperty(MapObjectProperties.COLLISION, Boolean.toString(chckbxHasCollision.isSelected()));
    }));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH, this.spinnerWidth.getValue().toString());
    }));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT, this.spinnerHeight.getValue().toString());
    }));

    this.comboBoxAlign.addActionListener(new MapObjectPropertyActionListener((m) -> {
      CollisionAlign align = (CollisionAlign) this.comboBoxAlign.getSelectedItem();
      m.setCustomProperty(MapObjectProperties.COLLISIONALGIN, align.toString());
    }));

    this.comboBoxValign.addActionListener(new MapObjectPropertyActionListener((m) -> {
      CollisionValign valign = (CollisionValign) this.comboBoxValign.getSelectedItem();
      m.setCustomProperty(MapObjectProperties.COLLISIONVALGIN, valign.toString());
    }));
  }

  @Override
  protected void clearControls() {
    this.chckbxHasCollision.setSelected(false);
    this.spinnerWidth.setValue(0);
    this.spinnerHeight.setValue(0);
    this.comboBoxAlign.setSelectedItem(CollisionAlign.CENTER);
    this.comboBoxAlign.setSelectedItem(CollisionValign.DOWN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISION) != null) {
      this.chckbxHasCollision.setSelected(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH) != null) {
      this.spinnerWidth.setValue(Float.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH)));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT) != null) {
      this.spinnerHeight.setValue(Float.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT)));
    }

    this.comboBoxAlign.setSelectedItem(CollisionAlign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN)));
    this.comboBoxValign.setSelectedItem(CollisionValign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN)));
  }
}
