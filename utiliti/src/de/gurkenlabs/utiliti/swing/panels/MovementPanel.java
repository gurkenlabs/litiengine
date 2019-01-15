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
public class MovementPanel extends PropertyPanel {
  private JCheckBox chckbxTurnOnMove;
  private JSpinner spinnerAcceleration;
  private JSpinner spinnerDeceleration;
  private JSpinner spinnerVelocity;

  private JLabel lblAcceleration;
  private JLabel lblDeceleration;
  private JLabel lblVelocity;
  private JLabel lblTurnOnMove;

  /**
   * Create the panel.
   */
  public MovementPanel() {
    super("panel_mobileEntity");
    
        this.lblAcceleration = new JLabel(Resources.strings().get("panel_acceleration"));
    
        this.spinnerAcceleration = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.lblDeceleration = new JLabel(Resources.strings().get("panel_deceleration"));
    
        this.spinnerDeceleration = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    this.lblVelocity = new JLabel(Resources.strings().get("panel_velocity"));
    
        this.spinnerVelocity = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    this.lblTurnOnMove = new JLabel(Resources.strings().get("panel_turnOnMove"));
    
        this.chckbxTurnOnMove = new JCheckBox("");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
            .addComponent(lblAcceleration)
            .addGroup(groupLayout.createSequentialGroup()
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(lblDeceleration)
                .addComponent(lblVelocity))
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                .addComponent(spinnerVelocity)
                .addComponent(spinnerAcceleration)
                .addComponent(spinnerDeceleration, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblTurnOnMove)
              .addGap(6)
              .addComponent(chckbxTurnOnMove)))
          .addContainerGap(145, Short.MAX_VALUE))
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblAcceleration)
            .addComponent(spinnerAcceleration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
            .addComponent(lblDeceleration)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(spinnerDeceleration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(ComponentPlacement.RELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(spinnerVelocity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblVelocity))))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblTurnOnMove)
              .addGap(166))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(chckbxTurnOnMove, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
              .addGap(159))))
    );
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.chckbxTurnOnMove.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.MOVEMENT_TURNONMOVE, chckbxTurnOnMove.isSelected())));

    this.spinnerAcceleration.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.MOVEMENT_ACCELERATION, this.spinnerAcceleration.getValue().toString())));

    this.spinnerDeceleration.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.MOVEMENT_DECELERATION, this.spinnerDeceleration.getValue().toString())));

    this.spinnerVelocity.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.MOVEMENT_VELOCITY, this.spinnerVelocity.getValue().toString())));
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
    this.chckbxTurnOnMove.setSelected(mapObject.getBoolValue(MapObjectProperty.MOVEMENT_TURNONMOVE));
    this.spinnerAcceleration.setValue(mapObject.getDoubleValue(MapObjectProperty.MOVEMENT_ACCELERATION));
    this.spinnerDeceleration.setValue(mapObject.getDoubleValue(MapObjectProperty.MOVEMENT_DECELERATION));
    this.spinnerVelocity.setValue(mapObject.getDoubleValue(MapObjectProperty.MOVEMENT_VELOCITY));

  }
}
