package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.swing.ColorChooser;

@SuppressWarnings("serial")
public class LightSourcePanel extends PropertyPanel<IMapObject> {
  private final JTextField textFieldColor;
  private final JSpinner spinnerBrightness;
  private final JComboBox<String> comboBoxLightShape;
  private final JButton btnSelectColor;
  private JSpinner spinnerIntensity;
  private JCheckBox checkBoxIsActive;

  public LightSourcePanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_lightSource"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel("alpha");

    this.spinnerBrightness = new JSpinner();
    this.spinnerBrightness.setModel(new SpinnerNumberModel(0, 0, 255, 1));

    JLabel lblColor = new JLabel(Resources.get("panel_color"));

    JLabel lblShape = new JLabel(Resources.get("panel_shape"));

    this.textFieldColor = new JTextField();
    this.textFieldColor.setText("#ffffff");
    this.textFieldColor.setEditable(false);
    this.textFieldColor.setColumns(10);

    this.comboBoxLightShape = new JComboBox<>();
    this.comboBoxLightShape.setModel(new DefaultComboBoxModel<String>(new String[] { LightSource.ELLIPSE, LightSource.RECTANGLE }));

    this.btnSelectColor = new JButton("...");

    this.spinnerIntensity = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));

    JLabel lblIntensity = new JLabel(Resources.get("panel_intensity"));

    this.checkBoxIsActive = new JCheckBox("is active");
    this.checkBoxIsActive.setSelected(true);

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout
        .setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup().addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldColor, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblShape, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblIntensity, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(comboBoxLightShape, Alignment.LEADING, 0, 204, Short.MAX_VALUE)
                            .addComponent(spinnerIntensity, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE).addComponent(spinnerBrightness, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE).addComponent(checkBoxIsActive, Alignment.LEADING))
                        .addGap(5)))
                .addContainerGap()));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(5)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerIntensity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblIntensity, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(textFieldColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnSelectColor)
                .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerBrightness, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblShape, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxLightShape, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addComponent(checkBoxIsActive).addContainerGap(175, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.spinnerBrightness.setValue(0);
    this.spinnerIntensity.setValue(LightSource.DEFAULT_INTENSITY);
    this.textFieldColor.setText("#ffffff");
    this.comboBoxLightShape.setSelectedItem(LightSource.ELLIPSE);
    this.checkBoxIsActive.setSelected(true);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    final String color = mapObject.getCustomProperty(MapObjectProperty.LIGHT_COLOR);
    final String shape = mapObject.getCustomProperty(MapObjectProperty.LIGHT_SHAPE);
    final String active = mapObject.getCustomProperty(MapObjectProperty.LIGHT_ACTIVE);

    boolean isActive = active != null && !active.isEmpty() ? Boolean.parseBoolean(active) : true;
    this.spinnerBrightness.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_ALPHA));
    this.spinnerIntensity.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY));
    this.textFieldColor.setText(color);
    this.comboBoxLightShape.setSelectedItem(shape);
    this.checkBoxIsActive.setSelected(isActive);
  }

  private void setupChangedListeners() {
    btnSelectColor.addActionListener(a -> {
      Color solid = Color.decode(textFieldColor.getText());
      Color current = new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), (int) this.spinnerBrightness.getValue());

      Color result = ColorChooser.showRgbDialog(Resources.get("panel_selectAmbientColor"), current);
      if (result == null) {
        return;
      }

      String h = "#" + Integer.toHexString(result.getRGB()).substring(2);
      textFieldColor.setText(h);
      this.spinnerBrightness.setValue(result.getAlpha());
      if (getDataSource() != null) {
        getDataSource().setCustomProperty(MapObjectProperty.LIGHT_COLOR, h);
        Game.getEnvironment().reloadFromMap(getDataSource().getId());
        Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
      }
    });

    spinnerBrightness.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHT_ALPHA, spinnerBrightness.getValue().toString());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    spinnerIntensity.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHT_INTENSITY, spinnerIntensity.getValue().toString());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    comboBoxLightShape.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHT_SHAPE, comboBoxLightShape.getSelectedItem().toString());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.checkBoxIsActive.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperty.LIGHT_ACTIVE, Boolean.toString(checkBoxIsActive.isSelected()));
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));
  }
}
