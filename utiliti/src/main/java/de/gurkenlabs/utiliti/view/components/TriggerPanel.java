package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
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
  private final EntityReferenceList entityListActivators;
  private final EntityReferenceList entityListTargets;

  public TriggerPanel() {
    super("panel_trigger", Icons.TRIGGER_24);

    this.textFieldMessage = new JTextField();
    this.textFieldMessage.setColumns(10);

    this.comboBoxActivationType = new JComboBox<>();
    this.comboBoxActivationType.setModel(new DefaultComboBoxModel<>(TriggerActivation.values()));
    this.chckbxOneTimeOnly = new JCheckBox(Resources.strings().get("panel_oneTimeOnly"));
    this.spinnerCooldown = new JSpinner();
    this.entityListActivators = new EntityReferenceList(Resources.strings().get("panel_activators"), TriggerPanel::getMapObjectReferences);
    this.entityListTargets = new EntityReferenceList(Resources.strings().get("panel_targets"), TriggerPanel::getMapObjectReferences);

    this.setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldMessage.setText("");
    this.comboBoxActivationType.setSelectedItem(TriggerActivation.COLLISION);
    this.entityListActivators.clear();
    this.entityListTargets.clear();
    this.chckbxOneTimeOnly.setSelected(false);
    this.spinnerCooldown.setValue(0);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldMessage.setText(mapObject.getStringValue(MapObjectProperty.TRIGGER_MESSAGE, null));

    this.entityListTargets.setJoinedString(mapObject.getStringValue(MapObjectProperty.TRIGGER_TARGETS, null));
    this.entityListActivators.setJoinedString(mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATORS, null));

    this.chckbxOneTimeOnly.setSelected(mapObject.getBoolValue(MapObjectProperty.TRIGGER_ONETIME, false));
    final TriggerActivation act =  mapObject.getEnumValue(MapObjectProperty.TRIGGER_ACTIVATION, TriggerActivation.class, TriggerActivation.COLLISION);
    this.comboBoxActivationType.setSelectedItem(act);
    this.spinnerCooldown.setValue(mapObject.getIntValue(MapObjectProperty.TRIGGER_COOLDOWN, 0));
  }

  private void setupChangedListeners() {
    this.setup(this.textFieldMessage, MapObjectProperty.TRIGGER_MESSAGE);
    this.setup(this.comboBoxActivationType, MapObjectProperty.TRIGGER_ACTIVATION);
    this.setup(this.chckbxOneTimeOnly, MapObjectProperty.TRIGGER_ONETIME);
    this.setup(this.spinnerCooldown, MapObjectProperty.TRIGGER_COOLDOWN);
    this.setup(this.entityListTargets, MapObjectProperty.TRIGGER_TARGETS);
    this.setup(this.entityListActivators, MapObjectProperty.TRIGGER_ACTIVATORS);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_activation", this.comboBoxActivationType),
            new LayoutItem("panel_message", this.textFieldMessage),
            new LayoutItem("panel_cooldown", this.spinnerCooldown),
        };

    return this.createLayout(
        layoutItems, this.entityListTargets, this.entityListActivators, this.chckbxOneTimeOnly);
  }

  private static List<EntityReferenceList.EntityReference> getMapObjectReferences() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return List.of();
    }

    IMap map = Game.world().environment().getMap();
    List<EntityReferenceList.EntityReference> references = new ArrayList<>();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      for (IMapObject mapObject : layer.getMapObjects()) {
        references.add(new EntityReferenceList.EntityReference(mapObject.getId(), mapObject.getName(), mapObject.getType(), false));
      }
    }
    return references;
  }
}
