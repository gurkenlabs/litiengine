package com.litiengine.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;

import com.litiengine.entities.StaticShadow;
import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.graphics.StaticShadowType;
import com.litiengine.utiliti.swing.Icons;

@SuppressWarnings("serial")
public class StaticShadowPanel extends PropertyPanel {
  private final JComboBox<StaticShadowType> comboBoxShadowType;
  private final JSpinner spinnerOffset;

  public StaticShadowPanel() {
    super("panel_staticShadow", Icons.SHADOWBOX);

    this.comboBoxShadowType = new JComboBox<>();
    this.comboBoxShadowType.setModel(new DefaultComboBoxModel<StaticShadowType>(StaticShadowType.values()));

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
    this.comboBoxShadowType.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.SHADOW_TYPE, StaticShadowType.class, StaticShadowType.NONE));
    this.spinnerOffset.setValue(mapObject.getIntValue(MapObjectProperty.SHADOW_OFFSET, StaticShadow.DEFAULT_OFFSET));
  }
  
  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
        new LayoutItem("panel_shadowType", this.comboBoxShadowType),
        new LayoutItem("panel_offset", this.spinnerOffset),
    };
    
    return this.createLayout(layoutItems);
  }
}
