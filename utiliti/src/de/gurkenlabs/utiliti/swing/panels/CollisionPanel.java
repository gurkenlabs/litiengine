package de.gurkenlabs.utiliti.swing.panels;

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

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;

public class CollisionPanel extends PropertyPanel<IMapObject> {
  private JCheckBox chckbxHasCollision;
  JSpinner spinnerWidth;
  JSpinner spinnerHeight;
  JComboBox<Align> comboBoxAlign;
  JComboBox<Valign> comboBoxValign;

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
    spinnerWidth.setModel(new SpinnerNumberModel(0, 0, null, 0.5f));

    spinnerHeight = new JSpinner();
    spinnerHeight.setModel(new SpinnerNumberModel(0, 0, null, 0.5f));

    comboBoxAlign = new JComboBox<>();
    comboBoxAlign.setModel(new DefaultComboBoxModel<Align>(Align.values()));

    comboBoxValign = new JComboBox<>();
    comboBoxValign.setModel(new DefaultComboBoxModel<Valign>(Valign.values()));

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
    this.chckbxHasCollision.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.COLLISION, Boolean.toString(chckbxHasCollision.isSelected()))));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.COLLISIONBOXWIDTH, this.spinnerWidth.getValue().toString())));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.COLLISIONBOXHEIGHT, this.spinnerHeight.getValue().toString())));

    this.comboBoxAlign.addActionListener(new MapObjectPropertyActionListener(m -> {
      Align align = (Align) this.comboBoxAlign.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.COLLISIONALGIN, align.toString());
    }));

    this.comboBoxValign.addActionListener(new MapObjectPropertyActionListener(m -> {
      Valign valign = (Valign) this.comboBoxValign.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.COLLISIONVALGIN, valign.toString());
    }));
  }

  @Override
  protected void clearControls() {
    this.chckbxHasCollision.setSelected(false);
    this.spinnerWidth.setValue(0);
    this.spinnerHeight.setValue(0);
    this.comboBoxAlign.setSelectedItem(Align.CENTER);
    this.comboBoxAlign.setSelectedItem(Valign.DOWN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxHasCollision.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION));
    this.spinnerWidth.setValue(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOXWIDTH));
    this.spinnerHeight.setValue(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOXHEIGHT));

    this.comboBoxAlign.setSelectedItem(Align.get(mapObject.getCustomProperty(MapObjectProperty.COLLISIONALGIN)));
    this.comboBoxValign.setSelectedItem(Valign.get(mapObject.getCustomProperty(MapObjectProperty.COLLISIONVALGIN)));
  }
}
