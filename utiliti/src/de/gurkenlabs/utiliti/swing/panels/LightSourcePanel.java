package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.LightSource;

public class LightSourcePanel extends PropertyPanel<IMapObject> {
  private final JTextField textFieldColor;
  private final JSpinner spinnerBrightness;
  private final JComboBox<String> comboBoxLightShape;
  private final JButton btnSelectColor;
  private JSpinner spinnerIntensity;
  private JLabel lblIntensity;
  private JCheckBox checkBoxIsActive;

  public LightSourcePanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_lightSource"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_brightness"));

    spinnerBrightness = new JSpinner();
    spinnerBrightness.setModel(new SpinnerNumberModel(0, 0, 255, 1));

    JLabel lblColor = new JLabel(Resources.get("panel_color"));

    JLabel lblShape = new JLabel(Resources.get("panel_shape"));

    textFieldColor = new JTextField();
    textFieldColor.setText("#ffffff");
    textFieldColor.setEditable(false);
    textFieldColor.setColumns(10);

    comboBoxLightShape = new JComboBox<>();
    comboBoxLightShape.setModel(new DefaultComboBoxModel<String>(new String[] { LightSource.ELLIPSE, LightSource.RECTANGLE }));

    btnSelectColor = new JButton("...");

    spinnerIntensity = new JSpinner();

    lblIntensity = new JLabel(Resources.get("panel_intensity"));

    checkBoxIsActive = new JCheckBox("is active");
    checkBoxIsActive.setSelected(true);

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(spinnerBrightness, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblIntensity, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                        .addGap(4)
                        .addComponent(spinnerIntensity, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(textFieldColor, GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE))
                    .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                        .addComponent(lblShape, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                            .addComponent(checkBoxIsActive)
                            .addComponent(comboBoxLightShape, 0, 365, Short.MAX_VALUE))))
                .addContainerGap()));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerBrightness, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(7)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGap(3)
                        .addComponent(lblIntensity, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
                    .addComponent(spinnerIntensity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(textFieldColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectColor))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblShape, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxLightShape, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(checkBoxIsActive)
                .addContainerGap(153, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.spinnerBrightness.setValue(0);
    this.spinnerIntensity.setValue(0);
    this.textFieldColor.setText("#ffffff");
    this.comboBoxLightShape.setSelectedItem(LightSource.ELLIPSE);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    final String color = mapObject.getCustomProperty(MapObjectProperty.LIGHTCOLOR);
    final String shape = mapObject.getCustomProperty(MapObjectProperty.LIGHTSHAPE);
    final String active = mapObject.getCustomProperty(MapObjectProperty.LIGHTACTIVE);

    boolean isActive = active != null && !active.isEmpty() ? Boolean.parseBoolean(active) : true;
    this.spinnerBrightness.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHTBRIGHTNESS));
    this.spinnerIntensity.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHTINTENSITY));
    this.textFieldColor.setText(color);
    this.comboBoxLightShape.setSelectedItem(shape);
    this.checkBoxIsActive.setSelected(isActive);
  }

  private void setupChangedListeners() {
    btnSelectColor.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, Resources.get("panel_selectAmbientColor"), Color.decode(textFieldColor.getText()));
      if (result == null) {
        return;
      }

      String h = "#" + Integer.toHexString(result.getRGB()).substring(2);
      textFieldColor.setText(h);
      if (getDataSource() != null) {
        getDataSource().setCustomProperty(MapObjectProperty.LIGHTCOLOR, h);
        Game.getEnvironment().reloadFromMap(getDataSource().getId());
        Game.getEnvironment().getAmbientLight().createImage();
      }
    });

    spinnerBrightness.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHTBRIGHTNESS, spinnerBrightness.getValue().toString());
      Game.getEnvironment().getAmbientLight().createImage();
    }));

    spinnerIntensity.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHTINTENSITY, spinnerIntensity.getValue().toString());
      Game.getEnvironment().getAmbientLight().createImage();
    }));

    comboBoxLightShape.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHTSHAPE, comboBoxLightShape.getSelectedItem().toString());
      Game.getEnvironment().getAmbientLight().createImage();
    }));

    this.checkBoxIsActive.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHTACTIVE, Boolean.toString(checkBoxIsActive.isSelected()));
      Game.getEnvironment().getAmbientLight().createImage();
    }));
  }
}
