package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;

public class StaticShadowPanel extends PropertyPanel<IMapObject> {
  JComboBox<String> comboBoxShadowType;

  public StaticShadowPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_staticShadow"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_shadowType"));

    comboBoxShadowType = new JComboBox<String>();
    comboBoxShadowType.setModel(new DefaultComboBoxModel(StaticShadowType.values()));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(comboBoxShadowType, 0, 95, Short.MAX_VALUE)
                        .addGap(4)))));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(comboBoxShadowType, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.comboBoxShadowType.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperties.SHADOWTYPE, ((StaticShadowType) this.comboBoxShadowType.getSelectedItem()).toString());
    }));
  }

  @Override
  protected void clearControls() {
    this.comboBoxShadowType.setSelectedItem(StaticShadowType.NOOFFSET);

  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    String shadowType = mapObject.getCustomProperty(MapObjectProperties.SHADOWTYPE);
    if (shadowType != null && !shadowType.isEmpty()) {
      this.comboBoxShadowType.setSelectedItem(StaticShadowType.valueOf(shadowType));
    }

  }

}
