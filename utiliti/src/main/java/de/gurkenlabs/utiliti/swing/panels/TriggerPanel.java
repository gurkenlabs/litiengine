package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.TextList;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;

public class TriggerPanel extends PropertyPanel {
  private final JTextField textFieldMessage;
  private final JComboBox<TriggerActivation> comboBoxActivationType;
  private final JSpinner spinnerCooldown;
  private final JCheckBox chckbxOneTimeOnly;
  private final TextList textListActivators;
  private final TextList textListTargets;

  public TriggerPanel() {
    super("panel_trigger", Icons.TRIGGER);

    this.textFieldMessage = new JTextField();
    this.textFieldMessage.setColumns(10);

    this.comboBoxActivationType = new JComboBox<>();
    this.comboBoxActivationType.setModel(new DefaultComboBoxModel<>(TriggerActivation.values()));
    this.chckbxOneTimeOnly = new JCheckBox(Resources.strings().get("panel_oneTimeOnly"));
    this.spinnerCooldown = new JSpinner();
    this.textListActivators = new TextList(Resources.strings().get("panel_activators"));
    this.textListTargets = new TextList(Resources.strings().get("panel_targets"));

    this.setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldMessage.setText("");
    this.comboBoxActivationType.setSelectedItem(TriggerActivation.COLLISION);
    this.textListActivators.clear();
    this.textListTargets.clear();
    this.chckbxOneTimeOnly.setSelected(false);
    this.spinnerCooldown.setValue(0);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldMessage.setText(mapObject.getStringValue(MapObjectProperty.TRIGGER_MESSAGE));

    this.textListTargets.setJoinedString(
        mapObject.getStringValue(MapObjectProperty.TRIGGER_TARGETS));
    this.textListActivators.setJoinedString(
        mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATORS));

    this.chckbxOneTimeOnly.setSelected(mapObject.getBoolValue(MapObjectProperty.TRIGGER_ONETIME));
    final TriggerActivation act =
        mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATION) == null
            ? TriggerActivation.COLLISION
            : TriggerActivation.valueOf(
                mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATION));
    this.comboBoxActivationType.setSelectedItem(act);
    this.spinnerCooldown.setValue(mapObject.getIntValue(MapObjectProperty.TRIGGER_COOLDOWN));
  }

  private void setupChangedListeners() {
    this.setup(this.textFieldMessage, MapObjectProperty.TRIGGER_MESSAGE);
    this.setup(this.comboBoxActivationType, MapObjectProperty.TRIGGER_ACTIVATION);
    this.setup(this.chckbxOneTimeOnly, MapObjectProperty.TRIGGER_ONETIME);
    this.setup(this.spinnerCooldown, MapObjectProperty.TRIGGER_COOLDOWN);
    this.setup(this.textListTargets, MapObjectProperty.TRIGGER_TARGETS);
    this.setup(this.textListActivators, MapObjectProperty.TRIGGER_ACTIVATORS);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_activation", this.comboBoxActivationType),
            new LayoutItem("panel_message", this.textFieldMessage),
            new LayoutItem("panel_cooldown", this.spinnerCooldown),
        };

    return this.createLayout(
        layoutItems, this.textListTargets, this.textListActivators, this.chckbxOneTimeOnly);
  }
}
