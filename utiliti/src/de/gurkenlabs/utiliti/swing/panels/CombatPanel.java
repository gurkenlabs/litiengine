package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;

@SuppressWarnings("serial")
public class CombatPanel extends PropertyPanel {
  private JCheckBox chckbxIndestructible;
  private JSpinner spinnerHitpoints;
  private JSpinner spinnerTeam;

  /**
   * Create the panel.
   */
  public CombatPanel() {
    super("panel_combatEntity");

    this.spinnerHitpoints = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
    this.spinnerTeam = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
    this.chckbxIndestructible = new JCheckBox("indestructible");

    setLayout(this.createLayout());
    this.setupChangedListeners();
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
    this.spinnerHitpoints.setValue(mapObject.getIntValue(MapObjectProperty.COMBAT_HITPOINTS, CombatEntity.DEFAULT_HITPOINTS));
    this.spinnerTeam.setValue(mapObject.getIntValue(MapObjectProperty.COMBAT_TEAM));
  }

  private void setupChangedListeners() {
    this.setup(this.chckbxIndestructible, MapObjectProperty.COMBAT_INDESTRUCTIBLE);
    this.setup(this.spinnerHitpoints, MapObjectProperty.COMBAT_HITPOINTS);
    this.setup(this.spinnerTeam, MapObjectProperty.COMBAT_TEAM);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
        new LayoutItem("panel_hitpoints", this.spinnerHitpoints),
        new LayoutItem("panel_team", this.spinnerTeam)
    };
    
    return this.createLayout(layoutItems, this.chckbxIndestructible);
  }
}
