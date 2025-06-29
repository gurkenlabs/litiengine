package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;

public class StaticShadowPanel extends PropertyPanel {
  private final JComboBox<StaticShadowType> comboBoxShadowType;
  private final JSpinner spinnerOffset;

  public StaticShadowPanel() {
    super("panel_staticShadow", Icons.SHADOWBOX_24);

    this.comboBoxShadowType = new JComboBox<>();
    this.comboBoxShadowType.setModel(
        new DefaultComboBoxModel<>(StaticShadowType.values()));

    this.spinnerOffset = new JSpinner();

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.setup(this.comboBoxShadowType, MapObjectProperty.SHADOW_TYPE);
    this.setup(this.spinnerOffset, MapObjectProperty.SHADOW_OFFSET);
  }

  @Override
  protected void clearControls() {
    this.comboBoxShadowType.setSelectedItem(StaticShadowType.NOOFFSET);
    this.spinnerOffset.setValue(StaticShadow.DEFAULT_OFFSET);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.comboBoxShadowType.setSelectedItem(
        mapObject.getEnumValue(
            MapObjectProperty.SHADOW_TYPE, StaticShadowType.class, StaticShadowType.NONE));
    this.spinnerOffset.setValue(
        mapObject.getIntValue(MapObjectProperty.SHADOW_OFFSET, StaticShadow.DEFAULT_OFFSET));
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_shadowType", this.comboBoxShadowType),
            new LayoutItem("panel_offset", this.spinnerOffset),
        };

    return this.createLayout(layoutItems);
  }
}
