package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class LightSourcePanel extends PropertyPanel {
  private final JComboBox<String> comboBoxLightShape;
  private final JSpinner spinnerIntensity;
  private final JCheckBox checkBoxIsActive;
  private final ColorComponent colorControl;

  public LightSourcePanel() {
    super("panel_lightSource", Icons.BULB_24);

    this.colorControl = new ColorComponent();

    this.comboBoxLightShape = new JComboBox<>();
    this.comboBoxLightShape.setModel(
        new DefaultComboBoxModel<>(new String[] {"ellipse", "rectangle"}));

    this.spinnerIntensity = new JSpinner(new SpinnerNumberModel(0, 0, 255, 5));
    this.checkBoxIsActive = new JCheckBox("is active");
    this.checkBoxIsActive.setSelected(true);

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
    this.updateLighting();
  }

  @Override
  protected void clearControls() {
    this.spinnerIntensity.setValue(LightSource.DEFAULT_INTENSITY);
    this.colorControl.clear();
    this.checkBoxIsActive.setSelected(true);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    final String shape = mapObject.getStringValue(MapObjectProperty.LIGHT_SHAPE, null);
    final boolean isActive = mapObject.getBoolValue(MapObjectProperty.LIGHT_ACTIVE, false);

    this.spinnerIntensity.setValue(mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY));
    this.colorControl.setColor(mapObject.getColorValue(MapObjectProperty.LIGHT_COLOR));
    this.comboBoxLightShape.setSelectedItem(shape);
    this.checkBoxIsActive.setSelected(isActive);
}

  private void setupChangedListeners() {
    this.colorControl.addActionListener(
        new MapObjectPropertyActionListener(
            m -> {
              if (!m.hasCustomProperty(MapObjectProperty.LIGHT_COLOR) || m.getStringValue(MapObjectProperty.LIGHT_COLOR, null) == null) {
                return true;
              }

              if (!m.hasCustomProperty(MapObjectProperty.LIGHT_INTENSITY)) {
                return true;
              }

              return !m.getStringValue(MapObjectProperty.LIGHT_COLOR, null).equals(this.colorControl.getHexColor())
                  || m.getIntValue(MapObjectProperty.LIGHT_INTENSITY, 0) != (int) this.spinnerIntensity.getValue();
            },
            m -> {
              m.setValue(MapObjectProperty.LIGHT_COLOR, this.colorControl.getHexColor());
              m.setValue(MapObjectProperty.LIGHT_INTENSITY, (int) this.spinnerIntensity.getValue());
              Game.world().environment().updateLighting(getDataSource().getBoundingBox());
            }));
    this.setup(this.spinnerIntensity, MapObjectProperty.LIGHT_INTENSITY);
    this.spinnerIntensity.addChangeListener(m -> this.updateLighting());

    this.setup(this.comboBoxLightShape, MapObjectProperty.LIGHT_SHAPE);
    this.comboBoxLightShape.addActionListener(m -> this.updateLighting());

    this.setup(this.checkBoxIsActive, MapObjectProperty.LIGHT_ACTIVE);
    this.checkBoxIsActive.addActionListener(m -> this.updateLighting());
  }

  private void updateLighting() {
    if (Editor.instance().getMapComponent().isFocussing()) {
      return;
    }

    final IMapObject datasource = getDataSource();
    if (datasource == null) {
      return;
    }

    Game.world().environment().updateLighting(getDataSource().getBoundingBox());
  }

  private LayoutManager createLayout() {

    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_shape", this.comboBoxLightShape),
            new LayoutItem(
                "panel_color", this.colorControl, this.colorControl.getPreferredSize().height),
            new LayoutItem("panel_intensity", this.spinnerIntensity),
        };

    return this.createLayout(layoutItems, this.checkBoxIsActive);
  }
}
