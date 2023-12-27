package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.listeners.MapObjectPropertyActionListener;
import de.gurkenlabs.utiliti.swing.ColorComponent;
import de.gurkenlabs.utiliti.swing.Icons;
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
  private final JSpinner offsetX;
  private final JSpinner offsetY;
  private final ColorComponent colorControl;

  public LightSourcePanel() {
    super("panel_lightSource", Icons.LIGHT);

    this.colorControl = new ColorComponent();

    this.comboBoxLightShape = new JComboBox<>();
    this.comboBoxLightShape.setModel(
      new DefaultComboBoxModel<>(new String[]{"ellipse", "rectangle"}));

    this.spinnerIntensity = new JSpinner(new SpinnerNumberModel(0, 0, 255, 5));
    this.checkBoxIsActive = new JCheckBox("is active");
    this.checkBoxIsActive.setSelected(true);

    this.offsetX = new JSpinner(new SpinnerNumberModel(0.0, -0.5, 0.5, 0.1));
    this.offsetY = new JSpinner(new SpinnerNumberModel(0.0, -0.5, 0.5, 0.1));

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
    final String color = mapObject.getStringValue(MapObjectProperty.LIGHT_COLOR);
    final String shape = mapObject.getStringValue(MapObjectProperty.LIGHT_SHAPE);
    final String active = mapObject.getStringValue(MapObjectProperty.LIGHT_ACTIVE);

    boolean isActive = active == null || active.isEmpty() || Boolean.parseBoolean(active);
    this.spinnerIntensity.setValue(
      mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY));
    this.colorControl.setHexColor(color);
    this.comboBoxLightShape.setSelectedItem(shape);
    this.checkBoxIsActive.setSelected(isActive);
    this.offsetX.setValue(
      (int) Math.max(
        Math.min(100 * mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETX), 100),
        -100));
    this.offsetY.setValue(
      (int) Math.max(
        Math.min(100 * mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETY), 100),
        -100));
  }

  private void setupChangedListeners() {
    this.colorControl.addActionListener(
      new MapObjectPropertyActionListener(
        m -> {
          if (!m.hasCustomProperty(MapObjectProperty.LIGHT_COLOR)
            || m.getStringValue(MapObjectProperty.LIGHT_COLOR) == null) {
            return true;
          }

          if (!m.hasCustomProperty(MapObjectProperty.LIGHT_INTENSITY)) {
            return true;
          }

          return
            !m.getStringValue(MapObjectProperty.LIGHT_COLOR).equals(this.colorControl.getHexColor())
              || m.getIntValue(MapObjectProperty.LIGHT_INTENSITY)
              != (int) this.spinnerIntensity.getValue();
        },
        m -> {
          m.setValue(MapObjectProperty.LIGHT_COLOR, this.colorControl.getHexColor());
          m.setValue(MapObjectProperty.LIGHT_INTENSITY, (int) this.spinnerIntensity.getValue());
          Game.world().environment().updateLighting(getMapObject().getBoundingBox());
        }));
    this.setup(this.spinnerIntensity, MapObjectProperty.LIGHT_INTENSITY);
    this.spinnerIntensity.addChangeListener(m -> this.updateLighting());

    this.setup(this.comboBoxLightShape, MapObjectProperty.LIGHT_SHAPE);
    this.comboBoxLightShape.addActionListener(m -> this.updateLighting());

    this.setup(this.checkBoxIsActive, MapObjectProperty.LIGHT_ACTIVE);
    this.checkBoxIsActive.addActionListener(m -> this.updateLighting());

    this.setup(this.offsetX, MapObjectProperty.LIGHT_FOCUSOFFSETX);
    this.offsetX.addChangeListener(m -> this.updateLighting());

    this.setup(this.offsetY, MapObjectProperty.LIGHT_FOCUSOFFSETY);
    this.offsetY.addChangeListener(m -> this.updateLighting());
  }

  private void updateLighting() {
    if (Editor.instance().getMapComponent().isFocussing()) {
      return;
    }

    final IMapObject datasource = getMapObject();
    if (datasource == null) {
      return;
    }

    Game.world().environment().updateLighting(getMapObject().getBoundingBox());
  }

  private LayoutManager createLayout() {

    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("panel_shape", this.comboBoxLightShape),
        new LayoutItem(
          "panel_color", this.colorControl, this.colorControl.getPreferredSize().height),
        new LayoutItem("panel_intensity", this.spinnerIntensity),
        new LayoutItem("offsetX", this.offsetX),
        new LayoutItem("offsetY", this.offsetY),
      };

    return this.createLayout(layoutItems, this.checkBoxIsActive);
  }
}
