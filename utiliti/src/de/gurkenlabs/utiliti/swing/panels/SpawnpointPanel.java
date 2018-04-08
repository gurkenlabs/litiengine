package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;

@SuppressWarnings("serial")
public class SpawnpointPanel extends PropertyPanel<IMapObject> {
  private JTextField textFieldType;
  private JComboBox<Direction> comboBoxDirection;

  public SpawnpointPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_spawnPoint"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_entity"));

    this.textFieldType = new JTextField();
    this.textFieldType.setColumns(10);

    JLabel lblDirection = new JLabel(Resources.get("panel_direction"));

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<Direction>(Direction.values()));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldType, GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblDirection, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(comboBoxDirection, 0, 371, Short.MAX_VALUE)))
            .addGap(4)));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(textFieldType, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblDirection, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxDirection, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addContainerGap(226, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldType.setText("");
    this.comboBoxDirection.setSelectedItem(Direction.DOWN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldType.setText(mapObject.getCustomProperty(MapObjectProperty.SPAWN_TYPE));

    String direction = mapObject.getCustomProperty(MapObjectProperty.SPAWN_DIRECTION);
    if (direction != null && !direction.isEmpty()) {
      this.comboBoxDirection.setSelectedItem(Direction.valueOf(direction));
    }
  }

  private void setupChangedListeners() {
    this.textFieldType.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setCustomProperty(MapObjectProperty.SPAWN_TYPE, textFieldType.getText())));
    this.textFieldType.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.SPAWN_TYPE, textFieldType.getText())));
    this.comboBoxDirection.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.SPAWN_DIRECTION, ((Direction) this.comboBoxDirection.getSelectedItem()).toString())));
  }
}
