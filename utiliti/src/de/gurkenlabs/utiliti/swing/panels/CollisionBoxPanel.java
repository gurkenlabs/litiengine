package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.JCheckBox;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;

@SuppressWarnings("serial")
public class CollisionBoxPanel extends PropertyPanel {
  private JCheckBox chckbxIsObstacle;
  private JCheckBox chckbxIsObstructingLights;

  public CollisionBoxPanel() {
    super("panel_collisionBox");

    this.chckbxIsObstacle = new JCheckBox(Resources.strings().get("panel_isObstacle"));
    this.chckbxIsObstructingLights = new JCheckBox(Resources.strings().get("panel_isObstructingLights"));

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.chckbxIsObstacle.setSelected(true);
    this.chckbxIsObstructingLights.setSelected(false);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxIsObstacle.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_OBSTACLE));
    this.chckbxIsObstructingLights.setSelected(mapObject.getBoolValue(MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS));
  }

  private void setupChangedListeners() {
    this.setup(this.chckbxIsObstacle, MapObjectProperty.PROP_OBSTACLE);
    this.setup(this.chckbxIsObstructingLights, MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS);
  }
  
  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
    };
    
    return this.createLayout(layoutItems, this.chckbxIsObstacle, this.chckbxIsObstructingLights);
  }
}
