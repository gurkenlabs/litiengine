package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;

public class CollisionBoxPanel extends PropertyPanel<IMapObject> {
  JCheckBox chckbxIsObstacle;

  public CollisionBoxPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_collisionBox"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    chckbxIsObstacle = new JCheckBox(Resources.get("panel_isObstacle"));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chckbxIsObstacle, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(322, Short.MAX_VALUE)));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addComponent(chckbxIsObstacle, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(258, Short.MAX_VALUE)));
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.chckbxIsObstacle.setSelected(true);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    String obstacle = mapObject.getCustomProperty(MapObjectProperties.OBSTACLE);
    if (obstacle != null && !obstacle.isEmpty()) {
      this.chckbxIsObstacle.setSelected(Boolean.valueOf(obstacle));
    }
  }

  private void setupChangedListeners() {
    this.chckbxIsObstacle.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperties.OBSTACLE, Boolean.toString(chckbxIsObstacle.isSelected()));
    }));
  }
}
