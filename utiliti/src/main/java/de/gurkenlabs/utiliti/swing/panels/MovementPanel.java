package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.LayoutManager;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class MovementPanel extends PropertyPanel {
  private final JCheckBox chckbxTurnOnMove;
  private final JSpinner spinnerAcceleration;
  private final JSpinner spinnerDeceleration;
  private final JSpinner spinnerVelocity;

  /** Create the panel. */
  public MovementPanel() {
    super("panel_mobileEntity");

    this.spinnerAcceleration = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.spinnerDeceleration = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.spinnerVelocity = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    this.chckbxTurnOnMove = new JCheckBox(Resources.strings().get("panel_turnOnMove"));

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.setup(this.chckbxTurnOnMove, MapObjectProperty.MOVEMENT_TURNONMOVE);
    this.setup(this.spinnerAcceleration, MapObjectProperty.MOVEMENT_ACCELERATION);
    this.setup(this.spinnerDeceleration, MapObjectProperty.MOVEMENT_DECELERATION);
    this.setup(this.spinnerVelocity, MapObjectProperty.MOVEMENT_VELOCITY);
  }

  @Override
  protected void clearControls() {
    this.chckbxTurnOnMove.setSelected(false);
    this.spinnerAcceleration.setValue(0.0);
    this.spinnerDeceleration.setValue(0.0);
    this.spinnerVelocity.setValue(0);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxTurnOnMove.setSelected(
        mapObject.getBoolValue(MapObjectProperty.MOVEMENT_TURNONMOVE));
    this.spinnerAcceleration.setValue(
        mapObject.getDoubleValue(MapObjectProperty.MOVEMENT_ACCELERATION));
    this.spinnerDeceleration.setValue(
        mapObject.getDoubleValue(MapObjectProperty.MOVEMENT_DECELERATION));
    this.spinnerVelocity.setValue(mapObject.getDoubleValue(MapObjectProperty.MOVEMENT_VELOCITY));
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_acceleration", this.spinnerAcceleration),
            new LayoutItem("panel_deceleration", this.spinnerDeceleration),
            new LayoutItem("panel_velocity", this.spinnerVelocity),
        };

    return this.createLayout(layoutItems, this.chckbxTurnOnMove);
  }
}
