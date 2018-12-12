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
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;

@SuppressWarnings("serial")
public class LightSourcePanel extends PropertyPanel {
  private final JTextField textFieldColor;
  private final JSpinner spinnerBrightness;
  private final JComboBox<String> comboBoxLightShape;
  private final JButton btnSelectColor;
  private JSpinner spinnerIntensity;
  private JCheckBox checkBoxIsActive;
  private JSlider sliderOffsetX;
  private JSlider sliderOffsetY;
  private JLabel lblOffsety;

  public LightSourcePanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.strings().get("panel_lightSource"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel("alpha");

    this.spinnerBrightness = new JSpinner();
    this.spinnerBrightness.setModel(new SpinnerNumberModel(0, 0, 255, 1));

    JLabel lblColor = new JLabel(Resources.strings().get("panel_color"));

    JLabel lblShape = new JLabel(Resources.strings().get("panel_shape"));

    this.textFieldColor = new JTextField();
    this.textFieldColor.setText("#ffffff");
    this.textFieldColor.setEditable(false);
    this.textFieldColor.setColumns(10);

    this.comboBoxLightShape = new JComboBox<>();
    this.comboBoxLightShape.setModel(new DefaultComboBoxModel<String>(new String[] { LightSource.ELLIPSE, LightSource.RECTANGLE }));

    this.btnSelectColor = new JButton("...");

    this.spinnerIntensity = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));

    JLabel lblIntensity = new JLabel(Resources.strings().get("panel_intensity"));

    this.checkBoxIsActive = new JCheckBox("is active");
    this.checkBoxIsActive.setSelected(true);

    sliderOffsetX = new JSlider();
    sliderOffsetX.setValue(0);
    sliderOffsetX.setMinimum(-100);

    JLabel lblOffsetx = new JLabel("offsetX");

    sliderOffsetY = new JSlider();
    sliderOffsetY.setValue(0);
    sliderOffsetY.setMinimum(-100);

    lblOffsety = new JLabel("offsetY");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(ComponentPlacement.RELATED)
              .addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(ComponentPlacement.RELATED)
              .addComponent(textFieldColor, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE))
            .addGroup(groupLayout.createSequentialGroup()
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(lblShape, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblIntensity, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblOffsetx, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblOffsety, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
              .addPreferredGap(ComponentPlacement.RELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(comboBoxLightShape, 0, 370, Short.MAX_VALUE)
                .addComponent(spinnerIntensity, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(85)
                  .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(spinnerBrightness, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
                .addComponent(sliderOffsetX, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addComponent(checkBoxIsActive)
                .addComponent(sliderOffsetY, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE))
              .addGap(5)))
          .addGap(0))
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGap(5)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(spinnerIntensity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblIntensity, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(textFieldColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(btnSelectColor)
            .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerBrightness, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblShape, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(comboBoxLightShape, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
            .addComponent(lblOffsetx, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(sliderOffsetX, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
            .addComponent(sliderOffsetY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblOffsety, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(checkBoxIsActive)
          .addContainerGap(111, Short.MAX_VALUE))
    );
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
    final String color = mapObject.getStringValue(MapObjectProperty.LIGHT_COLOR);
    final String shape = mapObject.getStringValue(MapObjectProperty.LIGHT_SHAPE);
    final String active = mapObject.getStringValue(MapObjectProperty.LIGHT_ACTIVE);

    boolean isActive = active != null && !active.isEmpty() ? Boolean.parseBoolean(active) : true;
    this.spinnerBrightness.setValue(mapObject.getIntValue(MapObjectProperty.LIGHT_ALPHA));
    this.spinnerIntensity.setValue(mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY));
    this.textFieldColor.setText(color);
    this.comboBoxLightShape.setSelectedItem(shape);
    this.checkBoxIsActive.setSelected(isActive);
    this.sliderOffsetX.setValue((int) Math.max(Math.min(100 * mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETX), 100), -100));
    this.sliderOffsetY.setValue((int) Math.max(Math.min(100 * mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETY), 100), -100));
  }

  private void setupChangedListeners() {
    this.btnSelectColor.addActionListener(a -> {
      Color solid = ColorHelper.decode(textFieldColor.getText());
      Color current = new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), (int) this.spinnerBrightness.getValue());
      Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLightColor"), current);
      if (result == null) {
        return;
      }

      String h = "#" + Integer.toHexString(result.getRGB()).substring(2);
      textFieldColor.setText(h);
      this.spinnerBrightness.setValue(result.getAlpha());
      if (getDataSource() != null) {
        getDataSource().setValue(MapObjectProperty.LIGHT_COLOR, h);
        Game.getEnvironment().reloadFromMap(getDataSource().getId());
        Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
      }
    });

    this.spinnerBrightness.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_ALPHA, spinnerBrightness.getValue().toString());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.spinnerIntensity.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_INTENSITY, spinnerIntensity.getValue().toString());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.comboBoxLightShape.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_SHAPE, comboBoxLightShape.getSelectedItem().toString());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.checkBoxIsActive.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_ACTIVE, checkBoxIsActive.isSelected());
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.sliderOffsetX.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_FOCUSOFFSETX, this.sliderOffsetX.getValue() / 100.0);
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.sliderOffsetY.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_FOCUSOFFSETY, this.sliderOffsetY.getValue() / 100.0);
      Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));
  }
}
