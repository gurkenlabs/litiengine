package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.swing.ColorControl;
import de.gurkenlabs.utiliti.swing.Icons;

@SuppressWarnings("serial")
public class LightSourcePanel extends PropertyPanel {
  private final JComboBox<String> comboBoxLightShape;
  private JSpinner spinnerIntensity;
  private JCheckBox checkBoxIsActive;
  private JSlider sliderOffsetX;
  private JSlider sliderOffsetY;
  private final ColorControl colorControl;

  public LightSourcePanel() {
    super("panel_lightSource", Icons.LIGHT);

    this.colorControl = new ColorControl();

    this.comboBoxLightShape = new JComboBox<>();
    this.comboBoxLightShape.setModel(new DefaultComboBoxModel<String>(new String[] { "ellipse", "rectangle" }));

    this.spinnerIntensity = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));

    this.checkBoxIsActive = new JCheckBox("is active");
    this.checkBoxIsActive.setSelected(true);

    this.sliderOffsetX = new JSlider(-100, 100, 0);
    this.sliderOffsetY = new JSlider(-100, 100, 0);
    
    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.spinnerIntensity.setValue(LightSource.DEFAULT_INTENSITY);
    this.colorControl.clear();
    this.comboBoxLightShape.setSelectedItem("ellipse");
    this.checkBoxIsActive.setSelected(true);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    final String color = mapObject.getStringValue(MapObjectProperty.LIGHT_COLOR);
    final String shape = mapObject.getStringValue(MapObjectProperty.LIGHT_SHAPE);
    final String active = mapObject.getStringValue(MapObjectProperty.LIGHT_ACTIVE);

    boolean isActive = active == null || active.isEmpty() || Boolean.parseBoolean(active);
    this.spinnerIntensity.setValue(mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY));
    this.colorControl.setHexColor(color);
    this.comboBoxLightShape.setSelectedItem(shape);
    this.checkBoxIsActive.setSelected(isActive);
    this.sliderOffsetX.setValue((int) Math.max(Math.min(100 * mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETX), 100), -100));
    this.sliderOffsetY.setValue((int) Math.max(Math.min(100 * mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETY), 100), -100));
  }

  private void setupChangedListeners() {
    this.colorControl.addActionListener(new MapObjectPropertyActionListener(m -> {
      if (getDataSource() != null) {
        getDataSource().setValue(MapObjectProperty.LIGHT_COLOR, this.colorControl.getHexColor());
        //Game.world().environment().reloadFromMap(getDataSource().getId());
        Game.world().environment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
      }
    }));

    this.spinnerIntensity.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_INTENSITY, spinnerIntensity.getValue().toString());
      Game.world().environment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.comboBoxLightShape.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_SHAPE, comboBoxLightShape.getSelectedItem().toString());
      Game.world().environment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.checkBoxIsActive.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_ACTIVE, checkBoxIsActive.isSelected());
      Game.world().environment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.sliderOffsetX.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_FOCUSOFFSETX, this.sliderOffsetX.getValue() / 100.0);
      Game.world().environment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));

    this.sliderOffsetY.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setValue(MapObjectProperty.LIGHT_FOCUSOFFSETY, this.sliderOffsetY.getValue() / 100.0);
      Game.world().environment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
    }));
  }

  private LayoutManager createLayout() {
   
    LayoutItem[] layoutItems = new LayoutItem[] { 
        new LayoutItem("panel_shape", this.comboBoxLightShape), 
        new LayoutItem("panel_color", this.colorControl), 
        new LayoutItem("panel_intensity", this.spinnerIntensity), 
        new LayoutItem("panel_offsetX", this.sliderOffsetX),
        new LayoutItem("panel_offsetY", this.sliderOffsetY),
    };

    return this.createLayout(layoutItems, this.checkBoxIsActive);
  }
}
