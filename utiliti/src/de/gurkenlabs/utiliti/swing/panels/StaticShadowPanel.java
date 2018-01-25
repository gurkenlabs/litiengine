package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;

public class StaticShadowPanel extends PropertyPanel<IMapObject> {
  JComboBox<StaticShadowType> comboBoxShadowType;
  JSpinner spinnerOffset;

  public StaticShadowPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_staticShadow"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_shadowType"));

    comboBoxShadowType = new JComboBox<>();
    comboBoxShadowType.setModel(new DefaultComboBoxModel<StaticShadowType>(StaticShadowType.values()));

    JLabel lblOffset = new JLabel("offset");

    spinnerOffset = new JSpinner();

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblShadowType).addComponent(lblOffset)).addGap(7)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(comboBoxShadowType, 0, 357, Short.MAX_VALUE).addGap(4))
                .addGroup(groupLayout.createSequentialGroup().addComponent(spinnerOffset, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE).addContainerGap(266, Short.MAX_VALUE)))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(comboBoxShadowType, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE).addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblOffset).addComponent(spinnerOffset, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addContainerGap(240, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.comboBoxShadowType.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.SHADOWTYPE, ((StaticShadowType) this.comboBoxShadowType.getSelectedItem()).toString())));
    this.spinnerOffset.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.SHADOWOFFSET, Integer.toString((int) this.spinnerOffset.getValue()))));
  }

  @Override
  protected void clearControls() {
    this.comboBoxShadowType.setSelectedItem(StaticShadowType.NOOFFSET);
    this.spinnerOffset.setValue(StaticShadow.DEFAULT_OFFSET);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    String shadowType = mapObject.getCustomProperty(MapObjectProperty.SHADOWTYPE);
    if (shadowType != null && !shadowType.isEmpty()) {
      this.comboBoxShadowType.setSelectedItem(StaticShadowType.valueOf(shadowType));
    }

    this.spinnerOffset.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.SHADOWOFFSET, StaticShadow.DEFAULT_OFFSET));
  }
}
