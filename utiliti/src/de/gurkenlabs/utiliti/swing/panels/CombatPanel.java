package de.gurkenlabs.utiliti.swing.panels;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;

@SuppressWarnings("serial")
public class CombatPanel extends PropertyPanel {
  private JCheckBox chckbxIndestructible;
  private JSpinner spinnerHitpoints;
  private JSpinner spinnerTeam;

  private JLabel lblHitpoints;
  private JLabel lblIndestructible;
  private JLabel lblTeam;

  /**
   * Create the panel.
   */
  public CombatPanel() {
    super("panel_combatEntity");

    this.lblHitpoints = new JLabel(Resources.strings().get("panel_hitpoints"));

    this.spinnerHitpoints = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
    this.lblIndestructible = new JLabel(Resources.strings().get("panel_indestructible"));
    this.lblTeam = new JLabel(Resources.strings().get("panel_team"));

    this.spinnerTeam = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
    this.chckbxIndestructible = new JCheckBox("");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(lblHitpoints).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblIndestructible).addComponent(lblTeam))
                .addPreferredGap(ComponentPlacement.UNRELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxIndestructible).addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(spinnerHitpoints).addComponent(spinnerTeam)))))
            .addContainerGap(253, Short.MAX_VALUE)));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblHitpoints).addComponent(spinnerHitpoints, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(lblIndestructible)
                        .addGroup(groupLayout.createSequentialGroup().addGap(26).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerTeam, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblTeam)))))
                .addGroup(groupLayout.createSequentialGroup().addGap(38).addComponent(chckbxIndestructible, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(191, Short.MAX_VALUE)));
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.chckbxIndestructible.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, chckbxIndestructible.isSelected())));

    this.spinnerHitpoints.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.COMBAT_HITPOINTS, this.spinnerHitpoints.getValue().toString())));

    this.spinnerTeam.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.COMBAT_TEAM, this.spinnerTeam.getValue().toString())));
  }

  @Override
  protected void clearControls() {
    this.chckbxIndestructible.setSelected(false);
    this.spinnerHitpoints.setValue(0.0);
    this.spinnerTeam.setValue(0);

  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxIndestructible.setSelected(mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE));
    this.spinnerHitpoints.setValue(mapObject.getIntValue(MapObjectProperty.COMBAT_HITPOINTS));
    this.spinnerTeam.setValue(mapObject.getIntValue(MapObjectProperty.COMBAT_TEAM));
  }
}
