package de.gurkenlabs.utiLITI;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;

public class EmitterPanel extends PropertyPanel<IMapObject> {
  private JTextField textFieldType;

  public EmitterPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_emitter"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_emitterType"));

    textFieldType = new JTextField();
    textFieldType.setColumns(10);

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(textFieldType, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                .addContainerGap()));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(textFieldType, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldType.setText("");
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldType.setText(mapObject.getCustomProperty(MapObjectProperties.EMITTERTYPE));
  }

  private void setupChangedListeners() {
    this.textFieldType.addFocusListener(new MapObjectPropteryFocusListener(m -> {
      m.setCustomProperty(MapObjectProperties.EMITTERTYPE, textFieldType.getText());
    }));

    this.textFieldType.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperties.EMITTERTYPE, textFieldType.getText());
    }));
  }
}
