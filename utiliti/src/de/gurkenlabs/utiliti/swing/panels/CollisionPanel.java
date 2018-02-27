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

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;

@SuppressWarnings("serial")
public class CollisionPanel extends PropertyPanel<IMapObject> {
  private JCheckBox chckbxHasCollision;
  private JSpinner spinnerWidth;
  private JSpinner spinnerHeight;
  private JComboBox<Align> comboBoxAlign;
  private JComboBox<Valign> comboBoxValign;

  /**
   * Create the panel.
   */
  public CollisionPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_collisionEntity"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblMaterial = new JLabel(Resources.get("panel_width"));

    this.chckbxHasCollision = new JCheckBox("collision");

    JLabel lblHeightFactor = new JLabel(Resources.get("panel_height"));

    this.spinnerWidth = new JSpinner();
    this.spinnerWidth.setModel(new SpinnerNumberModel(0, 0, null, 0.5f));

    this.spinnerHeight = new JSpinner();
    this.spinnerHeight.setModel(new SpinnerNumberModel(0, 0, null, 0.5f));

    this.comboBoxAlign = new JComboBox<>();
    this.comboBoxAlign.setModel(new DefaultComboBoxModel<Align>(Align.values()));

    this.comboBoxValign = new JComboBox<>();
    this.comboBoxValign.setModel(new DefaultComboBoxModel<Valign>(Valign.values()));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
        .addGroup(
            groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(12).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblHeightFactor)))
        .addGap(1)
        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxHasCollision)
            .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(spinnerWidth).addComponent(spinnerHeight, GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(comboBoxValign, 0, 257, Short.MAX_VALUE).addComponent(comboBoxAlign, 0, 257, Short.MAX_VALUE))))
        .addContainerGap()));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxAlign,
                GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(5).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblHeightFactor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(comboBoxValign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxHasCollision, GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE).addContainerGap()));
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.chckbxHasCollision.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.COLLISION, Boolean.toString(chckbxHasCollision.isSelected()))));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.COLLISIONBOX_WIDTH, this.spinnerWidth.getValue().toString())));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.COLLISIONBOX_HEIGHT, this.spinnerHeight.getValue().toString())));

    this.comboBoxAlign.addActionListener(new MapObjectPropertyActionListener(m -> {
      Align align = (Align) this.comboBoxAlign.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.COLLISION_ALGIN, align.toString());
    }));

    this.comboBoxValign.addActionListener(new MapObjectPropertyActionListener(m -> {
      Valign valign = (Valign) this.comboBoxValign.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.COLLISION_VALGIN, valign.toString());
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
    this.spinnerWidth.setValue(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH));
    this.spinnerHeight.setValue(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT));

    this.comboBoxAlign.setSelectedItem(Align.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALGIN)));
    this.comboBoxValign.setSelectedItem(Valign.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALGIN)));
  }
}
