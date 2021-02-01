package com.litiengine.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.physics.Collision;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.swing.Icons;

@SuppressWarnings("serial")
public class CollisionBoxPanel extends PropertyPanel {
  private JComboBox<Collision> comboBoxColl;
  private JCheckBox chckbxIsObstructingLights;

  public CollisionBoxPanel() {
    super("panel_collisionBox", Icons.COLLISIONBOX);

    this.comboBoxColl = new JComboBox<>();
    this.comboBoxColl.setModel(new DefaultComboBoxModel<>(new Collision[] { Collision.DYNAMIC, Collision.STATIC }));

    this.chckbxIsObstructingLights = new JCheckBox(Resources.strings().get("panel_isObstructingLights"));

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.comboBoxColl.setSelectedItem(Collision.STATIC);
    this.chckbxIsObstructingLights.setSelected(false);
  }


  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.comboBoxColl.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class, Collision.STATIC));
    this.chckbxIsObstructingLights.setSelected(mapObject.getBoolValue(MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS));
  }

  private void setupChangedListeners() {
    this.setup(this.comboBoxColl, MapObjectProperty.COLLISION_TYPE);
    this.setup(this.chckbxIsObstructingLights, MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS);
  }
  
  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
        new LayoutItem("collisionType", this.comboBoxColl), 
    };
    
    return this.createLayout(layoutItems, this.chckbxIsObstructingLights);
  }
}
