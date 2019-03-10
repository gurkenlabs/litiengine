package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Icons;

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
        new LayoutItem("panel_collisionType", this.comboBoxColl), 
    };
    
    return this.createLayout(layoutItems, this.chckbxIsObstructingLights);
  }
}
